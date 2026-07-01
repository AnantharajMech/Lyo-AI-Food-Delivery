package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
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

        // Robust single-flow splash and session recovery
        LaunchedEffect(Unit) {
          val startTime = System.currentTimeMillis()
          
          if (startPhone != null) {
            var recoverySuccessful = false
            try {
              // Read from local database only - 100% offline, lightning-fast
              val user = lyoRepository.findUserLocallyOnly(startPhone)
              if (user != null) {
                // Verify or re-establish Firebase Auth session
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                var isAuthSessionValid = false
                
                // If Firebase Auth already has a non-null currentUser, check if it matches startPhone and verify token
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                  val expectedEmail = "${startPhone.trim()}@lyofoods.in"
                  if (firebaseUser.email?.lowercase() == expectedEmail.lowercase()) {
                    try {
                      // Try to fetch id token (forcing refresh) to verify session validity
                      firebaseUser.getIdToken(true).await()
                      isAuthSessionValid = true
                      android.util.Log.d("MainActivity", "Firebase Auth session verified successfully via token")
                    } catch (tokenEx: Exception) {
                      android.util.Log.w("MainActivity", "Firebase getIdToken failed, will attempt re-auth: ${tokenEx.message}")
                    }
                  }
                }
                
                // If not verified yet, but we have stored password hash, re-authenticate
                if (!isAuthSessionValid) {
                  val storedHash = sharedPrefs.getString("logged_user_password_hash", null)
                  if (storedHash != null) {
                    try {
                      val email = "${startPhone.trim()}@lyofoods.in"
                      auth.signInWithEmailAndPassword(email, storedHash).await()
                      isAuthSessionValid = true
                      android.util.Log.d("MainActivity", "Firebase Auth session re-established via stored password hash")
                    } catch (signInEx: Exception) {
                      android.util.Log.e("MainActivity", "Firebase signInWithEmailAndPassword failed during recovery: ${signInEx.message}")
                    }
                  }
                }
                
                if (isAuthSessionValid) {
                  lyoRepository.currentUser.value = user
                  recoverySuccessful = true
                  android.util.Log.d("MainActivity", "Successfully recovered user session: ${user.phone}")
                  
                  // Trigger background retry for any pending local synced orders when auth session is restored!
                  CoroutineScope(Dispatchers.IO).launch {
                    try {
                      lyoRepository.retryPendingSyncs()
                    } catch (syncEx: Exception) {
                      android.util.Log.e("MainActivity", "Pending syncs retry failed: ${syncEx.message}")
                    }
                  }
                }
              }
            } catch (e: Exception) {
              android.util.Log.e("MainActivity", "Error during session recovery: ${e.message}")
            }
            
            if (!recoverySuccessful) {
              android.util.Log.w("MainActivity", "Firebase Auth session recovery failed. Clearing session and redirecting to login.")
              sharedPrefs.edit()
                .remove("logged_user_phone")
                .remove("logged_user_password_hash")
                .apply()
              lyoRepository.currentUser.value = null
              currentRoute = "LOGIN"
            }
          }

          // Ensure the splash screen stays visible for at least 2000ms total for visual elegance
          val elapsed = System.currentTimeMillis() - startTime
          val remainingDelay = (2000L - elapsed).coerceAtLeast(0L)
          delay(remainingDelay)

          // Navigate directly
          val currentUserVal = lyoRepository.currentUser.value
          if (currentUserVal != null) {
            currentRoute = when (currentUserVal.role) {
              "ADMIN" -> "ADMIN_DASHBOARD"
              "DELIVERY" -> "DELIVERY_DASHBOARD"
              else -> "CUSTOMER_DASHBOARD"
            }
          } else {
            // Only fall back to CUSTOMER_DASHBOARD if we didn't redirect to LOGIN due to failed session recovery
            if (currentRoute == "SPLASH") {
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
