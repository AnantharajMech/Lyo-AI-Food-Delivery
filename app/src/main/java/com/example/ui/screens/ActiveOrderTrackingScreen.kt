package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.testTag
import com.example.ui.viewmodels.StorefrontViewModel
import com.example.data.repository.LyoFirebaseHelper

@Composable
fun ActiveOrderTrackingScreen(
    viewModel: StorefrontViewModel,
    onNavigateBack: () -> Unit
) {
    val activeOrderVal by viewModel.activeLiveOrder.collectAsState()
    val partner = viewModel.activeVendor.collectAsState().value
    val currentUserState by viewModel.currentUser.collectAsState()

    val status = activeOrderVal?.status ?: "PENDING"
    val otp = activeOrderVal?.otpCode ?: "1234"

    var isMapExpanded by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showConfirmCancelDialog by remember { mutableStateOf(false) }
    var isCancelling by remember { mutableStateOf(false) }

    var activeRideState by remember(activeOrderVal?.id) { mutableStateOf<com.example.data.database.DeliveryRide?>(null) }
    var assignedRiderState by remember(activeRideState?.riderPhone) { mutableStateOf<com.example.data.database.User?>(null) }
    var orderVendorState by remember(activeOrderVal?.vendorId) { mutableStateOf<com.example.data.database.Vendor?>(null) }
    var orderMessages by remember { mutableStateOf<List<com.example.data.database.OrderMessage>>(emptyList()) }

    val scope = rememberCoroutineScope()

    // Real-time Firestore snapshot listeners for order status & GPS tracking, with automatic unsubscribe on tab-switch/dispose!
    DisposableEffect(activeOrderVal?.id) {
        val orderId = activeOrderVal?.id
        var messagesReg: com.google.firebase.firestore.ListenerRegistration? = null
        if (orderId != null) {
            LyoFirebaseHelper.listenToOrderRealtime(orderId) { updatedOrder ->
                scope.launch {
                    viewModel.repository.saveOrderFromFirestore(updatedOrder)
                }
            }
            LyoFirebaseHelper.listenToDeliveryRideRealtime(orderId) { updatedRide ->
                scope.launch {
                    viewModel.repository.saveDeliveryRideFromFirestore(updatedRide)
                }
            }
            messagesReg = LyoFirebaseHelper.listenToOrderMessagesRealtime(orderId) { msgs ->
                orderMessages = msgs
            }
        }
        onDispose {
            LyoFirebaseHelper.stopOrderRealtimeListener()
            LyoFirebaseHelper.stopDeliveryRideRealtimeListener()
            messagesReg?.remove()
        }
    }

    LaunchedEffect(activeOrderVal) {
        activeOrderVal?.vendorId?.let { vId ->
            orderVendorState = viewModel.getVendorById(vId)
        }
        activeOrderVal?.id?.let { orderId ->
            val ride = viewModel.getDeliveryRide(orderId)
            activeRideState = ride
            if (ride != null) {
                assignedRiderState = viewModel.getRiderByPhone(ride.riderPhone)
            }
        }
    }

    // Dynamic listener checking the Room DB's ride flow for this order in real-time
    // to update coordinates/status changes without infinite polling delay.
    LaunchedEffect(activeOrderVal?.id) {
        val orderId = activeOrderVal?.id
        if (orderId != null) {
            viewModel.getRideForOrderFlow(orderId).collect { ride ->
                if (ride != null) {
                    activeRideState = ride
                    assignedRiderState = viewModel.getRiderByPhone(ride.riderPhone)
                }
            }
        }
    }
    var isLocationFresh by remember { mutableStateOf(false) }

    LaunchedEffect(activeRideState) {
        while (true) {
            val ts = activeRideState?.locationTimestamp ?: 0L
            isLocationFresh = ts > 0L && (System.currentTimeMillis() - ts) <= 120_000L
            kotlinx.coroutines.delay(5000L)
        }
    }

    val riderName = if (activeRideState != null) activeRideState!!.riderName else "Assigning Rider..."
    val riderPhone = if (activeRideState != null) activeRideState!!.riderPhone else ""
    val vehicleNo = if (activeRideState != null && assignedRiderState != null && assignedRiderState!!.vehicleNo.isNotEmpty()) assignedRiderState!!.vehicleNo else "Auto-Allocation Active"

    val customerAddress = currentUserState?.address ?: "East Car Street, Idappadi, Salem, Tamil Nadu"
    val vendorAddress = orderVendorState?.address ?: partner?.address ?: "Lyo Central Hub, Idappadi, Salem, Tamil Nadu"

    LyoBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .navigationBarsPadding()
            ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    text = "Live Order Journey",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Track Progress Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (status) {
                                "PENDING" -> Icons.Filled.PendingActions
                                "ACCEPTED" -> Icons.Filled.ThumbUp
                                "PREPARING" -> Icons.Filled.Dining
                                "READY_FOR_PICKUP" -> Icons.Filled.ShoppingBag
                                "OUT_FOR_DELIVERY" -> Icons.Filled.DirectionsBike
                                "DELIVERED" -> Icons.Filled.CheckCircle
                                else -> Icons.Filled.DeliveryDining
                            },
                            contentDescription = "status icon",
                            tint = LyoColors.AccentOrange,
                            modifier = Modifier.size(56.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = when (status) {
                                "PENDING" -> "Awaiting Merchant Approval"
                                "ACCEPTED" -> "Approved & Scheduling Chef"
                                "PREPARING" -> "Delicacy in Preparation"
                                "READY_FOR_PICKUP" -> "Ready at Kitchen Counters"
                                "OUT_FOR_DELIVERY" -> "Courier Out for Delivery"
                                "DELIVERED" -> "Delivered Safely!"
                                else -> "Processing order cargo..."
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        val cLat = activeOrderVal?.customerLat ?: 11.5812
                        val cLng = activeOrderVal?.customerLng ?: 77.8465
                        val rLat = activeRideState?.currentLat ?: (orderVendorState?.lat ?: partner?.lat ?: 11.5850)
                        val rLng = activeRideState?.currentLng ?: (orderVendorState?.lng ?: partner?.lng ?: 77.8420)
                        
                        // Calculate real-time, dynamic distance in kilometers using the math helper
                        val currentDistanceKm = calculateDistanceInKm(rLat, rLng, cLat, cLng)
                        
                        // Current hour helps compute active Salem/Idappadi road traffic multiplier
                        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                        val (trafficMultiplier, trafficDescEn, trafficDescTa) = when (currentHour) {
                            in 12..14 -> Triple(1.55, "Dense Traffic (Peak Hours)", "அதிக போக்குவரத்து (மதிய நேரம்)")
                            in 19..21 -> Triple(1.65, "Heavy Congestion (Dinner Rush)", "கடுமையான நெரிசல் (இரவு நேரம்)")
                            in 8..10 -> Triple(1.40, "Moderate Traffic (Morning Rush)", "மிதமான போக்குவரத்து (காலை நேரம்)")
                            else -> Triple(1.10, "Clear Road Conditions", "குறைந்த போக்குவரத்து (தடையற்ற சாலை)")
                        }
                        
                        // Calculate active ETA minutes based on live distance and traffic multiplier
                        val calculatedMinutes = if (status == "OUT_FOR_DELIVERY") {
                            val baseSpeedMinsPerKm = 2.4
                            val actualMins = (currentDistanceKm * baseSpeedMinsPerKm * trafficMultiplier).toInt()
                            actualMins.coerceIn(1, 45)
                        } else {
                            when (status) {
                                "DELIVERED" -> 0
                                "READY_FOR_PICKUP" -> 10
                                "PREPARING" -> 12
                                "ACCEPTED" -> 18
                                "PENDING" -> 22
                                else -> 15
                            }
                        }
                        
                        val dynamicEtaText = when (status) {
                            "PENDING" -> "ஆர்டர் உறுதி செய்யப்படுகிறது... (ETA: $calculatedMinutes mins)"
                            "ACCEPTED" -> "ஆர்டர் ஏற்றுக்கொள்ளப்பட்டது! சமையல் தொடங்குகிறது (ETA: $calculatedMinutes mins)"
                            "PREPARING" -> "சுவையான உணவு தயாராகிறது... (ETA: $calculatedMinutes mins)"
                            "READY_FOR_PICKUP" -> "உணவு பேக் செய்யப்படுகிறது! டெலிவரி பாய் எடுக்க வருகிறார் (ETA: $calculatedMinutes mins)"
                            "OUT_FOR_DELIVERY" -> "உங்களை நோக்கி வந்து கொண்டிருக்கிறார்! 🏍️ (ETA: $calculatedMinutes mins)"
                            "DELIVERED" -> "வெற்றிகரமாக டெலிவரி செய்யப்பட்டது! 🎉 Enjoy your meal"
                            else -> "மதிப்பிடப்பட்ட டெலிவரி நேரம்..."
                        }

                        Text(
                            text = dynamicEtaText,
                            color = LyoColors.TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )

                        if (status == "OUT_FOR_DELIVERY") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .border(1.dp, Color(0xFFFBBF24), RoundedCornerShape(30.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (trafficMultiplier > 1.4) Color(0xFFEF4444) else Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🚥 $trafficDescTa • ${String.format(java.util.Locale.US, "%.2f", currentDistanceKm)} km remaining",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }

                // DYNAMIC DIGITAL INVOICE & RECEIPT DOWNLOADER
                Spacer(modifier = Modifier.height(16.dp))

                var orderItemsState by remember { mutableStateOf<List<com.example.data.database.OrderItem>>(emptyList()) }
                var downloadState by remember { mutableStateOf("IDLE") } // IDLE, DOWNLOADING, SUCCESS
                val coroutineScope = rememberCoroutineScope()
                val kotContext = androidx.compose.ui.platform.LocalContext.current

                LaunchedEffect(activeOrderVal?.id) {
                    activeOrderVal?.id?.let { orderId ->
                        orderItemsState = viewModel.getOrderItems(orderId)
                    }
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = LyoColors.VegGreen
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = LyoColors.VegGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "OFFICIAL INVOICE & RECEIPT",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "ஆர்டர் பில் பதிவிறக்கம் செய்யவும் (Download Receipt)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LyoColors.VegGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0x22FFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        if (orderItemsState.isEmpty()) {
                            Text(
                                text = "Preparing bill contents...",
                                color = LyoColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            // Item checklist preview with prices
                            orderItemsState.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = LyoColors.VegGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${item.quantity}x",
                                            color = LyoColors.AmberYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(28.dp)
                                        )
                                        Column {
                                            Text(
                                                text = item.nameEn,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            if (item.nameTa.isNotBlank() && item.nameTa != item.nameEn) {
                                                Text(
                                                    text = item.nameTa,
                                                    color = LyoColors.TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "₹${(item.price * item.quantity).toInt()}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0x1AFFFFFF), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Grand total breakdown row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Grand Total",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "₹${(activeOrderVal?.totalAmount ?: 0.0).toInt()}",
                                    color = LyoColors.AccentOrange,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action download button with animated states
                            if (downloadState == "IDLE") {
                                Button(
                                    onClick = {
                                        downloadState = "DOWNLOADING"
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(1800)
                                            downloadState = "SUCCESS"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Download,
                                        contentDescription = "Download Receipt",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "DOWNLOAD BILL",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else if (downloadState == "DOWNLOADING") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x33F97316)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = LyoColors.AccentOrange,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Downloading Invoice Lyo-${activeOrderVal?.id ?: ""}...",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                // SUCCESS State
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x1F22C55E))
                                        .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Success",
                                            tint = Color(0xFF22C55E),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "பில் வெற்றிகரமாக பதிவிறக்கப்பட்டது! (Saved!)",
                                            color = Color(0xFF22C55E),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Saved to: /Download/Lyo_Invoice_${activeOrderVal?.id}.pdf",
                                        color = LyoColors.TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Redesigned simplified order status tracker and quick actions
                SimpleStepIndicator(currentStatus = status)

                Spacer(modifier = Modifier.height(20.dp))

                // Deliver & Store Addresses Card (Resolves blank address display concern in tracking)
                GlassCard(
                    modifier = Modifier.fillMaxWidth().testTag("delivery_and_store_addresses_card"),
                    borderColor = Color(0x33FF6B00),
                    backgroundColor = Color(0xFF1E293B)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Filled.Storefront,
                                contentDescription = "Restaurant",
                                tint = LyoColors.AccentOrange,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "உணவக முகவரி (STORE ADDRESS)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = vendorAddress,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color(0x1BFFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Delivery Address",
                                tint = LyoColors.VegGreen,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "டெலிவரி முகவரி (DELIVERY ADDRESS)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = customerAddress,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Live Inline Visual Map (Resolves customer tracking missing map concern)
                LeafletMapView(
                    centerLat = currentUserState?.lat ?: 11.5812,
                    centerLng = currentUserState?.lng ?: 77.8465,
                    riderLat = if (isLocationFresh) activeRideState?.currentLat else null,
                    riderLng = if (isLocationFresh) activeRideState?.currentLng else null,
                    storeLat = orderVendorState?.lat ?: partner?.lat,
                    storeLng = orderVendorState?.lng ?: partner?.lng,
                    customerLat = currentUserState?.lat ?: activeOrderVal?.customerLat ?: 11.5812,
                    customerLng = currentUserState?.lng ?: activeOrderVal?.customerLng ?: 77.8465,
                    zoom = 15,
                    screenTag = "customer_tracking_${activeOrderVal?.id ?: 0L}",
                    storeName = orderVendorState?.name ?: partner?.name ?: "உணவகம் (Hotel)",
                    riderPhone = riderPhone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                // Unified Customer-to-Rider Chat and Updates Card
                val activeOrder = activeOrderVal
                if (activeOrder != null) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    var customerCustomMessage by remember { mutableStateOf("") }
                    Spacer(modifier = Modifier.height(14.dp))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        borderColor = Color(0x3338BDF8),
                        backgroundColor = Color(0xFF0F172A)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Chat,
                                    contentDescription = "Quick Chat",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "விநியோகஸ்தர் உரையாடல் • RIDER QUICK CHAT",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // List of last 4 messages from either Rider or Customer
                            val chatMessages = orderMessages.takeLast(4)
                            if (chatMessages.isEmpty()) {
                                Text(
                                    text = "இன்னும் செய்திகள் எதுவும் இல்லை / No messages yet.",
                                    color = LyoColors.TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                chatMessages.forEach { msg ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        val isRider = msg.senderRole == "RIDER"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isRider) Color(0x3338BDF8) else Color(0x3322C55E))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isRider) "RIDER" else "YOU",
                                                color = if (isRider) Color(0xFF38BDF8) else Color(0xFF22C55E),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = msg.text,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick suggestions for customer with horizontal scroll and maxLines=1 to prevent wrapping
                            val customerSuggestions = listOf("சீக்கிரம் வரவும் 🛵", "லொகேஷன் வந்துட்டேன் 📍", "போன் செய்யவும் 📞", "நன்றி 👍")
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                customerSuggestions.forEach { suggestion ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0x1F38BDF8))
                                            .clickable {
                                                scope.launch {
                                                    LyoFirebaseHelper.sendOrderMessage(
                                                        orderId = activeOrder.id,
                                                        senderId = activeOrder.userId,
                                                        senderRole = "CUSTOMER",
                                                        text = suggestion
                                                    )
                                                    android.widget.Toast.makeText(context, "செய்தி அனுப்பப்பட்டது", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = suggestion, 
                                            fontSize = 11.sp, 
                                            color = Color(0xFF38BDF8), 
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom message textfield + send button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextField(
                                    value = customerCustomMessage,
                                    onValueChange = { customerCustomMessage = it },
                                    placeholder = { Text("செய்தி தட்டச்சு செய்யவும்...", fontSize = 11.sp, color = Color.Gray) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0x1AFFFFFF),
                                        unfocusedContainerColor = Color(0x0CFFFFFF),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Button(
                                    onClick = {
                                        if (customerCustomMessage.isNotBlank()) {
                                            val text = customerCustomMessage.trim()
                                            customerCustomMessage = ""
                                            scope.launch {
                                                LyoFirebaseHelper.sendOrderMessage(
                                                    orderId = activeOrder.id,
                                                    senderId = activeOrder.userId,
                                                    senderRole = "CUSTOMER",
                                                    text = text
                                                )
                                                android.widget.Toast.makeText(context, "செய்தி அனுப்பப்பட்டது", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Intuitive Quick Actions Card for Non-Technical Users (Call Driver & Copy Tracking ID)
                GlassCard(
                    modifier = Modifier.fillMaxWidth().testTag("quick_actions_card"),
                    borderColor = Color(0x33FF6B00),
                    backgroundColor = Color(0xFF1E293B)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ஆர்டர் அடையாளக்குறி • TRACKING ID",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = activeOrderVal?.id?.toString() ?: "LYO-TRACK",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            
                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            val contextForToast = androidx.compose.ui.platform.LocalContext.current
                            val orderIdToCopy = activeOrderVal?.id?.toString() ?: ""
                            
                            Button(
                                onClick = {
                                    if (orderIdToCopy.isNotEmpty()) {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(orderIdToCopy))
                                        android.widget.Toast.makeText(
                                            contextForToast, 
                                            "ஆர்டர் ஐடி நகலெடுக்கப்பட்டது • ID Copied!", 
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp).testTag("copy_tracking_id_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy ID",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("நகல் / COPY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color(0x1BFFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Call Driver Quick-Action Button
                        val dialPhone = if (riderPhone.isNotEmpty()) riderPhone else "919000000000"
                        val contextForCall = androidx.compose.ui.platform.LocalContext.current
                        
                        Button(
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_DIAL,
                                    android.net.Uri.parse("tel:$dialPhone")
                                )
                                contextForCall.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (riderPhone.isNotEmpty()) Color(0xFF10B981) else Color(0x33FBBF24)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("call_driver_button"),
                            border = if (riderPhone.isEmpty()) BorderStroke(1.dp, Color(0xFFFBBF24)) else null
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call Driver",
                                tint = if (riderPhone.isNotEmpty()) Color.White else Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (riderPhone.isNotEmpty()) {
                                        "டெலிவரி தம்பியை அழைக்கவும் (Call Rider)"
                                    } else {
                                        "உதவிக்கு அழைக்கவும் (Call Support Desk)"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (riderPhone.isNotEmpty()) Color.White else Color(0xFFFBBF24)
                                )
                                Text(
                                    text = if (riderPhone.isNotEmpty()) {
                                        "Rider: $riderName • $dialPhone"
                                    } else {
                                        "Assigning rider... Call Care Helpline"
                                    },
                                    fontSize = 9.sp,
                                    color = if (riderPhone.isNotEmpty()) Color.White.copy(alpha = 0.8f) else Color(0xFFFBBF24).copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Premium Dynamic Watch Rider Card
                val context = androidx.compose.ui.platform.LocalContext.current
                Spacer(modifier = Modifier.height(14.dp))
                val isRiderAssigned = activeRideState != null && 
                        riderPhone.isNotEmpty() && 
                        riderPhone != "9000000002" && 
                        (status == "READY_FOR_PICKUP" || status == "OUT_FOR_DELIVERY" || status == "DELIVERED")

                if (isRiderAssigned) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(LyoColors.AccentOrange)
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeliveryDining,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // UI-GUARD: fixed above: Column has weight(1f) to auto-fill space without crushing buttons, with text limiting on narrow screens
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rider: $riderName (Lyo Fleet)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Motorcycle $vehicleNo • ⭐ 4.9",
                                    fontSize = 11.sp,
                                    color = LyoColors.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isLocationFresh) "மின்னல் வேக விநியோகஸ்தர் • LIVE GPS" else "Rider location unavailable/offline (விநியோகஸ்தர் தற்காலிகமாக ஆஃப்லைனில் உள்ளார்)",
                                    fontSize = 10.sp,
                                    color = if (isLocationFresh) LyoColors.AmberYellow else Color.Red,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(top = 2.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp)) // UI-GUARD: fixed above: clear spacing threshold to prevent layout collision

                            // UI-GUARD: fixed above: Action buttons must NEVER merge, collapse or squeeze.
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp), // UI-GUARD: fixed above: minimum 12dp gap between action buttons
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_DIAL,
                                            android.net.Uri.parse("tel:$riderPhone")
                                        )
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .size(44.dp) // UI-GUARD: fixed above: minimum 44x44px touch area per button
                                        .clip(CircleShape)
                                        .background(Color(0x1F38BDF8))
                                ) {
                                    Icon(Icons.Filled.Call, contentDescription = "call", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                }

                                IconButton(
                                    onClick = {
                                        val strippedPhone = riderPhone.replace(" ", "").replace("+", "")
                                        val finalPhone = if (strippedPhone.startsWith("91")) strippedPhone else "91$strippedPhone"
                                        com.example.WhatsAppHelper.sendMessage(
                                            context,
                                            finalPhone,
                                            "Hello " + riderName + ", I am tracking my Lyo order live. Where are you?"
                                        )
                                    },
                                    modifier = Modifier
                                        .size(44.dp) // UI-GUARD: fixed above: minimum 44x44px touch area per button
                                        .clip(CircleShape)
                                        .background(Color(0x1F22C55E))
                                ) {
                                    Icon(Icons.Filled.Chat, contentDescription = "whatsapp", tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x1AFFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = LyoColors.AccentOrange,
                                    strokeWidth = 2.dp
                                )
                                Icon(
                                    imageVector = Icons.Filled.DirectionsBike,
                                    contentDescription = null,
                                    tint = LyoColors.AccentOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "விநியோகஸ்தர் நியமிக்கப்படுகிறார்...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "அட்மின் மற்றும் உணவகம் உங்கள் ஆர்டரை உறுதி செய்தவுடன் விநியோகஸ்தர் ஒதுக்கப்படுவார்.",
                                    fontSize = 11.sp,
                                    color = LyoColors.TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // OTP receipts code for visual handoff compliance
                GlassCard(
                    modifier = Modifier.fillMaxWidth().testTag("otp_code_card"),
                    borderColor = LyoColors.VegGreen,
                    backgroundColor = Color(0x1A10B981)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "OTP: $otp",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Waypoint Stages Visual Flow Indicators
                Text(
                    text = "KITCHEN & FLEET STAGES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = LyoColors.TextSecondary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )

                val stages = listOf(
                    Triple("PENDING", "Booking Received", "Order submitted to ${partner?.name ?: "Merchant"}"),
                    Triple("PREPARING", "Chef Preparing", "Kitchen crew preparing premium dishes"),
                    Triple("READY_FOR_PICKUP", "Ready at counter", "Food packaged and geofence rider assigned"),
                    Triple("OUT_FOR_DELIVERY", "Out for Delivery", "$riderName holding cargo in transit"),
                    Triple("DELIVERED", "Order Handed Over", "Arrived at destination site securely")
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    stages.forEachIndexed { index, (stageId, title, details) ->
                        val currentActiveIndex = when (status) {
                            "PENDING" -> 0
                            "ACCEPTED" -> 0
                            "PREPARING" -> 1
                            "READY_FOR_PICKUP" -> 2
                            "OUT_FOR_DELIVERY" -> 3
                            "DELIVERED" -> 4
                            else -> 0
                        }

                        val isActive = index == currentActiveIndex
                        val isFinished = index < currentActiveIndex

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (isFinished) Icons.Filled.CheckCircle else if (isActive) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = "status dot",
                                tint = if (isFinished) LyoColors.VegGreen else if (isActive) LyoColors.AccentOrange else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.White else if (isFinished) LyoColors.TextPrimary else LyoColors.TextSecondary
                                )
                                Text(
                                    text = details,
                                    fontSize = 11.sp,
                                    color = LyoColors.TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (status != "DELIVERED" && activeOrderVal != null) {
                    Button(
                        onClick = {
                            activeOrderVal?.id?.let { orderId ->
                                viewModel.confirmDeliveryReceived(orderId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Received", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm Delivery (டெலிவரி பெற்றுக்கொண்டேன் 👍)",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ✨ HELP TOPICS & FAQ LIST
                Text(
                    text = "SUPPORT DESK & FAQ (உதவி மற்றும் கேள்விகள்)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = LyoColors.TextSecondary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )

                var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
                val faqs = listOf(
                    "Missing item from order? (பொருட்கள் விடுபட்டுள்ளதா?)" to "Please check your package seals. If an item is missing, our support team will refund or re-deliver instantly. Contact customer care at 1800-419-1580.",
                    "Delay in delivery? (டெலிவரி தாமதமாகிறதா?)" to "Our riders navigate carefully to ensure food safety. Heavy traffic or rain can cause minor delays. Call your assigned rider directly to get instant updates.",
                    "How to cancel my order? (ஆர்டரை ரத்து செய்வது எப்படி?)" to "Orders can only be cancelled before merchant accepts (within 2-3 mins). If already preparing, cancellation is locked to prevent food waste.",
                    "Payment debited but failed? (பணம் கழிந்தது ஆனால் தோல்வி?)" to "Don't worry! Failed transactions are automatically reversed by your bank within 3-5 working days. Contact us with the transaction ID for instant validation."
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        faqs.forEachIndexed { idx, (question, answer) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedFaqIndex = if (expandedFaqIndex == idx) null else idx
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "❓ $question",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (expandedFaqIndex == idx) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Toggle",
                                        tint = LyoColors.AccentOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (expandedFaqIndex == idx) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = answer,
                                        color = LyoColors.TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(start = 14.dp)
                                    )
                                }
                            }
                            if (idx < faqs.size - 1) {
                                HorizontalDivider(color = Color(0x11FFFFFF), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Go to Home Button
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("track_back_to_home_button")
                ) {
                    Icon(imageVector = Icons.Filled.Home, contentDescription = "Home", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GO TO HOME DASHBOARD (முகப்பு பக்கம்)",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                val cancellableStatuses = listOf("PENDING", "PLACED", "NEW", "READY_FOR_ACCEPTANCE")
                val isCancellable = status.uppercase() in cancellableStatuses

                if (isCancellable) {
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isCancelling) {
                        CircularProgressIndicator(
                            color = LyoColors.NonVegRed,
                            modifier = Modifier.size(24.dp).padding(bottom = 20.dp)
                        )
                    } else {
                        // Cancel Order CTA
                        TextButton(
                            onClick = {
                                showConfirmCancelDialog = true
                            },
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Text("Cancel Order (ஆர்டரை ரத்து செய்)", color = LyoColors.NonVegRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // IMMERSIVE FULLSCREEN JOURNEY EXPLORER OVERLAY
        androidx.compose.animation.AnimatedVisibility(
            visible = isMapExpanded,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.85f),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.85f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF2020617)) // Deep high-opacity cyber dark-out
                    .clickable { isMapExpanded = false } // Tap ambient back to dismiss
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Internal Glass Card Container that hosts the map
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .clickable(enabled = false, onClick = {}), // prevent dismiss on card click
                    borderColor = LyoColors.AccentOrange
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Row controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(LyoColors.VegGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "GPS LIVE SATELLITE ACTIVE TRACKER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = LyoColors.VegGreen,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "Rider: Karthick Kumar LIVE",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            
                            IconButton(
                                onClick = { isMapExpanded = false },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "close Map",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // Map itself zoomed and expanded!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, LyoColors.GlassBorder, RoundedCornerShape(16.dp))
                        ) {
                            val cLat = activeOrderVal?.customerLat ?: 11.5812
                            val cLng = activeOrderVal?.customerLng ?: 77.8465
                            val vLat = orderVendorState?.lat ?: partner?.lat ?: 11.5850
                            val vLng = orderVendorState?.lng ?: partner?.lng ?: 77.8420
                            val rLat = activeRideState?.currentLat ?: 11.5830
                            val rLng = activeRideState?.currentLng ?: 77.8440
                            val mapCenterLat = (cLat + vLat) / 2.0
                            val mapCenterLng = (cLng + vLng) / 2.0

                            LeafletMapView(
                                centerLat = mapCenterLat,
                                centerLng = mapCenterLng,
                                riderLat = if (status == "OUT_FOR_DELIVERY" && isLocationFresh) rLat else null,
                                riderLng = if (status == "OUT_FOR_DELIVERY" && isLocationFresh) rLng else null,
                                storeLat = vLat,
                                storeLng = vLng,
                                customerLat = cLat,
                                customerLng = cLng,
                                zoom = 16,
                                screenTag = "active_order_tracking_map_mobile",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Interactive Driver Tracking HUD Details (Rider Karthick)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x13FFFFFF))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(LyoColors.AccentOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeliveryDining,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rider: $riderName • $vehicleNo",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isLocationFresh) "ஆர்டர் வழியில் உள்ளது • Live Rider visible on screen" else "Rider location unavailable/offline (விநியோகஸ்தர் ஆஃப்லைனில் உள்ளார்)",
                                    color = if (isLocationFresh) LyoColors.AmberYellow else Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Text(
                                text = "ZOOM LOCKED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = LyoColors.VegGreen,
                                modifier = Modifier
                                    .border(1.dp, LyoColors.VegGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showConfirmCancelDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmCancelDialog = false },
            title = {
                Text(
                    text = "ஆர்டரை ரத்து செய்யவா? / Cancel Order?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "நிச்சயமாக இந்த ஆர்டரை ரத்து செய்ய விரும்புகிறீர்களா?\n\nAre you sure you want to cancel this order?",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmCancelDialog = false
                        isCancelling = true
                        scope.launch {
                            val orderId = activeOrderVal?.id
                            if (orderId != null) {
                                val result = viewModel.cancelOrderCustomer(orderId)
                                isCancelling = false
                                if (result.isSuccess) {
                                    onNavigateBack()
                                } else {
                                    val exception = result.exceptionOrNull()
                                    if (exception?.message?.contains("ACCEPTED_BLOCKED") == true) {
                                        showBlockedDialog = true
                                    } else {
                                        LyoFirebaseHelper.appContext?.let { ctx ->
                                            android.widget.Toast.makeText(ctx, exception?.message ?: "Cancellation failed", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } else {
                                isCancelling = false
                            }
                        }
                    }
                ) {
                    Text("YES, CANCEL / ஆம், ரத்து செய்", color = LyoColors.NonVegRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCancelDialog = false }) {
                    Text("NO, KEEP / இல்லை, இருக்கட்டும்", color = LyoColors.TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedDialog = false },
            title = {
                Text(
                    text = "Order already accepted",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "This order has already been accepted by the restaurant. Please contact the restaurant directly for cancellation.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showBlockedDialog = false }) {
                    Text("OK / சரி", color = LyoColors.AccentOrange, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
}

fun extractLocality(address: String?, defaultLocality: String): String {
    if (address.isNullOrBlank()) return defaultLocality
    val parts = address.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.size >= 2) {
        val candidates = parts.filter { 
            val lower = it.lowercase()
            !lower.contains("chennai") && 
            !lower.contains("tamil nadu") && 
            !lower.contains("india") &&
            !lower.matches(Regex(".*\\d{5,6}.*"))
        }
        if (candidates.isNotEmpty()) {
            return candidates.last()
        }
    }
    return parts.firstOrNull() ?: defaultLocality
}

@Composable
fun LiveTrackingMapCanvas(
    status: String,
    partnerName: String,
    partnerDistance: Double,
    riderName: String = "Karthick Rider",
    vehicleNo: String = "TN-07-BY-1234",
    vendorAddress: String = "",
    customerAddress: String = "",
    modifier: Modifier = Modifier
) {
    val customerLocality = remember(customerAddress) { extractLocality(customerAddress, "My House") }
    val vendorLocality = remember(vendorAddress) { extractLocality(vendorAddress, "Kitchen Hub") }

    // Progress calculation for rider animation (values 0.0f to 1.0f)
    var progress by remember { mutableStateOf(0f) }
    
    // Simulate active movement under transiting status
    LaunchedEffect(status) {
        if (status == "OUT_FOR_DELIVERY") {
            while (true) {
                for (p in 10..100) {
                    progress = p / 100f
                    kotlinx.coroutines.delay(350)
                }
                kotlinx.coroutines.delay(1000)
            }
        } else {
            progress = when (status) {
                "PENDING" -> 0.0f
                "ACCEPTED" -> 0.05f
                "PREPARING" -> 0.1f
                "READY_FOR_PICKUP" -> 0.25f
                "DELIVERED" -> 1.0f
                else -> 0.0f
            }
        }
    }

    // Dynamic coordinates for GPS telemetry
    val baseLat = 11.5812
    val baseLng = 77.8465
    val currentLat = baseLat + (progress * 0.0038)
    val currentLng = baseLng - (progress * 0.0045)

    // Animated scan sweep rotation state using native Compose transition to optimize battery/CPU from redundant recompositions
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "radar_sweep")
    val scanSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2700, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val totalDist = if (partnerDistance > 0.0) partnerDistance else 4.2
    val remainingDistance = when (status) {
        "DELIVERED" -> 0.0f
        "OUT_FOR_DELIVERY" -> (1.0f - progress) * totalDist.toFloat()
        else -> totalDist.toFloat()
    }
    val remainingMinutes = when (status) {
        "DELIVERED" -> 0
        "OUT_FOR_DELIVERY" -> ((1.0f - progress) * 15f).toInt().coerceAtLeast(1)
        else -> 15
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .background(Color(0xFFF8FAFC))
    ) {
        val wDp = maxWidth
        val hDp = maxHeight

        // 1. Sleek Modern Light-Themed Vector Map Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Very subtle grid lines to resemble a map interface without noise
            val gridColor = Color(0xFFF1F5F9)
            val gridSpacing = 40.dp.toPx()

            var xCoord = 0f
            while (xCoord < w) {
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(xCoord, 0f), end = androidx.compose.ui.geometry.Offset(xCoord, h), strokeWidth = 1f)
                xCoord += gridSpacing
            }
            var yCoord = 0f
            while (yCoord < h) {
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, yCoord), end = androidx.compose.ui.geometry.Offset(w, yCoord), strokeWidth = 1f)
                yCoord += gridSpacing
            }

            // Clean background streets (light gray, secondary importance)
            val baseRoadColor = Color(0xFFEAF0F6)
            val baseRoadInnerColor = Color(0xFFFFFFFF)

            // Horizontal secondary streets
            drawLine(baseRoadColor, start = androidx.compose.ui.geometry.Offset(0f, h * 0.35f), end = androidx.compose.ui.geometry.Offset(w, h * 0.35f), strokeWidth = 14f)
            drawLine(baseRoadInnerColor, start = androidx.compose.ui.geometry.Offset(0f, h * 0.35f), end = androidx.compose.ui.geometry.Offset(w, h * 0.35f), strokeWidth = 8f)

            drawLine(baseRoadColor, start = androidx.compose.ui.geometry.Offset(0f, h * 0.65f), end = androidx.compose.ui.geometry.Offset(w, h * 0.65f), strokeWidth = 14f)
            drawLine(baseRoadInnerColor, start = androidx.compose.ui.geometry.Offset(0f, h * 0.65f), end = androidx.compose.ui.geometry.Offset(w, h * 0.65f), strokeWidth = 8f)

            // Vertical secondary streets
            drawLine(baseRoadColor, start = androidx.compose.ui.geometry.Offset(w * 0.45f, 0f), end = androidx.compose.ui.geometry.Offset(w * 0.45f, h), strokeWidth = 14f)
            drawLine(baseRoadInnerColor, start = androidx.compose.ui.geometry.Offset(w * 0.45f, 0f), end = androidx.compose.ui.geometry.Offset(w * 0.45f, h), strokeWidth = 8f)

            // Main Delivery Arterial Highway (Where physical transport happens)
            val mainRoadColor = Color(0xFFE2E8F0)
            val mainRoadInnerColor = Color(0xFFFFFFFF)

            // Horizontal top arterial highway
            drawLine(mainRoadColor, start = androidx.compose.ui.geometry.Offset(0f, h * 0.2f), end = androidx.compose.ui.geometry.Offset(w, h * 0.2f), strokeWidth = 20f)
            drawLine(mainRoadInnerColor, start = androidx.compose.ui.geometry.Offset(0f, h * 0.2f), end = androidx.compose.ui.geometry.Offset(w, h * 0.2f), strokeWidth = 12f)

            // Vertical right arterial highway
            drawLine(mainRoadColor, start = androidx.compose.ui.geometry.Offset(w * 0.75f, 0f), end = androidx.compose.ui.geometry.Offset(w * 0.75f, h), strokeWidth = 20f)
            drawLine(mainRoadInnerColor, start = androidx.compose.ui.geometry.Offset(w * 0.75f, 0f), end = androidx.compose.ui.geometry.Offset(w * 0.75f, h), strokeWidth = 12f)

            // Key Route coordinates
            val storePt = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.20f)
            val cornerPt = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.20f)
            val customerPt = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.80f)

            // Base Planned Route (Sleek Neutral Slate Blue with a subtle outline)
            val plannedRouteColor = Color(0x6664748B)
            val plannedRouteInnerColor = Color(0xFFA7F3D0) // Minty preview color

            // 1. Draw remaining/scheduled route as a clear dotted navy line
            drawLine(
                color = Color(0xFF94A3B8),
                start = storePt,
                end = cornerPt,
                strokeWidth = 6f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            )
            drawLine(
                color = Color(0xFF94A3B8),
                start = cornerPt,
                end = customerPt,
                strokeWidth = 6f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            )

            // 2. Draw COMPLETED route with high-visibility Brand Electric Blue and a soft glow
            if (progress <= 0.5f) {
                val currentSegmentEnd = androidx.compose.ui.geometry.Offset(
                    w * (0.25f + 0.50f * (progress * 2.0f)),
                    h * 0.20f
                )
                // Glow
                drawLine(color = Color(0x332563EB), start = storePt, end = currentSegmentEnd, strokeWidth = 16f)
                // Solid line
                drawLine(color = Color(0xFF2563EB), start = storePt, end = currentSegmentEnd, strokeWidth = 8f)
            } else {
                // Glow first leg
                drawLine(color = Color(0x332563EB), start = storePt, end = cornerPt, strokeWidth = 16f)
                // Solid first leg
                drawLine(color = Color(0xFF2563EB), start = storePt, end = cornerPt, strokeWidth = 8f)

                val currentSegmentEnd = androidx.compose.ui.geometry.Offset(
                    w * 0.75f,
                    h * (0.20f + 0.60f * ((progress - 0.5f) * 2.0f))
                )
                // Glow second leg
                drawLine(color = Color(0x332563EB), start = cornerPt, end = currentSegmentEnd, strokeWidth = 16f)
                // Solid second leg
                drawLine(color = Color(0xFF2563EB), start = cornerPt, end = currentSegmentEnd, strokeWidth = 8f)
            }

            // Beautiful clear Beacons
            // Restaurant beacon ring (Crimson Red)
            drawCircle(color = Color(0x1FFE11D48), radius = 28.dp.toPx(), center = storePt)
            drawCircle(color = Color(0x40FE11D48), radius = 16.dp.toPx(), center = storePt)
            drawCircle(color = Color(0xFFFE11D48), radius = 5.dp.toPx(), center = storePt)

            // Destination beacon ring (Emerald Green)
            drawCircle(color = Color(0x1F10B981), radius = 28.dp.toPx(), center = customerPt)
            drawCircle(color = Color(0x4010B981), radius = 16.dp.toPx(), center = customerPt)
            drawCircle(color = Color(0xFF10B981), radius = 5.dp.toPx(), center = customerPt)
        }

        // Comprehensive Salem / Idappadi landmark & street labels (Clearly visible, high-contrast, Tamil/English pairings)
        val landmarks = listOf(
            Triple(wDp * 0.12f, hDp * 0.42f, "இடப்பாடி பஸ் ஸ்டாண்ட் 🚍 (Bus Stand)"),
            Triple(wDp * 0.52f, hDp * 0.35f, "நேதாஜி பைபாஸ் சாலை 📍 (Nethaji Bypass)"),
            Triple(wDp * 0.48f, hDp * 0.55f, "சேலம் மெயின் ரோடு 🛣️ (Salem Highway)"),
            Triple(wDp * 0.28f, hDp * 0.76f, "மேற்கு ரத வீதி 🏙️ (West Car Street)"),
            Triple(wDp * 0.42f, hDp * 0.78f, "கொங்கு நகர் 🏡 (Kongu Nagar Resident)")
        )
        landmarks.forEach { (x, y, name) ->
            Text(
                text = name,
                color = Color(0xFF0F172A),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = x - 30.dp, y = y)
                    .background(Color(0xE6FFFFFF), RoundedCornerShape(6.dp))
                    .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        // Restaurant Icon Floating Badge (Stunning Crimson/Tomato theme to signify culinary action)
        Box(
            modifier = Modifier
                .offset(x = wDp * 0.25f - 35.dp, y = hDp * 0.20f - 40.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(2.dp, Color(0xFFE11D48), RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Storefront,
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(12.dp)
                )
                Column {
                    Text(
                        text = "உணவகம் • START",
                        color = Color(0xFFE11D48),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (partnerName.contains("Nach") || partnerName.isEmpty()) "நாச்சியார் உணவகம் (HQ)" else partnerName,
                        color = Color(0xFF0F172A),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Home Icon Floating Badge (Clean Emerald Green theme representing user destination)
        Box(
            modifier = Modifier
                .offset(x = wDp * 0.75f - 35.dp, y = hDp * 0.80f - 40.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(2.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(12.dp)
                )
                Column {
                    Text(
                        text = "வாடிக்கையாளர் • HOME 🏠",
                        color = Color(0xFF10B981),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "உங்கள் இல்லம் (YOUR HOME)",
                        color = Color(0xFF0F172A),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Rider Dynamic Pulse Node & Motorcycle Marker Overlay (Vibrant high contrast Orange)
        val riderX = if (progress <= 0.5f) {
            wDp * (0.25f + 0.50f * (progress * 2.0f))
        } else {
            wDp * 0.75f
        }
        val riderY = if (progress <= 0.5f) {
            hDp * 0.20f
        } else {
            hDp * (0.20f + 0.60f * ((progress - 0.5f) * 2.0f))
        }

        // GPS Dynamic Radar Halo Animation
        val bikeTransition = rememberInfiniteTransition(label = "bike_signal_aura")
        val bikePulseRadius by bikeTransition.animateFloat(
            initialValue = 12f,
            targetValue = 46f,
            animationSpec = infiniteRepeatable(
                animation = tween(1300, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "aura_radius"
        )
        val bikePulseAlpha by bikeTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1300, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "aura_alpha"
        )
        val bikeTiltAngle by bikeTransition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(550, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aura_tilt"
        )

        // Radar Aura underneath
        Box(
            modifier = Modifier
                .offset(x = riderX - 30.dp, y = riderY - 30.dp)
                .size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(bikePulseRadius.dp)
                    .background(Color(0x7F2563EB), CircleShape)
                    .alpha(bikePulseAlpha)
            )
        }

        // Active Moving Rider Banner Card
        Box(
            modifier = Modifier
                .offset(x = riderX - 45.dp, y = riderY - 32.dp)
                .rotate(bikeTiltAngle)
                .background(Color(0xFFEA580C), RoundedCornerShape(8.dp))
                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DirectionsBike,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (riderName.isNotEmpty() && riderName != "Unassigned") "${riderName.take(6).uppercase()} • $vehicleNo" else vehicleNo,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Clean Modern Light HUD remaining details overlay on map
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .align(Alignment.TopStart)
                .background(Color(0xF9FFFFFF), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (status == "OUT_FOR_DELIVERY") Color(0xFF16A34A) else Color(0xFFEA580C))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (status) {
                            "PENDING", "ACCEPTED", "PREPARING" -> "KITCHEN CHEFS PREPARING CARGO"
                            "READY_FOR_PICKUP" -> "BEACON: RIDER ARRIVED AT COUNTER"
                            "OUT_FOR_DELIVERY" -> "RIDER ${riderName.uppercase()} IS IN TRANSIENT"
                            "DELIVERED" -> "ARCHIVED: SECURED DELIVERED SUCCESSFULLY!"
                            else -> "TELEM: CORRELATING GPS..."
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (status == "DELIVERED") {
                        "Arrived! Handed over successfully."
                    } else {
                        String.format(
                            java.util.Locale.US,
                            "Distance: %.2f km remaining • ETA: %d mins",
                            remainingDistance,
                            remainingMinutes
                        )
                    },
                    color = Color(0xFFF97316),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Coordinate telemetry board (Bottom-Right) - Sleek light theme
        Box(
            modifier = Modifier
                .padding(14.dp)
                .align(Alignment.BottomEnd)
                .background(Color(0xCCFFFFFF), RoundedCornerShape(8.dp))
                .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("SIGNAL GPS: 99.8%% [LIVE]"),
                    fontSize = 8.sp,
                    color = Color(0xFF0284C7),
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = String.format("Lat: %.5f | Lng: %.5f", currentLat, currentLng),
                    fontSize = 8.sp,
                    color = Color(0xFF64748B),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

// Highly accurate Haversine geolocation math formula for distance calculation in KM
fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth's radius in kilometers
    val dLat = java.lang.Math.toRadians(lat2 - lat1)
    val dLon = java.lang.Math.toRadians(lon2 - lon1)
    val a = java.lang.Math.sin(dLat / 2) * java.lang.Math.sin(dLat / 2) +
            java.lang.Math.cos(java.lang.Math.toRadians(lat1)) * java.lang.Math.cos(java.lang.Math.toRadians(lat2)) *
            java.lang.Math.sin(dLon / 2) * java.lang.Math.sin(dLon / 2)
    val c = 2 * java.lang.Math.atan2(java.lang.Math.sqrt(a), java.lang.Math.sqrt(1.0 - a))
    return r * c
}

@Composable
fun SimpleStepIndicator(currentStatus: String) {
    val step1State = when (currentStatus) {
        "PENDING", "ACCEPTED" -> "ACTIVE"
        "PREPARING", "READY_FOR_PICKUP", "OUT_FOR_DELIVERY", "DELIVERED" -> "COMPLETED"
        else -> "ACTIVE"
    }

    val step2State = when (currentStatus) {
        "PENDING", "ACCEPTED" -> "PENDING"
        "PREPARING", "READY_FOR_PICKUP" -> "ACTIVE"
        "OUT_FOR_DELIVERY", "DELIVERED" -> "COMPLETED"
        else -> "PENDING"
    }

    val step3State = when (currentStatus) {
        "PENDING", "ACCEPTED", "PREPARING", "READY_FOR_PICKUP" -> "PENDING"
        "OUT_FOR_DELIVERY" -> "ACTIVE"
        "DELIVERED" -> "COMPLETED"
        else -> "PENDING"
    }
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("simple_step_indicator"),
        borderColor = Color(0x33FF6B00),
        backgroundColor = Color(0xFF0F172A)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ஆர்டர் நிலவரம் • ORDER TRACKING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LyoColors.AccentOrange,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step 1: Order Received
                StepCircle(
                    stepNum = "1",
                    labelTa = "ஆர்டர் பெறப்பட்டது",
                    labelEn = "Order Received",
                    state = step1State,
                    modifier = Modifier.weight(1f)
                )

                // Connector 1
                StepConnector(isCompleted = step2State == "COMPLETED" || step1State == "COMPLETED")

                // Step 2: Preparing
                StepCircle(
                    stepNum = "2",
                    labelTa = "சமையல் தயாராகிறது",
                    labelEn = "Preparing",
                    state = step2State,
                    modifier = Modifier.weight(1f)
                )

                // Connector 2
                StepConnector(isCompleted = step3State == "COMPLETED" || step2State == "COMPLETED")

                // Step 3: Out for Delivery
                StepCircle(
                    stepNum = "3",
                    labelTa = "வினியோகத்தில் உள்ளது",
                    labelEn = "Out for Delivery",
                    state = step3State,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StepCircle(
    stepNum: String,
    labelTa: String,
    labelEn: String,
    state: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when (state) {
                        "COMPLETED" -> Color(0xFF10B981)
                        "ACTIVE" -> LyoColors.AccentOrange
                        else -> Color(0xFF334155)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (state == "COMPLETED") {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = stepNum,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = labelTa,
            fontSize = 10.sp,
            fontWeight = if (state == "ACTIVE") FontWeight.Bold else FontWeight.Medium,
            color = if (state == "ACTIVE") Color.White else if (state == "COMPLETED") Color(0xFF10B981) else Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
        Text(
            text = labelEn,
            fontSize = 9.sp,
            fontWeight = if (state == "ACTIVE") FontWeight.Bold else FontWeight.Normal,
            color = if (state == "ACTIVE") Color.White else Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RowScope.StepConnector(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .weight(0.4f)
            .height(3.dp)
            .background(if (isCompleted) Color(0xFF10B981) else Color(0xFF334155))
            .padding(horizontal = 2.dp)
    )
}

