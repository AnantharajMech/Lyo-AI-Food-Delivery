package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.database.User
import com.example.data.repository.LyoRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.LyoColors
import com.example.ui.viewmodels.*
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
  @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize notification channels for real-time order and chatbot alerts
    com.example.ui.screens.LyoNotificationHelper.createNotificationChannel(applicationContext)

    // Initialize robust database and repository singleton sources
    val db = AppDatabase.getInstance(applicationContext)
    val lyoRepository = LyoRepository(db).apply {
        initGstSettings(applicationContext)
        initAppPauseSettings(applicationContext)
    }

    // Perform all heavy operations (Firebase init, local backup restore, real-time sync) off the main thread to prevent main thread freeze/deadlock
    CoroutineScope(Dispatchers.IO).launch {
        try {
            com.example.data.repository.LyoFirebaseHelper.initialize(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to initialize Firebase: ${e.message}")
        }

        // Restore users from non-volatile local shared backup files if local DB has been cleared on re-install
        try {
            val restoredUsers = com.example.data.repository.LyoLocalBackupHelper.restoreUsers(applicationContext)
            for (user in restoredUsers) {
                if (db.userDao.getUserByPhone(user.phone) == null) {
                    db.userDao.insertUser(user)
                    android.util.Log.d("MainActivity", "Restored account from local backup file: ${user.phone}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to restore users from local backup files: ${e.message}")
        }

        // Start live bidirectional/real-time sync of categories, vendors, and items with role-awareness
        try {
            com.example.data.repository.LyoFirebaseHelper.startRealtimeSync(db, lyoRepository)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to start real-time sync: ${e.message}")
        }
    }
    
    val factory = LyoViewModelFactory(lyoRepository)

    // Instantiate ViewModels
    val authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    val storefrontViewModel = ViewModelProvider(this, factory)[StorefrontViewModel::class.java]
    val adminViewModel = ViewModelProvider(this, factory)[AdminViewModel::class.java]
    val deliveryViewModel = ViewModelProvider(this, factory)[DeliveryViewModel::class.java]

    setContent {
      MyApplicationTheme(darkTheme = true) { // Force-enable luxury premium dark theme
        var currentRoute by remember { mutableStateOf("SPLASH") }
        var previousRoute by remember { mutableStateOf("CUSTOMER_DASHBOARD") }
        var selectedVendorId by remember { mutableLongStateOf(0L) }
        var activeOrderId by remember { mutableLongStateOf(0L) }

        val sharedPrefs = remember {
          applicationContext.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
        }
        val startPhone = remember { sharedPrefs.getString("logged_user_phone", null) }

        val permissionsLauncher = rememberLauncherForActivityResult(
          contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { _ -> }

        // Robust single-flow splash and session recovery
        LaunchedEffect(Unit) {
          val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
          )
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
          }
          try {
            permissionsLauncher.launch(permissions.toTypedArray())
          } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to launch permissions: ${e.message}")
          }

          val startTime = System.currentTimeMillis()
          val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
          
          // a. Wait for Firebase Auth state to finish resolving only if we have a saved user phone
          var firebaseUser = auth.currentUser
          if (firebaseUser == null && startPhone != null) {
              var elapsedWait = 0L
              while (auth.currentUser == null && elapsedWait < 1500L) {
                  delay(100L)
                  elapsedWait += 100L
              }
              firebaseUser = auth.currentUser
          }

          var recoverySuccessful = false
          var errorMessage: String? = null

          if (firebaseUser != null) {
              // c. If an authenticated user exists, read exactly users/{auth.currentUser.uid}
              val uid = firebaseUser.uid
              try {
                  val dbInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                  val doc = dbInstance.collection("users").document(uid).get().await()
                  
                  // d. Confirm that the document exists
                  if (doc.exists()) {
                      // e. Confirm isActive === true
                      val isActive = doc.getBoolean("isActive") ?: doc.getBoolean("isActiveRider") ?: true
                      if (isActive) {
                          // f. Read and validate the role
                          val role = doc.getString("role") ?: "CUSTOMER"
                          val phone = doc.getString("phone") ?: ""
                          
                          val user = User(
                              phone = phone,
                              name = doc.getString("name") ?: "",
                              email = doc.getString("email") ?: "",
                              address = doc.getString("address") ?: "",
                              lat = doc.getDouble("lat") ?: 11.5812,
                              lng = doc.getDouble("lng") ?: 77.8465,
                              isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                              role = role,
                              vehicleNo = doc.getString("vehicleNo") ?: "",
                              isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                              salaryType = doc.getString("salaryType") ?: "MONTHLY",
                              salaryRate = doc.getDouble("salaryRate") ?: 0.0
                          )
                          
                          // Save/update locally
                          lyoRepository.userDao.insertUser(user)
                          lyoRepository.currentUser.value = user
                          recoverySuccessful = true
                          
                          // g. Route the user to the correct dashboard based on role
                          currentRoute = when (role) {
                              "ADMIN" -> "ADMIN_DASHBOARD"
                              "CUSTOMER_CARE" -> "CUSTOMER_CARE_DASHBOARD"
                              "RIDER", "DELIVERY" -> "DELIVERY_DASHBOARD"
                              else -> "CUSTOMER_DASHBOARD"
                          }
                          android.util.Log.d("MainActivity", "Successfully recovered user session for UID: $uid, Role: $role")
                          
                          // Trigger background retry for any pending local synced orders
                          CoroutineScope(Dispatchers.IO).launch {
                              try {
                                  lyoRepository.retryPendingSyncs()
                              } catch (syncEx: Exception) {
                                  android.util.Log.e("MainActivity", "Pending syncs retry failed: ${syncEx.message}")
                              }
                          }
                      } else {
                          errorMessage = "Your account is inactive. Please contact the administrator. (உங்கள் கணக்கு முடக்கப்பட்டுள்ளது!)"
                      }
                  } else {
                      // i. Show specific error when the profile document is genuinely missing
                      errorMessage = "Your user profile is missing in the system. (உங்கள் சுயவிவரம் காணப்படவில்லை!)"
                  }
              } catch (e: Exception) {
                  // h. Do not log out the user merely because Firestore temporarily fails due to network, cache, or timeout
                  android.util.Log.e("MainActivity", "Temporary Firestore error during session recovery: ${e.message}. Falling back to local cache.")
                  
                  val cachedUser = startPhone?.let { lyoRepository.findUserLocallyOnly(it) }
                  if (cachedUser != null) {
                      lyoRepository.currentUser.value = cachedUser
                      recoverySuccessful = true
                      currentRoute = when (cachedUser.role) {
                          "ADMIN" -> "ADMIN_DASHBOARD"
                          "CUSTOMER_CARE" -> "CUSTOMER_CARE_DASHBOARD"
                          "RIDER", "DELIVERY" -> "DELIVERY_DASHBOARD"
                          else -> "CUSTOMER_DASHBOARD"
                      }
                  } else {
                      errorMessage = "Network or cache error: Could not verify user profile. (${e.localizedMessage})"
                  }
              }
              
              if (!recoverySuccessful) {
                  if (errorMessage != null) {
                      android.widget.Toast.makeText(applicationContext, errorMessage, android.widget.Toast.LENGTH_LONG).show()
                  }
                  auth.signOut()
                  sharedPrefs.edit()
                      .remove("logged_user_phone")
                      .remove("logged_user_password_hash")
                      .apply()
                  lyoRepository.currentUser.value = null
                  currentRoute = "CUSTOMER_DASHBOARD"
              }
          } else {
              // b. If there is no authenticated user, show the customer dashboard by default
              sharedPrefs.edit()
                  .remove("logged_user_phone")
                  .remove("logged_user_password_hash")
                  .apply()
              lyoRepository.currentUser.value = null
              currentRoute = "CUSTOMER_DASHBOARD"
          }

          // Ensure the splash screen stays visible for at least 450ms total for visual elegance and high speed
          val elapsed = System.currentTimeMillis() - startTime
          val remainingDelay = (450L - elapsed).coerceAtLeast(0L)
          delay(remainingDelay)

          // Navigate directly if not already set by session recovery
          if (currentRoute == "SPLASH") {
              val currentUserVal = lyoRepository.currentUser.value
              if (currentUserVal != null) {
                  currentRoute = when (currentUserVal.role) {
                      "ADMIN" -> "ADMIN_DASHBOARD"
                      "CUSTOMER_CARE" -> "CUSTOMER_CARE_DASHBOARD"
                      "RIDER", "DELIVERY" -> "DELIVERY_DASHBOARD"
                      else -> "CUSTOMER_DASHBOARD"
                  }
              } else {
                  currentRoute = "CUSTOMER_DASHBOARD"
              }
          }
        }

        val navTrigger by storefrontViewModel.navigationTrigger.collectAsState()
        LaunchedEffect(navTrigger) {
          navTrigger?.let { destination ->
            if (destination == "CHECKOUT" && lyoRepository.currentUser.value == null) {
              android.widget.Toast.makeText(applicationContext, "Please login first to place an order! 🔐", android.widget.Toast.LENGTH_LONG).show()
              if (currentRoute != "LOGIN" && currentRoute != "SPLASH" && currentRoute != "REGISTER") {
                previousRoute = currentRoute
              }
              currentRoute = "LOGIN"
            } else {
              if (currentRoute != "LOGIN" && currentRoute != "SPLASH" && currentRoute != "REGISTER") {
                previousRoute = currentRoute
              }
              currentRoute = destination
            }
            storefrontViewModel.navigationTrigger.value = null
          }
        }

        // Collect pop-up dialog states
        val showOrderSuccess by storefrontViewModel.showOrderSuccessDialog.collectAsState()
        val successDialogTitle by storefrontViewModel.orderSuccessDialogTitle.collectAsState()
        val successDialogText by storefrontViewModel.orderSuccessDialogText.collectAsState()
        val showDeliverySuccess by storefrontViewModel.showDeliverySuccessDialog.collectAsState()

        // Auto-detect when active order gets DELIVERED in real-time to trigger the delivery success dialog
        val activeOrderForDialog by storefrontViewModel.activeLiveOrder.collectAsState()
        var lastKnownStatus by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(activeOrderForDialog?.id, activeOrderForDialog?.status) {
          val currentStatus = activeOrderForDialog?.status
          if (activeOrderForDialog != null && currentStatus == "DELIVERED" && lastKnownStatus != "DELIVERED") {
            storefrontViewModel.showDeliverySuccessDialog.value = true
          }
          lastKnownStatus = currentStatus
        }

        if (showOrderSuccess) {
          androidx.compose.material3.AlertDialog(
            onDismissRequest = { storefrontViewModel.showOrderSuccessDialog.value = false },
            title = {
              Text(
                text = successDialogTitle,
                color = LyoColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              )
            },
            text = {
              Text(
                text = successDialogText,
                color = LyoColors.TextSecondary,
                fontSize = 14.sp
              )
            },
            confirmButton = {
              androidx.compose.material3.Button(
                onClick = { storefrontViewModel.showOrderSuccessDialog.value = false },
                colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
              ) {
                Text("சரி (OK)", color = Color.White, fontWeight = FontWeight.Bold)
              }
            },
            containerColor = LyoColors.CardSlate
          )
        }

        if (showDeliverySuccess) {
          androidx.compose.material3.AlertDialog(
            onDismissRequest = { storefrontViewModel.showDeliverySuccessDialog.value = false },
            title = {
              Text(
                text = "டெலிவரி உறுதி செய்யப்பட்டது! ✅",
                color = LyoColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              )
            },
            text = {
              Text(
                text = "ஆம் வெற்றிகரமாக ஆர்டர் கஸ்டமரால் பெறப்பட்டது!",
                color = LyoColors.TextSecondary,
                fontSize = 14.sp
              )
            },
            confirmButton = {
              androidx.compose.material3.Button(
                onClick = { storefrontViewModel.showDeliverySuccessDialog.value = false },
                colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange)
              ) {
                Text("சரி (OK)", color = Color.White, fontWeight = FontWeight.Bold)
              }
            },
            containerColor = LyoColors.CardSlate
          )
        }

        Scaffold(
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          Crossfade(
            targetState = currentRoute,
            animationSpec = androidx.compose.animation.core.tween(300),
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
              .consumeWindowInsets(innerPadding)
          ) { targetRoute ->
            when (targetRoute) {
              "SPLASH" -> {
                SplashScreen(
                  onSplashFinished = {}
                )
              }

               "LOGIN" -> {
                LoginScreen(
                  viewModel = authViewModel,
                  onNavigateToRegister = {
                    authViewModel.clearRegistrationFields()
                    currentRoute = "REGISTER"
                  },
                  onLoginSuccess = { role, plainPass ->
                    val user = lyoRepository.currentUser.value
                    if (user != null) {
                      val editor = sharedPrefs.edit().putString("logged_user_phone", user.phone)
                      if (plainPass != null) {
                        editor.putString("logged_user_password_hash", com.example.data.repository.LyoFirebaseHelper.hashPassword(plainPass))
                      }
                      editor.apply()
                    }
                    val welcomeMsg = when (role) {
                      "ADMIN" -> "Welcome, Administrator"
                      "DELIVERY" -> "Welcome, Rider"
                      else -> "Welcome back, ${user?.name ?: "Customer"}"
                    }
                    android.widget.Toast.makeText(applicationContext, welcomeMsg, android.widget.Toast.LENGTH_SHORT).show()
                    currentRoute = when (role) {
                      "ADMIN" -> "ADMIN_DASHBOARD"
                      "DELIVERY" -> "DELIVERY_DASHBOARD"
                      else -> previousRoute
                    }
                  },
                  onBackToStore = {
                    currentRoute = previousRoute
                  }
                )
              }

              "REGISTER" -> {
                BackHandler {
                  authViewModel.clearRegistrationFields()
                  currentRoute = "LOGIN"
                }
                RegisterScreen(
                  viewModel = authViewModel,
                  onNavigateBack = {
                    authViewModel.clearRegistrationFields()
                    currentRoute = "LOGIN"
                  },
                  onRegistrationSuccess = { plainPass ->
                    val user = lyoRepository.currentUser.value
                    if (user != null) {
                      val editor = sharedPrefs.edit().putString("logged_user_phone", user.phone)
                      if (plainPass != null) {
                        editor.putString("logged_user_password_hash", com.example.data.repository.LyoFirebaseHelper.hashPassword(plainPass))
                      }
                      editor.apply()
                    }
                    authViewModel.clearRegistrationFields()
                    currentRoute = if (user?.role == "DELIVERY") "DELIVERY_DASHBOARD" else "CUSTOMER_DASHBOARD"
                  }
                )
              }

              "CUSTOMER_DASHBOARD" -> {
                val selectedTab by storefrontViewModel.selectedTabState.collectAsState()
                var lastBackPressTime by remember { mutableStateOf(0L) }
                val context = androidx.compose.ui.platform.LocalContext.current
                val activity = context as? android.app.Activity

                BackHandler(enabled = true) {
                  if (selectedTab != "HOME") {
                    storefrontViewModel.selectedTabState.value = "HOME"
                  } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                      activity?.finish()
                    } else {
                      lastBackPressTime = currentTime
                      android.widget.Toast.makeText(context, "Press BACK again to exit Lyo Securities", android.widget.Toast.LENGTH_SHORT).show()
                    }
                  }
                }
                StorefrontDashboardScreen(
                  viewModel = storefrontViewModel,
                  onNavigateToVendor = { vendorId ->
                    selectedVendorId = vendorId
                    currentRoute = "VENDOR_PROFILE"
                  },
                  onNavigateToActiveOrder = {
                    storefrontViewModel.selectedTabState.value = "TRACKER"
                   },
                  onLogoutClick = {
                    sharedPrefs.edit().remove("logged_user_phone").apply()
                    authViewModel.logout()
                    currentRoute = "LOGIN"
                  },
                  onNavigateToAdmin = {
                    currentRoute = "ADMIN_DASHBOARD"
                  }
                )
              }

              "VENDOR_PROFILE" -> {
                BackHandler {
                  currentRoute = "CUSTOMER_DASHBOARD"
                }
                VendorProfileScreen(
                  vendorId = selectedVendorId,
                  viewModel = storefrontViewModel,
                  onNavigateBack = { currentRoute = "CUSTOMER_DASHBOARD" },
                  onNavigateToCartCheckout = {
                    if (lyoRepository.currentUser.value == null) {
                      android.widget.Toast.makeText(applicationContext, "Please login first to place an order! 🔐", android.widget.Toast.LENGTH_LONG).show()
                      currentRoute = "LOGIN"
                    } else {
                      currentRoute = "CHECKOUT"
                    }
                  }
                )
              }

              "CHECKOUT" -> {
                BackHandler {
                  val activeVendor = storefrontViewModel.activeVendor.value
                  if (activeVendor == null) {
                    currentRoute = "CUSTOMER_DASHBOARD"
                  } else {
                    currentRoute = "VENDOR_PROFILE"
                  }
                }
                CheckoutCartScreen(
                  viewModel = storefrontViewModel,
                  onNavigateBack = {
                    val activeVendor = storefrontViewModel.activeVendor.value
                    if (activeVendor == null) {
                      currentRoute = "CUSTOMER_DASHBOARD"
                    } else {
                      currentRoute = "VENDOR_PROFILE"
                    }
                  },
                  onCheckoutSuccessful = { generatedId ->
                    activeOrderId = generatedId
                    storefrontViewModel.selectedTabState.value = "TRACKER"
                    currentRoute = "CUSTOMER_DASHBOARD"
                  }
                )
              }

              "ACTIVE_ORDER_TRACKING" -> {
                BackHandler {
                  currentRoute = "CUSTOMER_DASHBOARD"
                }
                ActiveOrderTrackingScreen(
                  viewModel = storefrontViewModel,
                  onNavigateBack = { currentRoute = "CUSTOMER_DASHBOARD" }
                )
              }

              "ADMIN_DASHBOARD" -> {
                val selectedAdminVendor by adminViewModel.selectedAdminVendor.collectAsState()
                var lastBackPressTime by remember { mutableStateOf(0L) }
                val context = androidx.compose.ui.platform.LocalContext.current
                val activity = context as? android.app.Activity

                BackHandler(enabled = true) {
                  if (selectedAdminVendor != null) {
                    adminViewModel.selectedAdminVendor.value = null
                  } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                      activity?.finish()
                    } else {
                      lastBackPressTime = currentTime
                      android.widget.Toast.makeText(context, "Press BACK again to exit Admin Portal", android.widget.Toast.LENGTH_SHORT).show()
                    }
                  }
                }
                AdminDashboardScreen(
                  viewModel = adminViewModel,
                  onLogoutClick = {
                    sharedPrefs.edit().remove("logged_user_phone").apply()
                    authViewModel.logout()
                    currentRoute = "LOGIN"
                  },
                  onSwitchToCustomer = {
                    currentRoute = "CUSTOMER_DASHBOARD"
                  }
                )
              }

              "DELIVERY_DASHBOARD" -> {
                var lastBackPressTime by remember { mutableStateOf(0L) }
                val context = androidx.compose.ui.platform.LocalContext.current
                val activity = context as? android.app.Activity

                BackHandler(enabled = true) {
                  val currentTime = System.currentTimeMillis()
                  if (currentTime - lastBackPressTime < 2000) {
                    activity?.finish()
                  } else {
                    lastBackPressTime = currentTime
                    android.widget.Toast.makeText(context, "Press BACK again to exit Rider Portal", android.widget.Toast.LENGTH_SHORT).show()
                  }
                }
                DeliveryPartnerDashboardScreen(
                  viewModel = deliveryViewModel,
                  onLogoutClick = {
                    sharedPrefs.edit().remove("logged_user_phone").apply()
                    authViewModel.logout()
                    currentRoute = "LOGIN"
                  }
                )
              }
            }
          }
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    try {
      com.example.data.repository.LyoFirebaseHelper.pauseSync()
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "Error pausing sync: ${e.message}")
    }
  }

  override fun onResume() {
    super.onResume()
    try {
      com.example.data.repository.LyoFirebaseHelper.resumeSync()
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "Error resuming sync: ${e.message}")
    }
  }
}
