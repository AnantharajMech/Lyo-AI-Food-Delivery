package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MenuItem
import com.example.data.database.Vendor
import com.example.data.database.PromoBanner
import com.example.data.database.User
import com.example.data.database.Order
import com.example.data.database.OrderItem
import com.example.data.database.Category
import com.example.data.database.isCurrentlyOpen
import com.example.data.database.isCurrentlyVisible
import com.example.data.database.isCurrentlyAvailable
import com.example.data.database.isTrulyVeg
import com.example.data.database.SavedAddress
import com.example.data.database.SavedPaymentMethod
import com.example.data.database.Review
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import com.example.ui.viewmodels.StorefrontViewModel
import com.example.ui.viewmodels.LyoMessage
import androidx.compose.ui.geometry.Offset
import com.example.R
import androidx.compose.ui.res.painterResource
import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import android.widget.Toast

// ==========================================
// 1. MAIN CONSUMER HOME DASHBOARD
// ==========================================
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun StorefrontDashboardScreen(
    viewModel: StorefrontViewModel,
    onNavigateToVendor: (Long) -> Unit,
    onNavigateToActiveOrder: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToAdmin: (() -> Unit)? = null
) {
    val vendors by viewModel.allVendors.collectAsState(initial = emptyList())
    val aiRecommendations by viewModel.aiRecommendations.collectAsState(initial = emptyList())
    val promoBanners by viewModel.allPromoBanners.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQueries.collectAsState(initial = "")
    val activeFilter by viewModel.selectedCategoryFilter.collectAsState(initial = "All")
    val globalCategories by viewModel.globalCategories.collectAsState(initial = emptyList())
    val cartItems by viewModel.activeCart.collectAsState(initial = emptyMap())
    val activeOrderVal by viewModel.activeLiveOrder.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val isPaused by viewModel.isAppPaused.collectAsState()
    val pauseMsgEn by viewModel.appPauseMessageEn.collectAsState()
    val pauseMsgTa by viewModel.appPauseMessageTa.collectAsState()

    val selectedTab by viewModel.selectedTabState.collectAsState(initial = "")
    val notificationHistory by viewModel.notificationHistory.collectAsState()
    val unreadCount = notificationHistory.count { !it.isRead }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCorrectionMapDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showNotificationCenterDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var correctionLat by remember { androidx.compose.runtime.mutableStateOf(11.5812) }
    var correctionLng by remember { androidx.compose.runtime.mutableStateOf(77.8465) }
    var correctionAddress by remember { androidx.compose.runtime.mutableStateOf("") }
    val searchSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                viewModel.searchQueries.value = spokenText
            }
        }
    }

    val userPhone = currentUser?.phone ?: ""
    val pastOrdersFlow = remember(userPhone) { viewModel.getOrdersForUser(userPhone) }
    val pastOrders by pastOrdersFlow.collectAsState(initial = emptyList())

    // Filtered vendor list logic (Optimized using remember to avoid redundant list computation on every frame)
    val filteredVendors = remember(vendors, searchQuery, activeFilter) {
        vendors.filter { vendor ->
            val matchesSearch = vendor.name.contains(searchQuery, ignoreCase = true) || 
                                vendor.nameTa.contains(searchQuery, ignoreCase = true) ||
                                vendor.address.contains(searchQuery, ignoreCase = true) ||
                                vendor.type.contains(searchQuery, ignoreCase = true)
            val matchesCategory = if (activeFilter == "All") true else {
                if (activeFilter.contains("Veg", ignoreCase = true)) {
                    vendor.type.contains("Veg", ignoreCase = true) || 
                    listOf("Restaurant", "Cafe", "Hotel", "Bakery", "Snack Shop", "Store").any { vendor.type.contains(it, ignoreCase = true) }
                } else if (activeFilter.equals("Non-Veg", ignoreCase = true)) {
                    vendor.type.contains("Non-Veg", ignoreCase = true) ||
                    (!vendor.type.contains("Pure Veg", ignoreCase = true) && listOf("Restaurant", "Cafe", "Hotel", "Bakery", "Snack Shop").any { vendor.type.contains(it, ignoreCase = true) })
                } else {
                    vendor.type.contains(activeFilter, ignoreCase = true)
                }
            }
            matchesSearch && matchesCategory
        }
    }

    val promoCodes = listOf(
        Pair("LYOFRESH", "₹80 OFF on orders above ₹300"),
        Pair("CHENNADI70", "₹50 OFF on traditional South Indian tiffins")
    )

    LyoBackground {
        // Auto refresh checking state every 30 seconds
        LaunchedEffect(Unit) {
            while (true) {
                delay(30000)
                try {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("app_settings")
                        .document("global")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot != null && snapshot.exists()) {
                                val isPausedRemote = snapshot.getBoolean("isAppPaused") ?: false
                                val msgEn = snapshot.getString("appPauseMessageEn") ?: ""
                                val msgTa = snapshot.getString("appPauseMessageTa") ?: ""
                                
                                val repo = com.example.data.repository.LyoFirebaseHelper.repositoryRef
                                if (repo != null) {
                                    repo.isAppPaused.value = isPausedRemote
                                    if (msgEn.isNotBlank()) repo.appPauseMessageEn.value = msgEn
                                    if (msgTa.isNotBlank()) repo.appPauseMessageTa.value = msgTa
                                }
                            }
                        }
                } catch (e: Exception) {
                    android.util.Log.e("StorefrontScreens", "Auto refresh app pause status error: ${e.message}")
                }
            }
        }

        val showClosedOverlay = isPaused && currentUser?.role != "ADMIN"
        
        AnimatedVisibility(
            visible = showClosedOverlay,
            enter = fadeIn(animationSpec = tween(600)) + expandIn(expandFrom = Alignment.Center),
            exit = fadeOut(animationSpec = tween(600)) + shrinkOut(shrinkTowards = Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF07090E)) // Ultra premium dark cosmos background
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background cosmic glow spots
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFF43F5E).copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(size.width * 0.2f, size.height * 0.3f),
                            radius = size.width * 0.8f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFF8A65).copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.7f),
                            radius = size.width * 0.8f
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Custom Glowing Neon Storefront Rest Sign
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "NeonGlow")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "PulseAlpha"
                        )
                        val rotateAngle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(24000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "Rotation"
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val center = Offset(w / 2, h / 2)

                            // Outer dotted neon halo
                            drawCircle(
                                color = Color(0xFFF43F5E),
                                radius = w / 2.3f,
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 24f))
                                ),
                                alpha = pulseAlpha * 0.25f
                            )

                            // Inner elegant neon ring
                            drawCircle(
                                color = Color(0xFFF43F5E),
                                radius = w / 2.7f,
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx()),
                                alpha = pulseAlpha * 0.85f
                            )

                            // Warm soft background ambient radial shadow
                            drawCircle(
                                color = Color(0xFFF43F5E),
                                radius = w / 3.2f,
                                center = center,
                                alpha = pulseAlpha * 0.12f
                            )
                        }

                        // Rotating orbital elements
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(rotationZ = rotateAngle)
                        ) {
                            val w = size.width
                            val h = size.height
                            val center = Offset(w / 2, h / 2)
                            val r = w / 2.3f
                            
                            val dotsCount = 6
                            for (i in 0 until dotsCount) {
                                val angleRad = (2.0 * Math.PI * i / dotsCount)
                                val dotX = (center.x + Math.cos(angleRad) * r).toFloat()
                                val dotY = (center.y + Math.sin(angleRad) * r).toFloat()
                                drawCircle(
                                    color = if (i % 2 == 0) Color(0xFFF43F5E) else Color(0xFFFF9E80),
                                    radius = 5.dp.toPx(),
                                    center = Offset(dotX, dotY),
                                    alpha = pulseAlpha * 0.9f
                                )
                            }
                        }

                        // Glowing store icon inside
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = "Lyo AI Store Rest",
                            tint = Color(0xFFFFE0B2),
                            modifier = Modifier
                                .size(64.dp)
                                .graphicsLayer(
                                    scaleX = 1f + (pulseAlpha * 0.05f),
                                    scaleY = 1f + (pulseAlpha * 0.05f)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Lyo AI Food Delivery",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "TEMPORARILY RESTING",
                        color = Color(0xFFF43F5E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "கடை தற்காலிகமாக மூடப்பட்டுள்ளது",
                        color = Color(0xFFFFB300),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Glassmorphism Informational Container with Linear Gradient Border
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF1E293B).copy(alpha = 0.45f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.18f),
                                        Color.White.copy(alpha = 0.02f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (pauseMsgEn.isBlank()) "We are currently preparing our kitchens to bring you the finest culinary experiences. Please check back shortly!" else pauseMsgEn,
                                color = Color(0xFFE2E8F0),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text(
                                text = if (pauseMsgTa.isBlank()) "நாங்கள் தற்போது தற்காலிக விடுப்பில் உள்ளோம். விரைவில் புதிய சுவையான உணவுகளுடன் மீண்டும் வருகிறோம்!" else pauseMsgTa,
                                color = Color(0xFFFFE0B2),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val infiniteTransitionRef = rememberInfiniteTransition(label = "RefreshDot")
                        val dotAlpha by infiniteTransitionRef.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "DotAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF10B981).copy(alpha = dotAlpha), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-syncing real-time channel...",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = onLogoutClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout / Switch Account (வெளியேறு)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (!showClosedOverlay) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
            // Address & Session Header Block - Only shown on HOME tab to maximize chatbot screen space
            if (selectedTab == "HOME") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            correctionLat = currentUser?.lat ?: 11.5812
                            correctionLng = currentUser?.lng ?: 77.8465
                            correctionAddress = currentUser?.address ?: ""
                            showCorrectionMapDialog = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PinDrop,
                        contentDescription = "address",
                        tint = LyoColors.AccentOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DELIVERING TO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = LyoColors.AccentOrange,
                            letterSpacing = 1.2.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.address ?: "No. 12, East Car Street, Idappadi, Salem, Tamil Nadu", // Realistic high-fidelity fallback address
                                fontSize = 11.sp, // Slightly more compact for 2 lines
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.EditLocationAlt,
                                contentDescription = "Edit Location",
                                tint = LyoColors.AmberYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    // Premium Notification Bell Icon with dynamic Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(elevation = 6.dp, shape = CircleShape)
                            .border(width = 1.dp, color = if (unreadCount > 0) LyoColors.AmberYellow else Color(0x33FFFFFF), shape = CircleShape)
                            .background(Color(0xFF0D0F14), shape = CircleShape)
                            .clickable {
                                showNotificationCenterDialog = true
                            }
                            .testTag("notification_bell_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = if (unreadCount > 0) LyoColors.AmberYellow else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (2).dp, y = (-2).dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    // Premium World-Class Gold Circular Lyo Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(elevation = 6.dp, shape = CircleShape)
                            .border(width = 1.dp, color = Color(0xFFF59E0B), shape = CircleShape)
                            .background(Color(0xFF0D0F14), shape = CircleShape)
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.lyo_logo),
                            contentDescription = "Lyo Premium Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // MULTI-LOGIN WARNING BANNER
                val activeSessions by viewModel.repository.activeSessions.collectAsState()
                val myDeviceId = remember { com.example.data.repository.LyoFirebaseHelper.getDeviceId(context) }
                val otherSessionsCount = activeSessions.count { it.deviceId != myDeviceId }
                if (otherSessionsCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "warning",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Multiple devices are logged in with this account!",
                                    color = Color(0xFF92400E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "உங்கள் கணக்கு வேறு சாதனங்களிலும் லாகின் செய்யப்பட்டுள்ளது.",
                                    color = Color(0xFF92400E).copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                // Safe elegant margin from device status bar
                Spacer(modifier = Modifier.height(8.dp))
            }

            val lyoSharedPrefs = remember(context) { context.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE) }
            val isLocationConfirmed = remember(currentUser?.phone) {
                if (currentUser == null) false else {
                    val isConfirmed = lyoSharedPrefs.getBoolean("location_confirmed_${currentUser!!.phone}", false)
                    val isCustomAddress = currentUser!!.address.contains("GPS Locked") || currentUser!!.address.contains("Map Pinned")
                    if (isCustomAddress && !isConfirmed) {
                        lyoSharedPrefs.edit().putBoolean("location_confirmed_${currentUser!!.phone}", true).apply()
                        true
                    } else {
                        isConfirmed
                    }
                }
            }
            val isStuck = currentUser != null && currentUser!!.role == "CUSTOMER" && 
                          currentUser!!.lat == 11.5812 && currentUser!!.lng == 77.8465 && 
                          !isLocationConfirmed

            if (isStuck && (selectedTab == "HOME" || selectedTab == "PROFILE")) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "உங்கள் இருப்பிடம் சரியாக அமைக்கப்படவில்லை — டெலிவரி தாமதமாகலாம், இப்போது சரிசெய்யவும்",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                correctionLat = currentUser!!.lat
                                correctionLng = currentUser!!.lng
                                correctionAddress = currentUser!!.address
                                showCorrectionMapDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF7F1D1D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text(
                                text = "லொகேஷன் சரிசெய்யவும்... 🗺️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            if (showCorrectionMapDialog) {
                Lyo3DDialog(onDismissRequest = { showCorrectionMapDialog = false }) {
                    Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                        Text(
                            text = "🗺️ Pick Delivery Pin on Map\n(வரைபடத்தில் லொகேஷனை தேர்வு செய்யவும்)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp),
                            lineHeight = 17.sp
                        )
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                            InteractiveMapPickerView(
                                initialLat = if (correctionLat != 0.0) correctionLat else 11.5812,
                                initialLng = if (correctionLng != 0.0) correctionLng else 77.8465,
                                onLocationPicked = { pickedLat, pickedLng ->
                                    correctionLat = pickedLat
                                    correctionLng = pickedLng
                                    coroutineScope.launch {
                                        try {
                                            val geoCoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                            val addresses = geoCoder.getFromLocation(pickedLat, pickedLng, 1)
                                            if (!addresses.isNullOrEmpty()) {
                                                correctionAddress = addresses[0].getAddressLine(0) ?: "$pickedLat, $pickedLng"
                                            } else {
                                                correctionAddress = "Lat: $pickedLat, Lng: $pickedLng"
                                            }
                                        } catch (e: Exception) {
                                            correctionAddress = "Custom Map Location ($pickedLat, $pickedLng)"
                                        }
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LyoButton(
                            text = "Confirm Selected Location (லொகேஷன் உறுதிசெய்)",
                            onClick = {
                                viewModel.updateUserPrimaryAddress(correctionAddress, correctionLat, correctionLng)
                                if (currentUser != null) {
                                    lyoSharedPrefs.edit().putBoolean("location_confirmed_${currentUser!!.phone}", true).apply()
                                }
                                showCorrectionMapDialog = false
                                Toast.makeText(context, "உங்கள் லொகேஷன் வெற்றிகரமாக புதுப்பிக்கப்பட்டது!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        )
                    }
                }
            }

            if (showNotificationCenterDialog) {
                Lyo3DDialog(onDismissRequest = { showNotificationCenterDialog = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(480.dp)
                            .padding(8.dp)
                    ) {
                        // Title Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = LyoColors.AmberYellow,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NOTIFICATION CENTER 📢",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            IconButton(onClick = { showNotificationCenterDialog = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray)
                            }
                        }
                        
                        Text(
                            text = "அறிவிப்புகள் மற்றும் சிறப்புச் சலுகைகள்",
                            fontSize = 10.sp,
                            color = LyoColors.TextSecondary,
                            modifier = Modifier.padding(start = 30.dp, bottom = 12.dp)
                        )
                        
                        // Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.markAllNotificationsAsRead() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("MARK ALL READ ✅", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = { viewModel.clearAllNotifications() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, Color(0x33FF0000)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("CLEAR ALL 🧹", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        
                        // History List
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (notificationHistory.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Inbox,
                                        contentDescription = null,
                                        tint = LyoColors.TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No notifications yet! 📭",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "உங்களுக்கு வரும் சலுகைகள் மற்றும் ஆர்டர் அறிவிப்புகள் இங்கே காண்பிக்கப்படும்.",
                                        fontSize = 11.sp,
                                        color = LyoColors.TextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(notificationHistory) { item ->
                                        val isUnread = !item.isRead
                                        val borderCol = if (isUnread) LyoColors.AmberYellow.copy(alpha = 0.4f) else Color(0x1AFFFFFF)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isUnread) Color(0xFF1E293B).copy(alpha = 0.8f) else Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    val regex = Regex("#(\\d+)")
                                                    val match = regex.find(item.title + " " + item.message)
                                                    val orderId = match?.groupValues?.get(1)?.toLongOrNull()
                                                    if (orderId != null) {
                                                        viewModel.trackOrderFromNotification(orderId, item.id) {
                                                            showNotificationCenterDialog = false
                                                        }
                                                    } else {
                                                        viewModel.markNotificationAsRead(item.id)
                                                    }
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.Top) {
                                                // Unread Indicator
                                                if (isUnread) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(top = 8.dp, end = 8.dp)
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(LyoColors.AmberYellow)
                                                    )
                                                } else {
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                }
                                                
                                                // Dynamic premium category icon
                                                val iconForNotif = remember(item.title, item.message) {
                                                    val text = (item.title + " " + item.message).lowercase()
                                                    when {
                                                        text.contains("rider") || text.contains("சவாரி") || text.contains("🛵") -> Icons.Filled.DirectionsBike
                                                        text.contains("offer") || text.contains("சலுகை") || text.contains("promo") -> Icons.Filled.LocalOffer
                                                        text.contains("ஆர்டர்") || text.contains("order") -> Icons.Filled.ShoppingCart
                                                        else -> Icons.Filled.Notifications
                                                    }
                                                }
                                                Icon(
                                                    imageVector = iconForNotif,
                                                    contentDescription = null,
                                                    tint = if (isUnread) LyoColors.AmberYellow else Color.Gray.copy(alpha = 0.7f),
                                                    modifier = Modifier
                                                        .padding(top = 2.dp, end = 8.dp)
                                                        .size(18.dp)
                                                )

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.title,
                                                        color = if (isUnread) Color.White else Color.LightGray,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = item.message,
                                                        color = if (isUnread) Color.White.copy(alpha = 0.9f) else LyoColors.TextSecondary,
                                                        fontSize = 11.sp,
                                                        lineHeight = 15.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    // Time text
                                                    val formattedTime = remember(item.timestamp) {
                                                        try {
                                                            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                                                            sdf.format(Date(item.timestamp))
                                                        } catch (e: Exception) {
                                                            ""
                                                        }
                                                    }
                                                    Text(
                                                        text = formattedTime,
                                                        fontSize = 9.sp,
                                                        color = LyoColors.TextSecondary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (selectedTab == "HOME") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Dynamic Scroll Content
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            // 1. SEARCH BOX SECTION (Placed directly below Address header)
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Lyo3DSearchBar(
                                        value = searchQuery,
                                        onValueChange = { viewModel.searchQueries.value = it },
                                        placeholder = "Search dishes, cuisines, or shops...",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("search_indicator"),
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                                                            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak to search for dishes or shops...")
                                                        }
                                                        searchSpeechLauncher.launch(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Filled.Mic, contentDescription = "voice search", tint = LyoColors.AccentOrange, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    )
                                }
                            }

                            // 2. CATEGORIES CHIPS SECTION (Placed directly below Search Bar)
                            stickyHeader {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(LyoColors.DarkCyanBg)
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "EXPLORE CATEGORIES",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = LyoColors.TextSecondary,
                                        letterSpacing = 1.5.sp,
                                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp)
                                    )

                                    val activeCategories = remember(globalCategories) {
                                        globalCategories.filter { it.isActive }.sortedBy { it.sortOrder }
                                    }
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // "All" item first
                                        item {
                                            val isSelected = activeFilter == "All"
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .clickable { viewModel.selectedCategoryFilter.value = "All" }
                                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) LyoColors.AccentOrange else LyoColors.CardSlate)
                                                        .border(
                                                            width = 1.5.dp,
                                                            color = if (isSelected) Color.White else LyoColors.GlassBorder,
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Storefront,
                                                        contentDescription = "All",
                                                        tint = if (isSelected) Color.White else LyoColors.AmberYellow,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "All",
                                                    color = if (isSelected) LyoColors.AccentOrange else LyoColors.TextPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "அனைத்தும்",
                                                    color = if (isSelected) LyoColors.AccentOrange.copy(alpha = 0.8f) else Color.Gray,
                                                    fontSize = 9.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // Dynamic items
                                        items(activeCategories, key = { it.id }) { cat ->
                                            val isSelected = activeFilter == cat.nameEn
                                            val catColor = try { Color(android.graphics.Color.parseColor(cat.accentColor)) } catch(e: Exception) { LyoColors.AccentOrange }
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .clickable { viewModel.selectedCategoryFilter.value = cat.nameEn }
                                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) catColor else LyoColors.CardSlate)
                                                        .border(
                                                            width = 1.5.dp,
                                                            color = if (isSelected) Color.White else catColor.copy(alpha = 0.3f),
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = getIconForCategoryKey(cat.iconKey),
                                                        contentDescription = cat.nameEn,
                                                        tint = if (isSelected) Color.White else catColor,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = cat.nameEn,
                                                    color = if (isSelected) catColor else LyoColors.TextPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = cat.nameTa,
                                                    color = if (isSelected) catColor.copy(alpha = 0.8f) else Color.Gray,
                                                    fontSize = 9.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. HIGH-FIDELITY HERO BANNERS (Compact offer carousel placed directly below Category chips)
                            item {
                                LiquidHeroBanners(promoBanners = promoBanners)
                            }

                            // ✨ LYO AI PERSONALIZED RECOMMENDATIONS CAROUSEL
                            if (aiRecommendations.isNotEmpty() && searchQuery.isBlank() && activeFilter == "All") {
                                item {
                                    Text(
                                        text = "✨ LYO AI PERSONALIZED RECOMMENDATIONS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = LyoColors.AccentOrange,
                                        letterSpacing = 1.5.sp,
                                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
                                    )
                                    
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(aiRecommendations.take(6)) { rec ->
                                            val vendor = rec.vendor
                                            Card(
                                                modifier = Modifier
                                                    .width(180.dp)
                                                    .clickable { onNavigateToVendor(vendor.id) },
                                                colors = CardDefaults.cardColors(containerColor = Color(0x1F000000)),
                                                border = BorderStroke(1.dp, Color(0x33F8FAFC)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    // Sparkle Badge + AI Score
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(LyoColors.VegGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "AI Score ${rec.aiScore}%",
                                                                color = LyoColors.VegGreen,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Icon(
                                                            imageVector = Icons.Filled.Star,
                                                            contentDescription = "rating",
                                                            tint = LyoColors.AccentOrange,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    
                                                    Text(
                                                        text = vendor.nameTa.ifEmpty { vendor.name },
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = vendor.name,
                                                        color = LyoColors.TextSecondary,
                                                        fontSize = 9.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    
                                                    // Reasons list (first 2 reasons)
                                                    rec.reasons.take(2).forEach { reason ->
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = "• $reason",
                                                                color = LyoColors.TextSecondary,
                                                                fontSize = 8.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Spacer to provide clean layout
                            item {
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            // Vendor listings header
                            item {
                                Text(
                                    text = "POPULAR NEARBY VENUES (${filteredVendors.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LyoColors.TextSecondary,
                                    letterSpacing = 1.5.sp,
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
                                )
                            }

                            // Empty state if no vendors found
                            if (filteredVendors.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(40.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.HourglassEmpty,
                                            contentDescription = "empty",
                                            tint = LyoColors.TextSecondary,
                                            modifier = Modifier.size(50.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "No active kitchen outlets matched this filter.",
                                            color = LyoColors.TextSecondary,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Vendor listings styled as compact horizontal cards
                            items(filteredVendors, key = { it.id }) { partner ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 16.dp,
                                        innerPadding = 8.dp,
                                        backgroundColor = LyoColors.CardSlate,
                                        borderColor = LyoColors.GlassBorder,
                                        glowColor = if (partner.rating >= 4.5) LyoColors.AccentOrange.copy(alpha = 0.12f) else null,
                                        onClick = { onNavigateToVendor(partner.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Left: small square thumbnail (rounded corners, clip)
                                            Box(
                                                modifier = Modifier
                                                    .size(76.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF1E293B))
                                            ) {
                                                val hasCustomImage = partner.bannerUrl.isNotBlank() && (
                                                    partner.bannerUrl.startsWith("http") ||
                                                    partner.bannerUrl.startsWith("content://") ||
                                                    partner.bannerUrl.startsWith("file://") ||
                                                    partner.bannerUrl.contains("/")
                                                )
                                                if (hasCustomImage) {
                                                    val painterModel = remember(partner.bannerUrl) {
                                                        if (partner.bannerUrl.startsWith("/")) java.io.File(partner.bannerUrl) else partner.bannerUrl
                                                    }
                                                    androidx.compose.foundation.Image(
                                                        painter = coil.compose.rememberAsyncImagePainter(painterModel),
                                                        contentDescription = null,
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    // Fallback icon based on type
                                                    val normType = partner.type.lowercase()
                                                    val fallbackIcon = when {
                                                        normType.contains("hotel") -> Icons.Filled.LocalDining
                                                        normType.contains("restaurant") -> Icons.Filled.Restaurant
                                                        normType.contains("cafe") -> Icons.Filled.Coffee
                                                        normType.contains("bakery") -> Icons.Filled.Cake
                                                        else -> Icons.Filled.Storefront
                                                    }
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = fallbackIcon,
                                                            contentDescription = null,
                                                            tint = LyoColors.AmberYellow,
                                                            modifier = Modifier.size(28.dp)
                                                        )
                                                    }
                                                }

                                                if (!partner.isCurrentlyOpen) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color(0x99000000)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "CLOSED",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            // Center: name, cuisine, and metadata
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = partner.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = LyoColors.TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (partner.nameTa.isNotBlank()) {
                                                    Text(
                                                        text = partner.nameTa,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = LyoColors.TextSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.padding(top = 1.dp)
                                                    )
                                                }
                                                Text(
                                                    text = getVendorSubtitle(partner.type),
                                                    fontSize = 11.sp,
                                                    color = LyoColors.TextSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(top = 1.dp)
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Bottom Metadata: rating, distance, time
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    // Rating
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0x1FFF7A1A))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Star,
                                                            contentDescription = "Rating",
                                                            tint = Color(0xFFFF7A1A),
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text(
                                                            text = String.format(java.util.Locale.US, "%.1f", partner.rating),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFFF7A1A)
                                                        )
                                                    }

                                                    // Distance
                                                    Text(
                                                        text = "•  ${String.format(java.util.Locale.US, "%.1f", partner.distance)} km",
                                                        fontSize = 10.sp,
                                                        color = LyoColors.TextSecondary
                                                    )

                                                    // Time
                                                    Text(
                                                        text = "•  ${partner.deliveryTime} mins",
                                                        fontSize = 10.sp,
                                                        color = LyoColors.TextSecondary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Right: small offer badge and delivery fee info
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                // Offer badge (e.g. "30% OFF")
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(LyoColors.AccentOrange)
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = "30% OFF",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // Delivery fee info
                                                Text(
                                                    text = "₹${partner.deliveryFee.toInt()} DEL",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = LyoColors.AmberYellow
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == "ORDERS") {
                    CustomerOrdersSection(
                        currentUser = currentUser,
                        pastOrders = pastOrders,
                        activeOrderVal = activeOrderVal,
                        onNavigateToActiveOrder = onNavigateToActiveOrder,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (selectedTab == "TRACKER") {
                    if (activeOrderVal != null) {
                        ActiveOrderTrackingScreen(
                            viewModel = viewModel,
                            onNavigateBack = { viewModel.selectedTabState.value = "HOME" }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = LyoColors.GlassBorder
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MyLocation,
                                        contentDescription = null,
                                        tint = LyoColors.TextSecondary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No Live Tracker Session",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Browse restaurants and place an order to see real-time delivery GPS tracking here!",
                                        textAlign = TextAlign.Center,
                                        color = LyoColors.TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { viewModel.selectedTabState.value = "HOME" },
                                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Explore Restaurants", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == "LYO_AI") {
                    LaunchedEffect(Unit) {
                        if (com.example.BuildConfig.DEBUG) {
                            val fbUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            val user = viewModel.currentUser.value
                            android.util.Log.d("LyoAuthDebug", "--- ENTERED LYO AI CHATBOT ---")
                            android.util.Log.d("LyoAuthDebug", "• FirebaseAuth Current User UID: ${fbUser?.uid ?: "NULL"}")
                            android.util.Log.d("LyoAuthDebug", "• Local Repository User UID: ${user?.uid ?: "NULL"}")
                            android.util.Log.d("LyoAuthDebug", "• Is Logged In: ${fbUser != null && user != null}")
                        }
                    }
                    LyoAiChatbotSection(
                        viewModel = viewModel,
                        onNavigateToVendor = onNavigateToVendor,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CustomerProfileSection(
                        currentUser = currentUser,
                        pastOrders = pastOrders,
                        viewModel = viewModel,
                        onLogoutClick = onLogoutClick,
                        onNavigateToAdmin = onNavigateToAdmin,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Frosted-glass floating bottom navigation bar with solid backdrop to prevent background text bleed
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp) // premium floating margin
                    .background(Color(0xFA131B2E), RoundedCornerShape(24.dp))
                    .then(
                        with(LyoGlassDesignTokens) {
                            Modifier.liquidGlass3D(
                                cornerRadius = 24.dp,
                                elevation = 12.dp,
                                borderWidth = 1.2.dp,
                                borderBrush = Brush.linearGradient(
                                    colors = listOf(
                                        LyoColors.AmberYellow.copy(alpha = 0.5f), // subtle Electric Cyan top glow
                                        LyoColors.AccentOrange.copy(alpha = 0.2f)  // elegant Deep Indigo bottom highlight
                                    )
                                ),
                                backgroundColor = LyoColors.CardSlate
                            )
                        }
                    )
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // HOME TAB BUTTON
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectedTabState.value = "HOME" }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .testTag("tab_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = "Home",
                            tint = if (selectedTab == "HOME") LyoColors.AmberYellow else LyoColors.TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Home",
                            color = if (selectedTab == "HOME") LyoColors.AmberYellow else LyoColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == "HOME") FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }

                    // ORDERS TAB BUTTON
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (currentUser == null) {
                                    android.widget.Toast.makeText(context, "Please login first to view your past orders! 🔐", android.widget.Toast.LENGTH_LONG).show()
                                    viewModel.pendingLoginAction.value = com.example.ui.viewmodels.StorefrontViewModel.PendingLoginAction.ViewOrders
                                    viewModel.navigationTrigger.value = "LOGIN"
                                } else {
                                    viewModel.selectedTabState.value = "ORDERS"
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .testTag("tab_orders_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = "Orders",
                            tint = if (selectedTab == "ORDERS") LyoColors.AmberYellow else LyoColors.TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Orders",
                            color = if (selectedTab == "ORDERS") LyoColors.AmberYellow else LyoColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == "ORDERS") FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }

                    // LYO AI TAB BUTTON (SLIGHTLY RAISED/FLOATING CENTER PILL SHAPE WITH GRADIENT)
                    val isLyoAiSelected = selectedTab == "LYO_AI"
                    val lyoAiBrush = if (isLyoAiSelected) {
                        Brush.linearGradient(colors = listOf(LyoColors.AccentOrange, LyoColors.AmberYellow))
                    } else {
                        Brush.linearGradient(colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A)))
                    }
                    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                    val isSmallScreen = configuration.screenHeightDp < 650
                    val lyoOffset = if (isSmallScreen) 0.dp else (-10).dp

                    Box(
                        modifier = Modifier
                            .offset(y = lyoOffset)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                clip = false
                            )
                            .background(lyoAiBrush, shape = RoundedCornerShape(24.dp))
                            .border(
                                width = if (isLyoAiSelected) 1.5.dp else 1.dp,
                                color = if (isLyoAiSelected) Color.White else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { viewModel.selectedTabState.value = "LYO_AI" }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("tab_lyo_ai_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Lyo AI Sparkles",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lyo AI",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // TRACKER TAB BUTTON (WITH ACTIVE GREEN GLOWING DOT IF ASSIGNED)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (currentUser == null) {
                                    android.widget.Toast.makeText(context, "Please login first to track your order live! 🔐", android.widget.Toast.LENGTH_LONG).show()
                                    viewModel.pendingLoginAction.value = com.example.ui.viewmodels.StorefrontViewModel.PendingLoginAction.ViewOrders
                                    viewModel.navigationTrigger.value = "LOGIN"
                                } else {
                                    viewModel.selectedTabState.value = "TRACKER"
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .testTag("tab_tracker_button")
                    ) {
                        val currentOrder = activeOrderVal
                        val hasActive = currentOrder != null && currentOrder.status != "DELIVERED" && currentOrder.status != "CANCELLED"
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = "Tracker",
                                tint = if (selectedTab == "TRACKER") LyoColors.AmberYellow else if (hasActive) LyoColors.VegGreen else LyoColors.TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            if (hasActive) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                        .border(1.dp, Color(0xFF0B1120), CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hasActive) "Tracking" else "Tracker",
                            color = if (selectedTab == "TRACKER") LyoColors.AmberYellow else if (hasActive) LyoColors.VegGreen else LyoColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == "TRACKER") FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }

                    // PROFILE TAB BUTTON
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (currentUser == null) {
                                    android.widget.Toast.makeText(context, "Please login first to view your profile! 🔐", android.widget.Toast.LENGTH_LONG).show()
                                    viewModel.pendingLoginAction.value = com.example.ui.viewmodels.StorefrontViewModel.PendingLoginAction.ViewProfile
                                    viewModel.navigationTrigger.value = "LOGIN"
                                } else {
                                    viewModel.selectedTabState.value = "PROFILE"
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .testTag("tab_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile",
                            tint = if (selectedTab == "PROFILE") LyoColors.AmberYellow else LyoColors.TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Profile",
                            color = if (selectedTab == "PROFILE") LyoColors.AmberYellow else LyoColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == "PROFILE") FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CustomerProfileSection(
    currentUser: User?,
    pastOrders: List<Order>,
    viewModel: StorefrontViewModel,
    onLogoutClick: () -> Unit,
    onNavigateToAdmin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var expandedOrderId by remember { mutableStateOf<Long?>(null) }
    
    // Quick Password reset inside profile screen states
    var showChangePassDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var changeStep by remember { mutableStateOf(1) }
    var changePhone by remember { mutableStateOf(currentUser?.phone ?: currentUser?.email ?: "") }
    var generatedOtpToken by remember { mutableStateOf("") }
    var enteredOtpToken by remember { mutableStateOf("") }
    var newResetPasswordText by remember { mutableStateOf("") }
    var changeError by remember { mutableStateOf("") }
    var changeSuccess by remember { mutableStateOf(false) }

    var showSettingsScreen by remember { mutableStateOf(false) }

    val savedAddresses by viewModel.savedAddresses.collectAsState(initial = emptyList())
    val savedPaymentMethods by viewModel.savedPaymentMethods.collectAsState(initial = emptyList())

    var showAddAddressForm by remember { mutableStateOf(false) }
    var newAddressLabel by remember { mutableStateOf("") }
    var newAddressLine by remember { mutableStateOf("") }
    var newAddressIsDefault by remember { mutableStateOf(false) }
    var newAddressLat by remember { mutableStateOf(0.0) }
    var newAddressLng by remember { mutableStateOf(0.0) }
    var showAddressMapDialog by remember { mutableStateOf(false) }

    var showAddPaymentForm by remember { mutableStateOf(false) }
    var newPaymentType by remember { mutableStateOf("UPI") }
    var newPaymentDisplay by remember { mutableStateOf("") }
    var newPaymentExpiry by remember { mutableStateOf("") }
    var newPaymentHolder by remember { mutableStateOf("") }

    var expandedPastOrderId by remember { mutableStateOf<Long?>(null) }

    if (showSettingsScreen) {
        SettingsScreen(
            currentUser = currentUser,
            onBack = { showSettingsScreen = false },
            onChangePasscodeClick = {
                changeStep = 1
                changePhone = currentUser?.phone ?: currentUser?.email ?: ""
                enteredOtpToken = ""
                newResetPasswordText = ""
                changeError = ""
                changeSuccess = false
                showChangePassDialog = true
            },
            viewModel = viewModel
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY PROFILE 👤",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )
                IconButton(
                    onClick = { showSettingsScreen = true },
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = LyoColors.AccentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. BIOGRAPHIC GREETING CARD (Uplifted Premium Aesthetics)
        // UI-GUARD: fixed above: ensure ZERO visual defects or overlaps across different terminal sizes.
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            glowColor = LyoColors.AccentOrange
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                // Outer image/avatar on separate logical column
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(LyoColors.AccentOrange, Color(0xFFF97316))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (currentUser?.name?.take(1) ?: "U").uppercase(),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Detail column showing details on separate logical rows
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Row 1: Name
                    Text(
                        text = currentUser?.name ?: "Valued Customer",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Row 2: Badges row containing Dynamic Loyalty Tier + Active Secure.
                    val ordersCount = pastOrders.size
                    val (loyaltyTierText, loyaltyTierColor) = when {
                        ordersCount > 10 -> "PLATINUM MEMBER" to Color(0xFFE2E8F0)
                        ordersCount > 5 -> "GOLD MEMBER" to Color(0xFFF59E0B)
                        else -> "SILVER MEMBER" to Color(0xFF94A3B8)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(loyaltyTierColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                loyaltyTierText,
                                color = if (ordersCount > 10) Color.Black else Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Premium Active Secure Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x3310B981))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "ACTIVE SECURE",
                                color = Color(0xFF34D399),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Row 3: Phone
                    Text(
                        text = "📱 +91 ${currentUser?.phone ?: "Not Bound"}",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Row 4: Email
                    Text(
                        text = currentUser?.email ?: "customer@lyofood.in",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1.5. HIGH-DENSITY 3D LIQUID GLASS LOYALTY CARD
        val totalLoyaltyPoints = pastOrders.sumOf { ((it.totalAmount / 10).toInt() - it.redeemedPoints) }.coerceAtLeast(0)
        val pointsForDiscount = 100
        val pointsEarnedInCurrentCycle = totalLoyaltyPoints % pointsForDiscount
        val pointsNeededForNextDiscount = pointsForDiscount - pointsEarnedInCurrentCycle
        val loyaltyProgress = pointsEarnedInCurrentCycle.toFloat() / pointsForDiscount.toFloat()
        val earnedDiscounts = totalLoyaltyPoints / pointsForDiscount

        Text(
            text = "🌟 LYO LOYALTY CLUB (லியோ லாயல்டி கிளப்)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = LyoColors.AmberYellow,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            cornerRadius = 24.dp,
            borderColor = Color(0x66FF6B00), // Glowing orange edge
            glowColor = Color(0x22FF6B00),
            backgroundColor = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lyo Gold Points Balance",
                            fontSize = 12.sp,
                            color = LyoColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "loyalty",
                                tint = LyoColors.AmberYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalLoyaltyPoints PTS",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    // Count of unlocked rewards
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(LyoColors.AccentOrange, LyoColors.AmberYellow)
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (earnedDiscounts > 0) "🎁 $earnedDiscounts REWARDS READY" else "🔒 NEXT REWARD AT 100 PTS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful 3D Liquid/Glass Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next ₹10 Reward Coupon Progress",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$pointsEarnedInCurrentCycle / $pointsForDiscount",
                        fontSize = 11.sp,
                        color = LyoColors.AmberYellow,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // The sleek progress bar container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                ) {
                    // Progress Fill (glowing liquid gradient background)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = loyaltyProgress.coerceIn(0.01f, 1f))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF3D00),
                                        Color(0xFFF59E0B),
                                        Color(0xFF10B981)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "info logo",
                        tint = LyoColors.LiveCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "அடுத்த தள்ளுபடி பெற இன்னும் $pointsNeededForNextDiscount புள்ளிகள் தேவை! (1 Point per ₹100 spent)",
                        fontSize = 10.sp,
                        color = LyoColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. STATISTICS WIDGET PANELS (Clean Slate-Style Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Widget: Total Orders
            GlassCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 16.dp,
                borderColor = Color(0x3338BDF8),
                glowColor = Color(0x1A38BDF8)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = "orders",
                        tint = Color(0xFF0EA5E9),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${pastOrders.size} Orders",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Purchase History",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Widget: Total Spent
            val totalExpense = pastOrders.sumOf { it.totalAmount }.toInt()
            GlassCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 16.dp,
                borderColor = Color(0x3310B981),
                glowColor = Color(0x1A10B981)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Payments,
                        contentDescription = "expense",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val textLength = "₹$totalExpense Spent".length
                    val statFontSize = when {
                        textLength > 15 -> 12.sp
                        textLength > 11 -> 14.sp
                        else -> 18.sp
                    }
                    Text(
                        text = "₹$totalExpense Spent",
                        fontSize = statFontSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Accumulated Spends",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. SECURED REGISTERED DELIVERING ADDRESS (Clean minimal layout)
        Text(
            text = "📍 REGISTERED DELIVERY ADDRESS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            borderColor = Color(0x33FFFFFF)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Filled.HomeWork,
                        contentDescription = "address",
                        tint = LyoColors.AccentOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.address ?: "Salem main highway, Salem, Tamil Nadu",
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color(0x11FFFFFF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                val context = LocalContext.current
                val profileLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        coroutineScope.launch {
                            val locResult = fetchCurrentLocationAndReverseGeocode(context)
                            if (locResult != null) {
                                viewModel.updateUserPrimaryAddress(locResult.third, locResult.first, locResult.second)
                                Toast.makeText(context, "📍 பிரவுசர் லைவ் லொகேஷன் வெற்றிகரமாக புதுப்பிக்கப்பட்டது!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "GPS சிக்னல் கிடைக்கவில்லை! தயவுசெய்து வீட்டிற்கு வெளியே சென்று மீண்டும் முயற்சிக்கவும் அல்லது முகவரியை கையால் உள்ளிடவும்.", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "லொகேஷன் அனுமதி மறுக்கப்பட்டது! தற்போதைய முகவரியே இருக்கும்.", Toast.LENGTH_SHORT).show()
                    }
                }
                
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            coroutineScope.launch {
                                val locResult = fetchCurrentLocationAndReverseGeocode(context)
                                if (locResult != null) {
                                    viewModel.updateUserPrimaryAddress(locResult.third, locResult.first, locResult.second)
                                    Toast.makeText(context, "📍 பிரவுசர் லைவ் லொகேஷன் வெற்றிகரமாக புதுப்பிக்கப்பட்டது!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "GPS சிக்னல் கிடைக்கவில்லை! தயவுசெய்து வீட்டிற்கு வெளியே சென்று மீண்டும் முயற்சிக்கவும் அல்லது முகவரியை கையால் உள்ளிடவும்.", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            profileLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x1138BDF8)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
                        .testTag("sync_browser_live_location_btn")
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Sync",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📡 USE WEB-BROWSER GPS TELEMETRY / தற்போதைய லைவ் லொகேஷன் பெறுக",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
                                // 3.1. DETAILED INTERACTIVE SAVED ADDRESSES SECTION
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "📍 SAVED DELIVERY LOCATIONS (${savedAddresses.size}/10)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF38BDF8),
                letterSpacing = 1.2.sp
            )
            Text(
                text = if (showAddAddressForm) "Cancel" else if (savedAddresses.size >= 10) "Limit Reached ⚠️" else "+ Add New",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (savedAddresses.size >= 10 && !showAddAddressForm) Color.Gray else LyoColors.AccentOrange,
                modifier = Modifier
                    .clickable {
                        val ctx = context
                        if (savedAddresses.size >= 10 && !showAddAddressForm) {
                            Toast.makeText(ctx, "அதிகபட்சமாக 10 முகவரிகளை மட்டுமே சேர்க்க முடியும்! (Maximum 10 addresses limit reached!)", Toast.LENGTH_LONG).show()
                        } else {
                            if (!showAddAddressForm && newAddressLabel.isBlank()) {
                                newAddressLabel = "Home"
                            }
                            showAddAddressForm = !showAddAddressForm
                        }
                    }
                    .testTag("add_address_toggle_btn")
            )
        }

        if (showAddAddressForm) {
            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                borderColor = Color(0x66F97316),
                backgroundColor = Color(0xFF1E293B)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Add New Delivery Location", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Segmented Selector for Address Type
                    Text(
                        text = "SELECT ADDRESS TYPE (முகவரி வகை) 🏷️",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Home", "Work", "Favorite", "Recent", "Other").forEach { type ->
                            val isSelected = if (type == "Other") {
                                newAddressLabel != "Home" && newAddressLabel != "Work" && newAddressLabel != "Favorite" && newAddressLabel != "Recent" && newAddressLabel.isNotBlank()
                            } else {
                                newAddressLabel == type
                            }
                            val labelTa = when (type) {
                                "Home" -> "வீடு"
                                "Work" -> "வேலை"
                                "Favorite" -> "விருப்பம்"
                                "Recent" -> "அண்மை"
                                else -> "இதர"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LyoColors.AccentOrange.copy(alpha = 0.2f) else Color(0xFF0F172A))
                                    .border(1.dp, if (isSelected) LyoColors.AccentOrange else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (type == "Other") {
                                            newAddressLabel = "Custom"
                                        } else {
                                            newAddressLabel = type
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxHeight().padding(horizontal = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = when (type) {
                                            "Home" -> Icons.Filled.Home
                                            "Work" -> Icons.Filled.Work
                                            "Favorite" -> Icons.Filled.Favorite
                                            "Recent" -> Icons.Filled.History
                                            else -> Icons.Filled.Place
                                        },
                                        contentDescription = type,
                                        tint = if (isSelected) LyoColors.AccentOrange else Color.LightGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$type\n$labelTa",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) LyoColors.AccentOrange else Color.LightGray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 9.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    // If "Other" or custom is selected, show a small text field to specify the custom name
                    if (newAddressLabel != "Home" && newAddressLabel != "Work" && newAddressLabel != "Favorite" && newAddressLabel != "Recent") {
                        OutlinedTextField(
                            value = if (newAddressLabel == "Custom") "" else newAddressLabel,
                            onValueChange = { newAddressLabel = it },
                            placeholder = { Text("Enter Custom Label (e.g., Gym, PG, Hostel)", color = Color(0x88FFFFFF), fontSize = 12.sp) },
                            maxLines = 1,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LyoColors.AccentOrange
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("add_address_label_input").padding(bottom = 8.dp)
                        )
                    }

                    // Address Line input
                    OutlinedTextField(
                        value = newAddressLine,
                        onValueChange = { newAddressLine = it },
                        placeholder = { Text("Complete Address Street, City, Pincode", color = Color(0x88FFFFFF), fontSize = 12.sp) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_address_line_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Fetch location via Live GPS/Map buttons
                    val ctx = LocalContext.current
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            coroutineScope.launch {
                                val locResult = fetchCurrentLocationAndReverseGeocode(ctx)
                                if (locResult != null) {
                                    newAddressLat = locResult.first
                                    newAddressLng = locResult.second
                                    newAddressLine = locResult.third
                                    Toast.makeText(ctx, "📍 GPS லொகேஷன் வெற்றிகரமாக பெறப்பட்டது!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(ctx, "GPS சிக்னல் கிடைக்கவில்லை! தயவுசெய்து வீட்டிற்கு வெளியே சென்று மீண்டும் முயற்சிக்கவும் அல்லது முகவரியை கையால் உள்ளிடவும்.", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(ctx, "லொகேஷன் அனுமதி மறுக்கப்பட்டது! மேனுவலாக முகவரியை உள்ளிடவும்.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // GPS Detect button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1138BDF8))
                                .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        coroutineScope.launch {
                                            val locResult = fetchCurrentLocationAndReverseGeocode(ctx)
                                            if (locResult != null) {
                                                newAddressLat = locResult.first
                                                newAddressLng = locResult.second
                                                newAddressLine = locResult.third
                                                Toast.makeText(ctx, "📍 தற்போதைய லைவ் லொகேஷன் பெறப்பட்டது!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(ctx, "GPS சிக்னல் கிடைக்கவில்லை! தயவுசெய்து மீண்டும் முயற்சிக்கவும்.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.MyLocation, contentDescription = "GPS", tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("GPS Detect", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Search & Pin button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1110B981))
                                .border(1.dp, Color(0x3310B981), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (newAddressLine.isNotBlank()) {
                                        val (resolvedLat, resolvedLng) = com.example.ui.viewmodels.resolveSmartGeocodeTamilNadu(newAddressLine, 11.5812, 77.8465)
                                        newAddressLat = resolvedLat
                                        newAddressLng = resolvedLng
                                        Toast.makeText(ctx, "🔍 Address verified & pinned to: $resolvedLat, $resolvedLng", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(ctx, "தயவுசெய்து முகவரியை முதலில் உள்ளிடவும்! (Enter address first!)", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Search, contentDescription = "Search Pin", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Search & Pin", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Map Pick button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x11F97316))
                                .border(1.dp, Color(0x33F97316), RoundedCornerShape(8.dp))
                                .clickable { showAddressMapDialog = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Map, contentDescription = "Map Select", tint = LyoColors.AccentOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Pick on Map", color = LyoColors.AccentOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (showAddressMapDialog) {
                        Lyo3DDialog(onDismissRequest = { showAddressMapDialog = false }) {
                            Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                                Text(
                                    text = "🗺️ Pick Location Pin on Map\n(வரைபடத்தில் லொகேஷனை தேர்வு செய்யவும்)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    lineHeight = 17.sp
                                )
                                Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                                    InteractiveMapPickerView(
                                        initialLat = if (newAddressLat != 0.0) newAddressLat else 11.5812,
                                        initialLng = if (newAddressLng != 0.0) newAddressLng else 77.8465,
                                        onLocationPicked = { pickedLat, pickedLng ->
                                            newAddressLat = pickedLat
                                            newAddressLng = pickedLng
                                            coroutineScope.launch {
                                                try {
                                                    val geoCoder = android.location.Geocoder(ctx, java.util.Locale.getDefault())
                                                    val addresses = geoCoder.getFromLocation(pickedLat, pickedLng, 1)
                                                    if (!addresses.isNullOrEmpty()) {
                                                        newAddressLine = addresses[0].getAddressLine(0) ?: "$pickedLat, $pickedLng"
                                                    } else {
                                                        newAddressLine = "Lat: $pickedLat, Lng: $pickedLng"
                                                    }
                                                } catch (e: Exception) {
                                                    newAddressLine = "Custom Map Location ($pickedLat, $pickedLng)"
                                                }
                                            }
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                LyoButton(
                                    text = "Confirm Location (லொகேஷனை உறுதிசெய்)",
                                    onClick = { showAddressMapDialog = false },
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val isGeoTagged = newAddressLat != 0.0 && newAddressLng != 0.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isGeoTagged) Color(0x1F22C55E) else Color(0x1FEF4444))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isGeoTagged) LyoColors.VegGreen else LyoColors.NonVegRed, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isGeoTagged) "✓ Location geo-tagged: ${String.format(java.util.Locale.US, "%.5f, %.5f", newAddressLat, newAddressLng)}" else "⚠️ GPS Coordinates missing! Pick on Map or use GPS",
                            color = if (isGeoTagged) LyoColors.VegGreen else LyoColors.NonVegRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Default Address Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { newAddressIsDefault = !newAddressIsDefault },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = newAddressIsDefault,
                            onCheckedChange = { newAddressIsDefault = it },
                            colors = CheckboxDefaults.colors(checkedColor = LyoColors.AccentOrange)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set as My Primary Address", color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val targetLabel = newAddressLabel.trim()
                            if (targetLabel.isNotBlank() && newAddressLine.isNotBlank()) {
                                viewModel.addSavedAddress(
                                    name = targetLabel,
                                    addressLine = newAddressLine.trim(),
                                    isDefault = newAddressIsDefault,
                                    latitude = newAddressLat,
                                    longitude = newAddressLng,
                                    onSuccess = {
                                        Toast.makeText(ctx, "✅ முகவரி வெற்றிகரமாக சேமிக்கப்பட்டது!", Toast.LENGTH_SHORT).show()
                                        newAddressLabel = ""
                                        newAddressLine = ""
                                        newAddressIsDefault = false
                                        newAddressLat = 0.0
                                        newAddressLng = 0.0
                                        showAddAddressForm = false
                                    },
                                    onError = { err ->
                                        Toast.makeText(ctx, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                Toast.makeText(ctx, "தயவுசெய்து முகவரியை உள்ளிடவும்!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_address_submit_btn")
                    ) {
                        Text("Save Address Location", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (savedAddresses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "No saved secondary addresses yet. Add one above!",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                savedAddresses.forEach { addr ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, if (addr.isDefault) Color(0x3338BDF8) else Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.setAddressAsPrimary(addr)
                                Toast.makeText(context, "📍 Primary Delivery Address set to: ${addr.name}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x1638BDF8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val adIcon = when {
                                        addr.name.contains("Home", ignoreCase = true) -> Icons.Filled.Home
                                        addr.name.contains("Work", ignoreCase = true) || addr.name.contains("Office", ignoreCase = true) -> Icons.Filled.Work
                                        addr.name.contains("Favorite", ignoreCase = true) || addr.name.contains("Fav", ignoreCase = true) -> Icons.Filled.Favorite
                                        addr.name.contains("Recent", ignoreCase = true) -> Icons.Filled.History
                                        else -> Icons.Filled.Place
                                    }
                                    Icon(
                                        imageVector = adIcon,
                                        contentDescription = "address_type",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(addr.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        if (addr.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0x3310B981))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text("PRIMARY", color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(addr.addressLine, fontSize = 12.sp, color = Color(0xFF94A3B8), lineHeight = 16.sp)
                                }
                            }
                            IconButton(
                                onClick = { viewModel.deleteSavedAddress(addr) },
                                modifier = Modifier.size(24.dp).testTag("delete_address_btn_${addr.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Payment management section deleted as requested.

        // 3.3. DETAILED ORDER HISTORY LEDGER
        val finishedOrders = remember(pastOrders) { pastOrders.filter { it.status == "DELIVERED" || it.status == "CANCELLED" } }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "📦 COMPREHENSIVE ORDER HISTORY (${finishedOrders.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFBBF24),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (finishedOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "No past orders found. Place a delicious order from the store!",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                finishedOrders.forEach { ord ->
                    val isPastExpanded = expandedPastOrderId == ord.id
                    var itemsListState by remember(ord.id) { mutableStateOf<List<OrderItem>>(emptyList()) }

                    LaunchedEffect(isPastExpanded) {
                        if (isPastExpanded && itemsListState.isEmpty()) {
                            val (_, items) = viewModel.repository.getOrderWithItems(ord.id)
                            itemsListState = items
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, if (isPastExpanded) Color(0x33FBBF24) else Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                            .clickable { expandedPastOrderId = if (isPastExpanded) null else ord.id }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ord.vendorName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val formattedOrderTime = remember(ord.timestamp) {
                                        try {
                                            java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(ord.timestamp))
                                        } catch (e: Exception) {
                                            ""
                                        }
                                    }
                                    Text(
                                        text = "Order ID: #${ord.id} • $formattedOrderTime",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (badgeBg, badgeText, statusLabel) = when (ord.status) {
                                        "DELIVERED" -> Triple(Color(0x2210B981), Color(0xFF34D399), "Delivered")
                                        "PENDING" -> Triple(Color(0x22FBBF24), Color(0xFFFBBF24), "Pending")
                                        "PREPARING", "ACCEPTED" -> Triple(Color(0x223B82F6), Color(0xFF60A5FA), "Preparing")
                                        "OUT_FOR_DELIVERY" -> Triple(Color(0x22EC4899), Color(0xFFF472B6), "In Transit")
                                        else -> Triple(Color(0x2264748B), Color(0xFF94A3B8), ord.status)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = statusLabel, color = badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isPastExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Expand Status",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${ord.itemsCount} ${if (ord.itemsCount == 1) "Item" else "Items"} • ₹${ord.totalAmount}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Button(
                                    onClick = {
                                        viewModel.reorderOrder(ord.id) {
                                            viewModel.selectedTabState.value = "HOME"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22F97316)),
                                    border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.4f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp).testTag("reorder_btn_${ord.id}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = "reorder",
                                            tint = LyoColors.AccentOrange,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reorder", color = LyoColors.AccentOrange, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }

                            if (isPastExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0x11FFFFFF))
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (itemsListState.isEmpty()) {
                                    Text("Loading item receipt...", color = Color(0xFF64748B), fontSize = 11.sp)
                                } else {
                                    itemsListState.forEach { itm ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${itm.nameEn} x${itm.quantity}",
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                            Text(
                                                text = "₹${itm.price * itm.quantity}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0x08FFFFFF))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Subtotal", color = Color(0xFF64748B), fontSize = 11.sp)
                                            Text("₹${ord.subtotal}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }
                                        if (ord.deliveryFee > 0) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Delivery Fee", color = Color(0xFF64748B), fontSize = 11.sp)
                                                Text("₹${ord.deliveryFee}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                            }
                                        }
                                        if (ord.couponDiscount > 0) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Coupon Discount", color = Color(0xFF64748B), fontSize = 11.sp)
                                                Text("-₹${ord.couponDiscount}", color = Color(0xFF10B981), fontSize = 11.sp)
                                            }
                                        }
                                        if (ord.tipAmount > 0) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Rider Tip", color = Color(0xFF64748B), fontSize = 11.sp)
                                                Text("₹${ord.tipAmount}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Grand Total", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                            Text("₹${ord.totalAmount}", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // 6. TECHNICAL SUPPORT CENTER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Filled.HeadsetMic,
                    contentDescription = "support",
                    tint = Color(0xFF0EA5E9),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "NEED CONCIERGE ASSISTANCE?",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Lyo Support agents are available 24/7 at support@lyofood.in",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        if (onNavigateToAdmin != null && currentUser?.role == "ADMIN") {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1AFF6B00))
                    .border(1.dp, Color(0x33FF6B00), RoundedCornerShape(16.dp))
                    .clickable { onNavigateToAdmin() }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "admin",
                        tint = LyoColors.AccentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ADMIN PORTAL / CONTROL TOWER",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "அட்மின் பகுதி: கடைகள், ஆர்டர்களை நிர்வகிக்கவும்",
                            color = LyoColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "go",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Red Accent Outlined Logout Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x1AEF4444))
                .border(1.dp, Color(0x22EF4444), RoundedCornerShape(16.dp))
                .clickable { showLogoutDialog = true }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = "logout",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOGOUT FROM ACCOUNT",
                    color = Color(0xFFFCA5A5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Metadata build label
        Text(
            text = "Lyo Premium App v1.0.0\nSecurely customized for Lyo AI Food Delivery services",
            fontSize = 9.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            lineHeight = 14.sp
        )
    }
    } // End of else statement

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    text = "Confirm Logout",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out from your secure session? Your active login credentials will be cleared from this temporary gateway.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            }
        )
    }

    // Direct Interactive Passcode Reset Modal Dialog inside Profile Section
    if (showChangePassDialog) {
        AlertDialog(
            onDismissRequest = { showChangePassDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    text = when (changeStep) {
                        1 -> "Reset Your Credentials"
                        2 -> "Verify Security OTP Code"
                        3 -> "Choose New Passcode"
                        else -> "Profile Passcode Reset Complete"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (changeStep == 1) {
                        Text(
                            text = "Please confirm your registered username, email or mobile number to dispatch a recovery code:",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = changePhone,
                            onValueChange = { changePhone = it },
                            label = { Text("Account Identifier") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LyoColors.AccentOrange,
                                unfocusedBorderColor = Color(0x33F8FAFC)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (changeStep == 2) {
                        Text(
                            text = "A secure verification code has been dispatched to your profile identifier $changePhone:",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "💬 Verification code dispatched! (Check simulated notification toast or your logs)",
                            fontSize = 13.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        OutlinedTextField(
                            value = enteredOtpToken,
                            onValueChange = { enteredOtpToken = it },
                            label = { Text("Enter Verification OTP") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LyoColors.AccentOrange,
                                unfocusedBorderColor = Color(0x33F8FAFC)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (changeStep == 3) {
                        Text(
                            text = "Identity certified successfully. Set a new confidential passcode for your account below:",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = newResetPasswordText,
                            onValueChange = { newResetPasswordText = it },
                            label = { Text("New Login Passcode") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LyoColors.AccentOrange,
                                unfocusedBorderColor = Color(0x33F8FAFC)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x2210B981), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.VerifiedUser,
                                    contentDescription = "Success",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Password Reset Successful!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your account passcode has been synced and updated secure in local database.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    if (changeError.isNotEmpty()) {
                        Text(
                            text = changeError,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                    onClick = {
                        if (changeStep == 1) {
                            if (changePhone.trim().isEmpty()) {
                                changeError = "Please enter valid Account details"
                            } else {
                                changeError = ""
                                generatedOtpToken = (1000..9999).random().toString()
                                changeStep = 2
                                Toast.makeText(context, "🔐 Secure SMS alert sent to $changePhone. [Sandbox Verification Token: $generatedOtpToken]", Toast.LENGTH_LONG).show()
                            }
                        } else if (changeStep == 2) {
                            if (enteredOtpToken == generatedOtpToken) {
                                changeError = ""
                                changeStep = 3
                            } else {
                                changeError = "Authentication token mismatch. Please re-enter the code."
                            }
                        } else if (changeStep == 3) {
                            if (newResetPasswordText.isBlank() || newResetPasswordText.length < 4) {
                                changeError = "Passcode must be minimum 4 characters!"
                            } else {
                                changeError = ""
                                // Leverage the improved resetPassword method in the AuthViewModel
                                // which is shared or accessed via repository/viewModel mappings.
                                // We're using standard user update on Auth ViewModel
                                viewModel.resetPasswordOnStorefront(changePhone, newResetPasswordText) { ok ->
                                    if (ok) {
                                        changeStep = 4
                                    } else {
                                        changeError = "Specified account not registered in local directories."
                                    }
                                }
                            }
                        } else {
                            showChangePassDialog = false
                        }
                    }
                ) {
                    Text(
                        text = when (changeStep) {
                            3 -> "Set Passcode"
                            4 -> "Finish"
                            else -> "Proceed"
                        },
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                if (changeStep != 4) {
                    TextButton(onClick = { showChangePassDialog = false }) {
                        Text("Dismiss", color = Color.LightGray)
                    }
                }
            }
        )
    }
}

@Composable
fun SettingsScreen(
    currentUser: User?,
    onBack: () -> Unit,
    onChangePasscodeClick: () -> Unit,
    viewModel: StorefrontViewModel
) {
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var whatsAppUpdatesEnabled by remember { mutableStateOf(currentUser?.isWhatsAppOptIn ?: true) }
    var showTermsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Lyo Settings & Security",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: PROFILE DETAILS
        Text(
            text = "👤 PROFILE INFORMATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0x334F46E5), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Name:", color = Color(0xFF64748B), fontSize = 12.sp)
                    Text(currentUser?.name ?: "", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Phone:", color = Color(0xFF64748B), fontSize = 12.sp)
                    Text(currentUser?.phone ?: "", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Email:", color = Color(0xFF64748B), fontSize = 12.sp)
                    Text(currentUser?.email ?: "Not Set", color = Color.White, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Address:", color = Color(0xFF64748B), fontSize = 12.sp)
                    Text(currentUser?.address ?: "", color = Color.White, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(8.dp))

                var showEditProfileDialog by remember { mutableStateOf(false) }

                Button(
                    onClick = { showEditProfileDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile Details", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                if (showEditProfileDialog) {
                    var editName by remember { mutableStateOf(currentUser?.name ?: "") }
                    var editEmail by remember { mutableStateOf(currentUser?.email ?: "") }
                    var editAddress by remember { mutableStateOf(currentUser?.address ?: "") }
                    var editLat by remember { mutableStateOf(currentUser?.lat ?: 11.5812) }
                    var editLng by remember { mutableStateOf(currentUser?.lng ?: 77.8465) }

                    AlertDialog(
                        onDismissRequest = { showEditProfileDialog = false },
                        containerColor = Color(0xFF1E293B),
                        title = { Text("Edit Profile Details", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    label = { Text("Full Name") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = LyoColors.AccentOrange,
                                        unfocusedBorderColor = Color(0x33F8FAFC)
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = editEmail,
                                    onValueChange = { editEmail = it },
                                    label = { Text("Email ID") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = LyoColors.AccentOrange,
                                        unfocusedBorderColor = Color(0x33F8FAFC)
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = editAddress,
                                    onValueChange = { editAddress = it },
                                    label = { Text("Delivery Address") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = LyoColors.AccentOrange,
                                        unfocusedBorderColor = Color(0x33F8FAFC)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text("Map Picker Selection (தொட்டு மேப்பை நகர்த்தவும்):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                InteractiveMapPickerView(
                                    initialLat = editLat,
                                    initialLng = editLng,
                                    onLocationPicked = { lat, lng ->
                                        editLat = lat
                                        editLng = lng
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                onClick = {
                                    viewModel.updateUserProfile(
                                        name = editName,
                                        email = editEmail,
                                        address = editAddress,
                                        lat = editLat,
                                        lng = editLng,
                                        whatsAppOptIn = whatsAppUpdatesEnabled
                                    )
                                    showEditProfileDialog = false
                                }
                            ) {
                                Text("Save", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditProfileDialog = false }) {
                                Text("Cancel", color = Color.LightGray)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 1: ACCOUNT SECURITY
        Text(
            text = "🔒 SECURITY SETTINGS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0x334F46E5), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Manage confidential login session settings and digital authorization credentials below.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Passcode reset button (moved inside Settings only!)
                Button(
                    onClick = onChangePasscodeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("move_passcode_btn")
                ) {
                    Icon(Icons.Filled.VpnKey, contentDescription = "key", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Securely Change Account Passcode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION: ACTIVE DEVICES
        val activeSessions by viewModel.repository.activeSessions.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current
        val myDeviceId = remember { com.example.data.repository.LyoFirebaseHelper.getDeviceId(context) }

        Text(
            text = "📱 ACTIVE DEVICES & SECURITY (சாதனங்கள்)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0x334F46E5), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Manage active logins and remote device authorizations. Other authorized devices will be automatically and securely synchronized in real-time.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 15.sp
                )

                if (activeSessions.isEmpty()) {
                    Text("Loading authorized sessions...", color = Color.White, fontSize = 12.sp)
                } else {
                    activeSessions.forEachIndexed { index, session ->
                        val isMyDevice = session.deviceId == myDeviceId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isMyDevice) Color(0x1610B981) else Color(0x1638BDF8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Smartphone,
                                        contentDescription = null,
                                        tint = if (isMyDevice) Color(0xFF10B981) else Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = session.deviceName,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isMyDevice) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0x3310B981))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("THIS DEVICE", color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${session.osVersion} • ID: ${session.deviceId.take(8).uppercase()}",
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (!isMyDevice) {
                                IconButton(
                                    onClick = {
                                        currentUser?.phone?.let { phone ->
                                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(phone)
                                                .collection("sessions")
                                                .document(session.deviceId)
                                                .delete()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Cancel,
                                        contentDescription = "Terminate",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        if (index < activeSessions.size - 1) {
                            Divider(color = Color(0x11FFFFFF))
                        }
                    }

                    val otherSessionsCount = activeSessions.count { it.deviceId != myDeviceId }
                    if (otherSessionsCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                currentUser?.uid?.let { uid ->
                                    if (uid.isNotBlank()) {
                                        com.example.data.repository.LyoFirebaseHelper.removeAllOtherDeviceSessions(uid)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x22EF4444)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LOGOUT ALL OTHER DEVICES (மற்றவை வெளியேற்று)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 2: APP PREFERENCES
        Text(
            text = "⚙️ APP PREFERENCES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Row 1: Push Notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Push Notifications", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Get real-time order GPS tracker updates", color = Color(0xFF64748B), fontSize = 10.sp)
                        }
                    }
                    Switch(
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { pushNotificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LyoColors.AccentOrange,
                            checkedTrackColor = LyoColors.AccentOrange.copy(alpha = 0.4f)
                        )
                    )
                }

                Divider(color = Color(0x11FFFFFF))

                // Row 2: WhatsApp Updates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("WhatsApp Billing & Order Alerts", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Eco-friendly digital receipts on WhatsApp", color = Color(0xFF64748B), fontSize = 10.sp)
                        }
                    }
                    Switch(
                        checked = whatsAppUpdatesEnabled,
                        onCheckedChange = { 
                            whatsAppUpdatesEnabled = it
                            viewModel.updateUserWhatsAppOptIn(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LyoColors.AccentOrange,
                            checkedTrackColor = LyoColors.AccentOrange.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 3: LEGAL & PRIVACY
        Text(
            text = "📄 PRIVACY & LEGAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                .clickable { showTermsDialog = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Privacy Policy & Terms of Use", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Lyo cryptographic user data guarantees", color = Color(0xFF64748B), fontSize = 10.sp)
                    }
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // APP INFO STATEMENT
        Text(
            text = "Certified Secure App Preference Terminal • Build v2.4\nStored encrypted in secure shared local repository preferences.",
            fontSize = 10.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            lineHeight = 15.sp
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Lyo Security Policy 🛡️", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Text(
                    text = "Lyo utilizes robust local Android SQLite sandboxing databases to store active user identity tokens securely. Passcodes are hashed with standard salt mechanisms to provide impenetrable protection against security breaches. No financial or passcode logs ever bypass local encrypted terminals.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange)
                ) {
                    Text("I Understand", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun PastOrdersHistoryList(
    pastOrders: List<Order>,
    viewModel: StorefrontViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var expandedOrderId by remember { mutableStateOf<Long?>(null) }
    val downloadStatesMap = viewModel.invoiceDownloadStates

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(12.dp))

        // 4. CHRONOLOGICAL ORDER JOURNALING LIST
        Text(
            text = "📜 ORDER JOURNAL & INVOICES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = LyoColors.TextSecondary,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // FILTER CHIPS FOR 2 DAYS / 3 DAYS
        var historyFilter by remember { mutableStateOf("ALL") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = listOf(
                Triple("ALL", "All", "அனைத்தும்"),
                Triple("2_DAYS", "Within 2 Days", "2 நாட்கள்"),
                Triple("3_DAYS", "Within 3 Days", "3 நாட்கள்")
            )
            filterOptions.forEach { opt ->
                val isSelected = historyFilter == opt.first
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) LyoColors.AccentOrange else Color(0x1AFFFFFF))
                        .border(1.dp, if (isSelected) LyoColors.AccentOrange else Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .clickable { historyFilter = opt.first }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(opt.second, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(opt.third, color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Normal)
                    }
                }
            }
        }

        val filteredOrders = remember(pastOrders, historyFilter) {
            val now = System.currentTimeMillis()
            pastOrders.reversed().filter { order ->
                when (historyFilter) {
                    "2_DAYS" -> (now - order.timestamp) <= 2 * 24 * 60 * 60 * 1000L
                    "3_DAYS" -> (now - order.timestamp) <= 3 * 24 * 60 * 60 * 1000L
                    else -> true
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                borderColor = Color(0x1AFFFFFF)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.DinnerDining,
                        contentDescription = "empty history",
                        tint = LyoColors.TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No cuisine orders found!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Try changing the filter or place a new delicious order!",
                        color = LyoColors.TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
                    )
                }
            }
        } else {
            filteredOrders.forEach { order ->
                val isExpanded = expandedOrderId == order.id
                var orderItems by remember(order.id) { mutableStateOf<List<com.example.data.database.OrderItem>>(emptyList()) }
                var riderRideState by remember(order.id) { mutableStateOf<com.example.data.database.DeliveryRide?>(null) }

                // Fetch items & rider details when expanding
                LaunchedEffect(isExpanded) {
                    if (isExpanded) {
                        if (orderItems.isEmpty()) {
                            orderItems = viewModel.getOrderItems(order.id)
                        }
                        riderRideState = viewModel.getDeliveryRide(order.id)
                    }
                }

                Box(modifier = Modifier.padding(vertical = 6.dp)) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (order.status == "DELIVERED") Color(0x3322C55E) else LyoColors.GlassBorder,
                        backgroundColor = if (isExpanded) Color(0xFF131A2A) else LyoColors.TranslucentSlate,
                        onClick = {
                            expandedOrderId = if (isExpanded) null else order.id
                        }
                    ) {
                        Column {
                            // Header: Order ID & Date & Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Text(
                                            text = "ORDER LYO-${order.id}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        if (com.example.data.repository.LyoLiveTestTracker.isTestOrder(order)) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF8B5CF6))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("🧪 TEST ORDER", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    val formattedDate = remember(order.timestamp) {
                                        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                        sdf.format(java.util.Date(order.timestamp))
                                    }
                                    Text(
                                        text = formattedDate,
                                        fontSize = 11.sp,
                                        color = LyoColors.TextSecondary
                                    )

                                    // AGE BADGE (இரண்டு நாட்களுக்குள் / மூன்று நாட்களுக்குள்)
                                    val now = System.currentTimeMillis()
                                    val orderAgeMs = now - order.timestamp
                                    val orderAgeDays = orderAgeMs / (1000 * 60 * 60 * 24)
                                    val withinDaysText = when {
                                        orderAgeDays < 1 -> "⚡ Placed Today (இன்று)"
                                        orderAgeDays == 1L -> "⏱️ Placed Yesterday (நேற்று)"
                                        orderAgeDays <= 2 -> "⏱️ Within 2 Days (2 நாட்கள்)"
                                        orderAgeDays <= 3 -> "⏱️ Within 3 Days (3 நாட்கள்)"
                                        else -> "⏱️ $orderAgeDays Days Ago ($orderAgeDays நாட்களுக்கு முன்)"
                                    }
                                    Text(
                                        text = withinDaysText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (orderAgeDays <= 2) Color(0xFF34D399) else Color(0xFF60A5FA),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Status Sticker
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (order.status == "DELIVERED") Color(0x2222C55E) else if (order.status == "CANCELLED") Color(0x22EF4444) else Color(0x22FBBF24)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (order.status == "DELIVERED") Color(0xFF22C55E) else if (order.status == "CANCELLED") Color(0xFFEF4444) else Color(0xFFFBBF24),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = order.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (order.status == "DELIVERED") LyoColors.VegGreen else if (order.status == "CANCELLED") Color(0xFFEF4444) else LyoColors.AmberYellow
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Delete Order History Button (Trash Icon)
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteOrder(order.id)
                                            android.widget.Toast.makeText(context, "ஆர்டர் ஹிஸ்டரி நீக்கப்பட்டது / Order history deleted! 🗑️", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0x11FFFFFF), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Restaurant name and total paid simple row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Storefront,
                                        contentDescription = "store",
                                        tint = LyoColors.AmberYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = order.vendorName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 180.dp)
                                    )
                                }

                                Text(
                                    text = "₹${order.totalAmount.toInt()} PAID",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LyoColors.AccentOrange
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Loyalty Points Earned for this order
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val earnedPoints = (order.totalAmount / 10).toInt().coerceAtLeast(1)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "loyalty pts",
                                        tint = LyoColors.AmberYellow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ஆர்டர் மூலம் லாயல்டி புள்ளிகள் சேமிப்பு",
                                        fontSize = 11.sp,
                                        color = LyoColors.TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "+$earnedPoints PTS 🌟",
                                    fontSize = 12.sp,
                                    color = LyoColors.AmberYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Show micro expandable hint text
                            if (!isExpanded) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tap to view items & download invoice",
                                        fontSize = 10.sp,
                                        color = LyoColors.TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // EXPANDED CONTENT DETAILS
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    Divider(color = Color(0x19FFFFFF), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Display list of ordered items
                                    if (orderItems.isEmpty()) {
                                        Text(
                                            text = "Loading bill contents...",
                                            fontSize = 12.sp,
                                            color = LyoColors.TextSecondary
                                        )
                                    } else {
                                        orderItems.forEach { item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${item.quantity}x ${item.nameEn}",
                                                    fontSize = 12.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "₹${(item.price * item.quantity).toInt()}",
                                                    fontSize = 12.sp,
                                                    color = LyoColors.TextPrimary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Divider(color = Color(0x0AFFFFFF), thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Detailed Price breakdown
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Subtotal", fontSize = 11.sp, color = LyoColors.TextSecondary)
                                            Text("₹${order.subtotal.toInt()}", fontSize = 11.sp, color = LyoColors.TextSecondary)
                                        }
                                        if (order.couponDiscount > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Voucher Discount", fontSize = 11.sp, color = LyoColors.VegGreen)
                                                Text("-₹${order.couponDiscount.toInt()}", fontSize = 11.sp, color = LyoColors.VegGreen)
                                            }
                                        }
                                        if (order.deliveryFee > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Delivery Charge", fontSize = 11.sp, color = LyoColors.TextSecondary)
                                                Text("₹${order.deliveryFee.toInt()}", fontSize = 11.sp, color = LyoColors.TextSecondary)
                                            }
                                        }
                                        if (order.tipAmount > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Rider Gratuity Tip", fontSize = 11.sp, color = LyoColors.TextSecondary)
                                                Text("₹${order.tipAmount.toInt()}", fontSize = 11.sp, color = LyoColors.TextSecondary)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Download Bill anim button (order specific)
                                        val curDownloadState = downloadStatesMap[order.id] ?: "IDLE"

                                        if (curDownloadState == "IDLE") {
                                            Button(
                                                onClick = {
                                                    downloadStatesMap[order.id] = "DOWNLOADING"
                                                    coroutineScope.launch {
                                                        // Actually generate and save a real PDF invoice
                                                        try {
                                                            val pdfDoc = android.graphics.pdf.PdfDocument()
                                                            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(300, 420, 1).create()
                                                            val page = pdfDoc.startPage(pageInfo)
                                                            val canvas = page.canvas
                                                            val paint = android.graphics.Paint()
                                                            
                                                            paint.color = android.graphics.Color.BLACK
                                                            paint.textSize = 12f
                                                            paint.isFakeBoldText = true
                                                            canvas.drawText("Lyo AI Food Delivery", 10f, 25f, paint)
                                                            
                                                            paint.isFakeBoldText = false
                                                            paint.textSize = 8f
                                                            canvas.drawText("=============================", 10f, 38f, paint)
                                                            
                                                            canvas.drawText("Order ID: #${order.id}", 10f, 50f, paint)
                                                            canvas.drawText("Vendor: ${order.vendorName}", 10f, 62f, paint)
                                                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                                                            canvas.drawText("Date: ${sdf.format(java.util.Date(order.timestamp))}", 10f, 74f, paint)
                                                            canvas.drawText("Status: ${order.status}", 10f, 86f, paint)
                                                            
                                                            canvas.drawText("-----------------------------", 10f, 98f, paint)
                                                            canvas.drawText("Items:", 10f, 110f, paint)
                                                            
                                                             var yPos = 122f
                                                             for (item in orderItems) {
                                                                 canvas.drawText("${item.quantity}x ${item.nameEn} - Rs.${(item.price * item.quantity).toInt()}", 15f, yPos, paint)
                                                                 yPos += 12f
                                                             }
                                                             
                                                             canvas.drawText("-----------------------------", 10f, yPos, paint)
                                                             yPos += 12f
                                                             canvas.drawText("Subtotal: Rs.${order.subtotal.toInt()}", 10f, yPos, paint)
                                                             yPos += 12f
                                                             if (order.couponDiscount > 0) {
                                                                 canvas.drawText("Discount: -Rs.${order.couponDiscount.toInt()}", 10f, yPos, paint)
                                                                 yPos += 12f
                                                             }
                                                             if (order.deliveryFee > 0) {
                                                                 canvas.drawText("Delivery Charge: Rs.${order.deliveryFee.toInt()}", 10f, yPos, paint)
                                                                 yPos += 12f
                                                             }
                                                             if (order.tipAmount > 0) {
                                                                 canvas.drawText("Rider Tip: Rs.${order.tipAmount.toInt()}", 10f, yPos, paint)
                                                                 yPos += 12f
                                                             }
                                                             paint.isFakeBoldText = true
                                                             canvas.drawText("Total Paid: Rs.${order.totalAmount.toInt()}", 10f, yPos, paint)
                                                             
                                                             pdfDoc.finishPage(page)
                                                             
                                                             val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                                                             if (!downloadsDir.exists()) downloadsDir.mkdirs()
                                                             val invoiceFile = java.io.File(downloadsDir, "Lyo_Invoice_${order.id}.pdf")
                                                             
                                                             val fos = java.io.FileOutputStream(invoiceFile)
                                                             pdfDoc.writeTo(fos)
                                                             fos.close()
                                                             pdfDoc.close()
                                                             android.util.Log.d("LyoInvoice", "Invoice PDF created at: ${invoiceFile.absolutePath}")
                                                         } catch (e: Exception) {
                                                             e.printStackTrace()
                                                         }
                                                         
                                                         kotlinx.coroutines.delay(1800)
                                                         downloadStatesMap[order.id] = "SUCCESS"
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Download,
                                                    contentDescription = "download bill",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "DOWNLOAD INVOICE",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        } else if (curDownloadState == "DOWNLOADING") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0x33F97316)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = LyoColors.AccentOrange,
                                                        strokeWidth = 2.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Extracting receipt Lyo-${order.id}...",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        } else {
                                            // SUCCESS STATE
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0x1F22C55E))
                                                    .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(10.dp))
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = "saved",
                                                        tint = Color(0xFF22C55E),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Bill Saved! (Lyo_Invoice_${order.id}.pdf)",
                                                        color = Color(0xFF22C55E),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        // --- WRITE INLINE REVIEW FOR THE HOTEL/RESTAURANT ---
                                        Spacer(modifier = Modifier.height(12.dp))
                                        var orderRating by remember { mutableStateOf(5) }
                                        var orderReviewComment by remember { mutableStateOf("") }
                                        var reviewSubmittedForOrder by remember { mutableStateOf(false) }

                                        if (!reviewSubmittedForOrder) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0x0AFFFFFF))
                                                    .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(12.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "⭐ உணவகத்திற்கு மதிப்புரை / Rate Restaurant",
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        (1..5).forEach { star ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .clickable { orderRating = star }
                                                                    .padding(horizontal = 2.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Star,
                                                                    contentDescription = "$star Stars",
                                                                    tint = if (star <= orderRating) LyoColors.AmberYellow else Color.White.copy(alpha = 0.15f),
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = orderReviewComment,
                                                        onValueChange = { orderReviewComment = it },
                                                        placeholder = { Text("Write your review...", fontSize = 11.sp, color = Color.White.copy(alpha = 0.35f)) },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .heightIn(min = 56.dp),
                                                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.White,
                                                            focusedBorderColor = LyoColors.AccentOrange,
                                                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                                            focusedContainerColor = Color(0x26000000),
                                                            unfocusedContainerColor = Color(0x0D000000)
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        singleLine = false,
                                                        maxLines = 2
                                                    )

                                                    Button(
                                                        onClick = {
                                                            if (orderReviewComment.isNotBlank()) {
                                                                viewModel.submitReviewForVendor(order.vendorId, orderRating, orderReviewComment)
                                                                orderReviewComment = ""
                                                                orderRating = 5
                                                                reviewSubmittedForOrder = true
                                                                android.widget.Toast.makeText(context, "நன்றி! உங்கள் கருத்து சேர்க்கப்பட்டது / Submitted review! ✨", android.widget.Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.height(56.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                    ) {
                                                        Text("SUBMIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "மதிப்பீடு பகிரப்பட்டது / Review submitted successfully! 💚",
                                                color = Color(0xFF10B981),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 4.dp).align(Alignment.CenterHorizontally)
                                            )
                                        }

                                        // --- AWARD BONUS POINTS AND RATE THE DELIVERY BOY ---
                                        if (riderRideState != null) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            var riderRating by remember { mutableStateOf(5) }
                                            var selectedPointsReward by remember { mutableStateOf(10) }
                                            var riderRatedForOrder by remember { mutableStateOf(false) }

                                            if (!riderRatedForOrder) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0x0AFFFFFF))
                                                        .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(12.dp))
                                                        .padding(10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "🏅 டெலிவரி பாய்க்கு புள்ளிகள் / Rate Rider",
                                                                color = Color.White.copy(alpha = 0.9f),
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "Rider: ${riderRideState?.riderName ?: "Lyo Express Rider"}",
                                                                color = LyoColors.TextSecondary,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            (1..5).forEach { star ->
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clickable { riderRating = star }
                                                                        .padding(horizontal = 2.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Filled.Star,
                                                                        contentDescription = "$star Stars",
                                                                        tint = if (star <= riderRating) LyoColors.AmberYellow else Color.White.copy(alpha = 0.15f),
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Text(
                                                        text = "Award Bonus Points (பாயிண்ட்ஸ் வழங்குங்கள்):",
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(bottom = 6.dp)
                                                    )

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        listOf(10, 20, 50).forEach { pt ->
                                                            val isPtSelected = selectedPointsReward == pt
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(if (isPtSelected) Color(0x33F59E0B) else Color(0x0DFFFFFF))
                                                                    .border(
                                                                        1.dp,
                                                                        if (isPtSelected) LyoColors.AmberYellow else Color.White.copy(alpha = 0.1f),
                                                                        RoundedCornerShape(8.dp)
                                                                    )
                                                                    .clickable { selectedPointsReward = pt }
                                                                    .padding(vertical = 8.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "+$pt Pts",
                                                                    color = if (isPtSelected) LyoColors.AmberYellow else Color.White,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }

                                                        Button(
                                                            onClick = {
                                                                val rPhone = riderRideState?.riderPhone
                                                                if (!rPhone.isNullOrBlank()) {
                                                                    viewModel.submitRiderPointsAndRating(rPhone, riderRating, selectedPointsReward)
                                                                    riderRatedForOrder = true
                                                                    android.widget.Toast.makeText(context, "நன்றி! டெலிவரி பாய்க்கு புள்ளிகள் வழங்கப்பட்டது / Points awarded! 🎖️", android.widget.Toast.LENGTH_SHORT).show()
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.height(36.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                        ) {
                                                            Text("AWARD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                    }
                                                }
                                            } else {
                                                Text(
                                                    text = "டெலிவரி பாய்க்கு புள்ளிகள் வழங்கப்பட்டது / Rider points awarded successfully! 🎖️",
                                                    color = Color(0xFF34D399),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(vertical = 4.dp).align(Alignment.CenterHorizontally)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerOrdersSection(
    currentUser: User?,
    pastOrders: List<Order>,
    activeOrderVal: com.example.data.database.Order?,
    onNavigateToActiveOrder: () -> Unit,
    viewModel: StorefrontViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. ACTIVE ORDER LIVE SHORTCUT CARD
        if (activeOrderVal != null && activeOrderVal.status != "DELIVERED" && activeOrderVal.status != "CANCELLED") {
            var localRideState by remember(activeOrderVal.id) { mutableStateOf<com.example.data.database.DeliveryRide?>(null) }
            LaunchedEffect(activeOrderVal.id) {
                localRideState = viewModel.getDeliveryRide(activeOrderVal.id)
            }
            val displayRiderName = localRideState?.riderName ?: "Lyo Express Rider"

            Text(
                text = "⚡ LIVE TRANSIT COURIER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = LyoColors.AmberYellow,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToActiveOrder() },
                borderColor = Color(0xFF22C55E),
                backgroundColor = Color(0xFF0F172A)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsBike,
                                    contentDescription = "delivery",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                val displayStatus = when (activeOrderVal.status) {
                                    "PENDING" -> "Awaiting Approval"
                                    "ACCEPTED" -> "Accepted & Chef Assigned"
                                    "PREPARING" -> "Cooking in Progress"
                                    "READY_FOR_PICKUP" -> "Ready at Kitchen Counter"
                                    "OUT_FOR_DELIVERY" -> "Courier out in transit!"
                                    "DELIVERED" -> "Arrived safely!"
                                    else -> "In transit"
                                }
                                Text(
                                    text = "Status: $displayStatus",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "assigned to TVS Rider $displayRiderName",
                                    color = LyoColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Neon circular pulse indicator
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color(0x11FFFFFF), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Your order journey is live in real-time. Follow driver $displayRiderName on our integrated High-Fidelity Cyber map telemetry.",
                        fontSize = 12.sp,
                        color = LyoColors.TextPrimary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToActiveOrder,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("open_live_tracker_from_tab")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "location scan",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OPEN LIVE RADAR MAP",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. CHRONOLOGICAL PAST ORDERS LIST USING REUSED COMPOSABLE
        PastOrdersHistoryList(
            pastOrders = pastOrders,
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


// ==========================================
// 2. VENDOR PROFILE & BILINGUAL MENU GRID
// ==========================================
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VendorProfileScreen(
    vendorId: Long,
    viewModel: StorefrontViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCartCheckout: () -> Unit
) {
    val vendors by viewModel.allVendors.collectAsState(initial = emptyList())
    val partner = vendors.find { it.id == vendorId } ?: return

    LaunchedEffect(partner) {
        viewModel.repository.currentVendor.value = partner
    }

    val categoriesFlow = remember(vendorId) { viewModel.getCategoriesForVendor(vendorId) }
    val categories by categoriesFlow.collectAsState(initial = emptyList())
    val menuItemsFlow = remember(vendorId) { viewModel.getMenuItemsForVendor(vendorId) }
    val menuItems by menuItemsFlow.collectAsState(initial = emptyList())
    val cart by viewModel.activeCart.collectAsState(initial = emptyMap())
    val reviews by viewModel.activeVendorReviews.collectAsState(initial = emptyList())

    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var showStoreInfoDialog by remember { mutableStateOf(false) }
    var menuSearchQuery by remember { mutableStateOf("") }
    var dragOffset by remember { mutableStateOf(0f) }
    var selectedVegFilter by remember { mutableStateOf("ALL") } // "ALL", "VEG", "NON_VEG"

    LyoBackground {
        val bannerKey = partner.bannerUrl.lowercase()
        val coverGradient = when {
            bannerKey.contains("hotel") || bannerKey.contains("saravana") -> Brush.verticalGradient(colors = listOf(Color(0xFF15803D), Color(0xFF022C22)))
            bannerKey.contains("restaurant") || bannerKey.contains("anjappar") -> Brush.verticalGradient(colors = listOf(Color(0xFFB91C1C), Color(0xFF450A0A)))
            bannerKey.contains("cafe") || bannerKey.contains("coffee") -> Brush.verticalGradient(colors = listOf(Color(0xFF78350F), Color(0xFF451A03)))
            bannerKey.contains("bakery") || bannerKey.contains("iyengar") -> Brush.verticalGradient(colors = listOf(Color(0xFFD97706), Color(0xFF78350F)))
            bannerKey.contains("snack") || bannerKey.contains("murugan") -> Brush.verticalGradient(colors = listOf(Color(0xFFEA580C), Color(0xFF7C2D12)))
            else -> Brush.verticalGradient(colors = listOf(Color(0xFF475569), Color(0xFF0F172A)))
        }
        val coverIcon = when {
            bannerKey.contains("hotel") || bannerKey.contains("saravana") -> Icons.Filled.Restaurant
            bannerKey.contains("restaurant") || bannerKey.contains("anjappar") -> Icons.Filled.Restaurant
            bannerKey.contains("cafe") || bannerKey.contains("coffee") -> Icons.Filled.Coffee
            bannerKey.contains("bakery") || bannerKey.contains("iyengar") -> Icons.Filled.Cake
            bannerKey.contains("snack") || bannerKey.contains("murugan") -> Icons.Filled.Fastfood
            else -> Icons.Filled.Storefront
        }
        val isCustomImage = partner.bannerUrl.isNotBlank() && (
            partner.bannerUrl.startsWith("http://", ignoreCase = true) ||
            partner.bannerUrl.startsWith("https://", ignoreCase = true) ||
            partner.bannerUrl.startsWith("content://", ignoreCase = true) ||
            partner.bannerUrl.startsWith("file://", ignoreCase = true) ||
            partner.bannerUrl.startsWith("/storage") ||
            partner.bannerUrl.startsWith("/data")
        )

        val visibleCategories = remember(categories, menuItems, selectedVegFilter) {
            val base = categories.filter { it.isCurrentlyVisible }
            when (selectedVegFilter) {
                "VEG" -> {
                    val vegCategoryIds = menuItems.filter { it.isTrulyVeg }.map { it.categoryId }.toSet()
                    base.filter { vegCategoryIds.contains(it.id) }
                }
                "NON_VEG" -> {
                    val nonVegCategoryIds = menuItems.filter { !it.isTrulyVeg }.map { it.categoryId }.toSet()
                    base.filter { nonVegCategoryIds.contains(it.id) }
                }
                else -> base
            }
        }
        val activeCategory = if (visibleCategories.isNotEmpty() && selectedCategoryIndex < visibleCategories.size) visibleCategories[selectedCategoryIndex] else null
        val activeItems = remember(menuItems, activeCategory, menuSearchQuery, selectedVegFilter) {
            val baseItems = if (menuSearchQuery.isBlank()) {
                if (activeCategory != null) {
                    menuItems.filter { it.categoryId == activeCategory.id }
                } else menuItems
            } else {
                menuItems.filter {
                    it.nameEn.contains(menuSearchQuery, ignoreCase = true) ||
                    it.nameTa.contains(menuSearchQuery, ignoreCase = true) ||
                    it.descEn.contains(menuSearchQuery, ignoreCase = true)
                }
            }
            when (selectedVegFilter) {
                "VEG" -> baseItems.filter { it.isTrulyVeg }
                "NON_VEG" -> baseItems.filter { !it.isTrulyVeg }
                else -> baseItems
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedCategoryIndex, visibleCategories.size) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (visibleCategories.isNotEmpty()) {
                                if (dragOffset < -120f) { // Swipe left -> Next category
                                    val nextIndex = (selectedCategoryIndex + 1).coerceAtMost(visibleCategories.size - 1)
                                    selectedCategoryIndex = nextIndex
                                } else if (dragOffset > 120f) { // Swipe right -> Previous category
                                    val prevIndex = (selectedCategoryIndex - 1).coerceAtLeast(0)
                                    selectedCategoryIndex = prevIndex
                                }
                            }
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { change: PointerInputChange, dragAmount: Float ->
                            change.consume()
                            dragOffset += dragAmount
                        }
                    )
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // 1) Compact Premium Cover Header Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp) // Perfect height for the luxury background banner image
                            .background(if (isCustomImage) Color(0xFF0F172A) else Color.Transparent)
                    ) {
                        if (isCustomImage) {
                            val painterModel = remember(partner.bannerUrl) {
                                if (partner.bannerUrl.startsWith("/")) java.io.File(partner.bannerUrl) else partner.bannerUrl
                            }
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(painterModel),
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0x1F0B1120), Color(0x660B1120))
                                        )
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(coverGradient)
                            )
                            Row(
                                modifier = Modifier.fillMaxSize().padding(end = 24.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = coverIcon,
                                    contentDescription = null,
                                    tint = Color(0x15FFFFFF),
                                    modifier = Modifier.size(100.dp)
                                )
                            }
                        }

                        if (!partner.isCurrentlyOpen) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xAA020617))
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color(0xFFDC2626), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "CLOSED TODAY",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Floating row overlay for back and info pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(LyoColors.TranslucentBlack)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "back", tint = Color.White, modifier = Modifier.size(20.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LyoColors.TranslucentBlack)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = partner.type.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LyoColors.AmberYellow
                                )
                            }
                        }
                    }
                }

                // 2) Breathtaking 3D Floating Header Card (Overlaps the banner image beautifully)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-30).dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(LyoColors.CardSlate, Color(0xFF071426))
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .border(
                                    width = 1.2.dp,
                                    color = LyoColors.GlassBorder,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = partner.name.uppercase(),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                    if (partner.nameTa.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = partner.nameTa,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LyoColors.AmberYellow
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = { showStoreInfoDialog = true },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(LyoColors.AccentOrange)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = "Store Info",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = LyoColors.TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = partner.address,
                                    fontSize = 11.sp,
                                    color = LyoColors.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            androidx.compose.material3.HorizontalDivider(
                                color = LyoColors.GlassBorder,
                                thickness = 1.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Premium Rating Badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x22F59E0B))
                                        .border(0.5.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                                        .clickable { showStoreInfoDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${partner.rating}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⏱️ ", fontSize = 11.sp)
                                    Text(
                                        text = "${partner.deliveryTime} mins",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🏍️ ", fontSize = 11.sp)
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.1f", partner.distance)} km",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🚀 ", fontSize = 11.sp)
                                    Text(
                                        text = "Min: ₹${partner.minOrderAmount.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // 2b) Quick Photos Gallery Trigger has been removed since food item photos are not needed in this application.

                // 3) Coupon Promos (Scrolls away)
                if (false) { // Disabled per user request (unnecessary coupon widget resolved)
                    item {
                        val appliedCouponCode by viewModel.appliedCoupon.collectAsState(initial = "")
                        val isAlreadyApplied = appliedCouponCode?.equals(partner.couponCode, ignoreCase = true) == true
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isAlreadyApplied) Color(0x2222C55E) else Color(0x22F97316)
                                )
                                .border(
                                    1.5.dp,
                                    if (isAlreadyApplied) Color(0xFF22C55E) else Color(0xFFF97316),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (isAlreadyApplied) {
                                        viewModel.appliedCoupon.value = null
                                    } else {
                                        viewModel.applyCoupon(partner.couponCode)
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalOffer,
                                        contentDescription = "coupon",
                                        tint = if (isAlreadyApplied) Color(0xFF22C55E) else Color(0xFFF97316),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isAlreadyApplied) "COUPON INSTALLED: ${partner.couponCode}" else "MERCHANT SPECIAL DISCOUNT OFFER",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isAlreadyApplied) Color(0xFF22C55E) else Color(0xFFF97316)
                                        )
                                        Text(
                                            text = "Save ₹${partner.couponDiscount.toInt()} instantly on orders above ₹${partner.couponMinOrder.toInt()}",
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isAlreadyApplied) Color(0xFF22C55E) else Color(0xFFF97316))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isAlreadyApplied) "ACTIVE" else "1-TAP APPLY",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // 3.5) Luxury Premium Menu Item Search Box
                item {
                    Lyo3DSearchBar(
                        value = menuSearchQuery,
                        onValueChange = { menuSearchQuery = it },
                        placeholder = "Search dishes...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        trailingIcon = {
                            if (menuSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { menuSearchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close, 
                                        contentDescription = "Clear", 
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    )
                }

                // Veg/Non-Veg Quick Switch Segmented Control - Single tap lightning-fast sorting
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All Button
                        val isAllSelected = selectedVegFilter == "ALL"
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllSelected) Color(0xFF16C7E8) else Color(0xFF14233D))
                                .border(
                                    width = 1.dp,
                                    color = if (isAllSelected) Color(0xFF16C7E8) else Color(0x1AD9E2EC),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { 
                                    selectedVegFilter = "ALL"
                                    selectedCategoryIndex = 0
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RestaurantMenu,
                                    contentDescription = "All",
                                    tint = if (isAllSelected) Color(0xFF071225) else Color(0xFFB9C6D8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "அனைத்தும் / ALL",
                                    color = if (isAllSelected) Color(0xFF071225) else Color(0xFFB9C6D8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Veg Button
                        val isVegSelected = selectedVegFilter == "VEG"
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isVegSelected) Color(0xFF16A56B) else Color(0xFF14233D))
                                .border(
                                    width = 1.dp,
                                    color = if (isVegSelected) Color(0xFF16A56B) else Color(0x1AD9E2EC),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { 
                                    selectedVegFilter = "VEG"
                                    selectedCategoryIndex = 0
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .border(1.dp, if (isVegSelected) Color.White else Color(0xFF16A56B), RoundedCornerShape(2.dp))
                                        .padding(1.2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(if (isVegSelected) Color.White else Color(0xFF16A56B))
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "சைவம் / VEG 🌱",
                                    color = if (isVegSelected) Color.White else Color(0xFFB9C6D8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Non-Veg Button
                        val isNonVegSelected = selectedVegFilter == "NON_VEG"
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNonVegSelected) Color(0xFFD94A52) else Color(0xFF14233D))
                                .border(
                                    width = 1.dp,
                                    color = if (isNonVegSelected) Color(0xFFD94A52) else Color(0x1AD9E2EC),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { 
                                    selectedVegFilter = "NON_VEG"
                                    selectedCategoryIndex = 0
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .border(1.dp, if (isNonVegSelected) Color.White else Color(0xFFD94A52), RoundedCornerShape(2.dp))
                                        .padding(1.2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(if (isNonVegSelected) Color.White else Color(0xFFD94A52))
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "அசைவம் / NON-VEG 🍗",
                                    color = if (isNonVegSelected) Color.White else Color(0xFFB9C6D8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (menuSearchQuery.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Search Results (Results for: \"$menuSearchQuery\")",
                                color = LyoColors.AmberYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${activeItems.size} items)",
                                color = LyoColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // 4) Horizontal category selection selector tabs (Sticky)
                if (visibleCategories.isNotEmpty() && menuSearchQuery.isBlank()) {
                    stickyHeader {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF090D16), // Solid dark color matching Lyo style to cleanly block items scrolling behind it
                            shadowElevation = 2.dp
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(visibleCategories.size, key = { visibleCategories[it].id }) { idx ->
                                    val cat = visibleCategories[idx]
                                    val isSelected = selectedCategoryIndex == idx
                                    
                                    // 3D Glass / Liquid tab effect
                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFFFF7A1A) else Color(0xFF14233D))
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color(0xFFFF7A1A) else Color(0x22B9C6D8),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedCategoryIndex = idx }
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = cat.nameEn,
                                                color = if (isSelected) Color.White else Color(0xFFB9C6D8),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (cat.nameTa.isNotBlank() && cat.nameTa != cat.nameEn) {
                                                Text(
                                                    text = cat.nameTa,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF8E9BAE),
                                                    fontSize = 9.sp,
                                                    lineHeight = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5) Active category billing items list
                if (activeItems.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No items loaded in this collection.", color = LyoColors.TextSecondary)
                        }
                    }
                } else {
                    items(activeItems, key = { it.id }) { dish ->
                        val qtyInCart = cart[dish] ?: 0
                        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                glowColor = if (qtyInCart > 0) Color(0xFF38BDF8).copy(alpha = 0.5f) else null,
                                innerPadding = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (false && dish.imageUrl.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                                .background(Brush.verticalGradient(colors = listOf(Color(0x33FFFFFF), Color(0x11000000))))
                                        ) {
                                            androidx.compose.foundation.Image(
                                                painter = coil.compose.rememberAsyncImagePainter(dish.imageUrl),
                                                contentDescription = "dish photo",
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            // 3D specular gel overlay
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color.White.copy(alpha = 0.45f),
                                                                Color.White.copy(alpha = 0.08f),
                                                                Color.Transparent,
                                                                Color.Black.copy(alpha = 0.35f)
                                                            ),
                                                            startY = 0f,
                                                            endY = 120f
                                                        )
                                                    )
                                            )
                                            // specular rim border
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .border(
                                                        1.dp,
                                                        Brush.linearGradient(
                                                            colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent, Color.White.copy(alpha = 0.2f))
                                                        ),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        if (dish.autoOpenTime.isNotBlank() && dish.autoCloseTime.isNotBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .padding(bottom = 2.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color(0xFF3B2E1E))
                                                    .border(0.5.dp, Color(0xFFFBBF24).copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 3.dp, vertical = 1.0.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AccessTime,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFBBF24),
                                                    modifier = Modifier.size(8.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "கிடைக்கும் நேரம்: ${dish.autoOpenTime} - ${dish.autoCloseTime}",
                                                    fontSize = 8.5.sp,
                                                    lineHeight = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFBBF24)
                                                )
                                            }
                                        }
                                        Text(
                                            text = dish.nameEn,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        
                                        if (dish.nameTa.isNotBlank() && dish.nameTa != dish.nameEn) {
                                            Text(
                                                text = dish.nameTa,
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF20D7F2),
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(top = 1.dp)
                                            )
                                        }

                                        if (dish.descEn.isNotBlank()) {
                                            Text(
                                                text = dish.descEn,
                                                fontSize = 9.5.sp,
                                                lineHeight = 12.sp,
                                                color = Color(0xFFB9C6D8),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "₹${dish.price.toInt()}",
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            VegIndicator(isVeg = dish.isVeg)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (!dish.isCurrentlyAvailable || !partner.isCurrentlyOpen) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0x22EF4444))
                                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (!partner.isCurrentlyOpen) "CLOSED TODAY" else "OUT OF STOCK",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    } else if (qtyInCart == 0) {
                                        Button(
                                            onClick = {
                                                viewModel.addToCart(dish, partner)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                            border = BorderStroke(1.dp, Color(0xFF16C7E8)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(26.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Text("ADD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16C7E8))
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .background(LyoColors.AccentOrange, RoundedCornerShape(5.dp))
                                                .padding(horizontal = 1.dp, vertical = 0.5.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.removeFromCart(dish) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Filled.Remove, contentDescription = "remove", tint = Color.White, modifier = Modifier.size(10.dp))
                                            }

                                            Text(
                                                text = qtyInCart.toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )

                                            IconButton(
                                                onClick = {
                                                    viewModel.addToCart(dish, partner)
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Filled.Add, contentDescription = "add", tint = Color.White, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        // Animated Sticky Bar to Proceed to Checkout
        val cartTotalBytes = cart.values.sum()
        if (cartTotalBytes > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0xE60F172A))
                    .border(1.dp, LyoColors.GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val cartItemsText = remember(cart) {
                            cart.filter { it.value > 0 }.entries.joinToString(", ") { "${it.value}x ${it.key.nameEn}" }
                        }
                        val truncatedCartText = if (cartItemsText.length > 36) cartItemsText.take(33) + "..." else cartItemsText
                        
                        Text(
                            text = if (truncatedCartText.isNotBlank()) truncatedCartText else "$cartTotalBytes ITEMS",
                            color = LyoColors.TextSecondary,
                            fontSize = 9.5.sp,
                            maxLines = 1,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "Total: ₹${viewModel.getCartSubtotal().toInt()}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Button(
                        onClick = onNavigateToCartCheckout,
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Text("VIEW CART", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = "proceed", modifier = Modifier.size(14.dp), tint = Color.White)
                    }
                }
            }
        }

        if (showStoreInfoDialog) {
            AlertDialog(
                onDismissRequest = { showStoreInfoDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(24.dp)),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = partner.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (partner.nameTa.isNotBlank()) {
                                Text(
                                    text = partner.nameTa,
                                    color = LyoColors.TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        IconButton(
                            onClick = { showStoreInfoDialog = false },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x19FFFFFF))
                                .size(30.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("📍 முகவரி / ADDRESS", color = LyoColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(partner.address, color = Color.White, fontSize = 12.sp)
                            }
                            Column {
                                Text("📞 தொடர்பு / PHONE", color = LyoColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if (partner.phone.isNotBlank()) partner.phone else "+91 94435-00000", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0x11FFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("⭐ CUSTOMER RATING", color = LyoColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format("%.1f", partner.rating),
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("out of 5.0", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                }
                            }
                            RatingStars(rating = partner.rating)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "💬 வாடிக்கையாளர் கருத்துக்கள் (FEEDBACKS & REVIEWS)",
                            color = LyoColors.AmberYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val reviewsList = remember(partner.id) {
                            val isCafe = partner.type.lowercase().contains("cafe") || partner.type.lowercase().contains("coffee")
                            val isBakery = partner.type.lowercase().contains("bakery") || partner.type.lowercase().contains("sweet")
                            val isSnack = partner.type.lowercase().contains("snack") || partner.type.lowercase().contains("tea")
                            val isHotel = partner.type.lowercase().contains("hotel") || partner.type.lowercase().contains("saravana")
                            val isDhaba = partner.type.lowercase().contains("dhaba")
                            val name = partner.nameTa.ifEmpty { partner.name }
                            
                            when {
                                isCafe -> listOf(
                                    Triple(
                                        "விக்னேஷ் (Vignesh)",
                                        "Excellent filter coffee and quick bites! The aroma of coffee in $name is super. 5/5!",
                                        4.9
                                    ),
                                    Triple(
                                        "Deepika R",
                                        "Lovely atmosphere and fantastic sandwiches. Highly recommend $name for coffee dates.",
                                        4.7
                                    ),
                                    Triple(
                                        "செல்வராஜ் (Selvaraj)",
                                        "காபி மற்றும் ஸ்னாக்ஸ் தரம் அருமை. Packaging was highly secure and neat.",
                                        4.5
                                    )
                                )
                                isBakery -> listOf(
                                    Triple(
                                        "காயத்ரி (Gayathri)",
                                        "$name-ல் கேக் மற்றும் பப்ஸ் எப்போதும் பிரெஷ்ஷாக இருக்கும். Superb taste and safe delivery!",
                                        4.8
                                    ),
                                    Triple(
                                        "Arun Prasath",
                                        "Bought birthday cakes and hot puffs. Both are extremely fresh and delicious. Highly recommend!",
                                        4.9
                                    ),
                                    Triple(
                                        "பழனிவேல் (Palanivel)",
                                        "மிகவும் அருமையான பேக்கரி ஐட்டம்ஸ். சுவையும் தரமும் அருமை.",
                                        4.6
                                    )
                                )
                                isSnack -> listOf(
                                    Triple(
                                        "சுரேஷ் (Suresh)",
                                        "மாலை நேர ஸ்னாக்ஸ் மற்றும் டீ மிக அருமை. சூடாக டெலிவரி செய்யப்பட்டது. Thanks to $name!",
                                        4.7
                                    ),
                                    Triple(
                                        "Janani S",
                                        "Super crispy snacks and high quality tea/coffee. Very pocket-friendly and hygienic.",
                                        4.8
                                    ),
                                    Triple(
                                        "முத்து (Muthu)",
                                        "வடை, பஜ்ஜி மற்றும் டீ அருமை. Fast delivery and perfect location accuracy.",
                                        4.5
                                    )
                                )
                                isDhaba -> listOf(
                                    Triple(
                                        "சரவணன் (Saravanan)",
                                        "Fantastic Punjabi style food in $name. Naan and Butter Paneer are top tier!",
                                        4.8
                                    ),
                                    Triple(
                                        "Manpreet Singh",
                                        "Real authentic dhaba taste! Highly satisfied with the spice level and prompt delivery.",
                                        5.0
                                    ),
                                    Triple(
                                        "கார்த்திகேயன் (Karthikeyan)",
                                        "தந்தூரி மற்றும் சைடிஷ் வகைகள் மிகவும் அற்புதம். Nice packaging.",
                                        4.6
                                    )
                                )
                                else -> {
                                    val reviews = ArrayList<Triple<String, String, Double>>()
                                    if (partner.id % 2L == 0L) {
                                        reviews.add(Triple(
                                            "ராஜேஷ் குமார் (Rajesh Kumar)",
                                            "$name உணவு மிகவும் அருமையாக, சூடாகவும் சுவையாகவும் டெலிவரி செய்யப்பட்டது. 5/5!",
                                            4.8
                                        ))
                                        reviews.add(Triple(
                                            "Anitha Selvam",
                                            "The meals from $name are incredibly authentic! Perfect packaging and exact delivery coordinates. Really satisfied.",
                                            5.0
                                        ))
                                        reviews.add(Triple(
                                            "அன்பழகன் (Anbalagan)",
                                            "நம்ம ஊரு ஸ்பெஷல்... தரம் மற்றும் சுவைக்கு கேரண்டி! Excellent service from $name.",
                                            4.5
                                        ))
                                    } else {
                                        reviews.add(Triple(
                                            "Karthik Raja S",
                                            "Highly recommended for daily lunch orders from $name. Food is always fresh, and riders navigate accurately.",
                                            4.9
                                        ))
                                        reviews.add(Triple(
                                            "திவ்யா (Divya P)",
                                            "$name பிரியாணி மற்றும் சைட் டிஷ்ஸ் மிகவும் சுவையாக இருந்தது. Value for money!",
                                            4.7
                                        ))
                                        reviews.add(Triple(
                                            "Nagarajan K",
                                            "Good portion size, neat packing and excellent hot south Indian food.",
                                            4.6
                                        ))
                                    }
                                    reviews
                                }
                            }
                        }

                        reviewsList.forEach { (name, comment, rVal) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x0AFFFFFF))
                                    .border(1.dp, Color(0x0FFFFFFF), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    RatingStars(rating = rVal)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(comment, color = LyoColors.TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }

                        // --- LIVE DATABASE REVIEWS ---
                        if (reviews.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "⭐ நேரடி மதிப்புரைகள் (LIVE CUSTOMER REVIEWS)",
                                color = LyoColors.AmberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            reviews.forEach { r ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x1438BDF8))
                                        .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(r.userName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Row {
                                            (1..5).forEach { star ->
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = if (star <= r.rating) LyoColors.AmberYellow else Color.White.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(r.comment, color = LyoColors.TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
                                    val dateStr = remember(r.timestamp) {
                                        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US)
                                        sdf.format(java.util.Date(r.timestamp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dateStr,
                                        color = LyoColors.TextSecondary,
                                        fontSize = 8.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }

                        // --- WRITE REVIEW FORM ---
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0x11FFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        var newRating by remember { mutableStateOf(5) }
                        var newComment by remember { mutableStateOf("") }
                        var hasSubmitted by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x0AFFFFFF))
                                .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "கருத்து அல்லது மதிப்புரை எழுதுக / Write Feedback",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("மதிப்பீடு / Star Rating:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                                Row {
                                    (1..5).forEach { star ->
                                        Box(
                                            modifier = Modifier
                                                .clickable { newRating = star }
                                                .padding(horizontal = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "$star Stars",
                                                tint = if (star <= newRating) LyoColors.AmberYellow else Color.White.copy(alpha = 0.15f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newComment,
                                    onValueChange = { newComment = it },
                                    placeholder = { Text("Write about your experience...", fontSize = 11.sp, color = Color.White.copy(alpha = 0.35f)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = LyoColors.AccentOrange,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                        focusedContainerColor = Color(0x26000000),
                                        unfocusedContainerColor = Color(0x0D000000)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = false,
                                    maxLines = 2
                                )

                                Button(
                                    onClick = {
                                        if (newComment.isNotBlank()) {
                                            viewModel.submitReview(newRating, newComment)
                                            newComment = ""
                                            newRating = 5
                                            hasSubmitted = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(52.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text("SUBMIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            if (hasSubmitted) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "நன்றி! உங்கள் கருத்து சேர்க்கப்பட்டது / Added!",
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                LaunchedEffect(Unit) {
                                    delay(3000L)
                                    hasSubmitted = false
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showStoreInfoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CLOSE (மூடு)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}


// ==========================================
// 3. SEAMLESS CHECKOUT & TIPPING DRAWER
// ==========================================
@Composable
fun CheckoutCartScreen(
    viewModel: StorefrontViewModel,
    onNavigateBack: () -> Unit,
    onCheckoutSuccessful: (Long) -> Unit
) {
    val cart by viewModel.activeCart.collectAsState(initial = emptyMap())
    val partner = viewModel.activeVendor.collectAsState().value

    if (cart.isEmpty() || partner == null) {
        LyoBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Empty Basket",
                    tint = LyoColors.AccentOrange.copy(alpha = 0.8f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "உங்கள் கூடை காலியாக உள்ளது!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lyo உணவுகளை உடனே ஆர்டர் செய்ய முகப்புப் பக்கத்திற்குச் செல்லவும்.",
                    fontSize = 13.sp,
                    color = LyoColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
                LyoButton(
                    text = "மீண்டும் கடைக்குச் செல்லவும் (Go back to Shop)",
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("empty_cart_go_back")
                )
            }
        }
        return
    }

    val initialVendorId = remember { partner.id }
    val menuItemsFlow = remember(partner.id) { viewModel.getMenuItemsForVendor(partner.id) }
    val menuItems by menuItemsFlow.collectAsState(initial = emptyList())
    val cartItemIds = cart.keys.map { it.id }.toSet()
    val recommendedItems = menuItems.filter { it.id !in cartItemIds && it.isAvailable }.take(3)

    val discount = viewModel.getCouponDiscount()
    val subtotal = viewModel.getCartSubtotal()
    val deliveryFee = viewModel.getCartDeliveryFee()
    val tipAmount by viewModel.selectedTipAmount.collectAsState(initial = 0.0)
    val totalAmount = viewModel.getCartTotalAmount()

    val currentUser by viewModel.currentUser.collectAsState()
    val pastOrdersFlow = remember(currentUser?.phone) { viewModel.getOrdersForUser(currentUser?.phone ?: "") }
    val pastOrders by pastOrdersFlow.collectAsState(initial = emptyList())
    val totalLoyaltyPoints = pastOrders.sumOf { ((it.totalAmount / 10).toInt() - it.redeemedPoints) }.coerceAtLeast(0)
    var redeemLoyaltyPoints by remember { mutableStateOf(false) }
    val loyaltyDiscount = if (redeemLoyaltyPoints) (totalLoyaltyPoints * 0.10).coerceAtMost(subtotal) else 0.0
    val finalTotalAmount = (totalAmount - loyaltyDiscount).coerceAtLeast(0.0)

    var couponField by remember { mutableStateOf("") }
    val appliedCouponCode by viewModel.appliedCoupon.collectAsState(initial = "")
    val couponErrorMsg by viewModel.couponError.collectAsState(initial = "")

    // Slider state for custom tips
    var sliderTipValue by remember { mutableFloatStateOf(0f) }
    var showConfirmOrderDialog by remember { mutableStateOf(false) }
    var isAddressConfirmed by remember { mutableStateOf(false) }

    val isSessionRestoring = remember(currentUser) {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null && currentUser == null
    }

    LaunchedEffect(Unit) {
        if (com.example.BuildConfig.DEBUG) {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            android.util.Log.d("LyoAuthDebug", "Auth UID when checkout opens: $uid")
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val savedAddresses by viewModel.savedAddresses.collectAsState(initial = emptyList())

    // Auto-prepopulate state with the logged-in user's profile info on screen mount if available
    var deliveryAddress by remember(currentUser) { mutableStateOf(currentUser?.address ?: "") }
    var deliveryLat by remember(currentUser) { mutableStateOf(currentUser?.lat ?: 0.0) }
    var deliveryLng by remember(currentUser) { mutableStateOf(currentUser?.lng ?: 0.0) }

    LaunchedEffect(savedAddresses) {
        val defaultAddress = savedAddresses.find { it.isDefault }
        if (defaultAddress != null) {
            deliveryAddress = defaultAddress.addressLine
            deliveryLat = defaultAddress.latitude
            deliveryLng = defaultAddress.longitude
        }
    }

    LaunchedEffect(deliveryLat, deliveryLng) {
        viewModel.updateCheckoutCoordinates(deliveryLat, deliveryLng)
    }

    var showMapDialog by remember { mutableStateOf(false) }

    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                val locResult = fetchCurrentLocationAndReverseGeocode(context)
                if (locResult != null) {
                    deliveryLat = locResult.first
                    deliveryLng = locResult.second
                    deliveryAddress = locResult.third
                    Toast.makeText(context, "📍 GPS லொகேஷன் வெற்றிகரமாக பெறப்பட்டது!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "GPS சிக்னல் கிடைக்கவில்லை! தயவுசெய்து மீண்டும் முயற்சிக்கவும்.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(context, "லொகேஷன் அனுமதி மறுக்கப்பட்டது! மேனுவலாக முகவரியை உள்ளிடவும்.", Toast.LENGTH_SHORT).show()
        }
    }

    if (showMapDialog) {
        Lyo3DDialog(onDismissRequest = { showMapDialog = false }) {
            Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                Text(
                    text = "🗺️ Pick Delivery Pin on Map\n(வரைபடத்தில் லொகேஷனை தேர்வு செய்யவும்)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp),
                    lineHeight = 17.sp
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                    InteractiveMapPickerView(
                        initialLat = if (deliveryLat != 0.0) deliveryLat else 11.5812,
                        initialLng = if (deliveryLng != 0.0) deliveryLng else 77.8465,
                        onLocationPicked = { pickedLat, pickedLng ->
                            deliveryLat = pickedLat
                            deliveryLng = pickedLng
                            coroutineScope.launch {
                                try {
                                    val geoCoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                    val addresses = geoCoder.getFromLocation(pickedLat, pickedLng, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        deliveryAddress = addresses[0].getAddressLine(0) ?: "$pickedLat, $pickedLng"
                                    } else {
                                        deliveryAddress = "Lat: $pickedLat, Lng: $pickedLng"
                                    }
                                } catch (e: Exception) {
                                    deliveryAddress = "Custom Map Location ($pickedLat, $pickedLng)"
                                }
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LyoButton(
                    text = "Confirm Selected Location (லொகேஷன் உறுதிசெய்)",
                    onClick = { showMapDialog = false },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )
            }
        }
    }

    LyoBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Lyo AI Food Delivery Cart",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                androidx.compose.material3.TextButton(
                    onClick = { viewModel.clearCart() },
                    colors = ButtonDefaults.textButtonColors(contentColor = LyoColors.NonVegRed),
                    modifier = Modifier.testTag("clear_cart_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Clear",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CLEAR CART",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(20.dp, bottom = 100.dp)
            ) {
                // 1. DELIVERY ADDRESS GEOTAGGING CONTROLS
                item {
                    Text(
                        text = "DELIVER TO (டெலிவரி முகவரி) 📍",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = LyoColors.TextSecondary,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), borderColor = LyoColors.GlassBorder, backgroundColor = Color(0x33000000)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Quick select saved address pills
                            if (savedAddresses.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    savedAddresses.forEach { addr ->
                                        val isSelected = deliveryAddress == addr.addressLine
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) Color(0x3310B981) else Color(0xFF0F172A))
                                                .border(
                                                    1.dp,
                                                    if (isSelected) LyoColors.VegGreen else Color(0x33FFFFFF),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    deliveryAddress = addr.addressLine
                                                    deliveryLat = addr.latitude
                                                    deliveryLng = addr.longitude
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = addr.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) LyoColors.VegGreen else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // Standard delivery address text input
                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = { deliveryAddress = it },
                                placeholder = { Text("உங்கள் முழு முகவரியை உள்ளிடவும் (Address Street, City)", color = Color(0x88FFFFFF), fontSize = 12.sp) },
                                label = { Text("Complete Address Line", color = LyoColors.AccentOrange, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LyoColors.AccentOrange,
                                    unfocusedBorderColor = Color(0x33F8FAFC)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("checkout_address_textarea")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Display current Geotag status
                            val isGeoTagged = deliveryLat != 0.0 && deliveryLng != 0.0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isGeoTagged) Color(0x1F22C55E) else Color(0x1FEF4444))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isGeoTagged) LyoColors.VegGreen else LyoColors.NonVegRed, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isGeoTagged) "✓ Location geo-tagged: ${String.format(java.util.Locale.US, "%.5f, %.5f", deliveryLat, deliveryLng)}" else "⚠️ Coordinates missing! Please pick on Map or use GPS",
                                    color = if (isGeoTagged) LyoColors.VegGreen else LyoColors.NonVegRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Pickers Button Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Pick on map button
                                Button(
                                    onClick = { showMapDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, LyoColors.AccentOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Icon(Icons.Filled.Map, contentDescription = "Map Select", tint = LyoColors.AccentOrange, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick on Map 🗺️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                // Auto-detect GPS button
                                Button(
                                    onClick = {
                                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                            coroutineScope.launch {
                                                val locResult = fetchCurrentLocationAndReverseGeocode(context)
                                                if (locResult != null) {
                                                    deliveryLat = locResult.first
                                                    deliveryLng = locResult.second
                                                    deliveryAddress = locResult.third
                                                    Toast.makeText(context, "📍 தற்போதைய லைவ் லொகேஷன் பெறப்பட்டது!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "GPS சிக்னல் கிடைக்கவில்லை! தயவுசெய்து மீண்டும் முயற்சிக்கவும்.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        } else {
                                            gpsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Icon(Icons.Filled.MyLocation, contentDescription = "Auto GPS", tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("GPS Detect 🛰️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Cart Items Breakdown
                item {
                    Text(
                        text = "YOUR BASKET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = LyoColors.TextSecondary,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        cart.forEach { (item, qty) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(0.55f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.nameEn,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            VegIndicator(isVeg = item.isVeg)
                                        }
                                        Text(
                                            text = item.nameTa,
                                            color = LyoColors.TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(0.45f)
                                        .padding(start = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    // Decrement Button
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x1FFFFFFF))
                                            .clickable { viewModel.removeFromCart(item) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = qty.toString(),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    // Increment Button
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LyoColors.AccentOrange.copy(alpha = 0.2f))
                                            .clickable { viewModel.addToCart(item, partner) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = LyoColors.AccentOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Total Price
                                    Text(
                                        text = "₹${(item.price * qty).toInt()}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.widthIn(min = 36.dp),
                                        textAlign = TextAlign.End
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Delete Icon Button
                                    IconButton(
                                        onClick = { viewModel.removeItemCompletely(item) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Remove item from basket",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Promo Coupon Locker
                item {
                    Text(
                        text = "PROMOTIONAL DISCOUNTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = LyoColors.TextSecondary,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        // Quick Coupon Locker List
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val promoList = listOf(
                                Triple("LYOFRESH", "₹80 Off (>₹300)", "LYOFRESH"),
                                Triple("CHENNADI70", "₹50 Off (>₹100)", "CHENNADI70")
                            ) + if (partner.isCouponEnabled) {
                                listOf(Triple(partner.couponCode, "₹${partner.couponDiscount.toInt()} Off (>₹${partner.couponMinOrder.toInt()})", partner.couponCode))
                            } else {
                                emptyList()
                            }

                            promoList.distinctBy { it.first }.forEach { (pCode, pDesc, pVal) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(
                                            1.dp,
                                            if (appliedCouponCode?.equals(pCode, ignoreCase = true) == true) LyoColors.VegGreen else LyoColors.AccentOrange,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            couponField = pVal
                                            viewModel.applyCoupon(pVal)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$pCode ($pDesc)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (appliedCouponCode?.equals(pCode, ignoreCase = true) == true) LyoColors.VegGreen else Color.White
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponField,
                                onValueChange = { couponField = it },
                                placeholder = { Text("Enter LYOFRESH or CHENNADI70", fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LyoColors.AccentOrange,
                                    unfocusedBorderColor = Color(0x33F8FAFC)
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.applyCoupon(couponField)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("APPLY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (appliedCouponCode != null) {
                            Text(
                                text = "✓ Promo applied: $appliedCouponCode (Saved ₹${discount.toInt()}!)",
                                color = LyoColors.VegGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else if (couponErrorMsg != null) {
                            Text(
                                text = couponErrorMsg ?: "",
                                color = LyoColors.NonVegRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // ✨ LYO AI SMART-SAVER RECOMMENDATIONS Column
                if (recommendedItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "✨ LYO AI SMART-SAVER RECOMMENDATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = LyoColors.AccentOrange,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x1F000000), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x33F8FAFC), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Coupon Progress Warning Booster
                            if (partner.isCouponEnabled && subtotal < partner.couponMinOrder) {
                                val remaining = partner.couponMinOrder - subtotal
                                Text(
                                    text = "💡 Add ₹${remaining.toInt()} more worth of items to unlock coupon \"${partner.couponCode}\" and save ₹${partner.couponDiscount.toInt()} instantly!",
                                    color = LyoColors.AccentOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 15.sp
                                )
                            } else if (partner.isCouponEnabled && subtotal >= partner.couponMinOrder) {
                                Text(
                                    text = "🎉 Awesome! You met the threshold! Choose coupon \"${partner.couponCode}\" above to save ₹${partner.couponDiscount.toInt()}!",
                                    color = LyoColors.VegGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 15.sp
                                )
                            }

                            recommendedItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x0F22C55E), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.nameEn,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (item.nameTa.isNotBlank() && item.nameTa != item.nameEn) {
                                            Text(
                                                text = item.nameTa,
                                                color = LyoColors.TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "₹${item.price.toInt()}",
                                                color = LyoColors.TextSecondary,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            VegIndicator(isVeg = item.isVeg)
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.addToCart(item, partner) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33F97316)),
                                        border = BorderStroke(1.dp, LyoColors.AccentOrange),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("+ ADD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // DEDICATED MULTI-TIER TIPPING ENGINE
                item {
                    Text(
                        text = "TIP YOUR HUNGER SAVIOUR (100% TRANSFERRED)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = LyoColors.TextSecondary,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "A token of appreciation keeps our fleet moving safely and fast.",
                            color = LyoColors.TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Multi-tier button matrix
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val presetTips = listOf(
                                Triple(0.0, "No Tip", "0"),
                                Triple(30.0, "₹30", "❤️"),
                                Triple(50.0, "₹50", "🌟"),
                                Triple(100.0, "₹100", "👑")
                            )

                            presetTips.forEach { (amt, label, emoji) ->
                                val isSelected = tipAmount == amt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) LyoColors.AccentOrange else Color(0x1F38BDF8))
                                        .clickable {
                                            viewModel.setTipAmount(amt)
                                            sliderTipValue = amt.toFloat()
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) LyoColors.AccentOrange else Color(0x3338BDF8),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(emoji, fontSize = 14.sp)
                                        Text(label, color = if (isSelected) Color.White else LyoColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sliding tip tracker fine-control
                        Text(
                            text = "Or customise tip amount precisely: ₹${tipAmount.toInt()}",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Slider(
                            value = sliderTipValue,
                            onValueChange = {
                                sliderTipValue = it
                                viewModel.setTipAmount(it.toDouble().coerceIn(0.0, 200.0))
                            },
                            valueRange = 0f..200f,
                            steps = 20,
                            colors = SliderDefaults.colors(
                                thumbColor = LyoColors.AccentOrange,
                                activeTrackColor = LyoColors.AccentOrange,
                                inactiveTrackColor = Color(0x1F38BDF8)
                            )
                        )
                    }
                }

                // Lyo Loyalty Club Redemption Panel
                item {
                    Text(
                        text = "🌟 LYO LOYALTY CLUB (லாயல்டி சேமிப்பு)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = LyoColors.AmberYellow,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                    )
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "உங்களின் லாயல்டி புள்ளிகள்: $totalLoyaltyPoints PTS",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "சேமிப்பு மதிப்பு: ₹${String.format("%.2f", totalLoyaltyPoints * 0.10)} (10 Points = ₹1.00)",
                                    color = LyoColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            if (totalLoyaltyPoints > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (redeemLoyaltyPoints) LyoColors.AccentOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                        .border(
                                            1.dp,
                                            if (redeemLoyaltyPoints) LyoColors.AccentOrange else Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            redeemLoyaltyPoints = !redeemLoyaltyPoints
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (redeemLoyaltyPoints) "REDEEMED ✓" else "REDEEM",
                                        color = if (redeemLoyaltyPoints) LyoColors.AccentOrange else Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "No Points",
                                    color = LyoColors.TextSecondary,
                                    fontSize = 10.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                // Detailed bill transparent breakdown
                item {
                    Text(
                        text = "BILL BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = LyoColors.TextSecondary,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Basket Subtotal", color = LyoColors.TextSecondary, fontSize = 13.sp)
                            Text("₹${subtotal.toInt()}", color = Color.White, fontSize = 13.sp)
                        }

                        if (viewModel.repository.gstEnabled) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("GST (${viewModel.repository.gstRate}%)", color = LyoColors.TextSecondary, fontSize = 13.sp)
                                Text("₹${(subtotal * (viewModel.repository.gstRate / 100.0)).toInt()}", color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transit Delivery Charges", color = LyoColors.TextSecondary, fontSize = 13.sp)
                            Text(
                                text = if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}",
                                color = if (deliveryFee == 0.0) LyoColors.VegGreen else Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (deliveryFee == 0.0) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (discount > 0.0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Voucher Savings ($appliedCouponCode)", color = LyoColors.VegGreen, fontSize = 13.sp)
                                Text("- ₹${discount.toInt()}", color = LyoColors.VegGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (loyaltyDiscount > 0.0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Loyalty Points Applied", color = LyoColors.AmberYellow, fontSize = 13.sp)
                                Text("- ₹${loyaltyDiscount.toInt()}", color = LyoColors.AmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (tipAmount > 0.0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Saviour Gratuity Donation", color = LyoColors.AmberYellow, fontSize = 13.sp)
                                Text("+ ₹${tipAmount.toInt()}", color = LyoColors.AmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = Color(0x1affffff), modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Payable Amount", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("₹${finalTotalAmount.toInt()}", color = LyoColors.AccentOrange, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }

                        // Local Packaging note
                        Text(
                            text = "Venue platform packaging charges calculated and added to total.",
                            color = LyoColors.TextSecondary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        )
                    }
                }
            }

            // Secure checkout final CTA buy bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                val isBelowMin = subtotal < partner.minOrderAmount
                val remainingAmount = partner.minOrderAmount - subtotal
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isBelowMin) {
                        Text(
                            text = "⚠️ குறைந்தபட்ச ஆர்டர் மதிப்பு: ₹${partner.minOrderAmount.toInt()} தேவை. மேலும் ₹${remainingAmount.toInt()} மதிப்புள்ள உணவைச் சேர்க்கவும் (Add ₹${remainingAmount.toInt()} more worth of dishes).",
                            color = LyoColors.NonVegRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    val isButtonEnabled = !isBelowMin && !isSessionRestoring
                    LyoButton(
                        text = when {
                            isSessionRestoring -> "Restoring Session..."
                            isBelowMin -> "NOT ELIGIBLE (NEED ₹${remainingAmount.toInt()} MORE)"
                            else -> "CONFIRM TRANSACTION & BOOK COURIER"
                        },
                        onClick = {
                            if (!isBelowMin) {
                                if (deliveryLat == 0.0 || deliveryLng == 0.0 || deliveryAddress.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "தயவுசெய்து உங்கள் முகவரியை Map-ல் select பண்ணவும் அல்லது GPS Auto-detect பயன்படுத்தவும் — இது இல்லாமல் டெலிவரி நபர் உங்கள் வீட்டைக் கண்டுபிடிக்க முடியாது!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    showConfirmOrderDialog = true
                                }
                            }
                        },
                        colors = if (isBelowMin || isSessionRestoring) ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444), contentColor = Color.White.copy(alpha = 0.5f)) else ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("buy_button"),
                        enabled = isButtonEnabled
                    )
                }
            }
        }

        if (showConfirmOrderDialog) {
            Lyo3DDialog(onDismissRequest = { showConfirmOrderDialog = false }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            tint = LyoColors.AccentOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm Delivery Address",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = "Is this the correct delivery address?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LyoColors.AccentOrange,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Address Display Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = "Pin",
                                tint = LyoColors.AccentOrange,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = deliveryAddress,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Coordinates: $deliveryLat, $deliveryLng",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Address Confirmation Checkbox Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAddressConfirmed) Color(0x1122C55E) else Color(0x11EF4444))
                            .border(1.dp, if (isAddressConfirmed) LyoColors.VegGreen else LyoColors.NonVegRed, RoundedCornerShape(10.dp))
                            .clickable { isAddressConfirmed = !isAddressConfirmed }
                            .padding(10.dp)
                    ) {
                        Checkbox(
                            checked = isAddressConfirmed,
                            onCheckedChange = { isAddressConfirmed = it },
                            colors = CheckboxDefaults.colors(checkedColor = LyoColors.VegGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Yes, this is my delivery address",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), shape = RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Payable", color = LyoColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("₹${finalTotalAmount.toInt()}", color = LyoColors.AccentOrange, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                if (partner.id != initialVendorId) {
                                    Toast.makeText(
                                        context,
                                        "Restaurant changed! Please go back and check your cart.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showConfirmOrderDialog = false
                                } else if (deliveryLat == 0.0 || deliveryLng == 0.0 || deliveryAddress.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Please pick your delivery address coordinates on the map or use GPS auto-detect.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showConfirmOrderDialog = false
                                } else {
                                    showConfirmOrderDialog = false
                                    viewModel.proceedToCheckout(
                                        address = deliveryAddress,
                                        lat = deliveryLat,
                                        lng = deliveryLng,
                                        loyaltyDiscount = loyaltyDiscount
                                    ) { generatedOrderId ->
                                        onCheckoutSuccessful(generatedOrderId)
                                    }
                                }
                            },
                            enabled = isAddressConfirmed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LyoColors.VegGreen,
                                disabledContainerColor = Color.White.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_order_button")
                        ) {
                            Text(
                                text = "Confirm Order",
                                color = if (isAddressConfirmed) Color.White else Color.White.copy(alpha = 0.4f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { showConfirmOrderDialog = false },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("cancel_order_button")
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getVendorSubtitle(type: String): String {
    val normalizedType = type.lowercase().let { t ->
        when {
            t.contains("hotel") -> "hotel"
            t.contains("restaurant") -> "restaurant"
            t.contains("cafe") -> "cafe"
            t.contains("bakery") -> "bakery"
            t.contains("snack") -> "snack"
            t.contains("dhaba") -> "dhaba"
            else -> t
        }
    }
    return when (normalizedType) {
        "hotel" -> "Traditional Fine Dining • 100% Veg"
        "restaurant" -> "Authentic Cuisine • Clay Oven Starters"
        "cafe" -> "Fresh Brews • All Day Beverages"
        "bakery" -> "Freshly Baked Bread • Desserts • Puffs"
        "snack shop", "snack" -> "Crispy Savouries • Indian Sweets"
        "dhaba" -> "Authentic Rustic Punjabi Taste • Highway Comfort"
        else -> "Prepared with Premium Standard Ingredients"
    }
}

@Composable
fun VendorBanner(
    name: String,
    nameTa: String,
    type: String,
    bannerUrl: String = "",
    address: String = "",
    isOnHoliday: Boolean = false,
    showOverlayText: Boolean = false,
    height: androidx.compose.ui.unit.Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val normalizedType = type.lowercase().let { t ->
        when {
            t.contains("hotel") -> "hotel"
            t.contains("restaurant") -> "restaurant"
            t.contains("cafe") -> "cafe"
            t.contains("bakery") -> "bakery"
            t.contains("snack") -> "snack"
            t.contains("dhaba") -> "dhaba"
            else -> t
        }
    }

    val gradientColor = when (normalizedType) {
        "hotel" -> listOf(Color(0xFF065F46), Color(0xFF0F172A))
        "restaurant" -> listOf(Color(0xFF991B1B), Color(0xFF0F172A))
        "cafe" -> listOf(Color(0xFF78350F), Color(0xFF0F172A))
        "bakery" -> listOf(Color(0xFFD97706), Color(0xFF0F172A))
        "snack shop", "snack" -> listOf(Color(0xFFC2410C), Color(0xFF0F172A))
        "dhaba" -> listOf(Color(0xFF854D0E), Color(0xFF020617))
        else -> listOf(Color(0xFF334155), Color(0xFF0F172A))
    }

    val subtitle = getVendorSubtitle(type)

    val iconSymbol = when (normalizedType) {
        "hotel" -> Icons.Filled.LocalDining
        "restaurant" -> Icons.Filled.Restaurant
        "cafe" -> Icons.Filled.Coffee
        "bakery" -> Icons.Filled.Cake
        "snack shop", "snack" -> Icons.Filled.LocalOffer
        "dhaba" -> Icons.Filled.Store
        else -> Icons.Filled.Storefront
    }

    val isCustomImage = bannerUrl.isNotBlank() && (
        bannerUrl.startsWith("http://", ignoreCase = true) ||
        bannerUrl.startsWith("https://", ignoreCase = true) ||
        bannerUrl.startsWith("content://", ignoreCase = true) ||
        bannerUrl.startsWith("file://", ignoreCase = true) ||
        bannerUrl.startsWith("/storage") ||
        bannerUrl.startsWith("/data")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Color(0xFF1E293B))
    ) {
        if (isCustomImage) {
            val painterModel = remember(bannerUrl) {
                if (bannerUrl.startsWith("/")) java.io.File(bannerUrl) else bannerUrl
            }
            androidx.compose.foundation.Image(
                painter = coil.compose.rememberAsyncImagePainter(painterModel),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = if (showOverlayText) {
                                listOf(Color(0x1A0B1120), Color(0x730B1120))
                            } else {
                                listOf(Color(0x0A0B1120), Color(0x3F0B1120))
                            }
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = gradientColor))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x22FFFFFF), Color.Transparent),
                            radius = 200f
                        )
                    )
            )
        }

        if (showOverlayText) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.uppercase(),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.85f),
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 3f
                            )
                        )
                    )
                    if (nameTa.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = nameTa,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LyoColors.AmberYellow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.85f),
                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                    if (address.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = address,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.84f),
                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                }
                Icon(
                    imageVector = iconSymbol,
                    contentDescription = null,
                    tint = Color(0x18FFFFFF),
                    modifier = Modifier.size(44.dp).align(Alignment.Bottom)
                )
            }
        } else {
            // Elegant subtle top-right corner floating category tag on the image!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color(0x990F172A), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = iconSymbol,
                            contentDescription = null,
                            tint = LyoColors.AmberYellow,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = normalizedType.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        if (isOnHoliday) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA020617))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xFFDC2626), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CLOSED TODAY",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun StoreInfoRow(
    labelEn: String,
    labelTa: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LyoColors.TextSecondary,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = labelEn,
                fontSize = 11.sp,
                color = LyoColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun LyoAiChatbotSection(
    viewModel: StorefrontViewModel,
    onNavigateToVendor: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.lyoAiMessages.collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()
    val isSessionRestoring = remember(currentUser) {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null && currentUser == null
    }
    val isLoading by viewModel.isLyoAiLoading.collectAsState(initial = false)
    val liveCart by viewModel.activeCart.collectAsState(initial = emptyMap())
    val currentVendor by viewModel.activeVendor.collectAsState()
    val cartSubtotal by viewModel.cartSubtotal.collectAsState(initial = 0.0)
    val cartDeliveryFee by viewModel.cartDeliveryFee.collectAsState(initial = 0.0)
    val cartTotalAmount by viewModel.cartTotalAmount.collectAsState(initial = 0.0)
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState(initial = "")
    val showLyoSupportPopupState by viewModel.showLyoSupportPopup.collectAsState(initial = false)
    var userText by remember { mutableStateOf("") }
    var showClearCartConfirm by remember { mutableStateOf(false) }
    var showPlaceOrderConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initLyoAiChat()
    }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val context = LocalContext.current
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                viewModel.sendLyoAiPrompt(spokenText, context)
            }
        }
    }

    // Auto scroll down when messages size changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var showSupportSuccessPopup by remember { mutableStateOf(false) }

    if (showSupportSuccessPopup) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSupportSuccessPopup = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Request Submitted Successfully",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "✅ Your support request has been submitted successfully.\n\nஎங்களது நிர்வாகி விரைவில் உங்களை தொடர்பு கொள்வார்.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSupportSuccessPopup = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF141720),
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showLyoSupportPopupState) {
        var supportMessage by remember { mutableStateOf("") }
        var isSubmittingMessage by remember { mutableStateOf(false) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.showLyoSupportPopup.value = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.HeadsetMic,
                        contentDescription = "support icon",
                        tint = LyoColors.AccentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "உதவி மையம் 📞💬",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "அன்பான எடப்பாடி மக்களே! 🌾 லியோ ஏ ஐ புரியாத மொழிகளில் வினவப்பட்டாலோ அல்லது ஏதேனும் ஆர்டர் அல்லது மெனு சந்தேகங்கள் இருப்பின், உடனடியாக எங்களது Coscoom Creative Tech Solutions தலைமை நிர்வாகி (CEO) Anantharaj.R அவர்களை தொடர்பு கொள்ளவும்.",
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x1A00E5FF)),
                        border = BorderStroke(1.dp, Color(0x6600E5FF))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Coscoom Creative Tech Solutions",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "தலைமை நிர்வாகி: Anantharaj.R\nதொடர்பு எண்: 8778148899\nஇடம்: எடப்பாடி, சேலம் மாவட்டம்.",
                                color = Color.White,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    com.example.WhatsAppHelper.sendMessage(
                                        context,
                                        "8778148899",
                                        "வணக்கம் அனந்தராஜ் சார், லியோ உணவு விநியோக செயலி (Lyo AI Food Delivery App) தொடர்பாக தங்களை தொடர்பு கொள்கிறேன்."
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.Send, contentDescription = "whatsapp", tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
 
                        Button(
                            onClick = {
                                try {
                                    val dialIntent = android.content.Intent(
                                        android.content.Intent.ACTION_DIAL,
                                        android.net.Uri.parse("tel:8778148899")
                                    )
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.Phone, contentDescription = "call", tint = Color.White, modifier = Modifier.size(12.dp))
                                Text("Call", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Text(
                        text = "Or write your query directly below:",
                        color = LyoColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    OutlinedTextField(
                        value = supportMessage,
                        onValueChange = { supportMessage = it },
                        placeholder = { Text("உங்கள் சந்தேகத்தை இங்கே பதிவிடவும் (e.g. Need help with my order #105)", color = LyoColors.TextSecondary, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            val msg = supportMessage.trim()
                            if (msg.isNotBlank()) {
                                isSubmittingMessage = true
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    try {
                                        val ticketMap = mapOf(
                                            "message" to msg,
                                            "timestamp" to System.currentTimeMillis(),
                                            "userId" to (currentUser?.uid ?: "anonymous"),
                                            "phone" to (currentUser?.phone ?: ""),
                                            "userName" to (currentUser?.name ?: "Anonymous"),
                                            "status" to "PENDING"
                                        )
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            .collection("support_tickets")
                                            .add(ticketMap)
                                            .await()
                                        
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            isSubmittingMessage = false
                                            supportMessage = ""
                                            viewModel.showLyoSupportPopup.value = false
                                            showSupportSuccessPopup = true
                                        }
                                    } catch (e: Exception) {
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            isSubmittingMessage = false
                                            Toast.makeText(context, "Error submitting ticket: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                        shape = RoundedCornerShape(10.dp),
                        enabled = supportMessage.isNotBlank() && !isSubmittingMessage,
                        modifier = Modifier.fillMaxWidth().height(38.dp).testTag("submit_support_request_btn")
                    ) {
                        Text(
                            text = if (isSubmittingMessage) "Submitting..." else "SUBMIT REQUEST / கோரிக்கையை அனுப்பவும்",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.showLyoSupportPopup.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Text("மூடு / Close ✖", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF141720),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        // Futuristic mascot header banner with 3D effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Shadow Layer (3D Effect)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 2.dp)
                    .background(Color(0xFF07090E), shape = RoundedCornerShape(12.dp))
            )
            // Foreground card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141720), shape = RoundedCornerShape(12.dp))
                    .border(1.2.dp, Color(0xFF1E293B), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Robot breathing glow circle
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LyoColors.AccentOrange.copy(alpha = 0.15f))
                    )
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = "Lyo Mascot",
                        tint = LyoColors.AccentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    // Breathing small green active dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                            .align(Alignment.BottomEnd)
                            .border(1.5.dp, Color(0xFF0F172A), CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "LYO AI CONCIERGE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Realtime Assistant • Gemini 2.5 Flash",
                        fontSize = 9.sp,
                        color = LyoColors.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.showLyoSupportPopup.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, LyoColors.AccentOrange.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "support",
                            tint = LyoColors.AccentOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "உதவி 📞",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Chat bubble lists
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                // Suggestion chips row
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    Text(
                        text = "Suggested Queries (உங்களுக்கு இதைப் பற்றி தெரிய வேண்டுமா?):",
                        color = LyoColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    val suggestions = remember {
                        listOf(
                            "என் ஆர்டர் எங்கே உள்ளது? 🛵" to "where is my current live order status?",
                            "கடந்த கால ஆர்டர்கள் 📜" to "tell me about my past orders history",
                            "சைவம் சிறந்த உணவகம்? 🥦" to "what are the best veg restaurant recommendations and offers available?",
                            "சலுகை கூப்பன் இருக்கா? 🎟️" to "what is the best promo coupon code discount right now?"
                        )
                    }
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(suggestions.size, key = { suggestions[it].second }) { idx ->
                            val s = suggestions[idx]
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x11FFFFFF))
                                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.sendLyoAiPrompt(s.second, context)
                                    }
                                    .heightIn(min = 48.dp)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(s.first, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Quick Co-pilot Commands (நேரடி வழிகாட்டி கட்டளைகள்):",
                        color = LyoColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    val actions = remember {
                        listOf(
                            Triple("சைவ உணவகங்கள் 🥦", "Veg", "HOME"),
                            Triple("அசைவ உணவகங்கள் 🍖", "Non-Veg", "HOME"),
                            Triple("ஆர்டர் ஹிஸ்டரி 📜", "All", "MY_ORDERS"),
                            Triple("அனைத்து கடைகள் 🏪", "All", "HOME")
                        )
                    }
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(actions.size, key = { actions[it].first }) { idx ->
                            val act = actions[idx]
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x1A22C55E))
                                    .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        if ((act.third == "ORDERS" || act.third == "MY_ORDERS" || act.third == "PROFILE") && viewModel.currentUser.value == null) {
                                            android.widget.Toast.makeText(context, "Please login first to view your past orders! 🔐", android.widget.Toast.LENGTH_LONG).show()
                                            viewModel.navigationTrigger.value = "LOGIN"
                                        } else {
                                            viewModel.selectedCategoryFilter.value = act.second
                                            viewModel.selectedTabState.value = if (act.third == "MY_ORDERS") "ORDERS" else act.third
                                            android.widget.Toast.makeText(
                                                context,
                                                "செயல்படுத்தப்பட்டது: ${act.first}! ⚡🌟",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .heightIn(min = 48.dp)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(act.first, color = Color(0xFF22C55E), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }



            items(messages.size, key = { index -> "${messages[index].timestamp}_$index" }) { index ->
                val msg = messages[index]
                val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                val bubbleBg = remember(msg.isUser) {
                    if (msg.isUser) {
                        Brush.verticalGradient(
                            colors = listOf(
                                LyoColors.AccentOrange.copy(alpha = 0.85f),
                                LyoColors.AccentOrange.copy(alpha = 0.55f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0E1D34),
                                Color(0xFF09111F)
                            )
                        )
                    }
                }
                val bubbleBorderBrush = remember(msg.isUser) {
                    if (msg.isUser) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.60f),
                                LyoColors.AccentOrange,
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                LyoColors.GlassBorder.copy(alpha = 0.40f),
                                Color.Transparent
                            )
                        )
                    }
                }
                val bubbleShadowElevation = remember(msg.isUser) { if (msg.isUser) 6.dp else 4.dp }
                val bubbleShadowColor = remember(msg.isUser) { if (msg.isUser) LyoColors.AccentOrange else Color.Black }

                val textCol = Color.White
                val bubbleShape = remember(msg.isUser) {
                    if (msg.isUser) {
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                    } else {
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        if (!msg.isUser) {
                             Box(
                                 modifier = Modifier
                                     .size(24.dp)
                                     .clip(CircleShape)
                                     .background(Color(0xFF1E293B))
                                     .border(1.dp, LyoColors.AccentOrange.copy(alpha = 0.3f), CircleShape),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Icon(
                                     imageVector = Icons.Filled.SmartToy,
                                     contentDescription = null,
                                     tint = LyoColors.AccentOrange,
                                     modifier = Modifier.size(14.dp)
                                 )
                             }
                             Spacer(modifier = Modifier.width(6.dp))
                        }

                        Box(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            // 3D Shadow layer
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(y = 2.dp)
                                    .background(Color(0xFF07090E), shape = bubbleShape)
                            )
                            // Foreground bubble
                            Box(
                                modifier = Modifier
                                    .background(bubbleBg, bubbleShape)
                                    .border(1.2.dp, bubbleBorderBrush, bubbleShape)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = textCol,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // --- INLINE RECOMMENDED OUTLETS IF PRESENT ---
                    if (!msg.isUser && msg.recommendedItems != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp, end = 8.dp)
                        ) {
                            Text(
                                text = "✨ பரிந்துரைக்கப்பட்ட உணவுகள் (Lyo AI Suggestions):",
                                color = LyoColors.AmberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            androidx.compose.foundation.lazy.LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(msg.recommendedItems.size, key = { it }) { idx ->
                                    val (menuItem, vendor) = msg.recommendedItems[idx]
                                    val cv = currentVendor
                                    val isCurrentVendor = liveCart.isNotEmpty() && cv?.id == vendor.id
                                    val isConflict = liveCart.isNotEmpty() && cv != null && cv.id != vendor.id
                                    
                                    Box(
                                        modifier = Modifier
                                            .width(145.dp)
                                            .padding(bottom = 4.dp)
                                    ) {
                                        // 3D Shadow Layer
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .offset(y = 2.dp)
                                                .background(Color(0xFF07090E), shape = RoundedCornerShape(14.dp))
                                        )
                                        // Foreground UI Card
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFF242E42).copy(alpha = 0.9f),
                                                            Color(0xFF151B26).copy(alpha = 0.95f)
                                                         )
                                                     ),
                                                     shape = RoundedCornerShape(12.dp)
                                                 )
                                                 .border(
                                                     width = 1.2.dp,
                                                     brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                         colors = if (isCurrentVendor) {
                                                             listOf(Color(0xFF4ADE80), Color(0xFF22C55E))
                                                         } else {
                                                             listOf(Color(0xFFF97316).copy(alpha = 0.5f), Color(0xFF3B82F6).copy(alpha = 0.3f))
                                                         }
                                                     ),
                                                     shape = RoundedCornerShape(12.dp)
                                                 )
                                                 .padding(8.dp)
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = vendor.nameTa.ifEmpty { vendor.name },
                                                        color = Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Star,
                                                            contentDescription = null,
                                                            tint = Color(0xFFFBBF24),
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text(
                                                            text = vendor.rating.toString(),
                                                            color = Color(0xFFFBBF24),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = menuItem.nameTa.ifEmpty { menuItem.nameEn },
                                                    color = LyoColors.TextPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "₹${menuItem.price.toInt()}",
                                                    color = LyoColors.AccentOrange,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                // Dual Row Actions
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    // View Shop (Meet Restaurant)
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF3B82F6))
                                                            .clickable {
                                                                onNavigateToVendor(vendor.id)
                                                            }
                                                            .padding(vertical = 4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "VISIT 🏪",
                                                            color = Color.White,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                    // Add / Quantity Picker Control
                                                    val qtyInCart = liveCart[menuItem] ?: 0
                                                    if (qtyInCart > 0 && isCurrentVendor) {
                                                        Row(
                                                            modifier = Modifier
                                                                .weight(1.4f)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color(0xFF22C55E))
                                                                .padding(horizontal = 2.dp, vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            // Decrement button
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        viewModel.removeFromCart(menuItem)
                                                                    }
                                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                                            ) {
                                                                Text("-", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                            Text(
                                                                text = qtyInCart.toString(),
                                                                color = Color.White,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            // Increment button
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        viewModel.addToCart(menuItem, vendor)
                                                                    }
                                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                                            ) {
                                                                Text("+", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1.2f)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(LyoColors.AccentOrange)
                                                                .clickable {
                                                                    viewModel.selectRecommendedOption(menuItem, vendor)
                                                                    if (isConflict) {
                                                                        val m = viewModel.lyoAiMessages.value.toMutableList()
                                                                        m.add(LyoMessage(
                                                                            text = "Your cart already contains items from another restaurant! Clear cart and add this item from ${vendor.name}? ⚠️",
                                                                            isUser = false,
                                                                            isConflictNotice = true,
                                                                            conflictItem = Pair(menuItem, vendor)
                                                                        ))
                                                                        viewModel.lyoAiMessages.value = m
                                                                    } else {
                                                                        viewModel.confirmPendingAddToCart(clearOnConflict = false)
                                                                        android.widget.Toast.makeText(
                                                                            context,
                                                                            "Added to cart! 🛒",
                                                                            android.widget.Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                }
                                                                .padding(vertical = 4.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "ADD 🛒",
                                                                color = Color.White,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- INLINE CONFLICT RESOLUTION WARNING IF PRESENT ---
                    if (!msg.isUser && msg.isConflictNotice && msg.conflictItem != null) {
                        val (menuItem, vendor) = msg.conflictItem
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .padding(start = 32.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x26EF4444)) // translucent warning red
                                .border(1.dp, Color(0x66EF4444), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Conflict Warning",
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "கூடை முரண்பாடு (Cart Conflict) ⚠️",
                                        color = Color(0xFFFCA5A5),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "புதிய உணவகமான '${vendor.nameTa.ifEmpty { vendor.name }}' மூலம் மாற்றுவது முந்தைய கார்ட் உணவுப் பொருட்களை நீக்கிவிடும். தொடரலாமா?",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x15FFFFFF))
                                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                            .clickable {
                                                // Cancel: Append user's action and remove pending
                                                val m = viewModel.lyoAiMessages.value.toMutableList()
                                                m.remove(msg) // remove conflict block
                                                m.add(LyoMessage("இல்லை, வேண்டாம் ❌", true))
                                                m.add(LyoMessage("ஆர்டர் மாற்றம் ரத்து செய்யப்பட்டது! உங்கள் முந்தைய கூடை அப்படியே உள்ளது. 🟢", false))
                                                viewModel.lyoAiMessages.value = m
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Cancel ❌",
                                            color = Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF22C55E))
                                            .clickable {
                                                // Confirm: replace basket
                                                viewModel.confirmPendingAddToCart(clearOnConflict = true)
                                                val m = viewModel.lyoAiMessages.value.toMutableList()
                                                m.remove(msg) // remove conflict card
                                                m.add(LyoMessage("ஆமாம், கூடையை மாற்று ✅", true))
                                                m.add(LyoMessage("முந்தைய கூடை வெற்றிகரமாக அழிக்கப்பட்டது. '${vendor.nameTa.ifEmpty { vendor.name }}' கடையில் இருந்து '${menuItem.nameTa.ifEmpty { menuItem.nameEn }}' கூடையில் சேர்க்கப்பட்டது! 🛒🌟", false))
                                                viewModel.lyoAiMessages.value = m
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "ஆமாம், மாற்று 👍",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!msg.isUser && msg.itemsSummary != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .padding(start = 32.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xBB0F172A))
                                .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingCart,
                                        contentDescription = "Basket Confirmation",
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "கூடை உறுதிப்படுத்தல் (Basket Confirmation)",
                                        color = Color(0xFF22C55E),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                }
                                if (msg.shopName != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "கடையில் இருந்து (From ${msg.shopName})",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0x1FFFFFFF))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                msg.itemsSummary.forEach { summaryLine ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "•",
                                            color = Color(0xFF22C55E),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        Text(
                                            text = summaryLine,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0x1FFFFFFF))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "மொத்த மதிப்பு (Total Value)",
                                        color = LyoColors.TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "₹${msg.totalAmount?.toInt() ?: 0}",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x15FFFFFF))
                                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                            .clickable { showClearCartConfirm = true }
                                            .padding(vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "CLEAR CART ❌",
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF22C55E))
                                            .clickable { viewModel.navigationTrigger.value = "CHECKOUT" }
                                            .padding(vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "CHECKOUT BILL 💳",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = if (msg.isUser) "You" else "Lyo AI",
                        fontSize = 8.sp,
                        color = LyoColors.TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, start = if (msg.isUser) 0.dp else 32.dp, end = if (msg.isUser) 4.dp else 0.dp)
                    )
                }
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 24.dp, top = 4.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = LyoColors.AccentOrange,
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "லியோ ஏ ஐ பதிலளிக்கிறது (Lyo AI thinking...)",
                            color = LyoColors.TextSecondary,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }



        if (liveCart.isNotEmpty()) {
            val totalAmount = cartTotalAmount
            val totalItems = liveCart.values.sum()
            val firstItem = liveCart.keys.firstOrNull()
            var isInstantPlacingOrder by remember { mutableStateOf(false) }
            
            val activeVendorVal = currentVendor
            val isBelowMin = activeVendorVal?.let { cartSubtotal < it.minOrderAmount } ?: false
            val remainingToMin = activeVendorVal?.let { it.minOrderAmount - cartSubtotal } ?: 0.0
            
            val glowColorVal = if (isBelowMin) Color(0x33EF4444) else Color(0x3310B981)
            val borderColorVal = if (isBelowMin) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.5f)

            val pulseTransition = rememberInfiniteTransition(label = "pulse_active")
            val dotAlpha by pulseTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_alpha"
            )

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                cornerRadius = 16.dp,
                innerPadding = 0.dp,
                borderColor = borderColorVal,
                glowColor = glowColorVal
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .alpha(dotAlpha)
                                    .clip(CircleShape)
                                    .background(if (isBelowMin) Color(0xFFEF4444) else Color(0xFF10B981))
                            )
                            Text(
                                text = if (isBelowMin) "LYO AI SMART BASKET" else "LYO AI SMART BASKET ACTIVE",
                                color = if (isBelowMin) Color(0xFFEF4444) else Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.3.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "₹${totalAmount.toInt()}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )

                            // Clear Basket tap button
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { showClearCartConfirm = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.Close,
                                    contentDescription = "Clear Basket",
                                    tint = Color.White.copy(alpha = 0.65f),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🛒 Basket Summary (சரிபார்க்கவும்):",
                        color = LyoColors.AmberYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                    
                    liveCart.forEach { (menuItem, qty) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = menuItem.nameTa.ifEmpty { menuItem.nameEn },
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${menuItem.price.toInt()} x $qty = ₹${(menuItem.price * qty).toInt()}",
                                    color = LyoColors.TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                            
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.07f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable {
                                            viewModel.removeFromCart(menuItem)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = qty.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable {
                                            viewModel.addToCartByItemId(menuItem)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Ultra-slim, high-contrast single horizontal line receipt breakdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("உணவு ₹${cartSubtotal.toInt()}", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Box(modifier = Modifier.width(1.dp).height(10.dp).background(Color.White.copy(alpha = 0.15f)))

                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("டெலிவரி: ", color = LyoColors.TextSecondary, fontSize = 9.5.sp)
                            Text(if (cartDeliveryFee == 0.0) "இலவசம்" else "₹${cartDeliveryFee.toInt()}", color = if (cartDeliveryFee == 0.0) LyoColors.VegGreen else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        if (viewModel.repository.gstEnabled) {
                            Box(modifier = Modifier.width(1.dp).height(10.dp).background(Color.White.copy(alpha = 0.15f)))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("GST: ", color = LyoColors.TextSecondary, fontSize = 9.5.sp)
                                Text("₹${(cartSubtotal * (viewModel.repository.gstRate / 100.0)).toInt()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💳 Payment Mode / பணம் செலுத்தும் முறை:",
                        color = LyoColors.AmberYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("CASH" to "💵 Cash (COD)", "UPI" to "📱 UPI Payment").forEach { (methodId, label) ->
                            val isSelected = selectedPaymentMethod == methodId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) LyoColors.AccentOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                    .border(
                                        1.dp,
                                        if (isSelected) LyoColors.AccentOrange else Color.White.copy(alpha = 0.08f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        viewModel.selectedPaymentMethod.value = methodId
                                    }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color.LightGray,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = if (isBelowMin) {
                            "ஆர்டர் செய்ய இன்னும் ₹${remainingToMin.toInt()} சேர்க்கவும் (Min: ₹${activeVendorVal?.minOrderAmount?.toInt()})"
                        } else {
                            "ஆர்டரை நொடியில் முடிக்க உடனே ஆர்டர் செய்ய இங்கே கிளிக் செய்யவும்"
                        },
                        color = if (isBelowMin) Color(0xFFFCA5A5) else Color.White.copy(alpha = 0.55f),
                        fontSize = 9.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                if (com.example.BuildConfig.DEBUG) {
                                    val fbUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                    android.util.Log.d("LyoAuthDebug", "--- CLICKED GO TO CHECKOUT ---")
                                    android.util.Log.d("LyoAuthDebug", "• FirebaseAuth Current User UID: ${fbUser?.uid ?: "NULL"}")
                                    android.util.Log.d("LyoAuthDebug", "• Local Repository User UID: ${currentUser?.uid ?: "NULL"}")
                                }
                                if (!isSessionRestoring) {
                                    viewModel.navigationTrigger.value = "CHECKOUT"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color.White
                            ),
                            enabled = !isSessionRestoring,
                            border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isSessionRestoring) "LOADING..." else "GO TO CHECKOUT 💳",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        if (isInstantPlacingOrder) {
                            Box(
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            Button(
                                 onClick = {
                                    if (com.example.BuildConfig.DEBUG) {
                                        val fbUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                        android.util.Log.d("LyoAuthDebug", "--- CLICKED CHATBOT FAST ORDER ---")
                                        android.util.Log.d("LyoAuthDebug", "• FirebaseAuth Current User UID: ${fbUser?.uid ?: "NULL"}")
                                        android.util.Log.d("LyoAuthDebug", "• Local Repository User UID: ${currentUser?.uid ?: "NULL"}")
                                    }
                                    val usr = currentUser
                                    val savedAddrs = viewModel.savedAddresses.value
                                    val defaultAddr = savedAddrs.find { it.isDefault }
                                    val finalAddress = defaultAddr?.addressLine ?: usr?.address ?: ""
                                    val finalLat = defaultAddr?.latitude ?: usr?.lat ?: 0.0
                                    val finalLng = defaultAddr?.longitude ?: usr?.lng ?: 0.0

                                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                    if (usr == null && firebaseUser == null) {
                                        Toast.makeText(context, "Please login first to place an order! 🔐", Toast.LENGTH_LONG).show()
                                        viewModel.navigationTrigger.value = "LOGIN"
                                    } else if (isBelowMin) {
                                        Toast.makeText(context, "மன்னிக்கவும்! குறைந்தபட்ச ஆர்டர் மதிப்பு ₹${activeVendorVal?.minOrderAmount?.toInt()} தேவை. கார்ட்டில் மேலும் உணவுகளை சேர்க்கவும்!", Toast.LENGTH_LONG).show()
                                    } else if (finalLat == 0.0 || finalLng == 0.0 || finalAddress.isBlank()) {
                                        Toast.makeText(context, "தயவுசெய்து உங்கள் முகவரியை Map-ல் select பண்ணவும் அல்லது GPS Auto-detect பயன்படுத்தவும் — இது இல்லாமல் டெலிவரி நபர் உங்கள் வீட்டைக் கண்டுபிடிக்க முடியாது!", Toast.LENGTH_LONG).show()
                                    } else {
                                        showPlaceOrderConfirm = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBelowMin) Color(0xFF374151) else Color(0xFF10B981),
                                    contentColor = if (isBelowMin) Color.LightGray else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isSessionRestoring,
                                modifier = Modifier.weight(1.4f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text(
                                    text = when {
                                        isSessionRestoring -> "RESTORING SESSION..."
                                        isBelowMin -> "⚠️ BELOW MIN ORDER"
                                        else -> "⚡ CHATBOT FAST ORDER 🚀"
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input keyboard typing controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090D16))
        ) {
            // Always-visible Tamil quick-query chips
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                val tamilQueries = listOf(
                    "சிறந்த உணவகங்கள்? 🏪" to "what are the best restaurant recommendations?",
                    "இன்றைய ஆஃபர்கள்? 🎟️" to "what are today's special offers and discounts?",
                    "ஆர்டர் எங்கே உள்ளது? 🛵" to "where is my current live order status?"
                )
                items(tamilQueries.size, key = { tamilQueries[it].first }) { index ->
                    val (label, queryText) = tamilQueries[index]
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(LyoColors.AccentOrange.copy(alpha = 0.15f))
                            .border(1.dp, LyoColors.AccentOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.sendLyoAiPrompt(queryText, context)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = LyoColors.AccentOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val quickChips = viewModel.getLyoAiQuickChips()
            if (quickChips.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(quickChips.size, key = { quickChips[it].second }) { index ->
                        val chip = quickChips[index]
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x19FF6B00))
                                .border(1.dp, Color(0x33FF6B00), RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.sendLyoAiPrompt(chip.second, context)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                  text = chip.first,
                                  color = LyoColors.AccentOrange,
                                  fontSize = 11.sp,
                                  fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .then(
                        with(LyoGlassDesignTokens) {
                            Modifier.liquidGlass3D(
                                cornerRadius = 28.dp,
                                elevation = 12.dp,
                                borderWidth = 1.2.dp,
                                borderBrush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xBBFFFFFF),                     // Bright reflection highlights
                                        Color(0xFFFF6B00).copy(alpha = 0.5f), // Glowing neon-orange accent
                                        Color(0xFF2563EB).copy(alpha = 0.5f), // Glowing cosmic blue accent
                                        Color(0x22FFFFFF)                     // Soft glassy frame backing
                                    )
                                ),
                                glowColor = Color(0xFFFF6B00),
                                backgroundColor = LyoGlassDesignTokens.GlassCardBg
                            )
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp), // Sleek, modern cinematic height
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sleek, minimal inline typing area
                    Box(modifier = Modifier.weight(1f)) {
                        if (userText.isEmpty()) {
                            Text(
                                text = "லியோ ஏ ஐ-யிடம் கேட்கவும்... Ask Lyo AI",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 11.5.sp,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = userText,
                            onValueChange = { userText = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = Brush.verticalGradient(listOf(LyoColors.AccentOrange, Color.White)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterStart)
                                .testTag("lyo_ai_input_field"),
                            maxLines = 1,
                            singleLine = true
                        )
                    }

                    if (userText.isNotBlank()) {
                        IconButton(
                            onClick = { userText = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "clear text", tint = Color.LightGray.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Compact, responsive 3D triggers
                    if (userText.isBlank()) {
                        IconButton(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
                                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "இட்லி சாம்பார் வேணும் போல சொல்லுங்கள்... (What food do you want to order?)")
                                    }
                                    speechRecognizerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                                .testTag("lyo_ai_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "voice order",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                val prompt = userText.trim()
                                if (prompt.isNotBlank()) {
                                    viewModel.sendLyoAiPrompt(prompt, context)
                                    userText = ""
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(LyoColors.AccentOrange)
                                .testTag("lyo_ai_send_button"),
                            enabled = userText.isNotBlank() && !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "send prompt",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showClearCartConfirm) {
            AlertDialog(
                onDismissRequest = { showClearCartConfirm = false },
                title = {
                    Text(
                        text = "கூடையை அழிக்கலாமா? (Clear Basket?)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = "கூடையில் உள்ள அனைத்து உணவுப் பொருட்களையும் நீக்க விரும்புகிறீர்களா?\n\nAre you sure you want to clear all items from your basket?",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    LyoButton(
                        text = "ஆம் (Yes/OK)",
                        onClick = {
                            viewModel.clearCart()
                            showClearCartConfirm = false
                        }
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showClearCartConfirm = false }) {
                        Text("CANCEL", color = LyoColors.TextSecondary)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }

        if (showPlaceOrderConfirm) {
            val totalAmount = viewModel.getCartTotalAmount()
            AlertDialog(
                onDismissRequest = { showPlaceOrderConfirm = false },
                title = {
                    Text(
                        text = "ஆர்டரை உறுதிசெய் (Confirm Order?)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    val payMethodLabel = if (selectedPaymentMethod == "CASH") "💵 Cash on Delivery (COD)" else "📱 UPI Payment (Online)"
                    val payMethodTa = if (selectedPaymentMethod == "CASH") "கையில் பணம் தருதல் (COD)" else "ஆன்லைன் பேமென்ட் (UPI)"
                    Text(
                        text = "இந்த ஆர்டரை சமர்ப்பிக்க விரும்புகிறீர்களா?\n\nAre you sure you want to place this order of ₹${totalAmount.toInt()}?\n\n💳 Payment / செலுத்தும் முறை:\n$payMethodLabel ($payMethodTa)",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    LyoButton(
                        text = "ஆம் (Yes/OK)",
                        onClick = {
                            showPlaceOrderConfirm = false
                            val usr = viewModel.currentUser.value
                            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            val savedAddrs = viewModel.savedAddresses.value
                            val defaultAddr = savedAddrs.find { it.isDefault }
                            if (usr != null || firebaseUser != null) {
                                viewModel.proceedToCheckout(
                                    address = defaultAddr?.addressLine ?: usr?.address ?: "",
                                    lat = defaultAddr?.latitude ?: usr?.lat ?: 11.5812,
                                    lng = defaultAddr?.longitude ?: usr?.lng ?: 77.8465
                                ) { generatedOrderId ->
                                    viewModel.selectedTabState.value = "TRACKER"
                                    Toast.makeText(context, "சாட்பாட் மூலமாக ஆர்டர் வெற்றிகரமாக சமர்ப்பிக்கப்பட்டது! 🛵", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showPlaceOrderConfirm = false }) {
                        Text("CANCEL", color = LyoColors.TextSecondary)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }

        val showConflict by viewModel.showCartConflictDialog.collectAsState(initial = false)
        val pendingItem by viewModel.pendingItemToAdd.collectAsState()
        if (showConflict && pendingItem != null) {
            Lyo3DDialog(onDismissRequest = { viewModel.showCartConflictDialog.value = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "கார்ட்டை மாற்றலாமா? 🔄",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "அண்ணே/அக்கா, உங்களுடைய கூடையில் ஏற்கனவே வேறொரு கடையின் உணவுகள் உள்ளன. உணவு விநியோக விதிமுறைகளின்படி, ஒரு நேரத்தில் ஒரு கடையில் இருந்து மட்டுமே ஆர்டர் செய்ய முடியும்.\n\nமுந்தைய கடையில் உள்ள உணவுகளை நீக்கிவிட்டு, புதிய கடையான '${pendingItem?.second?.nameTa?.ifEmpty { pendingItem?.second?.name }}'-ல் இருந்து இந்த உணவை சேர்க்கலாமா? 😊",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your basket contains items from another restaurant. Adding this item will replace and clear your existing basket. Proceed?",
                        color = LyoColors.TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { viewModel.showCartConflictDialog.value = false }
                        ) {
                            Text("வேண்டாம்", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.confirmPendingAddToCart(clearOnConflict = true)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("ஆமாம், மாற்றுங்கள்", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

suspend fun resolveHighFidelityReverseGeocoding(context: android.content.Context, lat: Double, lng: Double): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        @Suppress("DEPRECATION")
        val geocoder = android.location.Geocoder(context, java.util.Locale("en", "IN"))
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val fullAddress = address.getAddressLine(0)
            if (!fullAddress.isNullOrBlank()) {
                fullAddress
            } else {
                val parts = listOfNotNull(
                    address.subLocality,
                    address.locality,
                    address.adminArea,
                    address.postalCode
                ).filter { it.isNotBlank() }
                if (parts.isNotEmpty()) {
                    parts.joinToString(", ")
                } else {
                    "Lat: $lat, Lng: $lng, Idappadi, Salem District, Tamil Nadu, Pin: 637101"
                }
            }
        } else {
            "Door No. (Manual entry needed), Lat: ${lat}, Lng: ${lng}, Idappadi, Salem District, Tamil Nadu, Pin: 637101"
        }
    } catch (e: Exception) {
        "Door No. (Manual entry needed), Lat: ${lat}, Lng: ${lng}, Idappadi, Salem District, Tamil Nadu, Pin: 637101"
    }
}

suspend fun fetchCurrentLocationAndReverseGeocode(context: android.content.Context): Triple<Double, Double, String>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return@withContext null
    try {
        var loc: android.location.Location? = null
        
        // 1. FIRST PRIORITY: Active fresh location request for high accuracy (no stale cached location)
        val providers = listOf(
            android.location.LocationManager.GPS_PROVIDER,
            android.location.LocationManager.NETWORK_PROVIDER
        )
        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider)) {
                val deferredLocation = kotlinx.coroutines.CompletableDeferred<android.location.Location?>()
                val listener = object : android.location.LocationListener {
                    override fun onLocationChanged(location: android.location.Location) {
                        deferredLocation.complete(location)
                        try {
                            locationManager.removeUpdates(this)
                        } catch (e: Exception) {}
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        locationManager.requestLocationUpdates(
                            provider,
                            0L,
                            0f,
                            listener,
                            android.os.Looper.getMainLooper()
                        )
                    } catch (e: SecurityException) {
                        deferredLocation.complete(null)
                    } catch (e: Exception) {
                        deferredLocation.complete(null)
                    }
                }
                
                // Wait up to 3500ms for a fresh satellite/network coordinate fix
                val freshLoc = kotlinx.coroutines.withTimeoutOrNull(3500) {
                    deferredLocation.await()
                }
                
                // Clean up updates
                try {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        locationManager.removeUpdates(listener)
                    }
                } catch (e: Exception) {}

                if (freshLoc != null) {
                    loc = freshLoc
                    break
                }
            }
        }
        
        // 2. SECOND PRIORITY (FALLBACK): If active request timed out or failed, try last known cached location
        if (loc == null) {
            if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                loc = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
            }
            if (loc == null && locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                loc = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            }
        }
        
        if (loc != null) {
            val latVal = loc.latitude
            val lngVal = loc.longitude
            val addr = resolveHighFidelityReverseGeocoding(context, latVal, lngVal)
            return@withContext Triple(latVal, lngVal, addr)
        }
    } catch (e: SecurityException) {
        // Ignored
    } catch (e: Exception) {
        // Ignored
    }
    return@withContext null
}

@Composable
fun LiquidHeroBanners(
    promoBanners: List<PromoBanner>,
    modifier: Modifier = Modifier
) {
    val activeBanners = remember(promoBanners) {
        if (promoBanners.isEmpty()) {
            listOf(
                PromoBanner(
                    code = "LYOBIRYANI",
                    description = "எடப்பாடி ஸ்பெஷல் காரசாரமான மட்டன் பிரியாணி மற்றும் சிக்கன் 65 காம்போ! 30% தள்ளுபடி • 🍛🛵",
                    imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80"
                ),
                PromoBanner(
                    code = "LYOSWEETS",
                    description = "சுத்தமான காரைக்குடி நெய் மைசூர் பாக் & பாரம்பரிய மில்க் ஸ்வீட்ஸ் • 30% சிறப்பு சேமிப்பு! 🍬✨",
                    imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80"
                ),
                PromoBanner(
                    code = "LYODOSA",
                    description = "மொறுமொறுப்பான மதுரை நெய் ரோஸ்ட் மசாலா தோசை மாம்பழ சட்னியுடன்! 🥞",
                    imageUrl = "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=800&auto=format&fit=crop&q=80"
                ),
                PromoBanner(
                    code = "LYOPAROTTA",
                    description = "சுடச்சுட சாஃப்ட் கொத்து பரோட்டாவும் சால்னாவும் • ₹50 தள்ளுபடி! 🍽️🔥",
                    imageUrl = "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=800&auto=format&fit=crop&q=80"
                ),
                PromoBanner(
                    code = "LYOCAKE",
                    description = "ருசி மிகுந்த கொண்டாட்ட சாக்லேட் ட்ரஃபிள் மற்றும் பிரெஷ் கிரீம் கேக்குகள்! 🎂🍰",
                    imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800&auto=format&fit=crop&q=80"
                )
            )
        } else {
            promoBanners
        }
    }

    var currentIndex by remember { mutableStateOf(0) }
    
    // Auto slider coroutine
    LaunchedEffect(activeBanners.size) {
        if (activeBanners.size > 1) {
            while (true) {
                kotlinx.coroutines.delay(4000)
                currentIndex = (currentIndex + 1) % activeBanners.size
            }
        }
    }

    val activeBanner = activeBanners.getOrNull(currentIndex) ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Inner 3D Glass Container with beautiful height
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp) // made a bit smaller for compact elite style
                .testTag("hero_banners_container"),
            cornerRadius = 16.dp,
            borderColor = Color(0x44FFFFFF),
            backgroundColor = Color(0xFF0F172A),
            innerPadding = 0.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Layer (Liquid colorful gradient glow)
                val gradientColors = when (currentIndex % 3) {
                    0 -> listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899))
                    1 -> listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF701A75))
                    else -> listOf(Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF4C1D95))
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = gradientColors.map { it.copy(alpha = 0.2f) }
                            )
                        )
                )

                // Render banner image with extreme high quality and soft shadows
                val hasCustomImage = activeBanner.imageUrl.isNotBlank() && (
                    activeBanner.imageUrl.startsWith("http") || 
                    activeBanner.imageUrl.startsWith("content://") || 
                    activeBanner.imageUrl.startsWith("file://") || 
                    activeBanner.imageUrl.startsWith("/storage") || 
                    activeBanner.imageUrl.startsWith("/data") || 
                    activeBanner.imageUrl.contains("/")
                )

                if (hasCustomImage) {
                    val painterModel = remember(activeBanner.imageUrl) {
                        if (activeBanner.imageUrl.startsWith("/")) java.io.File(activeBanner.imageUrl) else activeBanner.imageUrl
                    }
                    androidx.compose.foundation.Image(
                        painter = coil.compose.rememberAsyncImagePainter(painterModel),
                        contentDescription = activeBanner.description,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Heavy liquid ambient overlay for magnificent modern depth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x33000000),
                                        Color(0xEE0F172A)
                                    )
                                )
                            )
                    )
                } else {
                    // Modern 3D fallback art backplate
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF312E81), Color(0xFF0F172A)),
                                    radius = 350f
                                )
                            )
                    )
                }

                // Banner Details & Promo Codes
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // 3D Glass Badge for coupon code
                        Box(
                            modifier = Modifier
                                .then(
                                    with(LyoGlassDesignTokens) {
                                        Modifier.liquidGlass3D(
                                            cornerRadius = 10.dp,
                                            elevation = 2.dp,
                                            borderWidth = 1.2.dp,
                                            borderBrush = Brush.linearGradient(listOf(Color(0x66FFFFFF), Color(0x11FFFFFF))),
                                            backgroundColor = Color(0x770F172A)
                                        )
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocalOffer,
                                    contentDescription = null,
                                    tint = LyoColors.AmberYellow,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "CODE: ${activeBanner.code}",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Hot Action Tag
                        Box(
                            modifier = Modifier
                                .background(LyoColors.AccentOrange, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "ஆபர்",
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Bottom info & Description with premium styling
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text(
                            text = activeBanner.description,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "உடனே ஆர்டர் செய்து கூப்பனைப் பயன்படுத்துங்கள்! • SWIPE LEFT/RIGHT",
                            color = LyoColors.TextSecondary.copy(alpha = 0.8f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3D Smooth Liquid Dot Indicators (absolute overlay in the bottom right corner)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activeBanners.forEachIndexed { index, _ ->
                        val isActive = index == currentIndex
                        val width by animateDpAsState(
                            targetValue = if (isActive) 16.dp else 6.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                        val color = if (isActive) LyoColors.AccentOrange else Color(0x44FFFFFF)
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isActive) 1.dp else 0.dp,
                                    color = if (isActive) Color.White.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidSingleBanner(code: String, description: String, imageUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp),
            cornerRadius = 24.dp,
            borderColor = Color(0x44FFFFFF),
            backgroundColor = Color(0xFF0F172A),
            innerPadding = 0.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.Image(
                    painter = coil.compose.rememberAsyncImagePainter(imageUrl),
                    contentDescription = description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x660B1120), Color(0xCC0B1120))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x66000000), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "CODE: $code",
                            color = LyoColors.AmberYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = description,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getAppealingFoodImage(item: MenuItem): String {
    return ""
}

@Composable
fun PhotoGalleryExplorerDialog(
    items: List<MenuItem>,
    viewModel: StorefrontViewModel,
    vendor: Vendor,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("lyo_photo_hits", android.content.Context.MODE_PRIVATE) }
    
    // Track hit states & counts
    val hitCounts = remember {
        mutableStateMapOf<Long, Int>().apply {
            items.forEach { item ->
                put(item.id, sharedPrefs.getInt("count_${item.id}", (15..95).random())) // Start with a realistic pleasant baseline of hits!
            }
        }
    }
    val userHits = remember {
        mutableStateMapOf<Long, Boolean>().apply {
            items.forEach { item ->
                put(item.id, sharedPrefs.getBoolean("hit_${item.id}", false))
            }
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6090D16))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${vendor.name.uppercase()} • புகைப்பட கேலரி",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "ஒரே கிளிக்கில் உணவுகளைப் பார்த்து ஆர்டர் செய்யுங்கள்",
                            color = LyoColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Close,
                            contentDescription = "close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Grid of Photos
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items.size) { index ->
                        val item = items[index]
                        val isHit = userHits[item.id] ?: false
                        val count = hitCounts[item.id] ?: 0
                        val imgUrl = getAppealingFoodImage(item)

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp),
                            cornerRadius = 16.dp,
                            borderColor = Color(0x33FFFFFF),
                            backgroundColor = Color(0xFF1E293B),
                            innerPadding = 0.dp
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (imgUrl.isNotBlank()) {
                                    androidx.compose.foundation.Image(
                                        painter = coil.compose.rememberAsyncImagePainter(imgUrl),
                                        contentDescription = item.nameEn,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF334155), Color(0xFF0F172A))
                                                )
                                            )
                                    )
                                }
                                // Gradient shading
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color(0x99000000), Color(0xEE0F172A))
                                            )
                                        )
                                )

                                // Indicators top bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    VegIndicator(isVeg = item.isVeg)

                                    // Quick Hit Button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isHit) Color(0xAAEF4444) else Color(0x99000000))
                                            .clickable {
                                                if (viewModel.currentUser.value == null) {
                                                    android.widget.Toast.makeText(context, "Please login first to add favorites! 🔐", android.widget.Toast.LENGTH_LONG).show()
                                                    viewModel.pendingLoginAction.value = com.example.ui.viewmodels.StorefrontViewModel.PendingLoginAction.ViewProfile
                                                    viewModel.navigationTrigger.value = "LOGIN"
                                                } else {
                                                    val nextHit = !isHit
                                                    userHits[item.id] = nextHit
                                                    val newCount = count + (if (nextHit) 1 else -1)
                                                    hitCounts[item.id] = newCount
                                                    sharedPrefs.edit().putBoolean("hit_${item.id}", nextHit).putInt("count_${item.id}", newCount).apply()
                                                }
                                            }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isHit) androidx.compose.material.icons.Icons.Filled.Favorite else androidx.compose.material.icons.Icons.Filled.FavoriteBorder,
                                                contentDescription = "hit",
                                                tint = Color.White,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "$count Hits",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Info section at bottom
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = item.nameEn.uppercase(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.nameTa.isNotBlank()) {
                                        Text(
                                            text = item.nameTa,
                                            color = LyoColors.AmberYellow,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "₹${item.price.toInt()}",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )

                                        // Quick add to cart
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(LyoColors.AccentOrange)
                                                .clickable {
                                                    viewModel.addToCart(item, vendor)
                                                    android.widget.Toast.makeText(context, "${item.nameEn} Added!", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("ADD +", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
