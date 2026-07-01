package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Category
import com.example.data.database.MenuItem
import com.example.data.database.Vendor
import com.example.data.database.User
import com.example.data.database.PromoBanner
import com.example.ui.viewmodels.AdminViewModel
import kotlinx.coroutines.flow.Flow
import com.example.data.repository.LyoFirebaseHelper

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogoutClick: () -> Unit
) {
    val vendors by viewModel.allVendors.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val selectedVendor by viewModel.selectedAdminVendor.collectAsState()

    val riders by viewModel.allRiders.collectAsState()
    val activeRides by viewModel.activeDeliveryRides.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val admins by viewModel.allAdmins.collectAsState()

    val scope = rememberCoroutineScope()



    var showAssignRiderOrderId by remember { mutableStateOf<Long?>(null) }
    var showDispatchConfirmDialog by remember { mutableStateOf(false) }
    var dispatchOrderState by remember { mutableStateOf<com.example.data.database.Order?>(null) }
    var dispatchVendorState by remember { mutableStateOf<com.example.data.database.Vendor?>(null) }
    var dispatchOrderItemsState by remember { mutableStateOf<List<com.example.data.database.OrderItem>>(emptyList()) }
    var dispatchRiderNameState by remember { mutableStateOf("") }
    var dispatchRiderPhoneState by remember { mutableStateOf("") }
    var confirmRiderToAssign by remember { mutableStateOf<com.example.data.database.User?>(null) }
    var confirmStatusChangeOrder by remember { mutableStateOf<Pair<com.example.data.database.Order, String>?>(null) }

    if (confirmStatusChangeOrder != null) {
        val (ord, status) = confirmStatusChangeOrder!!
        val statusTamil = when (status) {
            "ACCEPTED" -> "ஏற்கப்பட்டது (ACCEPTED)"
            "PREPARING" -> "சமையலறையில் தயாரிக்கப்படுகிறது (PREPARING)"
            "READY_FOR_PICKUP" -> "விநியோகிக்க தயாராக உள்ளது (READY FOR PICKUP)"
            else -> status
        }
        AlertDialog(
            onDismissRequest = { confirmStatusChangeOrder = null },
            title = {
                Text(
                    text = "நிலை மாற்றம் (Confirm Status Change)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "ஆர்டர் #${ord.id}-ன் நிலையை '$statusTamil' என மாற்ற விரும்புகிறீர்களா?\n\nAre you sure you want to change the status of Order #${ord.id} to '$status'?",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateOrderStatus(ord.id, status)
                        confirmStatusChangeOrder = null
                    }
                ) {
                    Text("YES / CONFIRM", color = LyoColors.AccentOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmStatusChangeOrder = null }) {
                    Text("CANCEL", color = LyoColors.TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    var selectedImageUriStr by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = java.io.File(context.filesDir, "media_${System.currentTimeMillis()}.jpg")
                    val outputStream = java.io.FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    selectedImageUriStr = file.absolutePath
                } else {
                    selectedImageUriStr = uri.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                selectedImageUriStr = uri.toString()
            }
        }
    }

    var activeTab by remember { mutableStateOf("ANALYTICS") } // ANALYTICS, VENDORS, LOGISTICS, ONBOARDING

    val newVendorName by viewModel.newVendorName.collectAsState()
    val newVendorNameTa by viewModel.newVendorNameTa.collectAsState()
    val newVendorPhone by viewModel.newVendorPhone.collectAsState()
    val newVendorType by viewModel.newVendorType.collectAsState()
    val newVendorAddress by viewModel.newVendorAddress.collectAsState()
    val newVendorLat by viewModel.newVendorLat.collectAsState()
    val newVendorLng by viewModel.newVendorLng.collectAsState()
    val newVendorFee by viewModel.newVendorDeliveryFee.collectAsState()
    val minOrderVal by viewModel.newVendorMinThreshold.collectAsState()
    val freeDelVal by viewModel.newVendorFreeThreshold.collectAsState()
    val newVendorBannerUrl by viewModel.newVendorBannerUrl.collectAsState()
    val isOnboarding by viewModel.isOnboarding.collectAsState()

    LaunchedEffect(selectedImageUriStr) {
        selectedImageUriStr?.let { uri ->
            if (activeTab == "ONBOARDING") {
                viewModel.newVendorBannerUrl.value = uri
                selectedImageUriStr = null
            }
        }
    }

    val onboardingZones = listOf(
        Triple("Idappadi Bus Stand Ring Road", 11.5812, 77.8465),
        Triple("Salem New Bus Stand Junction", 11.6687, 78.1172),
        Triple("Salem Bypass Outer Bypass Road", 11.5835, 77.8442),
        Triple("Sankagiri Main Road, Idappadi", 11.5714, 77.8395),
        Triple("Magudanchavadi High Road, Salem", 11.5801, 77.8490)
    )

    LyoBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding()
        ) {
            // Header Operations Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONSOLE PORTAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = LyoColors.AccentOrange,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Lyo Supply Administration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF))
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = "logout", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1FFFFFFF))
                    .padding(4.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                val tabs = mutableListOf(
                    "ANALYTICS" to "Analytics",
                    "SMART_MENU" to "Smart Menu 🤖",
                    "VENDORS" to "Stores",
                    "BANNERS" to "Banners",
                    "LOGISTICS" to "Orders",
                    "ONBOARDING" to "+ Store",
                    "RIDERS" to "Riders",
                    "CUSTOMERS" to "Customers",
                    "FIREBASE" to "Firebase"
                )
                if (currentUser?.phone == "Anantharajmech") {
                    tabs.add("ADMINS" to "Admins Panel")
                }
                tabs.forEach { (tabId, label) ->
                    val isSel = activeTab == tabId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) LyoColors.AccentOrange else Color.Transparent)
                            .clickable {
                                activeTab = tabId
                                viewModel.selectedAdminVendor.value = null
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (isSel) Color.White else LyoColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab View Contents
            when (activeTab) {
                "ANALYTICS" -> {
                    AnalyticsDashboardTab(viewModel = viewModel)
                }

                "SMART_MENU" -> {
                    SmartMenuManagerTab(viewModel = viewModel)
                }

                "BANNERS" -> {
                    BannersManagementTab(
                        viewModel = viewModel,
                        onBrowsePhoto = { galleryLauncher.launch("image/*") },
                        selectedImageUriStr = if (activeTab == "BANNERS") selectedImageUriStr else null,
                        onClearSelectedImage = { selectedImageUriStr = null }
                    )
                }

                "ADMINS" -> {
                    AdminsManagementTab(viewModel = viewModel)
                }

                "CUSTOMERS" -> {
                    CustomersManagementTab(viewModel = viewModel)
                }

                "FIREBASE" -> {
                    FirebaseSettingsTab(viewModel = viewModel)
                }

                "RIDERS" -> {
                    RidersManagementTab(viewModel = viewModel)
                }

                "ONBOARDING" -> {
                    // DYNAMIC VENDOR ONBOARDING MAP DROPPIN
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Onboard Premium Merchant",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                OutlinedTextField(
                                    value = newVendorName,
                                    onValueChange = { viewModel.newVendorName.value = it },
                                    label = { Text("Vendor Display Name (English)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = newVendorNameTa,
                                    onValueChange = { viewModel.newVendorNameTa.value = it },
                                    label = { Text("Vendor Name (Tamil)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = newVendorPhone,
                                    onValueChange = { viewModel.newVendorPhone.value = it },
                                    label = { Text("Vendor Mobile / WhatsApp Number") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Dropdown selector for categories
                                Text("Vendor Classification Type:", color = LyoColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val customTypesMap by viewModel.customBusinessTypes.collectAsState()
                                    val types = remember(customTypesMap) { customTypesMap.keys.toList() }
                                    var showOnboardingCustomTypeDialog by remember { mutableStateOf(false) }

                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        items(types, key = { it }) { t ->
                                            val isSelected = newVendorType == t
                                            val emoji = customTypesMap[t] ?: "🏪"
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 6.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) LyoColors.AccentOrange else Color(0x1Fffffff))
                                                    .clickable { viewModel.newVendorType.value = t }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("$emoji $t", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0x1F22C55E))
                                                    .border(1.dp, Color(0x6622C55E), RoundedCornerShape(8.dp))
                                                    .clickable { showOnboardingCustomTypeDialog = true }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("➕ Add Custom (புதிய வகை)", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (showOnboardingCustomTypeDialog) {
                                        var customTypeName by remember { mutableStateOf("") }
                                        var customTypeEmoji by remember { mutableStateOf("🏪") }
                                        androidx.compose.material3.AlertDialog(
                                            onDismissRequest = { showOnboardingCustomTypeDialog = false },
                                            title = { Text("Add Custom Business Type (புதிய வகை)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                            text = {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customTypeName,
                                                        onValueChange = { customTypeName = it },
                                                        label = { Text("Type Name (e.g. Tea, Bakery, Mess)", color = Color.Gray) },
                                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Text("Select Emoji Icon:", color = Color.Gray, fontSize = 11.sp)
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        listOf("🏪", "🍵", "🍳", "🥤", "🌾", "🍿", "🍗", "🥣", "🧁", "🐟", "🌶️").forEach { em ->
                                                            val isSelected = customTypeEmoji == em
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(36.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) LyoColors.AccentOrange else Color(0x1Fffffff))
                                                                    .clickable { customTypeEmoji = em },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(em, fontSize = 18.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            confirmButton = {
                                                androidx.compose.material3.Button(
                                                    onClick = {
                                                        if (customTypeName.isNotBlank()) {
                                                            viewModel.addCustomBusinessType(customTypeName, customTypeEmoji)
                                                            viewModel.newVendorType.value = customTypeName
                                                            showOnboardingCustomTypeDialog = false
                                                        }
                                                    },
                                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange)
                                                ) {
                                                    Text("ADD (சேர்)", color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            dismissButton = {
                                                androidx.compose.material3.TextButton(onClick = { showOnboardingCustomTypeDialog = false }) {
                                                    Text("CANCEL", color = Color.LightGray)
                                                }
                                            },
                                            containerColor = Color(0xFF0F172A),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // INTERACTIVE MAP DROPPIN SIMULATOR GEOFENCE
                                Text(
                                    text = "📍 CHOOSE EXACT GEOGRAPHICAL LOCATION ON MAP",
                                    color = LyoColors.AccentOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                        .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    var showNewVendorMapDialog by remember { mutableStateOf(false) }
                                    var latInputStr by remember(newVendorLat) { 
                                        mutableStateOf(if (newVendorLat == 0.0) "" else String.format(java.util.Locale.US, "%.6f", newVendorLat)) 
                                    }
                                    var lngInputStr by remember(newVendorLng) { 
                                        mutableStateOf(if (newVendorLng == 0.0) "" else String.format(java.util.Locale.US, "%.6f", newVendorLng)) 
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = latInputStr,
                                            onValueChange = { newVal ->
                                                latInputStr = newVal
                                                newVal.toDoubleOrNull()?.let { viewModel.newVendorLat.value = it }
                                            },
                                            label = { Text("Latitude", fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = lngInputStr,
                                            onValueChange = { newVal ->
                                                lngInputStr = newVal
                                                newVal.toDoubleOrNull()?.let { viewModel.newVendorLng.value = it }
                                            },
                                            label = { Text("Longitude", fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { showNewVendorMapDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LyoColors.AccentOrange,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = "Map Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "வரைபடத்தில் இடத்தை தேர்வு செய் (Open Map)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (showNewVendorMapDialog) {
                                         Lyo3DDialog(onDismissRequest = { showNewVendorMapDialog = false }) {
                                             Column(modifier = Modifier.fillMaxWidth().height(480.dp)) {
                                                 Row(
                                                     modifier = Modifier.fillMaxWidth(),
                                                     horizontalArrangement = Arrangement.SpaceBetween,
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Text(
                                                         text = "🗺️ Pick Location on Map\n(வரைபடத்தில் லொகேஷனை தேர்வு செய்யவும்)",
                                                         fontSize = 12.sp,
                                                         fontWeight = FontWeight.Bold,
                                                         color = Color.White,
                                                         lineHeight = 16.sp,
                                                         modifier = Modifier.weight(1f)
                                                     )
                                                     IconButton(
                                                         onClick = { showNewVendorMapDialog = false },
                                                         modifier = Modifier.size(28.dp).background(Color(0x1Fffffff), CircleShape)
                                                     ) {
                                                         Icon(
                                                             imageVector = Icons.Filled.Close,
                                                             contentDescription = "Close",
                                                             tint = Color.White,
                                                             modifier = Modifier.size(16.dp)
                                                         )
                                                     }
                                                 }
                                                 Spacer(modifier = Modifier.height(8.dp))
                                                 Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                                                     InteractiveMapPickerView(
                                                         initialLat = if (newVendorLat != 0.0) newVendorLat else 11.5812,
                                                         initialLng = if (newVendorLng != 0.0) newVendorLng else 77.8465,
                                                         onLocationPicked = { pickedLat, pickedLng ->
                                                             viewModel.lockCoordinates("Geocoding...", pickedLat, pickedLng)
                                                             scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                 try {
                                                                     val url = java.net.URL("https://nominatim.openstreetmap.org/reverse?lat=${pickedLat}&lon=${pickedLng}&format=json")
                                                                     val conn = url.openConnection() as java.net.HttpURLConnection
                                                                     conn.requestMethod = "GET"
                                                                     conn.setRequestProperty("User-Agent", "LyoFreshClient/1.0 (Android; dev-agent)")
                                                                     conn.connectTimeout = 3000
                                                                     conn.readTimeout = 3000
                                                                     if (conn.responseCode == 200) {
                                                                         val text = conn.inputStream.bufferedReader().use { it.readText() }
                                                                         val json = org.json.JSONObject(text)
                                                                         val addressObj = json.optJSONObject("address")
                                                                         val road = addressObj?.optString("road") ?: ""
                                                                         val suburb = addressObj?.optString("suburb") ?: addressObj?.optString("village") ?: addressObj?.optString("town") ?: ""
                                                                         val county = addressObj?.optString("county") ?: addressObj?.optString("city") ?: ""
                                                                         var shortAddr = listOf(road, suburb, county).filter { it.isNotBlank() }.joinToString(", ")
                                                                         if (shortAddr.isBlank()) {
                                                                             shortAddr = json.optString("display_name", "").take(50)
                                                                         }
                                                                         if (shortAddr.isBlank()) {
                                                                             shortAddr = "Salem Area Pin"
                                                                         }
                                                                         kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                             viewModel.newVendorAddress.value = shortAddr
                                                                         }
                                                                     }
                                                                 } catch (e: Exception) {
                                                                     e.printStackTrace()
                                                                 }
                                                             }
                                                         }
                                                     )
                                                 }
                                                 Spacer(modifier = Modifier.height(12.dp))
                                                 LyoButton(
                                                     text = "Confirm Location (உறுதிசெய்)",
                                                     onClick = { showNewVendorMapDialog = false },
                                                     modifier = Modifier.fillMaxWidth()
                                                 )
                                             }
                                         }
                                     }

                                     /*
                                     Text("Simulated Interactive Location Pin-Drop:", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .height(180.dp)
                                             .clip(RoundedCornerShape(10.dp))
                                             .background(Color(0xFF090D16))
                                             .border(1.dp, Color(0x33F8FAFC), RoundedCornerShape(10.dp))
                                             .pointerInput(Unit) {
                                                 detectTapGestures { offset ->
                                                     val pctX = offset.x / size.width
                                                     val pctY = offset.y / size.height

                                                     // Interpolate between Idappadi Salem coordinates boundaries
                                                     val latRange = 11.6000 - 11.5700
                                                     val lngRange = 77.8700 - 77.8300
                                                     val computedLat = 11.6000 - (pctY * latRange)
                                                     val computedLng = 77.8300 + (pctX * lngRange)

                                                     viewModel.lockCoordinates(
                                                         "Geocoding...",
                                                         computedLat,
                                                         computedLng
                                                     )

                                                     scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                         try {
                                                             val url = java.net.URL("https://nominatim.openstreetmap.org/reverse?lat=${computedLat}&lon=${computedLng}&format=json")
                                                             val conn = url.openConnection() as java.net.HttpURLConnection
                                                             conn.requestMethod = "GET"
                                                             conn.setRequestProperty("User-Agent", "LyoFreshClient/1.0 (Android; dev-agent)")
                                                             conn.connectTimeout = 3000
                                                             conn.readTimeout = 3000
                                                             if (conn.responseCode == 200) {
                                                                 val text = conn.inputStream.bufferedReader().use { it.readText() }
                                                                 val json = org.json.JSONObject(text)
                                                                 val addressObj = json.optJSONObject("address")
                                                                 val road = addressObj?.optString("road") ?: ""
                                                                 val suburb = addressObj?.optString("suburb") ?: addressObj?.optString("village") ?: addressObj?.optString("town") ?: ""
                                                                 val county = addressObj?.optString("county") ?: addressObj?.optString("city") ?: ""
                                                                 var shortAddr = listOf(road, suburb, county).filter { it.isNotBlank() }.joinToString(", ")
                                                                 if (shortAddr.isBlank()) {
                                                                     shortAddr = json.optString("display_name", "").take(50)
                                                                 }
                                                                 if (shortAddr.isBlank()) {
                                                                     shortAddr = "Salem Idappadi District"
                                                                 }
                                                                 kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                     viewModel.newVendorAddress.value = shortAddr
                                                                 }
                                                             } else {
                                                                 kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                     viewModel.newVendorAddress.value = "Salem Area Pin (${String.format(java.util.Locale.US, "%.4f", computedLat)}, ${String.format(java.util.Locale.US, "%.4f", computedLng)})"
                                                                 }
                                                             }
                                                         } catch (e: Exception) {
                                                             e.printStackTrace()
                                                             kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                 viewModel.newVendorAddress.value = "Salem Area Pin (${String.format(java.util.Locale.US, "%.4f", computedLat)}, ${String.format(java.util.Locale.US, "%.4f", computedLng)})"
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                     ) {
                                         // Draw Map visual elements using Canvas
                                         Canvas(modifier = Modifier.fillMaxSize()) {
                                             // Roads (Main and side streets)
                                             drawLine(color = Color(0x4438BDF8), start = Offset(0f, size.height * 0.35f), end = Offset(size.width, size.height * 0.35f), strokeWidth = 8f)
                                              // 1. Satellite Base Terrain (Dark Green/Forest tone)
                                              drawRect(color = Color(0xFF0F2610))

                                              // 2. Agricultural field clusters
                                              drawRect(color = Color(0xFF1E501F), topLeft = Offset(10f, 10f), size = Size(size.width * 0.3f, size.height * 0.4f))
                                              drawRect(color = Color(0xFF246125), topLeft = Offset(size.width * 0.7f, 15f), size = Size(size.width * 0.25f, size.height * 0.35f))
                                              drawRect(color = Color(0xFF19441A), topLeft = Offset(size.width * 0.05f, size.height * 0.65f), size = Size(size.width * 0.35f, size.height * 0.3f))

                                              // 3. Huge Sivagiri Thirumalai Hills (Charcoal and Shaded Brown contours)
                                              drawCircle(color = Color(0xFF32281D), radius = 45.dp.toPx(), center = Offset(size.width * 0.15f, size.height * 0.25f))
                                              drawCircle(color = Color(0xFF221A11), radius = 30.dp.toPx(), center = Offset(size.width * 0.15f, size.height * 0.25f))
                                              drawCircle(color = Color(0xFF150F07), radius = 15.dp.toPx(), center = Offset(size.width * 0.15f, size.height * 0.25f))

                                              // 4. Suriya Malai (Hills) contour on the bottom-left
                                              drawCircle(color = Color(0xFF2B2118), radius = 35.dp.toPx(), center = Offset(size.width * 0.82f, size.height * 0.22f))
                                              drawCircle(color = Color(0xFF1D150E), radius = 20.dp.toPx(), center = Offset(size.width * 0.82f, size.height * 0.22f))

                                              // 5. Deep Emerald Lake/Canal (பெரிய ஏரி - Big Lake)
                                              drawCircle(color = Color(0xFF113045), radius = 42.dp.toPx(), center = Offset(size.width * 0.8f, size.height * 0.75f))
                                              drawCircle(color = Color(0xFF1A4660), radius = 28.dp.toPx(), center = Offset(size.width * 0.8f, size.height * 0.75f))
                                              drawCircle(color = Color(0xFF2E6381), radius = 14.dp.toPx(), center = Offset(size.width * 0.8f, size.height * 0.75f))

                                              // 6. Primary Dual-Lane Asphalt Highway
                                              drawLine(color = Color(0xFF1D2022), start = Offset(0f, size.height * 0.5f), end = Offset(size.width, size.height * 0.5f), strokeWidth = 14.dp.toPx())
                                              drawLine(color = Color(0xFFE5B107), start = Offset(0f, size.height * 0.5f - 4f), end = Offset(size.width, size.height * 0.5f - 4f), strokeWidth = 3f)
                                              drawLine(color = Color(0xFFE5B107), start = Offset(0f, size.height * 0.5f + 4f), end = Offset(size.width, size.height * 0.5f + 4f), strokeWidth = 3f)

                                              // 7. Bazaar Street / Shop Street (கடைவீதி)
                                              drawLine(color = Color(0xFF2A2E33), start = Offset(size.width * 0.45f, 0f), end = Offset(size.width * 0.45f, size.height), strokeWidth = 10.dp.toPx())

                                              // 8. Named Alleys & Santhukkal (Dotted white/grey paths)
                                              val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                              drawLine(color = Color(0xFF7F8C8D), start = Offset(size.width * 0.45f, size.height * 0.25f), end = Offset(0f, size.height * 0.25f), strokeWidth = 2.dp.toPx(), pathEffect = dashEffect)
                                              drawLine(color = Color(0xFF7F8C8D), start = Offset(size.width * 0.45f, size.height * 0.75f), end = Offset(size.width, size.height * 0.75f), strokeWidth = 2.dp.toPx(), pathEffect = dashEffect)
                                              drawLine(color = Color(0xFF7F8C8D), start = Offset(size.width * 0.2f, size.height * 0.5f), end = Offset(size.width * 0.2f, size.height), strokeWidth = 2.dp.toPx(), pathEffect = dashEffect)
                                             // (replaced by satellite map)
                                              //
                                             // Replaced by satellite
                                              //
                                             // Replaced by satellite
                                              //

                                             // Park
                                             // Replaced by satellite park
                                              //

                                             // Lake/River
                                             // Replaced by satellite big lake
                                              //
                                         }

                                         // Beautiful Bilingual Labels Overlay
                                         Box(modifier = Modifier.fillMaxSize()) {
                                              Text(
                                                  text = "⛰️ சிவகிரி திருமலை\n(Sivagiri Hill)",
                                                  color = Color(0xFFFFCCAA),
                                                  fontSize = 7.sp,
                                                  lineHeight = 9.sp,
                                                  fontWeight = FontWeight.Bold,
                                                  modifier = Modifier.align(Alignment.TopStart).offset(x = 12.dp, y = 14.dp)
                                              )
                                              Text(
                                                  text = "⛰️ சூரிய மலை\n(Suriya Hill)",
                                                  color = Color(0xFFFFCCAA),
                                                  fontSize = 7.sp,
                                                  fontWeight = FontWeight.Bold,
                                                  modifier = Modifier.align(Alignment.TopEnd).offset(x = (-10).dp, y = 14.dp)
                                              )
                                              Text(
                                                  text = "🛒 இடப்பாடி கடைவீதி\n(Kadaiveethi)",
                                                  color = Color.White,
                                                  fontSize = 7.sp,
                                                  lineHeight = 9.sp,
                                                  fontWeight = FontWeight.Black,
                                                  modifier = Modifier.align(Alignment.TopCenter).offset(x = (-30).dp, y = 45.dp)
                                              )
                                              Text(
                                                  text = "🛣️ சேலம் மெயின் ரோடு [SH-17]",
                                                  color = Color(0xFFF39C12),
                                                  fontSize = 7.sp,
                                                  fontWeight = FontWeight.Bold,
                                                  modifier = Modifier.align(Alignment.CenterStart).offset(x = 10.dp, y = 15.dp)
                                              )
                                              Text(
                                                  text = "🌊 பெரிய ஏரி (Lake)",
                                                  color = Color(0xFF81D4FA),
                                                  fontSize = 7.sp,
                                                  fontWeight = FontWeight.Bold,
                                                  modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-15).dp, y = (-25).dp)
                                              )
                                              Text(
                                                  text = "◽ கணேஷ் சந்து (Ganesh Lane)",
                                                  color = Color.LightGray,
                                                  fontSize = 6.sp,
                                                  modifier = Modifier.align(Alignment.BottomStart).offset(x = 12.dp, y = (-20).dp)
                                              )
                                              Text(
                                                  text = "◽ பழைய பஸ் நிலையம் (Bus Stand)",
                                                  color = Color.LightGray,
                                                  fontSize = 6.sp,
                                                  modifier = Modifier.align(Alignment.BottomCenter).offset(x = 60.dp, y = (-55).dp)
                                              )
                                          }

                                         // Text reminder
                                         Text(
                                              text = "🛰️ IDAPPADI HYPERLOCAL MAP (Tap anywhere to drop pin marker)",
                                             color = Color.White.copy(alpha = 0.5f),
                                             fontSize = 9.sp,
                                             fontWeight = FontWeight.Bold,
                                             modifier = Modifier.align(Alignment.BottomCenter).padding(6.dp)
                                         )

                                         // Custom Pin Marker Positioned
                                         val actLat = newVendorLat
                                         val actLng = newVendorLng

                                         // Normalize position based on boundaries [11.5700, 11.6000] & [77.8300, 77.8700]
                                         val normX = if (actLng in 77.8300..77.8700) {
                                             ((actLng - 77.8300) / (77.8700 - 77.8300)).toFloat()
                                         } else {
                                             0.45f
                                         }
                                         val normY = if (actLat in 11.5700..11.6000) {
                                             ((11.6000 - actLat) / (11.6000 - 11.5700)).toFloat()
                                         } else {
                                             0.45f
                                         }

                                         // Place the custom glowing pin drop
                                         Box(
                                             modifier = Modifier
                                                 .align(Alignment.TopStart)
                                                 .offset(
                                                     x = (normX * 280).dp,
                                                     y = (normY * 130).dp
                                                 )
                                                 .size(30.dp),
                                             contentAlignment = Alignment.Center
                                         ) {
                                             Icon(
                                                 imageVector = Icons.Filled.PinDrop,
                                                 contentDescription = "merchant dropped mark",
                                                 tint = LyoColors.AccentOrange,
                                                 modifier = Modifier.size(24.dp)
                                             )
                                         }
                                     }

                                     Spacer(modifier = Modifier.height(14.dp))
                                     */
                                     Text("Manual Geofence Quick Zones:", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                     Spacer(modifier = Modifier.height(6.dp))

                                     onboardingZones.forEach { (zoneAddress, lt, ln) ->
                                        val isThisOne = newVendorLat == lt && newVendorLng == ln
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isThisOne) Color(0x22F97316) else Color.Transparent)
                                                .clickable {
                                                    viewModel.lockCoordinates(zoneAddress, lt, ln)
                                                }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isThisOne) Icons.Filled.PinDrop else Icons.Filled.LocationOn,
                                                contentDescription = "pin",
                                                tint = if (isThisOne) LyoColors.AccentOrange else LyoColors.TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(zoneAddress, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = newVendorAddress,
                                    onValueChange = { viewModel.newVendorAddress.value = it },
                                    label = { Text("Physical Base Address") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Threshold and Delivery configurations
                                Text("Premium Logistics Settings:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = newVendorFee,
                                        onValueChange = { viewModel.newVendorDeliveryFee.value = it },
                                        label = { Text("Base Deliver Charge (₹)") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = minOrderVal,
                                        onValueChange = { viewModel.newVendorMinThreshold.value = it },
                                        label = { Text("Min Order (₹)") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = freeDelVal,
                                    onValueChange = { viewModel.newVendorFreeThreshold.value = it },
                                    label = { Text("Free Logistics Threshold Cap (₹)") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                val targetRadius by viewModel.newVendorVisibilityRadius.collectAsState()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "STORE VISIBILITY RADIUS",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "இந்தக் கடையின் மேப் விசிபிலிட்டி ரேடியஸ்",
                                            color = LyoColors.AccentOrange,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.1f", targetRadius)} KM",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Slider(
                                    value = targetRadius.toFloat(),
                                    onValueChange = { viewModel.newVendorVisibilityRadius.value = it.toDouble() },
                                    valueRange = 1.0f..100.0f,
                                    steps = 99,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF38BDF8),
                                        activeTrackColor = Color(0xFF38BDF8),
                                        inactiveTrackColor = Color(0x33FFFFFF)
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Premium Custom Image Cover Setup
                                Text("Premium Custom Image Cover Setup:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F38BDF8)),
                                        border = BorderStroke(1.dp, Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("CHOOSE REAL PHOTO FROM GALLERY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (newVendorBannerUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth().height(120.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            val painterModel = remember(newVendorBannerUrl) {
                                                if (newVendorBannerUrl.startsWith("/")) java.io.File(newVendorBannerUrl) else newVendorBannerUrl
                                            }
                                            androidx.compose.foundation.Image(
                                                painter = coil.compose.rememberAsyncImagePainter(painterModel),
                                                contentDescription = "selected photo",
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            IconButton(
                                                onClick = { viewModel.newVendorBannerUrl.value = "" },
                                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            ) {
                                                Icon(Icons.Filled.Close, contentDescription = "clear custom image", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = newVendorBannerUrl,
                                    onValueChange = { viewModel.newVendorBannerUrl.value = it },
                                    label = { Text("Or Paste Real Restaurant Photo Image URL") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 14.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                LyoButton(
                                    text = if (isOnboarding) "ONBOARDING MERCHANT..." else "ONBOARD MERCHANT ON-GROUND",
                                    onClick = {
                                        viewModel.onboardVendor {
                                            activeTab = "VENDORS"
                                        }
                                    },
                                    enabled = !isOnboarding,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                "LOGISTICS" -> {
                    // LIVE ORDER EVENTS MONITORING - DISPATCH COORDINATION
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        item {
                            Text(
                                text = "SYSTEM ORDER TRAFFIC (${orders.size} active)",
                                color = LyoColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        if (orders.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("No orders placed yet across user dashboards.", color = LyoColors.TextSecondary)
                                }
                            }
                        }

                        items(orders, key = { it.id }) { ord ->
                            val matchedRide = activeRides.firstOrNull { it.orderId == ord.id }
                            Box(modifier = Modifier.padding(vertical = 8.dp)) {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    borderColor = if (ord.status == "PENDING") LyoColors.AccentOrange else Color(0x33F8FAFC)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Order ID: #LYO-${ord.id}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text("From customer phone: ${ord.userId}", color = LyoColors.TextSecondary, fontSize = 12.sp)
                                                Text("Store: ${ord.vendorName}", color = LyoColors.TextSecondary, fontSize = 12.sp)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        when (ord.status) {
                                                            "DELIVERED" -> Color(0x3322C55E)
                                                            "PENDING" -> Color(0x33F97316)
                                                            else -> Color(0x3338BDF8)
                                                        }
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = ord.status,
                                                    fontSize = 11.sp,
                                                    color = when (ord.status) {
                                                        "DELIVERED" -> LyoColors.VegGreen
                                                        "PENDING" -> LyoColors.AccentOrange
                                                        else -> Color(0xFF38BDF8)
                                                    },
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Divider(color = Color(0x1affffff), modifier = Modifier.padding(vertical = 10.dp))

                                        Text(
                                            text = "Total amount: ₹${ord.totalAmount.toInt()} (Tip included: ₹${ord.tipAmount.toInt()})",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // RIDER DISPATCH CONTROL PANEL
                                        if (matchedRide != null) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0x1122C55E), RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0x3322C55E), RoundedCornerShape(8.dp))
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.DirectionsBike,
                                                    contentDescription = null,
                                                    tint = LyoColors.VegGreen,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Rider: ${matchedRide.riderName}",
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Text(
                                                        text = "Phone: ${matchedRide.riderPhone} | Est. Pay: ₹${matchedRide.earnings.toInt()}",
                                                        color = LyoColors.TextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                    Text(
                                                        text = "Dispatcher Step: ${matchedRide.status}",
                                                        color = LyoColors.VegGreen,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Button(
                                                    onClick = { showAssignRiderOrderId = ord.id },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF6B00)),
                                                    border = BorderStroke(1.dp, LyoColors.AccentOrange),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("CHANGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LyoColors.AccentOrange)
                                                }
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0x11EF4444), RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0x33EF4444), RoundedCornerShape(8.dp))
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Rider Status: UNASSIGNED (ஒதுக்கப்படவில்லை)",
                                                        color = Color(0xFFFCA5A5),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "Assign a fleet delivery agent to trigger instant WhatsApp KOT & Invoice dispatch.",
                                                        color = LyoColors.TextSecondary,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                
                                                Button(
                                                    onClick = { showAssignRiderOrderId = ord.id },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Add,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("ASSIGN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Divider(color = Color(0x1affffff), modifier = Modifier.padding(vertical = 10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (ord.status != "DELIVERED") {
                                                val nextStatus = when (ord.status) {
                                                    "PENDING" -> "ACCEPTED"
                                                    "ACCEPTED" -> "PREPARING"
                                                    "PREPARING" -> "READY_FOR_PICKUP"
                                                    else -> "READY_FOR_PICKUP"
                                                }
                                                if (ord.status != "READY_FOR_PICKUP" && ord.status != "OUT_FOR_DELIVERY") {
                                                    Button(
                                                        onClick = {
                                                            confirmStatusChangeOrder = ord to nextStatus
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("MARK $nextStatus", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Text(
                                                        text = "Rider Transit Active",
                                                        color = LyoColors.TextSecondary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            } else {
                                                Text("Completed Cargo Secured", color = LyoColors.VegGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Professional PDF Invoice & WhatsApp generation block
                                        val invoiceContext = androidx.compose.ui.platform.LocalContext.current
                                        val invoiceScope = rememberCoroutineScope()
                                        Button(
                                            onClick = {
                                                invoiceScope.launch {
                                                    val items = viewModel.getOrderItems(ord.id)
                                                    val customer = viewModel.repository.findUser(ord.userId)
                                                    val cName = customer?.name ?: "Lyo Customer"
                                                    val cPhone = customer?.phone ?: ord.userId
                                                    val cAddr = customer?.address ?: "Lyo Delivery Address"
                                                    com.example.WhatsAppHelper.sendInvoiceMessage(invoiceContext, ord, items, cName, cPhone, cAddr)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Send,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Invoice அனுப்பு (Text Invoice)",
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        val coroutineScope = rememberCoroutineScope()
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val items = viewModel.getOrderItems(ord.id)
                                                    LyoNotificationHelper.generateOrderPdfAndShare(context, ord, items)
                                                 }
                                             },
                                             colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F22C55E)),
                                             border = BorderStroke(1.dp, Color(0xFF22C55E)),
                                             shape = RoundedCornerShape(10.dp),
                                             modifier = Modifier.fillMaxWidth()
                                         ) {
                                             Icon(
                                                 imageVector = Icons.Filled.Share,
                                                 contentDescription = null,
                                                 tint = Color(0xFF22C55E),
                                                 modifier = Modifier.size(16.dp)
                                             )
                                             Spacer(modifier = Modifier.width(8.dp))
                                             Text(
                                                 text = "வாட்ஸ்அப் PDF பில் • WHATSAPP PDF INVOICE",
                                                 color = Color(0xFF22C55E),
                                                 fontSize = 11.sp,
                                                 fontWeight = FontWeight.Black
                                             )
                                         }

                                         Spacer(modifier = Modifier.height(8.dp))
                                         Button(
                                             onClick = {
                                                 coroutineScope.launch {
                                                     val items = viewModel.getOrderItems(ord.id)
                                                     val vendor = vendors.find { it.id == ord.vendorId }
                                                     val ownerPhone = vendor?.phone ?: ""
                                                     val kotText = buildString {
                                                         append("━━━━━━━━━━━━━━━━━━━━━━━\n")
                                                         append("👨‍🍳 *LYO FRESH — KITCHEN ORDER (KOT)* 👨‍🍳\n")
                                                         append("━━━━━━━━━━━━━━━━━━━━━━━\n")
                                                         append("🏪 *உணவகம் (Shop):* ${vendor?.name ?: "Lyo Partner"}\n")
                                                         append("📦 *ஆர்டர் ஐடி (Order ID):* #Lyo-${ord.id}\n")
                                                         append("━━━━━━━━━━━━━━━━━━━━━━━\n\n")
                                                         append("*தயாரிக்க வேண்டிய உணவுகள் (Items):*\n")
                                                         items.forEach { item ->
                                                             append("   • *${item.quantity}x*  ${item.nameEn}")
                                                             if (item.nameTa.isNotBlank() && item.nameTa != item.nameEn) {
                                                                 append(" / ${item.nameTa}")
                                                             }
                                                             append("\n")
                                                         }
                                                         append("\n━━━━━━━━━━━━━━━━━━━━━━━\n")
                                                         append("💰 *மொத்தத் தொகை (Total):* *₹${ord.totalAmount.toInt()}*\n")
                                                         append("❤️ *நன்றி!* 🙏 — *Lyo AI*\n")
                                                         append("━━━━━━━━━━━━━━━━━━━━━━━")
                                                     }
                                                     val cleanPhone = ownerPhone.replace(Regex("[^0-9+]"), "").let { 
                                                         if (it.length == 10) "91$it" else it 
                                                     }
                                                     com.example.WhatsAppHelper.sendMessage(context, cleanPhone, kotText)
                                                 }
                                             },
                                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                             shape = RoundedCornerShape(10.dp),
                                             modifier = Modifier.fillMaxWidth()
                                         ) {
                                             Icon(
                                                 imageVector = Icons.Filled.Send,
                                                 contentDescription = null,
                                                 tint = Color.Black,
                                                 modifier = Modifier.size(16.dp)
                                             )
                                             Spacer(modifier = Modifier.width(8.dp))
                                             Text(
                                                 text = "Owner-க்கு KOT அனுப்பு (Text KOT)",
                                                 color = Color.Black,
                                                 fontSize = 11.sp,
                                                 fontWeight = FontWeight.Bold
                                             )
                                         }

                                         Spacer(modifier = Modifier.height(8.dp))
                                         Button(
                                             onClick = {
                                                 coroutineScope.launch {
                                                     val items = viewModel.getOrderItems(ord.id)
                                                     LyoNotificationHelper.generateOrderPdfAndShare(context, ord, items)
                                                 }
                                             },
                                             colors = ButtonDefaults.buttonColors(containerColor = Color(0x1FFFF980)),
                                             border = BorderStroke(1.dp, Color(0xFFFF9800)),
                                             shape = RoundedCornerShape(10.dp),
                                             modifier = Modifier.fillMaxWidth()
                                         ) {
                                             Icon(
                                                 imageVector = Icons.Filled.Share,
                                                 contentDescription = null,
                                                 tint = Color(0xFFFF9800),
                                                 modifier = Modifier.size(16.dp)
                                             )
                                             Spacer(modifier = Modifier.width(8.dp))
                                             Text(
                                                 text = "Owner-க்கு PDF பில் • OWNER PDF INVOICE",
                                                 color = Color(0xFFFF9800),
                                                 fontSize = 11.sp,
                                                 fontWeight = FontWeight.Black
                                             )
                                         }

                                         Button(
                                             onClick = {
                                                 val dummyList = emptyList<com.example.data.database.OrderItem>()
                                                 val dummyContext = context
                                                 val dummyOrder = ord
                                                 if (dummyContext != null) {
                                                     val x = 1
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F22C55E)),
                                            border = BorderStroke(1.dp, Color(0xFF22C55E)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Share,
                                                contentDescription = null,
                                                tint = Color(0xFF22C55E),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "வாட்ஸ்அப் PDF பில் • WHATSAPP PDF INVOICE",
                                                color = Color(0xFF22C55E),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }

                 "VENDORS" -> {
                    // ACTIVE VENDORS LIST - CLIK INTO NESTED CATALOG MANAGER
                    var vendorSearchQuery by remember { mutableStateOf("") }
                    var menuSearchQuery by remember { mutableStateOf("") }
                    var showClassificationsDialog by remember { mutableStateOf(false) }

                    if (selectedVendor == null) {
                        val filteredVendors = remember(vendors, vendorSearchQuery) {
                            vendors.filter {
                                it.name.contains(vendorSearchQuery, ignoreCase = true) ||
                                it.type.contains(vendorSearchQuery, ignoreCase = true) ||
                                it.address.contains(vendorSearchQuery, ignoreCase = true)
                            }
                        }

                        if (showClassificationsDialog) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            var classificationsOrderState by remember {
                                val sharedPrefs = context.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
                                val saved = sharedPrefs.getString("category_custom_order", null)
                                val list = if (saved != null) {
                                    saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                } else {
                                    listOf("Restaurant", "Cafe", "Hotel", "Bakery", "Snack Shop", "Dhaba")
                                }
                                mutableStateOf(list)
                            }

                            AlertDialog(
                                onDismissRequest = { showClassificationsDialog = false },
                                title = {
                                    Text(
                                        "முகப்பு கேட்டகிரி வரிசை (Home Categories)",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                },
                                text = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            "முகப்புப் பக்கத்தில் கடைகளின் வகைப்பாடுகள் காட்டும் வரிசையை மாற்றிக் கொள்ளுங்கள்:",
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            classificationsOrderState.forEachIndexed { index, name ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF1E293B))
                                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(name, color = Color.White,  fontWeight = FontWeight.Bold, fontSize = 13.sp)

                                                    Row {
                                                        IconButton(
                                                            onClick = {
                                                                if (index > 0) {
                                                                    val newList = classificationsOrderState.toMutableList()
                                                                    val temp = newList[index]
                                                                    newList[index] = newList[index - 1]
                                                                    newList[index - 1] = temp
                                                                    classificationsOrderState = newList
                                                                }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Move Up", tint = Color.White, modifier = Modifier.size(16.dp))
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                if (index < classificationsOrderState.size - 1) {
                                                                    val newList = classificationsOrderState.toMutableList()
                                                                    val temp = newList[index]
                                                                    newList[index] = newList[index + 1]
                                                                    newList[index + 1] = temp
                                                                    classificationsOrderState = newList
                                                                }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Filled.ArrowDownward, contentDescription = "Move Down", tint = Color.White, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val sharedPrefs = context.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
                                            sharedPrefs.edit().putString("category_custom_order", classificationsOrderState.joinToString(",")).apply()
                                            android.widget.Toast.makeText(context, "கேட்டகிரி வரிசை வெற்றிகரமாக சேமிக்கப்பட்டது! 🎉", android.widget.Toast.LENGTH_LONG).show()
                                            showClassificationsDialog = false
                                        }
                                    ) {
                                        Text("சேமி (SAVE)", color = LyoColors.VegGreen, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showClassificationsDialog = false }) {
                                        Text("மூடு (CLOSE)", color = Color.White)
                                    }
                                },
                                containerColor = Color(0xFF0F172A),
                                titleContentColor = Color.White,
                                textContentColor = Color.LightGray
                            )
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(20.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { activeTab = "ONBOARDING" },
                                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.1f)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "புதிய உணவகம்",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Button(
                                        onClick = { showClassificationsDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        border = BorderStroke(1.dp, Color(0xFF334155)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(0.9f)
                                    ) {
                                        Icon(Icons.Filled.Sort, contentDescription = null, tint = LyoColors.AccentOrange, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "முகப்பு கேட்டகிரி",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                               item {
                                 Lyo3DSearchBar(
                                     value = vendorSearchQuery,
                                     onValueChange = { vendorSearchQuery = it },
                                     placeholder = "Search Store by Name or Category...",
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .padding(bottom = 16.dp)
                                 )
                            }

                            items(filteredVendors, key = { it.id }) { item ->
                                Box(modifier = Modifier.padding(vertical = 8.dp)) {
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { viewModel.selectedAdminVendor.value = item }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                Text(
                                                    text = if (item.nameTa.isNotBlank()) "${item.name} (${item.nameTa})" else item.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Category: ${item.type} • Order Rank: ${item.sortOrder}",
                                                    color = LyoColors.TextSecondary,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Address: ${item.address}",
                                                    color = Color.LightGray.copy(alpha = 0.6f),
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            viewModel.updateVendor(item.copy(sortOrder = item.sortOrder - 1))
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Move Up", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            viewModel.updateVendor(item.copy(sortOrder = item.sortOrder + 1))
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Move Down", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(Icons.Filled.ArrowForward, contentDescription = "nested", tint = LyoColors.AccentOrange)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // NESTED CATALOG MANAGEMENT MODE SPECIFICS
                        val partner = selectedVendor!!
                        val categoriesFlow = remember(partner.id) { viewModel.getCategoriesForVendorFlow(partner.id) }
                        val categoriesList by categoriesFlow.collectAsState(initial = emptyList())
                        val itemsFlow = remember(partner.id) { viewModel.getMenuItemsForVendorFlow(partner.id) }
                        val itemsList by itemsFlow.collectAsState(initial = emptyList())

                        LaunchedEffect(categoriesList) {
                            if (categoriesList.isNotEmpty() && viewModel.selectedCategoryId.value == null) {
                                viewModel.selectedCategoryId.value = categoriesList.first().id
                            }
                        }

                        // Active coupon settings local model states
                        var isCouponEnabled by remember(partner.id) { mutableStateOf(partner.isCouponEnabled) }
                        var couponCode by remember(partner.id) { mutableStateOf(partner.couponCode) }
                        var couponDiscount by remember(partner.id) { mutableStateOf(partner.couponDiscount.toInt().toString()) }
                        var couponMinOrder by remember(partner.id) { mutableStateOf(partner.couponMinOrder.toInt().toString()) }
                        var selectedBannerPreset by remember(partner.id) { mutableStateOf(partner.bannerUrl) }

                        var editName by remember(partner.id) { mutableStateOf(partner.name) }
                        var editType by remember(partner.id) { mutableStateOf(partner.type) }
                        var editAddress by remember(partner.id) { mutableStateOf(partner.address) }
                        var editIsOnHoliday by remember(partner.id) { mutableStateOf(partner.isOnHoliday) }
                        var editAutoOpenTime by remember(partner.id) { mutableStateOf(partner.autoOpenTime) }
                        var editAutoCloseTime by remember(partner.id) { mutableStateOf(partner.autoCloseTime) }
                        var editDeliveryFee by remember(partner.id) { mutableStateOf(partner.deliveryFee.toInt().toString()) }
                        var editIsDynamicDelivery by remember(partner.id) { mutableStateOf(partner.isDynamicDelivery) }
                        var editMinOrder by remember(partner.id) { mutableStateOf(partner.minOrderAmount.toInt().toString()) }
                        var editBannerUrl by remember(partner.id) { mutableStateOf(partner.bannerUrl) }
                        var editPhone by remember(partner.id) { mutableStateOf(partner.phone) }
                        var editVisibilityRadiusKm by remember(partner.id) { mutableStateOf(partner.visibilityRadiusKm) }
                        var editLat by remember(partner.id) { mutableStateOf(partner.lat) }
                        var editLng by remember(partner.id) { mutableStateOf(partner.lng) }
                        var editSortOrder by remember(partner.id) { mutableStateOf(partner.sortOrder.toString()) }

                        LaunchedEffect(partner) {
                            isCouponEnabled = partner.isCouponEnabled
                            couponCode = partner.couponCode
                            couponDiscount = partner.couponDiscount.toInt().toString()
                            couponMinOrder = partner.couponMinOrder.toInt().toString()
                            selectedBannerPreset = partner.bannerUrl
                            editName = partner.name
                            editType = partner.type
                            editAddress = partner.address
                            editIsOnHoliday = partner.isOnHoliday
                            editAutoOpenTime = partner.autoOpenTime
                            editAutoCloseTime = partner.autoCloseTime
                            editDeliveryFee = partner.deliveryFee.toInt().toString()
                            editIsDynamicDelivery = partner.isDynamicDelivery
                            editMinOrder = partner.minOrderAmount.toInt().toString()
                            editBannerUrl = partner.bannerUrl
                            editPhone = partner.phone
                            editVisibilityRadiusKm = partner.visibilityRadiusKm
                            editLat = partner.lat
                            editLng = partner.lng
                            editSortOrder = partner.sortOrder.toString()
                        }

                        LaunchedEffect(selectedImageUriStr) {
                            selectedImageUriStr?.let {
                                editBannerUrl = it
                                selectedImageUriStr = null
                            }
                        }

                        // Dialog controls for editing
                        var editingMenuItem by remember { mutableStateOf<MenuItem?>(null) }
                        var menuItemToDelete by remember { mutableStateOf<MenuItem?>(null) }
                        var categoryToDelete by remember { mutableStateOf<Category?>(null) }
                        var categoryToEdit by remember { mutableStateOf<Category?>(null) }
                        var menuSearchQuery by remember(partner.id) { mutableStateOf("") }

                        // Onboarding category states
                        val catEn by viewModel.newCategoryNameEn.collectAsState()
                        val catTa by viewModel.newCategoryNameTa.collectAsState()

                        // Menu Item inputs states
                        val itemEn by viewModel.newItemNameEn.collectAsState()
                        val itemTa by viewModel.newItemNameTa.collectAsState()
                        val itemDescEn by viewModel.newItemDescEn.collectAsState()
                        val itemDescTa by viewModel.newItemDescTa.collectAsState()
                        val itemPrice by viewModel.newItemPrice.collectAsState()
                        val itemIsVeg by viewModel.newItemIsVeg.collectAsState()
                        val activeCatId by viewModel.selectedCategoryId.collectAsState()
                        val itemImageUrl by viewModel.newItemImageUrl.collectAsState()

                        LazyColumn(
                            modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                            contentPadding = PaddingValues(20.dp)
                        ) {
                            // Back to vendor selective list header
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectedAdminVendor.value = null }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "back", tint = LyoColors.AccentOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Back to Venues Registry", color = LyoColors.AccentOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = partner.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            // Sub-section A00: Operating Settings & Vendor Management Form
                            item {
                                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                                    GlassCard(cornerRadius = 14.dp, modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "🏪 VENUE SETTINGS & OPERATING STATUS",
                                            color = LyoColors.AmberYellow,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        OutlinedTextField(
                                            value = editName,
                                            onValueChange = { editName = it },
                                            label = { Text("Shop Name") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        val customTypesMap by viewModel.customBusinessTypes.collectAsState()
                                        val editTypes = remember(customTypesMap) { customTypesMap.keys.toList() }
                                        var showEditCustomTypeDialog by remember { mutableStateOf(false) }

                                        Text("Shop Classification:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {



                                            items(editTypes, key = { it }) { t ->
                                                val isSelected = editType.lowercase() == t.lowercase()
                                                val emoji = customTypesMap[t] ?: "🏪"
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) LyoColors.AccentOrange else Color(0x15FFFFFF))
                                                        .border(1.dp, if (isSelected) LyoColors.AccentOrange else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                                        .clickable { editType = t }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text("$emoji $t", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0x1F22C55E))
                                                        .border(1.dp, Color(0x6622C55E), RoundedCornerShape(8.dp))
                                                        .clickable { showEditCustomTypeDialog = true }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text("➕ Add Custom", color = Color(0xFF22C55E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        if (showEditCustomTypeDialog) {
                                            var customTypeName by remember { mutableStateOf("") }
                                            var customTypeEmoji by remember { mutableStateOf("🏪") }
                                            androidx.compose.material3.AlertDialog(
                                                onDismissRequest = { showEditCustomTypeDialog = false },
                                                title = { Text("Add Custom Business Type", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                                text = {
                                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customTypeName,
                                                            onValueChange = { customTypeName = it },
                                                            label = { Text("Type Name", color = Color.Gray) },
                                                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )
                                                        Text("Select Emoji:", color = Color.Gray, fontSize = 11.sp)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            listOf("🏪", "🍵", "🍳", "🥤", "🌾", "🍿", "🍗", "🥣", "🧁", "🐟", "🌶️").forEach { em ->
                                                                val isSelected = customTypeEmoji == em
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(36.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (isSelected) LyoColors.AccentOrange else Color(0x1Fffffff))
                                                                        .clickable { customTypeEmoji = em },
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(em, fontSize = 18.sp)
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                confirmButton = {
                                                    androidx.compose.material3.Button(
                                                        onClick = {
                                                            if (customTypeName.isNotBlank()) {
                                                                viewModel.addCustomBusinessType(customTypeName, customTypeEmoji)
                                                                editType = customTypeName
                                                                showEditCustomTypeDialog = false
                                                            }
                                                        },
                                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange)
                                                    ) {
                                                        Text("ADD", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                },
                                                dismissButton = {
                                                    androidx.compose.material3.TextButton(onClick = { showEditCustomTypeDialog = false }) {
                                                        Text("CANCEL", color = Color.LightGray)
                                                    }
                                                },
                                                containerColor = Color(0xFF0F172A),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                        }

                                        OutlinedTextField(
                                            value = editAddress,
                                            onValueChange = { editAddress = it },
                                            label = { Text("Shop Address") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                        )

                                        OutlinedTextField(
                                            value = editPhone,
                                            onValueChange = { editPhone = it },
                                            label = { Text("Shop Owner Phone (தேவைப்பட்டால் வாட்ஸ்அப் KOT எண்)") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("இன்றைக்கு லீவு (Holiday Status)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("Mark shop closed today/On Holiday", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                            }
                                            Switch(
                                                checked = editIsOnHoliday,
                                                onCheckedChange = { editIsOnHoliday = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = LyoColors.AccentOrange)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("ஆட்டோமேட்டிக் கடை திறக்கும் நேரம் (Optional Scheduling):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("If set, the store will auto-open and auto-close (24-hr format HH:MM e.g. 08:30 and 22:30). Leave empty to use manual holiday switch.", color = LyoColors.TextSecondary, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedTextField(
                                                value = editAutoOpenTime,
                                                onValueChange = { editAutoOpenTime = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                label = { Text("OPEN (HH:MM)") },
                                                placeholder = { Text("08:30") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = editAutoCloseTime,
                                                onValueChange = { editAutoCloseTime = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                label = { Text("CLOSE (HH:MM)") },
                                                placeholder = { Text("21:00") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Divider(color = Color(0x22FFFFFF))
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text("Operating Metrics:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                            OutlinedTextField(
                                                value = editDeliveryFee,
                                                onValueChange = { editDeliveryFee = it },
                                                label = { Text("Delivery (₹)") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = editMinOrder,
                                                onValueChange = { editMinOrder = it },
                                                label = { Text("Min Order (₹)") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("டைனமிக் டெலிவரி சார்ஜ் (Dynamic Delivery Fee)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(if (editIsDynamicDelivery) "Distance-based: ₹$editDeliveryFee base + ₹15/km after 3km" else "Fixed-charge: ₹$editDeliveryFee regardless of distance", color = LyoColors.AccentOrange, fontSize = 11.sp)
                                            }
                                            Switch(
                                                checked = editIsDynamicDelivery,
                                                onCheckedChange = { editIsDynamicDelivery = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = LyoColors.AccentOrange)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "STORE VISIBILITY RADIUS",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "இந்தக் கடையின் மேப் விசிபிலிட்டி ரேடியஸ்",
                                                    color = LyoColors.AccentOrange,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF38BDF8).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "${String.format(java.util.Locale.US, "%.1f", editVisibilityRadiusKm)} KM",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                        Slider(
                                            value = editVisibilityRadiusKm.toFloat(),
                                            onValueChange = { editVisibilityRadiusKm = it.toDouble() },
                                            valueRange = 1.0f..100.0f,
                                            steps = 99,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFF38BDF8),
                                                activeTrackColor = Color(0xFF38BDF8),
                                                inactiveTrackColor = Color(0x33FFFFFF)
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Custom Image Cover Setup:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                             Button(
                                                 onClick = { galleryLauncher.launch("image/*") },
                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F38BDF8)),
                                                 border = BorderStroke(1.dp, Color(0xFF0284C7)),
                                                 shape = RoundedCornerShape(8.dp),
                                                 modifier = Modifier.weight(1f)
                                             ) {
                                                 Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                 Spacer(modifier = Modifier.width(6.dp))
                                                 Text("BROWSE PHOTO", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                             }
                                         }

                                         OutlinedTextField(
                                             value = editBannerUrl,
                                             onValueChange = { editBannerUrl = it },
                                             label = { Text("Or Paste Internet Image URL") },
                                             colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                             modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 14.dp)
                                         )

                                        OutlinedTextField(
                                            value = editSortOrder,
                                            onValueChange = { editSortOrder = it },
                                            label = { Text("வரிசை முன்னுரிமை எண் (Sort Order Priority - Lower numbers first)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "📍 PIN EXACT GEOGRAPHICAL LOCATION ON MAP (RE-EDIT LOCATION)",
                                            color = LyoColors.AccentOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                                .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(12.dp))
                                                .padding(10.dp)
                                        ) {
                                            var showEditVendorMapDialog by remember { mutableStateOf(false) }
                                            var latInputStr by remember(partner.id, editLat) { 
                                                mutableStateOf(if (editLat == 0.0) "" else String.format(java.util.Locale.US, "%.6f", editLat)) 
                                            }
                                            var lngInputStr by remember(partner.id, editLng) { 
                                                mutableStateOf(if (editLng == 0.0) "" else String.format(java.util.Locale.US, "%.6f", editLng)) 
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = latInputStr,
                                                    onValueChange = { newVal ->
                                                        latInputStr = newVal
                                                        newVal.toDoubleOrNull()?.let { editLat = it }
                                                    },
                                                    label = { Text("Latitude", fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = lngInputStr,
                                                    onValueChange = { newVal ->
                                                        lngInputStr = newVal
                                                        newVal.toDoubleOrNull()?.let { editLng = it }
                                                    },
                                                    label = { Text("Longitude", fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Button(
                                                onClick = { showEditVendorMapDialog = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = LyoColors.AccentOrange,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocationOn,
                                                    contentDescription = "Map Icon",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "வரைபடத்தில் இடத்தை தேர்வு செய் (Open Map)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))


                                            if (showEditVendorMapDialog) {
                                                Lyo3DDialog(onDismissRequest = { showEditVendorMapDialog = false }) {
                                                    Column(modifier = Modifier.fillMaxWidth().height(480.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = "🗺️ Pick Location on Map\n(வரைபடத்தில் லொகேஷனை தேர்வு செய்யவும்)",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                lineHeight = 16.sp,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            IconButton(
                                                                onClick = { showEditVendorMapDialog = false },
                                                                modifier = Modifier.size(28.dp).background(Color(0x1Fffffff), CircleShape)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Close,
                                                                    contentDescription = "Close",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                                                            InteractiveMapPickerView(
                                                                initialLat = if (editLat != 0.0) editLat else 11.5812,
                                                                initialLng = if (editLng != 0.0) editLng else 77.8465,
                                                                onLocationPicked = { pickedLat, pickedLng ->
                                                                    editLat = pickedLat
                                                                    editLng = pickedLng
                                                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                        try {
                                                                            val url = java.net.URL("https://nominatim.openstreetmap.org/reverse?lat=${pickedLat}&lon=${pickedLng}&format=json")
                                                                            val conn = url.openConnection() as java.net.HttpURLConnection
                                                                            conn.requestMethod = "GET"
                                                                            conn.setRequestProperty("User-Agent", "LyoFreshClient/1.0 (Android; dev-agent)")
                                                                            conn.connectTimeout = 3000
                                                                            conn.readTimeout = 3000
                                                                            if (conn.responseCode == 200) {
                                                                                val text = conn.inputStream.bufferedReader().use { it.readText() }
                                                                                val json = org.json.JSONObject(text)
                                                                                val addressObj = json.optJSONObject("address")
                                                                                val road = addressObj?.optString("road") ?: ""
                                                                                val suburb = addressObj?.optString("suburb") ?: addressObj?.optString("village") ?: addressObj?.optString("town") ?: ""
                                                                                val county = addressObj?.optString("county") ?: addressObj?.optString("city") ?: ""
                                                                                var shortAddr = listOf(road, suburb, county).filter { it.isNotBlank() }.joinToString(", ")
                                                                                if (shortAddr.isBlank()) {
                                                                                    shortAddr = json.optString("display_name", "").take(50)
                                                                                }
                                                                                if (shortAddr.isBlank()) {
                                                                                    shortAddr = "Salem Area Pin"
                                                                                }
                                                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                                    editAddress = shortAddr
                                                                                }
                                                                            }
                                                                        } catch (e: Exception) {
                                                                            e.printStackTrace()
                                                                        }
                                                                    }
                                                                }
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(12.dp))
                                                        LyoButton(
                                                            text = "Confirm Location (உறுதிசெய்)",
                                                            onClick = { showEditVendorMapDialog = false },
                                                            modifier = Modifier.fillMaxWidth()
                                                        )
                                                    }
                                                }
                                            }

                                            /*
                                            Text("Adjust location coordinates visually by clicking on the map:", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.height(6.dp))

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF090D16))
                                                    .border(1.dp, Color(0x33F8FAFC), RoundedCornerShape(10.dp))
                                                    .pointerInput(Unit) {
                                                        detectTapGestures { offset ->
                                                            val pctX = offset.x / size.width
                                                            val pctY = offset.y / size.height

                                                            // Interpolate between Idappadi Salem coordinates boundaries
                                                            val latRange = 11.6000 - 11.5700
                                                            val lngRange = 77.8700 - 77.8300
                                                            val computedLat = 11.6000 - (pctY * latRange)
                                                            val computedLng = 77.8300 + (pctX * lngRange)

                                                            editLat = computedLat
                                                            editLng = computedLng

                                                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                try {
                                                                    val url = java.net.URL("https://nominatim.openstreetmap.org/reverse?lat=${computedLat}&lon=${computedLng}&format=json")
                                                                    val conn = url.openConnection() as java.net.HttpURLConnection
                                                                    conn.requestMethod = "GET"
                                                                    conn.setRequestProperty("User-Agent", "LyoFreshClient/1.0 (Android; dev-agent)")
                                                                    conn.connectTimeout = 3000
                                                                    conn.readTimeout = 3000
                                                                    if (conn.responseCode == 200) {
                                                                        val text = conn.inputStream.bufferedReader().use { it.readText() }
                                                                        val json = org.json.JSONObject(text)
                                                                        val addressObj = json.optJSONObject("address")
                                                                        val road = addressObj?.optString("road") ?: ""
                                                                        val suburb = addressObj?.optString("village") ?: addressObj?.optString("suburb") ?: addressObj?.optString("town") ?: ""
                                                                        val county = addressObj?.optString("county") ?: addressObj?.optString("city") ?: ""
                                                                        var shortAddr = listOf(road, suburb, county).filter { it.isNotBlank() }.joinToString(", ")
                                                                        if (shortAddr.isBlank()) {
                                                                            shortAddr = json.optString("display_name", "").take(50)
                                                                        }
                                                                        if (shortAddr.isBlank()) {
                                                                            shortAddr = "Salem Idappadi District"
                                                                        }
                                                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                            editAddress = shortAddr
                                                                        }
                                                                    }
                                                                } catch (e: Exception) {
                                                                    e.printStackTrace()
                                                                }
                                                            }
                                                        }
                                                    }
                                            ) {
                                                // Draw Satellite Map elements using Canvas
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    // 1. Terrain Base (Olive/Dark Green vegetation)
                                                    drawRect(color = Color(0xFF0F2610))

                                                    // 2. Agricultural Crop Lands / Fields
                                                    drawRect(color = Color(0xFF1E501F), topLeft = Offset(5f, 5f), size = Size(size.width * 0.28f, size.height * 0.35f))
                                                    drawRect(color = Color(0xFF246125), topLeft = Offset(size.width * 0.68f, 10f), size = Size(size.width * 0.3f, size.height * 0.42f))
                                                    drawRect(color = Color(0xFF19441A), topLeft = Offset(10f, size.height * 0.7f), size = Size(size.width * 0.25f, size.height * 0.25f))

                                                    // 3. Water Canal / Lake in Idappadi
                                                    drawCircle(color = Color(0xFF1A5276), radius = 32.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.75f))
                                                    drawCircle(color = Color(0xFF2980B9), radius = 22.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.75f))

                                                    // 4. Clustered Residential Buildings and Houses (Reddish, Brownish, Silver Roofs)
                                                    drawRect(color = Color(0xFF8E44AD), topLeft = Offset(size.width * 0.45f, size.height * 0.1f), size = Size(10.dp.toPx(), 8.dp.toPx()))
                                                    drawRect(color = Color(0xFFD35400), topLeft = Offset(size.width * 0.52f, size.height * 0.12f), size = Size(12.dp.toPx(), 10.dp.toPx()))
                                                    drawRect(color = Color(0xFF7F8C8D), topLeft = Offset(size.width * 0.48f, size.height * 0.22f), size = Size(8.dp.toPx(), 8.dp.toPx()))
                                                    drawRect(color = Color(0xFFBDC3C7), topLeft = Offset(size.width * 0.58f, size.height * 0.18f), size = Size(14.dp.toPx(), 12.dp.toPx()))
                                                    drawRect(color = Color(0xFFE67E22), topLeft = Offset(size.width * 0.12f, size.height * 0.52f), size = Size(11.dp.toPx(), 9.dp.toPx()))
                                                    drawRect(color = Color(0xFFCA6F1E), topLeft = Offset(size.width * 0.18f, size.height * 0.55f), size = Size(10.dp.toPx(), 10.dp.toPx()))
                                                    drawRect(color = Color(0xFF7D6608), topLeft = Offset(size.width * 0.56f, size.height * 0.65f), size = Size(12.dp.toPx(), 10.dp.toPx()))
                                                    drawRect(color = Color(0xFF5D6D7E), topLeft = Offset(size.width * 0.64f, size.height * 0.62f), size = Size(15.dp.toPx(), 12.dp.toPx()))

                                                    // 5. Dual-Lane Asphalt Highways (Salem Main Road - Horizontal)
                                                    // Base Asphalt
                                                    drawLine(color = Color(0xFF232B2B), start = Offset(0f, size.height * 0.45f), end = Offset(size.width, size.height * 0.45f), strokeWidth = 14.dp.toPx())
                                                    // Double yellow center split lines
                                                    drawLine(color = Color(0xFFF1C40F), start = Offset(0f, size.height * 0.45f - 1.dp.toPx()), end = Offset(size.width, size.height * 0.45f - 1.dp.toPx()), strokeWidth = 1.dp.toPx())
                                                    drawLine(color = Color(0xFFF1C40F), start = Offset(0f, size.height * 0.45f + 1.dp.toPx()), end = Offset(size.width, size.height * 0.45f + 1.dp.toPx()), strokeWidth = 1.dp.toPx())

                                                    // 6. Secondary Bypass Road (Vertical)
                                                    // Base Asphalt
                                                    drawLine(color = Color(0xFF2A2A2A), start = Offset(size.width * 0.38f, 0f), end = Offset(size.width * 0.38f, size.height), strokeWidth = 11.dp.toPx())
                                                    // White side stripes
                                                    drawLine(color = Color(0xFFE5E8E8), start = Offset(size.width * 0.38f - 5.dp.toPx(), 0f), end = Offset(size.width * 0.38f - 5.dp.toPx(), size.height), strokeWidth = 0.8.dp.toPx())
                                                    drawLine(color = Color(0xFFE5E8E8), start = Offset(size.width * 0.38f + 5.dp.toPx(), 0f), end = Offset(size.width * 0.38f + 5.dp.toPx(), size.height), strokeWidth = 0.8.dp.toPx())
                                                }

                                                // Absolute overlay of Street Labels & Telemetry HUD
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    Text(
                                                        text = "SALEM MAIN RD [SH-17] ➔",
                                                        color = Color(0xFFF39C12),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .offset(x = 10.dp, y = 58.dp)
                                                    )

                                                    Text(
                                                        text = "▲ IDAPPADI BYPASS RD",
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .offset(x = 120.dp, y = 14.dp)
                                                    )

                                                    Text(
                                                        text = "IDAPPADI RESERVOIR 🛰️",
                                                        color = Color(0xFF5DADE2),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .padding(end = 12.dp, bottom = 42.dp)
                                                    )

                                                    // Satellite Status Badge
                                                    Row(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(6.dp)
                                                            .background(Color(0xCC0F172A), RoundedCornerShape(4.dp))
                                                            .border(0.5.dp, Color(0xFF2ECC71), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .background(Color(0xFF2ECC71), CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "LIVE LYO-SAT STREAM (15cm)",
                                                            color = Color(0xFF2ECC71),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                }

                                                // Beautiful Bilingual Labels Overlay
                                          Box(modifier = Modifier.fillMaxSize()) {
                                              Text(
                                                  text = "⛰️ சிவகிரி திருமலை\n(Sivagiri Hill)",
                                                  color = Color(0xFFFFCCAA),
                                                  fontSize = 7.sp,
                                                  lineHeight = 9.sp,
                                                  fontWeight = FontWeight.Bold,
                                                  modifier = Modifier.align(Alignment.TopStart).offset(x = 12.dp, y = 14.dp)
                                              )
                                              Text(
                                                  text = "⛰️ சூரிய மலை\n(Suriya Hill)",
                                                  color = Color(0xFFFFCCAA),
                                                  fontSize = 7.sp,
                                                  fontWeight = FontWeight.Bold,
                                                  modifier = Modifier.align(Alignment.TopEnd).offset(x = (-10).dp, y = 14.dp)
                                              )
                                              Text(
                                                  text = "🛒 இடப்பாடி கடைவீதி\n(Kadaiveethi)",
                                                  color = Color.White,
                                                  fontSize = 7.sp,
                                                  lineHeight = 9.sp,
                                                  fontWeight = FontWeight.Black,
                                                  modifier = Modifier.align(Alignment.TopCenter).offset(x = (-30).dp, y = 45.dp)
                                              )
                                              Text(
                                                  text = "🛣️ சேலம் மெயின் ரோடு [SH-17]",
                                                  color = Color(0xFFF39C12),
                                                  fontSize = 7.sp,
                                                  fontWeight = FontWeight.Bold,
                                                  modifier = Modifier.align(Alignment.CenterStart).offset(x = 10.dp, y = 15.dp)
                                              )
                                              Text(
                                                  text = "🌊 பெரிய ஏரி (Lake)",
                                                  color = Color(0xFF81D4FA),
                                                  fontSize = 7.sp,
                                                  fontWeight = FontWeight.Bold,
                                                  modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-15).dp, y = (-25).dp)
                                              )
                                              Text(
                                                  text = "◽ கணேஷ் சந்து (Ganesh Lane)",
                                                  color = Color.LightGray,
                                                  fontSize = 6.sp,
                                                  modifier = Modifier.align(Alignment.BottomStart).offset(x = 12.dp, y = (-20).dp)
                                              )
                                              Text(
                                                  text = "◽ பழைய பஸ் நிலையம் (Bus Stand)",
                                                  color = Color.LightGray,
                                                  fontSize = 6.sp,
                                                  modifier = Modifier.align(Alignment.BottomCenter).offset(x = 60.dp, y = (-55).dp)
                                              )
                                          }

                                          // Text reminder

                                                Text(
                                                    text = "🛰️ ENHANCED REAL-TIME SATELLITE MAP (வான்வழி வரைபடம்)",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                        .padding(bottom = 4.dp)
                                                )

                                                // Normalize pin position based on boundaries [11.5700, 11.6000] & [77.8300, 77.8700]
                                                val normX = if (editLng in 77.8300..77.8700) {
                                                    ((editLng - 77.8300) / (77.8700 - 77.8300)).toFloat()
                                                } else {
                                                    0.45f
                                                }
                                                val normY = if (editLat in 11.5700..11.6000) {
                                                    ((11.6000 - editLat) / (11.6000 - 11.5700)).toFloat()
                                                } else {
                                                    0.45f
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .offset(
                                                            x = (normX * 280).dp,
                                                            y = (normY * 130).dp
                                                        )
                                                        .size(30.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.PinDrop,
                                                        contentDescription = "merchant dropped mark",
                                                     )
                                                 }
                                             }
                                             */
                                             /*
                                                        tint = LyoColors.AccentOrange,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))
                                             */
                                            // Coordinates managed via unified card above
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val updated = partner.copy(
                                                        name = editName.trim(),
                                                        type = editType,
                                                        address = editAddress.trim(),
                                                        isOnHoliday = editIsOnHoliday,
                                                        autoOpenTime = editAutoOpenTime.trim(),
                                                        autoCloseTime = editAutoCloseTime.trim(),
                                                        deliveryFee = editDeliveryFee.toDoubleOrNull() ?: partner.deliveryFee,
                                                        minOrderAmount = editMinOrder.toDoubleOrNull() ?: partner.minOrderAmount,
                                                        bannerUrl = editBannerUrl.trim(),
                                                        phone = editPhone.trim(),
                                                        visibilityRadiusKm = editVisibilityRadiusKm,
                                                        lat = editLat,
                                                        lng = editLng,
                                                        sortOrder = editSortOrder.toIntOrNull() ?: partner.sortOrder,
                                                        isDynamicDelivery = editIsDynamicDelivery
                                                    )
                                                    viewModel.updateVendor(updated)
                                                 },
                                                colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1.5f)
                                            ) {
                                                Text("SAVE VENUE CHANGES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }

                                            var showConfirmDelete by remember { mutableStateOf(false) }

                                            Button(
                                                onClick = { showConfirmDelete = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "delete", tint = Color.White, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("DELETE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }

                                            if (showConfirmDelete) {
                                                AlertDialog(
                                                    onDismissRequest = { showConfirmDelete = false },
                                                    title = { Text("அமைப்பிலிருந்து நீக்கு • Danger Zone", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black) },
                                                    text = { Text("Are you absolutely sure you want to completely delete ${partner.name}? This will wipe out all categories, items, and listings. This cannot be undone.", color = Color.LightGray, fontSize = 12.sp) },
                                                    confirmButton = {
                                                        TextButton(
                                                            onClick = {
                                                                showConfirmDelete = false
                                                                viewModel.deleteVendor(partner)
                                                            }
                                                        ) {
                                                            Text("YES, DELETE EVERYTHING", color = Color(0xFFEF4444), fontWeight = FontWeight.Black)
                                                        }
                                                    },
                                                    dismissButton = {
                                                        TextButton(onClick = { showConfirmDelete = false }) {
                                                            Text("CANCEL", color = Color.White)
                                                        }
                                                    },
                                                    containerColor = Color(0xFF0F172A),
                                                    titleContentColor = Color.White,
                                                    textContentColor = Color.LightGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Sub-section A0: Premium Merchant Customizer (Coupon & Beautiful Photo)
                            item {
                                GlassCard(
                                    cornerRadius = 14.dp,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = "⚙️ MERCHANDISE CUSTOMIZER & COUPONS",
                                        color = LyoColors.AmberYellow,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    // 1. Photo Backdrop Setup
                                    Text("Curate Premium Visual Banner:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(
                                            listOf(
                                                "hotel" to "South Indian (Green)",
                                                "restaurant" to "Royal Chettinad (Red)",
                                                "cafe" to "Filter Coffee (Brown)",
                                                "bakery" to "Iyengar Oven (Amber)",
                                                "snack" to "Crisp munchies (Orange)",
                                                "dhaba" to "Rustic Dhaba (Yellow)"
                                            ),
                                            key = { it.first }
                                        ) { (presetValue, presetLabel) ->
                                            val isSelected = selectedBannerPreset.lowercase() == presetValue
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSelected) LyoColors.AccentOrange else Color(0x15FFFFFF)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) LyoColors.AccentOrange else Color(0x33FFFFFF),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        selectedBannerPreset = presetValue
                                                        val updated = partner.copy(bannerUrl = presetValue)
                                                        viewModel.updateVendor(updated)
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(presetLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = Color(0x22FFFFFF))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // 2. Coupon Configuration Settings
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Activate Store Promo Offer", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Let buyers claim 1-tap vouchers", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                        }
                                        Switch(
                                            checked = isCouponEnabled,
                                            onCheckedChange = {
                                                isCouponEnabled = it
                                                val updated = partner.copy(isCouponEnabled = it)
                                                viewModel.updateVendor(updated)
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = LyoColors.AccentOrange)
                                        )
                                    }

                                    if (isCouponEnabled) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        OutlinedTextField(
                                            value = couponCode,
                                            onValueChange = { couponCode = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            label = { Text("Coupon promo Code") },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedTextField(
                                                value = couponDiscount,
                                                onValueChange = { couponDiscount = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                label = { Text("Discount (₹)") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = couponMinOrder,
                                                onValueChange = { couponMinOrder = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                label = { Text("Min Cart (₹)") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        LyoButton(
                                            text = "SAVE ACTIVE PROMO OFFER",
                                            onClick = {
                                                val updated = partner.copy(
                                                    isCouponEnabled = isCouponEnabled,
                                                    couponCode = couponCode.trim().uppercase(),
                                                    couponDiscount = couponDiscount.toDoubleOrNull() ?: 80.0,
                                                    couponMinOrder = couponMinOrder.toDoubleOrNull() ?: 300.0,
                                                    bannerUrl = selectedBannerPreset
                                                )
                                                viewModel.updateVendor(updated)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // Sub-section A: Category nesting manager
                            item {
                                GlassCard(
                                    cornerRadius = 14.dp,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = "Add Billing Category (Bilingual English-Tamil Mapping)",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )

                                    OutlinedTextField(
                                        value = catEn,
                                        onValueChange = { viewModel.newCategoryNameEn.value = it },
                                        label = { Text("English name: e.g., 'Indian Breads'") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = catTa,
                                        onValueChange = { viewModel.newCategoryNameTa.value = it },
                                        label = { Text("Tamil Translation: e.g., 'இந்திய ரொட்டி வகைகள்'") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    LyoButton(
                                        text = "CREATE CATEGORY",
                                        onClick = { viewModel.createCategory(partner.id) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (categoriesList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Divider(color = Color(0x15FFFFFF))
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "MANAGE EXISTING CATEGORIES (${categoriesList.size})",
                                            color = LyoColors.TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            categoriesList.forEach { categoryNode ->
                                                val isHidden = categoryNode.isHidden
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isHidden) Color(0x05FFFFFF) else Color(0x0AFFFFFF))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = categoryNode.nameEn,
                                                                color = if (isHidden) Color.Gray else Color.White,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            if (isHidden) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(Color(0xFF334155))
                                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                ) {
                                                                    Text("HIDDEN", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                        Text(
                                                            text = categoryNode.nameTa,
                                                            color = if (isHidden) Color(0x66FF6B00) else LyoColors.AccentOrange,
                                                            fontSize = 11.sp
                                                        )
                                                        Text(
                                                            text = "Sequence Order: ${categoryNode.sortOrder}",
                                                            color = Color(0xFF64748B),
                                                            fontSize = 9.sp
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.updateCategory(categoryNode.copy(sortOrder = categoryNode.sortOrder - 1))
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                             Icon(Icons.Filled.ArrowUpward, contentDescription = "reorder up", tint = Color.White, modifier = Modifier.size(14.dp))
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                viewModel.updateCategory(categoryNode.copy(sortOrder = categoryNode.sortOrder + 1))
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                             Icon(Icons.Filled.ArrowDownward, contentDescription = "reorder down", tint = Color.White, modifier = Modifier.size(14.dp))
                                                        }

                                                        Spacer(modifier = Modifier.width(4.dp))

                                                        IconButton(
                                                            onClick = {
                                                                viewModel.updateCategory(categoryNode.copy(isHidden = !categoryNode.isHidden))
                                                            },
                                                            modifier = Modifier.size(26.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                                contentDescription = "toggle visibility",
                                                                tint = if (isHidden) Color.Gray else Color(0xFF38BDF8),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }

                                                        IconButton(
                                                            onClick = { categoryToEdit = categoryNode },
                                                            modifier = Modifier.size(26.dp)
                                                        ) {
                                                            Icon(Icons.Filled.Edit, contentDescription = "edit category", tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                                        }

                                                        IconButton(
                                                            onClick = { categoryToDelete = categoryNode },
                                                            modifier = Modifier.size(26.dp)
                                                        ) {
                                                            Icon(Icons.Filled.Delete, contentDescription = "delete category", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Sub-section B: Menu item designer inside category
                            item {
                                if (categoriesList.isNotEmpty()) {
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Onboard Menu Dishes (Pair Bilingual Translators)",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )

                                        // Select Category dropdown spinner representation
                                        Text("Select billing category folder:", color = Color.White, fontSize = 11.sp)
                                        LazyRow(modifier = Modifier.padding(vertical = 6.dp)) {
                                            items(categoriesList, key = { it.id }) { categoryNode ->
                                                val isThisCat = activeCatId == categoryNode.id
                                                Box(
                                                    modifier = Modifier
                                                        .padding(end = 6.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isThisCat) LyoColors.AccentOrange else Color(0x22FFFFFF))
                                                        .clickable { viewModel.selectedCategoryId.value = categoryNode.id }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(categoryNode.nameEn, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Item Name English
                                        OutlinedTextField(
                                            value = itemEn,
                                            onValueChange = { viewModel.newItemNameEn.value = it },
                                            label = { Text("DISH NAME IN ENGLISH (e.g. Garlic Rotti)") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Item Name Tamil script Translate MANDATORY
                                        OutlinedTextField(
                                            value = itemTa,
                                            onValueChange = { viewModel.newItemNameTa.value = it },
                                            label = { Text("DISH NAME IN TAMIL SCRIPT (e.g. கார்லிக் ரொட்டி)") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Description English
                                        OutlinedTextField(
                                            value = itemDescEn,
                                            onValueChange = { viewModel.newItemDescEn.value = it },
                                            label = { Text("English description") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Description Tamil
                                        OutlinedTextField(
                                            value = itemDescTa,
                                            onValueChange = { viewModel.newItemDescTa.value = it },
                                            label = { Text("Tamil description") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            OutlinedTextField(
                                                value = itemPrice,
                                                onValueChange = { viewModel.newItemPrice.value = it },
                                                label = { Text("Price in INR (₹)") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = itemIsVeg,
                                                    onCheckedChange = { viewModel.newItemIsVeg.value = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = LyoColors.VegGreen)
                                                )
                                                Text("Veg Dish", color = Color.White, fontSize = 13.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = itemImageUrl,
                                            onValueChange = { viewModel.newItemImageUrl.value = it },
                                            label = { Text("OPTIONAL FOOD PHOTO URL (Unsplash / JPG Web Link)") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        LyoButton(
                                            text = "ADD DISH TO MENU",
                                            onClick = { viewModel.createMenuItem(partner.id) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    Text(
                                        "Please create at least one category folder (above) to load dishes.",
                                        color = LyoColors.TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(20.dp)
                                    )
                                }
                            }

                            // Dynamic listing of existing catalogue items
                            item {
                                Column(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
                                    Text(
                                        text = "EXISTING INTEGRATED MENUS (${itemsList.size})",
                                        color = LyoColors.TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Lyo3DSearchBar(
                                        value = menuSearchQuery,
                                        onValueChange = { menuSearchQuery = it },
                                        placeholder = "Search product / dish name...",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            val filteredMenuItems = itemsList.filter {
                                it.nameEn.contains(menuSearchQuery, ignoreCase = true) ||
                                it.nameTa.contains(menuSearchQuery, ignoreCase = true) ||
                                it.descEn.contains(menuSearchQuery, ignoreCase = true) ||
                                it.descTa.contains(menuSearchQuery, ignoreCase = true)
                            }

                            items(filteredMenuItems, key = { it.id }) { dish ->
                                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { editingMenuItem = dish },
                                        cornerRadius = 12.dp,
                                        backgroundColor = Color(0xFF0F172A)
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(dish.nameEn, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(dish.nameTa, color = LyoColors.AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(dish.descEn, color = LyoColors.TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        VegIndicator(isVeg = dish.isVeg)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("₹${dish.price.toInt()}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(
                                                                if (dish.isAvailable) Color(0x1122C55E) else Color(0x11EF4444)
                                                            )
                                                            .border(
                                                                1.dp,
                                                                if (dish.isAvailable) Color(0xFF22C55E) else Color(0xFFEF4444),
                                                                RoundedCornerShape(6.dp)
                                                            )
                                                            .clickable {
                                                                viewModel.updateMenuItem(dish.copy(isAvailable = !dish.isAvailable))
                                                            }
                                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = if (dish.isAvailable) "IN STOCK" else "OUT STOCK",
                                                            color = if (dish.isAvailable) Color(0xFF22C55E) else Color(0xFFEF4444),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Divider(color = Color(0x11FFFFFF))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        viewModel.updateMenuItem(dish.copy(isAvailable = !dish.isAvailable))
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (dish.isAvailable) Icons.Filled.Close else Icons.Filled.CheckCircle,
                                                        contentDescription = "toggle availability",
                                                        tint = LyoColors.TextPrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        if (dish.isAvailable) "Mark Out-Of-Stock" else "Mark Available",
                                                        fontSize = 11.sp,
                                                        color = LyoColors.TextPrimary
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(12.dp))
                                                
                                                IconButton(
                                                    onClick = { editingMenuItem = dish },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(Icons.Filled.Edit, contentDescription = "edit dish", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                                }
                                                
                                                Spacer(modifier = Modifier.width(6.dp))
                                                
                                                IconButton(
                                                    onClick = { menuItemToDelete = dish },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "delete dish", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Dialogue overlay for editing menu dish
                        if (editingMenuItem != null) {
                            val item = editingMenuItem!!
                            var nameEn by remember(item.id) { mutableStateOf(item.nameEn) }
                            var nameTa by remember(item.id) { mutableStateOf(item.nameTa) }
                            var descEn by remember(item.id) { mutableStateOf(item.descEn) }
                            var descTa by remember(item.id) { mutableStateOf(item.descTa) }
                            var priceStr by remember(item.id) { mutableStateOf(item.price.toInt().toString()) }
                            var isVeg by remember(item.id) { mutableStateOf(item.isVeg) }
                            var isAvailable by remember(item.id) { mutableStateOf(item.isAvailable) }
                            var autoOpen by remember(item.id) { mutableStateOf(item.autoOpenTime) }
                            var autoClose by remember(item.id) { mutableStateOf(item.autoCloseTime) }
                            var selectedCatId by remember(item.id) { mutableStateOf(item.categoryId) }
                            var imageUrl by remember(item.id) { mutableStateOf(item.imageUrl) }

                            AlertDialog(
                                onDismissRequest = { editingMenuItem = null },
                                title = { Text("Edit Dish Details", color = Color.White, fontWeight = FontWeight.Bold) },
                                containerColor = Color(0xFF1E293B),
                                text = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = nameEn,
                                            onValueChange = { nameEn = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            label = { Text("English Name") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = nameTa,
                                            onValueChange = { nameTa = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            label = { Text("Tamil Name Script") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = descEn,
                                            onValueChange = { descEn = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            label = { Text("English Description") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = descTa,
                                            onValueChange = { descTa = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            label = { Text("Tamil Description") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = imageUrl,
                                            onValueChange = { imageUrl = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            label = { Text("Food Photo Image URL (Unsplash or web link)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = priceStr,
                                                onValueChange = { priceStr = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                label = { Text("Price (₹)") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = isVeg,
                                                    onCheckedChange = { isVeg = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = LyoColors.VegGreen)
                                                )
                                                Text("Veg", color = Color.White, fontSize = 12.sp)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Active Instock Availability", color = Color.White, fontSize = 12.sp)
                                            Switch(
                                                checked = isAvailable,
                                                onCheckedChange = { isAvailable = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = LyoColors.AccentOrange)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("உணவு தானியங்கி நேரம் (Automatic Schedule):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = autoOpen,
                                                onValueChange = { autoOpen = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                label = { Text("OPEN (HH:MM)") },
                                                placeholder = { Text("08:00") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = autoClose,
                                                onValueChange = { autoClose = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                label = { Text("CLOSE (HH:MM)") },
                                                placeholder = { Text("22:00") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("Select billing category folder:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(categoriesList, key = { it.id }) { catItem ->
                                                val isThisCat = selectedCatId == catItem.id
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isThisCat) LyoColors.AccentOrange else Color(0x15FFFFFF))
                                                        .border(1.dp, if (isThisCat) LyoColors.AccentOrange else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                                        .clickable { selectedCatId = catItem.id }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(catItem.nameEn, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    LyoButton(
                                        text = "SAVE CHANGES",
                                        onClick = {
                                            val price = priceStr.toDoubleOrNull() ?: item.price
                                            val updated = item.copy(
                                                nameEn = nameEn.trim(),
                                                nameTa = nameTa.trim(),
                                                descEn = descEn.trim(),
                                                descTa = descTa.trim(),
                                                price = price,
                                                isVeg = isVeg,
                                                isAvailable = isAvailable,
                                                autoOpenTime = autoOpen.trim(),
                                                autoCloseTime = autoClose.trim(),
                                                categoryId = selectedCatId,
                                                imageUrl = imageUrl.trim()
                                            )
                                            viewModel.updateMenuItem(updated)
                                            editingMenuItem = null
                                        }
                                    )
                                },
                                dismissButton = {
                                    TextButton(onClick = { editingMenuItem = null }) {
                                        Text("CANCEL", color = LyoColors.TextSecondary)
                                    }
                                }
                            )
                        }

                        // Dialogue overlay for editing category
                        if (categoryToEdit != null) {
                            val catNode = categoryToEdit!!
                            var nameEn by remember(catNode.id) { mutableStateOf(catNode.nameEn) }
                            var nameTa by remember(catNode.id) { mutableStateOf(catNode.nameTa) }
                            var autoOpen by remember(catNode.id) { mutableStateOf(catNode.autoOpenTime) }
                            var autoClose by remember(catNode.id) { mutableStateOf(catNode.autoCloseTime) }

                            AlertDialog(
                                onDismissRequest = { categoryToEdit = null },
                                title = { Text("வகைப்பாட்டைத் திருத்து • Edit Category", color = Color.White, fontWeight = FontWeight.Bold) },
                                containerColor = Color(0xFF1E293B),
                                text = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = nameEn,
                                            onValueChange = { nameEn = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            label = { Text("English Category Name") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = nameTa,
                                            onValueChange = { nameTa = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                            label = { Text("Tamil Category Name (தமிழ் வடிவம்)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("வகைப்பாடு தானியங்கி நேரம் (Automatic Schedule):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = autoOpen,
                                                onValueChange = { autoOpen = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                label = { Text("OPEN (HH:MM)") },
                                                placeholder = { Text("08:00") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = autoClose,
                                                onValueChange = { autoClose = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                label = { Text("CLOSE (HH:MM)") },
                                                placeholder = { Text("22:00") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    LyoButton(
                                        text = "வகைப்பாட்டைச் சேமி (SAVE)",
                                        onClick = {
                                            if (nameEn.isNotBlank() && nameTa.isNotBlank()) {
                                                viewModel.updateCategory(
                                                    catNode.copy(
                                                        nameEn = nameEn.trim(),
                                                        nameTa = nameTa.trim(),
                                                        autoOpenTime = autoOpen.trim(),
                                                        autoCloseTime = autoClose.trim()
                                                    )
                                                )
                                                categoryToEdit = null
                                            }
                                        }
                                    )
                                },
                                dismissButton = {
                                    TextButton(onClick = { categoryToEdit = null }) {
                                        Text("CANCEL", color = LyoColors.TextSecondary)
                                    }
                                }
                            )
                        }

                        // Dialogue overlay for deleting category
                        if (categoryToDelete != null) {
                            val catNode = categoryToDelete!!
                            AlertDialog(
                                onDismissRequest = { categoryToDelete = null },
                                title = { Text("பிரிவை நீக்கலாமா? • Delete Category?", color = Color.White, fontWeight = FontWeight.Bold) },
                                text = { Text("வகைப்பாடு '${catNode.nameEn}' இனை முற்றிலும் நீக்கலாமா? இது அதிலுள்ள அனைத்து உணவுகளையும் நீக்கிவிடும்! \n\nAre you absolutely sure you want to delete Category '${catNode.nameEn}' and all its menu dishes? This action cannot be undone.", color = Color.LightGray, fontSize = 12.sp) },
                                containerColor = Color(0xFF1E293B),
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteCategory(catNode)
                                            categoryToDelete = null
                                        }
                                    ) {
                                        Text("ஆம், நீக்கு (YES, DELETE)", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { categoryToDelete = null }) {
                                        Text("CANCEL", color = Color.White)
                                    }
                                }
                            )
                        }

                        // Dialogue overlay for deleting menu item
                        if (menuItemToDelete != null) {
                            val dish = menuItemToDelete!!
                            AlertDialog(
                                onDismissRequest = { menuItemToDelete = null },
                                title = { Text("உணவை நீக்கலாமா? • Delete dish?", color = Color.White, fontWeight = FontWeight.Bold) },
                                text = { Text("Are you absolutely sure you want to delete '${dish.nameEn}'? This cannot be undone.", color = Color.LightGray, fontSize = 12.sp) },
                                containerColor = Color(0xFF1E293B),
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteMenuItem(dish)
                                            menuItemToDelete = null
                                        }
                                    ) {
                                        Text("ஆம், நீக்கு (YES, DELETE)", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { menuItemToDelete = null }) {
                                        Text("CANCEL", color = Color.White)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // DIALOG 1: SELECT RIDER FOR ASSIGNMENT
        if (showAssignRiderOrderId != null) {
            AlertDialog(
                onDismissRequest = { showAssignRiderOrderId = null },
                title = { 
                    Text(
                        text = "SELECT RIDER",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Choose a logistics partner to dispatch this cargo instantly.",
                            color = LyoColors.TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 1. Registered active riders in the platform
                        val activePlatformRiders = riders.filter { it.role == "DELIVERY" && it.isActiveRider }.distinctBy { it.phone }
                        if (activePlatformRiders.isNotEmpty()) {
                            Divider(
                                color = Color.White.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                            Text(
                                text = "கிடைக்கும் விநியோகஸ்தர்கள் • REGISTERED ON-DUTY AGENTS:",
                                color = LyoColors.AccentOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            activePlatformRiders.forEach { rdr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x1F22C55E), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0x3322C55E), RoundedCornerShape(8.dp))
                                        .clickable {
                                            confirmRiderToAssign = rdr
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.DirectionsBike, contentDescription = null, tint = LyoColors.VegGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rdr.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("📞 ${rdr.phone} | Bike: ${rdr.vehicleNo}", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .background(Color(0x1FEE4040), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFEE4040).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚠️ இப்போது active rider இல்லை — Settings-ல் rider சேர்க்கவும்",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                     }
                 },
                 confirmButton = {
                     TextButton(onClick = { showAssignRiderOrderId = null }) {
                         Text("CANCEL", color = LyoColors.TextSecondary)
                     }
                 },
                 containerColor = Color(0xFF1E293B)
             )
         }

         // DIALOG 2: AUTOMATED COMMUNICATIONS DISPATCH PANEL
         if (showDispatchConfirmDialog && dispatchOrderState != null) {
             val context = androidx.compose.ui.platform.LocalContext.current
             val ord = dispatchOrderState!!
             
             AlertDialog(
                 onDismissRequest = { showDispatchConfirmDialog = false },
                 title = {
                     Text(
                         text = "மின்னல் வேக கம்யூனிகேஷன்",
                         color = Color.White,
                         fontWeight = FontWeight.Bold,
                         fontSize = 16.sp
                     )
                 },
                 text = {
                     Column(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalAlignment = Alignment.CenterHorizontally
                     ) {
                         Box(
                             modifier = Modifier
                                 .size(56.dp)
                                 .clip(CircleShape)
                                 .background(Color(0x2222C55E)),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(
                                 imageVector = Icons.Filled.CheckCircle,
                                 contentDescription = null,
                                 tint = LyoColors.VegGreen,
                                 modifier = Modifier.size(36.dp)
                             )
                         }
                         
                         Spacer(modifier = Modifier.height(14.dp))
                         
                         Text(
                             text = "Rider Activated! 🛵",
                             color = Color.White,
                             fontWeight = FontWeight.ExtraBold,
                             fontSize = 16.sp
                         )
                         Text(
                             text = "$dispatchRiderNameState ($dispatchRiderPhoneState) has completed live courier registration. Status is updated to confirmation & preparation!",
                             color = LyoColors.TextSecondary,
                             textAlign = TextAlign.Center,
                             fontSize = 11.sp,
                             modifier = Modifier.padding(vertical = 8.dp)
                         )
                         
                         Divider(color = Color(0x1affffff), modifier = Modifier.padding(vertical = 12.dp))
                         
                         Text(
                             text = "AUTO DISPATCH TICKETS TO WHATSAPP:",
                             color = LyoColors.TextSecondary,
                             fontWeight = FontWeight.Bold,
                             fontSize = 10.sp,
                             modifier = Modifier.align(Alignment.Start)
                         )
                         
                         Spacer(modifier = Modifier.height(12.dp))
                         
                         // Action 1: Send Customer Invoice PDF
                         Button(
                             onClick = {
                                 LyoNotificationHelper.generateOrderPdfAndShare(context, ord, dispatchOrderItemsState)
                             },
                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                             shape = RoundedCornerShape(10.dp),
                             modifier = Modifier.fillMaxWidth()
                         ) {
                             Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.width(8.dp))
                             Text("CUSTOMER PDF BILL INVOICE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                         }
                         
                         Spacer(modifier = Modifier.height(10.dp))
                         
                         // Action 2: Send Owner KOT (No Price)
                         Button(
                             onClick = {
                                 try {
                                     val kotText = buildString {
                                         append("━━━━━━━━━━━━━━━━━━━━━━━\n")
                                         append("👨‍🍳 *LYO FRESH — KITCHEN ORDER (KOT)* 👨‍🍳\n")
                                         append("━━━━━━━━━━━━━━━━━━━━━━━\n")
                                         append("🏪 *உணவகம் (Shop):* ${dispatchVendorState?.name ?: "Lyo Partner"}\n")
                                         append("📦 *ஆர்டர் ஐடி (Order ID):* #Lyo-${ord.id}\n")
                                         append("━━━━━━━━━━━━━━━━━━━━━━━\n\n")
                                         append("*தயாரிக்க வேண்டிய உணவுகள் (Items):*\n")
                                         dispatchOrderItemsState.forEach { item ->
                                             append("   • *${item.quantity}x*  ${item.nameEn}")
                                             if (item.nameTa.isNotBlank() && item.nameTa != item.nameEn) {
                                                 append(" / ${item.nameTa}")
                                             }
                                             append("\n")
                                         }
                                         append("\n━━━━━━━━━━━━━━━━━━━━━━━\n")
                                         append("🛵 *விநியோக நபர் (Rider):* $dispatchRiderNameState ($dispatchRiderPhoneState)\n")
                                         append("⚠️ _No pricing details attached._\n")
                                         append("❤️ *நன்றி!* 🙏 — *Lyo AI*\n")
                                         append("━━━━━━━━━━━━━━━━━━━━━━━")
                                     }
                                     // WhatsApp direct integration below
                                     val phoneToUse = dispatchVendorState?.phone ?: ""
                                     val cleanPhone = phoneToUse.replace(Regex("[^0-9+]"), "").let { 
                                         if (it.length == 10) "91$it" else it 
                                     }
                                     com.example.WhatsAppHelper.sendMessage(context, cleanPhone, kotText)
                                 } catch (e: Exception) {
                                     e.printStackTrace()
                                 }
                             },
                             colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                             shape = RoundedCornerShape(10.dp),
                             modifier = Modifier.fillMaxWidth()
                         ) {
                             Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.width(8.dp))
                             Text("SEND KOT TO STORE OWNER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                         }
                     }
                 },
                 confirmButton = {
                     LyoButton(
                         text = "ALL DONE (முடிந்தது)",
                         onClick = { showDispatchConfirmDialog = false }
                     )
                 },
                 containerColor = Color(0xFF1E293B)
             )
         }
     }

     if (confirmRiderToAssign != null) {
         val rdr = confirmRiderToAssign!!
         AlertDialog(
             onDismissRequest = { confirmRiderToAssign = null },
             title = {
                 Text(
                     text = "விநியோகஸ்தரை நியமிக்கவா? (Confirm Rider?)",
                     color = Color.White,
                     fontWeight = FontWeight.Bold,
                     fontSize = 16.sp
                 )
             },
             text = {
                 Text(
                     text = "ஆர்டரை விநியோகிக்க '${rdr.name}' (${rdr.phone}) என்பவரை நியமிக்க விரும்புகிறீர்களா?\n\nAre you sure you want to assign '${rdr.name}' to this order?",
                     color = Color.LightGray,
                     fontSize = 13.sp
                 )
             },
             confirmButton = {
                 TextButton(
                     onClick = {
                         val currentOrderId = showAssignRiderOrderId
                         confirmRiderToAssign = null
                         if (currentOrderId != null) {
                             viewModel.assignRiderToOrder(
                                 orderId = currentOrderId,
                                 riderName = rdr.name,
                                 riderPhone = rdr.phone
                             ) { order, vendor, items, customer ->
                                 dispatchOrderState = order
                                 dispatchVendorState = vendor
                                 dispatchOrderItemsState = items
                                 dispatchRiderNameState = rdr.name
                                 dispatchRiderPhoneState = rdr.phone
                                 showDispatchConfirmDialog = true
                                 // AUTOMATIC TRIGGER 1: WhatsApp messages to Customer and Restaurant Owner KOT
                                 val settings = com.example.WhatsAppHelper.getSettings(context)
                                 val cName = customer?.name ?: "Lyo Customer"
                                 val cPhone = customer?.phone ?: order.userId
                                 val cAddr = customer?.address ?: "Lyo Delivery Address"
                                 com.example.WhatsAppHelper.sendOrderAssignedMessages(
                                     context = context,
                                     order = order,
                                     items = items,
                                     settings = settings,
                                     customerName = cName,
                                     customerPhone = cPhone,
                                     deliveryAddress = cAddr,
                                     riderName = rdr.name
                                 )
                                 showAssignRiderOrderId = null
                             }
                         }
                     }
                 ) {
                     Text("YES / CONFIRM", color = LyoColors.AccentOrange, fontWeight = FontWeight.Bold)
                 }
             },
             dismissButton = {
                 TextButton(onClick = { confirmRiderToAssign = null }) {
                     Text("CANCEL", color = LyoColors.TextSecondary)
                 }
             },
             containerColor = Color(0xFF1E293B)
         )
     }
 }

@Composable
fun RidersManagementTab(viewModel: AdminViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val riders by viewModel.allRiders.collectAsState()
    val uniqueRiders = remember(riders) { riders.distinctBy { it.phone } }
    
    val name by viewModel.newRiderName.collectAsState()
    val phone by viewModel.newRiderPhone.collectAsState()
    val email by viewModel.newRiderEmail.collectAsState()
    val password by viewModel.newRiderPassword.collectAsState()
    val vehicleNo by viewModel.newRiderVehicleNo.collectAsState()
    val address by viewModel.newRiderAddress.collectAsState()
    
    var errorTxt by remember { mutableStateOf("") }
    var successTxt by remember { mutableStateOf("") }
    var riderPasswordVisible by remember { mutableStateOf(false) }
    var riderToEdit by remember { mutableStateOf<User?>(null) }
    var riderToDelete by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(name, phone, password, vehicleNo) {
        if (name.isNotEmpty() || phone.isNotEmpty() || password.isNotEmpty() || vehicleNo.isNotEmpty()) {
            errorTxt = ""
            successTxt = ""
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(20.dp)
    ) {
        // Section: Onboard New Rider Form Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add Delivery Partner",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (errorTxt.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                            .border(1.dp, LyoColors.NonVegRed, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(text = errorTxt, fontSize = 12.sp, color = Color(0xFFFCA5A5))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (successTxt.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x3322C55E), RoundedCornerShape(8.dp))
                            .border(1.dp, LyoColors.VegGreen, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(text = successTxt, fontSize = 12.sp, color = Color(0xFFA7F3D0))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.newRiderName.value = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = LyoColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.newRiderPhone.value = it },
                    label = { Text("Mobile (10 digits)") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = LyoColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.newRiderPassword.value = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = LyoColors.TextSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { riderPasswordVisible = !riderPasswordVisible }) {
                            Icon(
                                imageVector = if (riderPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "toggle password visibility",
                                tint = LyoColors.TextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    singleLine = true,
                    visualTransformation = if (riderPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = vehicleNo,
                    onValueChange = { viewModel.newRiderVehicleNo.value = it },
                    label = { Text("Vehicle Reg No") },
                    leadingIcon = { Icon(Icons.Filled.TwoWheeler, contentDescription = null, tint = LyoColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.newRiderEmail.value = it },
                    label = { Text("Email (Optional)") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = LyoColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { viewModel.newRiderAddress.value = it },
                    label = { Text("Base Address (Optional)") },
                    leadingIcon = { Icon(Icons.Filled.Map, contentDescription = null, tint = LyoColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("💵 SALARY CONFIGURATION / ஊதிய முறை:", color = LyoColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                val selectedSalaryType by viewModel.newRiderSalaryType.collectAsState()
                val salaryRateStr by viewModel.newRiderSalaryRate.collectAsState()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Salary Type Tab Bar Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), CircleShape)
                            .border(1.dp, Color(0x33F8FAFC), CircleShape)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("MONTHLY" to "Monthly Base (மாதச் சம்பளம் 💰)", "PER_KM" to "Per Km (கி.மீ சம்பளம் 🏍️)").forEach { (typeVal, typeLabel) ->
                            val isSelected = selectedSalaryType == typeVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) LyoColors.AccentOrange else Color.Transparent)
                                    .clickable { viewModel.newRiderSalaryType.value = typeVal }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = typeLabel,
                                    color = if (isSelected) Color.White else LyoColors.TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Salary Rate input field
                    OutlinedTextField(
                        value = salaryRateStr,
                        onValueChange = { viewModel.newRiderSalaryRate.value = it },
                        label = { Text(if (selectedSalaryType == "MONTHLY") "Monthly Base Pay (மாத சம்பளத்தொகை) - ₹" else "Rate per Kilometer (ஒரு கி.மீ-க்கான தொகை) - ₹") },
                        leadingIcon = { Icon(Icons.Filled.CurrencyRupee, contentDescription = null, tint = LyoColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LyoButton(
                    text = "RECRUIT RIDER PARTNER",
                    onClick = {
                        viewModel.onboardRider(
                            onSuccess = {
                                successTxt = "Rider registered successfully!"
                                errorTxt = ""
                            },
                            onError = { err ->
                                errorTxt = err
                                successTxt = ""
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section: Active Fleet Listings Heading
        item {
            Text(
                text = "ACTIVE FLEETS AND LOGISTICS AGENTS (${uniqueRiders.size} partners)",
                color = LyoColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )
        }

        if (uniqueRiders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No riders registered yet.",
                        color = LyoColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        items(uniqueRiders, key = { it.phone }) { rider ->
            Box(modifier = Modifier.padding(vertical = 6.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (rider.isActiveRider) Color(0x3322C55E) else Color(0x33EF4444)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header Row: Avatar, Name & Status and Toggle Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Avatar Profile Frame with custom bike vector
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (rider.isActiveRider) Color(0x2222C55E) else Color(0x22EF4444),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.TwoWheeler,
                                        contentDescription = null,
                                        tint = if (rider.isActiveRider) LyoColors.VegGreen else LyoColors.NonVegRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = rider.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // Status Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (rider.isActiveRider) LyoColors.VegGreen else LyoColors.NonVegRed,
                                                    CircleShape
                                                )
                                        )
                                        Text(
                                            text = if (rider.isActiveRider) "ONLINE" else "OFFLINE",
                                            color = if (rider.isActiveRider) LyoColors.VegGreen else LyoColors.NonVegRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }

                            // Active Switch (Availability toggler) with labels
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (rider.isActiveRider) "ACTIVE" else "INACTIVE",
                                    color = if (rider.isActiveRider) LyoColors.VegGreen else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Switch(
                                    checked = rider.isActiveRider,
                                    onCheckedChange = { viewModel.toggleRiderActiveStatus(rider) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = LyoColors.VegGreen,
                                        checkedTrackColor = Color(0x3322C55E),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color(0x1Fffffff)
                                    )
                                )
                            }
                        }

                        // Info Column block: Phone, Vehicle, Base Location & Salary Details
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x0EFFFFFF), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "📞 ${rider.phone}",
                                    color = LyoColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "🏍️ ${rider.vehicleNo}",
                                    color = LyoColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text(
                                text = "📍 Base: ${rider.address}",
                                color = LyoColors.TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            val salaryTypeTa = if (rider.salaryType == "MONTHLY") "மாதச் சம்பளம்" else "கி.மீ சம்பளம்"
                            val salaryTypeEn = if (rider.salaryType == "MONTHLY") "Monthly" else "Per Km"
                            Text(
                                text = "💰 Salary / ஊதியம்: ₹${rider.salaryRate.toInt()} / $salaryTypeEn ($salaryTypeTa)",
                                color = Color(0xFFA5F3FC),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Row 3: Action Buttons (WhatsApp Chat, Edit Form, Decline/Delete)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val strippedPhone = rider.phone.replace(" ", "").replace("+", "")
                                    val finalPhone = if (strippedPhone.startsWith("91")) strippedPhone else "91$strippedPhone"
                                    com.example.WhatsAppHelper.sendMessage(
                                        context,
                                        finalPhone,
                                        "Hello ${rider.name}, Lyo Fresh Admin here. Are you available for pickup?"
                                    )
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x1F22C55E), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Chat,
                                    contentDescription = "chat on WhatsApp",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { riderToEdit = rider },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x1F38BDF8), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit details",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { riderToDelete = rider },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x1AEF4444), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Decline rider",
                                    tint = LyoColors.NonVegRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (riderToEdit != null) {
        val currentRider = riderToEdit!!
        var editName by remember(currentRider) { mutableStateOf(currentRider.name) }
        var editVehicle by remember(currentRider) { mutableStateOf(currentRider.vehicleNo) }
        var editAddress by remember(currentRider) { mutableStateOf(currentRider.address) }
        var editEmail by remember(currentRider) { mutableStateOf(currentRider.email) }
        var editPassword by remember(currentRider) { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }
        var editSalaryType by remember(currentRider) { mutableStateOf(currentRider.salaryType) }
        var editSalaryRate by remember(currentRider) { mutableStateOf(currentRider.salaryRate.toString()) }

        AlertDialog(
            onDismissRequest = { riderToEdit = null },
            title = { Text("திருத்து: ${currentRider.phone}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("பெயர் (Full Name)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editVehicle,
                        onValueChange = { editVehicle = it },
                        label = { Text("வண்டி எண் (Vehicle No)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("முகவரி (Base Address)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("மின்னஞ்சல் (Email - Optional)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it },
                        label = { Text("கடவுச்சொல் (Password)", color = Color.Gray) },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = LyoColors.TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("💵 SALARY CONFIGURATION / ஊதிய முறை:", color = LyoColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    // Salary Type Tab Bar Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), CircleShape)
                            .border(1.dp, Color(0x33F8FAFC), CircleShape)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("MONTHLY" to "Monthly (மாதச் சம்பளம் 💰)", "PER_KM" to "Per Km (கி.மீ சம்பளம் 🏍️)").forEach { (typeVal, typeLabel) ->
                            val isSelected = editSalaryType == typeVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) LyoColors.AccentOrange else Color.Transparent)
                                    .clickable { editSalaryType = typeVal }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = typeLabel,
                                    color = if (isSelected) Color.White else LyoColors.TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Salary Rate input field
                    OutlinedTextField(
                        value = editSalaryRate,
                        onValueChange = { editSalaryRate = it },
                        label = { Text(if (editSalaryType == "MONTHLY") "Monthly Base Pay (மாத சம்பளத்தொகை) - ₹" else "Rate per Kilometer (ஒரு கி.மீ-க்கான தொகை) - ₹") },
                        leadingIcon = { Icon(Icons.Filled.CurrencyRupee, contentDescription = null, tint = LyoColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange, unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                    onClick = {
                        val updated = currentRider.copy(
                            name = editName,
                            vehicleNo = editVehicle,
                            address = editAddress,
                            email = editEmail,
                            salaryType = editSalaryType,
                            salaryRate = editSalaryRate.toDoubleOrNull() ?: currentRider.salaryRate
                        )
                        viewModel.updateRider(updated, editPassword.ifBlank { null })
                        riderToEdit = null
                    }
                ) {
                    Text("சேமி (SAVE)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { riderToEdit = null }) {
                    Text("CANCEL", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (riderToDelete != null) {
        AlertDialog(
            onDismissRequest = { riderToDelete = null },
            title = { Text("Delete Rider Account (டெலிவரி பார்ட்னரை நீக்கு)", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete delivery partner \"${riderToDelete?.name}\"? Their account and associated delivery profile will be wiped from this device database.\n\nஇந்த டெலிவரி பார்ட்னர் '${riderToDelete?.name}'-ஐ நிரந்தரமாக நீக்க வேண்டுமா? இது இந்த device database-ல் இருந்து மட்டும் நீக்கப்படும்.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.NonVegRed),
                    onClick = {
                        riderToDelete?.let { viewModel.deleteRider(it) }
                        riderToDelete = null
                    }
                ) {
                    Text("Delete Permanently (நீக்கு)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { riderToDelete = null }) {
                    Text("Cancel (ரத்து)", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun CustomersManagementTab(viewModel: AdminViewModel) {
    val customers by viewModel.allCustomers.collectAsState()
    var customerToDelete by remember { mutableStateOf<User?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "CUSTOMER DIRECTORY (${customers.size} Active)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = LyoColors.AccentOrange,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View details and manage registered customer credentials. Swipe or tap to delete inactive or test profiles securely.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 15.sp
                )
            }
        }

        if (customers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = "No customers",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No customers registered yet.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(customers, key = { it.phone }) { customer ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF0F172A), CircleShape)
                                    .border(1.dp, LyoColors.AccentOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "User",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = customer.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = "phone",
                                        tint = LyoColors.TextSecondary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+91 " + customer.phone,
                                        color = LyoColors.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                if (!customer.email.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Email,
                                            contentDescription = "email",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = customer.email,
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "secured hash",
                                        tint = Color(0xFFA5F3FC),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Password: Protected (••••••••)",
                                        color = Color(0xFFA5F3FC),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { customerToDelete = customer },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x1AEF4444), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete customer",
                                tint = LyoColors.NonVegRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer Account", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete customer profile \"${customerToDelete?.name}\"? All of their historic order records and database credentials will be wiped from this device database.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.NonVegRed),
                    onClick = {
                        customerToDelete?.let { viewModel.deleteCustomer(it) }
                        customerToDelete = null
                    }
                ) {
                    Text("Delete Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun AdminsManagementTab(viewModel: AdminViewModel) {
    val admins by viewModel.allAdmins.collectAsState()
    
    var adminName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var opError by remember { mutableStateOf<String?>(null) }
    var opSuccessMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ADMINISTRATORS LIST (${admins.size} / 6 Slots)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = LyoColors.AccentOrange,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Super Admin Active",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "As Super Admin Anantharajmech (Eswaran), you can manage and create custom secondary helper admin logins with distinct password codes to operate the supply portal simultaneously.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 15.sp
                )
            }
        }

        // Error & Success Feedback
        if (opError != null || opSuccessMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (opError != null) Color(0x33EF4444) else Color(0x3310B981)
                        )
                        .border(
                            1.dp,
                            if (opError != null) Color(0xFFEF4444) else Color(0xFF10B981),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = opError ?: opSuccessMessage ?: "",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // New Admin Creator Form
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "⚡ CREATE NEW AUXILIARY ADMIN CREDENTIALS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = adminName,
                        onValueChange = { adminName = it },
                        label = { Text("Admin Full Name", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Balaji E") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username / Phone", fontSize = 12.sp) },
                        placeholder = { Text("e.g. balaji_lyo") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password Code", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Code2026") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            opError = null
                            opSuccessMessage = null
                            if (admins.size >= 6) {
                                opError = "மன்னிக்கவும்! நீங்கள் அதிகபட்சமாக 5 அல்லது 6 அட்மின் வரை மட்டுமே உருவாக்க முடியும் (Standard threshold reaches 6 admins limit reached)."
                                return@Button
                            }
                            viewModel.createOrUpdateAdmin(
                                username = username.trim(),
                                name = adminName.trim(),
                                pass = password.trim(),
                                onSuccess = {
                                    username = ""
                                    adminName = ""
                                    password = ""
                                    opSuccessMessage = "புதிய அட்மின் கணக்கு வெற்றிகரமாக உருவாக்கப்பட்டது! New helper admin credential generated successfully."
                                },
                                onError = { opError = it }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CREATE ADMIN LOGINS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }

        // Seeded & helper accounts heading
        item {
            Text(
                text = "REGISTERED HELPER CONSOLE ACCOUNTS",
                color = LyoColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )
        }

        // Active admin list
        items(admins, key = { it.phone }) { admin ->
            val isSuper = admin.phone == "Anantharajmech"
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0F172A), CircleShape)
                                .border(1.dp, if (isSuper) LyoColors.AccentOrange else Color(0x33FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSuper) Icons.Filled.Star else Icons.Filled.AdminPanelSettings,
                                contentDescription = "Admin icon",
                                tint = if (isSuper) LyoColors.AccentOrange else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isSuper) "Eswaran Super Admin (Anantharajmech)" else "${admin.name} (${admin.phone})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (isSuper) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0x33F97316))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("OWNER", color = Color(0xFFF97316), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(
                                text = "Admin Account",
                                color = Color(0xFFA5F3FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!isSuper) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Edit trigger
                            IconButton(
                                onClick = {
                                    username = admin.phone
                                    adminName = admin.name
                                    password = ""
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0x15FFFFFF), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit admin",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteAdmin(admin) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0x1AEF4444), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete admin",
                                    tint = LyoColors.NonVegRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FirebaseSettingsTab(viewModel: com.example.ui.viewmodels.AdminViewModel) {
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf("Database synchronized successfully") }
    var lastSyncedTime by remember { mutableStateOf("Just now") }
    val coroutineScope = rememberCoroutineScope()
    val radiusState by viewModel.maxStoreDistanceRadius.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "FIREBASE CONNECTOR CONSOLE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = LyoColors.AccentOrange,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Firebase இணைப்பு & WhatsApp அமைவுகள்",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 15.sp
                )
            }
        }

        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            var waSettings by remember { mutableStateOf(com.example.WhatsAppHelper.getSettings(context)) }
            
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "💬 WhatsApp Automation Settings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF25D366)
                    )
                    
                    Text(
                        text = "Configure KOT automatic message triggers and restaurant profile settings for instant WhatsApp routing.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    // Restaurant owner phone
                    OutlinedTextField(
                        value = waSettings.restaurantOwnerPhone,
                        onValueChange = { newVal ->
                            val updated = waSettings.copy(restaurantOwnerPhone = newVal)
                            waSettings = updated
                            com.example.WhatsAppHelper.saveSettings(context, updated)
                        },
                        label = { Text("Restaurant Owner WhatsApp Number", fontSize = 12.sp, color = Color.White) },
                        placeholder = { Text("9999999999", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF25D366),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Text(
                        text = "KOT automatic-ஆக இந்த number-க்கு போகும் (Format: 10-digit number template e.g. 9840112233)",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    
                    // Restaurant name
                    OutlinedTextField(
                        value = waSettings.restaurantName,
                        onValueChange = { newVal ->
                            val updated = waSettings.copy(restaurantName = newVal)
                            waSettings = updated
                            com.example.WhatsAppHelper.saveSettings(context, updated)
                        },
                        label = { Text("Restaurant Name", fontSize = 12.sp, color = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF25D366),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    
                    // Test button
                    Button(
                        onClick = {
                            val testMsg = "✅ Lyo WhatsApp connection test successful!\nKOT messages இந்த number-க்கு வரும்."
                            com.example.WhatsAppHelper.sendMessage(context, waSettings.restaurantOwnerPhone, testMsg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = "phone", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("📲 Test WhatsApp Connection", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            var gstEnabledState by remember { mutableStateOf(viewModel.repository.gstEnabled) }
            var gstRateState by remember { mutableStateOf(viewModel.repository.gstRate) }
            var gstRateInput by remember { mutableStateOf(viewModel.repository.gstRate.toString()) }

            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚖️ GST Taxation Settings (ஜிஎஸ்டி வரி அமைப்பு)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LyoColors.AmberYellow
                    )
                    
                    Text(
                        text = "உணவுப் பொருட்களுக்கான GST வரியை தேவைப்பட்டால் இங்கு இயக்கவும் அல்லது அணைக்கவும். GST சதவீதத்தை மாற்றிக்கொள்ளலாம்.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable GST Tax (ஜிஎஸ்டி வரி)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(if (gstEnabledState) "Status: ENABLED" else "Status: DISABLED (ஜிஎஸ்டி கிடையாது)", color = if (gstEnabledState) LyoColors.VegGreen else Color.LightGray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = gstEnabledState,
                            onCheckedChange = { checked ->
                                gstEnabledState = checked
                                viewModel.repository.updateGstSettings(context, checked, gstRateState)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LyoColors.AccentOrange,
                                checkedTrackColor = LyoColors.AccentOrange.copy(alpha = 0.5f)
                            )
                        )
                    }

                    if (gstEnabledState) {
                        OutlinedTextField(
                            value = gstRateInput,
                            onValueChange = { newVal ->
                                gstRateInput = newVal
                                newVal.toDoubleOrNull()?.let { rate ->
                                    gstRateState = rate
                                    viewModel.repository.updateGstSettings(context, gstEnabledState, rate)
                                }
                            },
                            label = { Text("GST Percentage Rate (%)", fontSize = 12.sp, color = Color.White) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LyoColors.AccentOrange,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DEFAULT ONBOARDING VISIBILITY RADIUS",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "புதிய கடைகளுக்கான விசிபிலிட்டி ரேடியஸ் (Default)",
                                color = LyoColors.AccentOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF38BDF8).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", radiusState)} KM",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Define the default geographic radius/distance filters set for newly onboarded merchants, bakers, and kitchens.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Slider(
                        value = radiusState.toFloat(),
                        onValueChange = { newVal ->
                            viewModel.updateStoreVisibilityRadius(newVal.toDouble())
                        },
                        valueRange = 1.0f..100.0f,
                        steps = 99,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF38BDF8),
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1.0 KM (Local)", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("50.0 KM (Midsized)", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("100.0 KM (Maximum)", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FIREBASE STATUS",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF22C55E).copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                                .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE CONNECTED",
                                color = Color(0xFF22C55E),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FirebaseKeyRow(label = "Application ID (Android)", value = com.example.BuildConfig.FIREBASE_APP_ID)
                    FirebaseKeyRow(label = "Project Unique ID", value = com.example.BuildConfig.FIREBASE_PROJECT_ID)
                    FirebaseKeyRow(label = "Security API Secret Key", value = com.example.BuildConfig.FIREBASE_API_KEY, isSecret = true)
                    FirebaseKeyRow(label = "Database URL", value = com.example.BuildConfig.FIREBASE_DATABASE_URL)
                    FirebaseKeyRow(label = "Storage Bucket Node", value = com.example.BuildConfig.FIREBASE_STORAGE_BUCKET)
                    FirebaseKeyRow(label = "Messaging Sender ID", value = "368208047268")
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE CLOUD DATABASE SYNC ENGINE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Synchronize local SQLite Room databases with remote Cloud Firestore and Realtime Database securely using encrypted secure protocols.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Last Synced: $lastSyncedTime",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val isFirebaseConnected = com.example.data.repository.LyoFirebaseHelper.auth != null
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Firebase: ",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (isFirebaseConnected) "CONNECTED" else "OFFLINE",
                                    color = if (isFirebaseConnected) LyoColors.VegGreen else Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                isSyncing = true
                                viewModel.syncWithFirestore { msg ->
                                    isSyncing = false
                                    syncMessage = msg
                                    lastSyncedTime = "Just now"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isSyncing
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Handshaking...", fontSize = 12.sp, color = Color.White)
                            } else {
                                Icon(Icons.Filled.Sync, contentDescription = "sync", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "info",
                                tint = LyoColors.AccentOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = syncMessage,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FirebaseKeyRow(label: String, value: String, isSecret: Boolean = false) {
    val displayValue = if (isSecret && value.length >= 12) {
        value.take(8) + "••••••••••••••••••" + value.takeLast(4)
    } else {
        value
    }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = LyoColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x1A000000), RoundedCornerShape(6.dp))
                .border(0.5.dp, Color(0x33F8FAFC), RoundedCornerShape(6.dp))
                .padding(8.dp, 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = displayValue,
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isSecret) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "locked",
                        tint = LyoColors.AccentOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        if (isSecret) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🔒 Firebase Console-ல் மட்டுமே full key காண முடியும்",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AnalyticsDashboardTab(viewModel: AdminViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val vendors by viewModel.allVendors.collectAsState()
    val riders by viewModel.allRiders.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()

    val completedOrders = orders.filter { it.status == "DELIVERED" }
    val pendingOrders = orders.filter { it.status != "DELIVERED" && it.status != "CANCELLED" }
    
    val totalRevenue = completedOrders.sumOf { it.totalAmount }
    val platformEarned = totalRevenue * 0.15

    val hotVendor = if (orders.isNotEmpty() && vendors.isNotEmpty()) {
        val group = orders.groupBy { it.vendorId }
        val maxId = group.maxByOrNull { it.value.size }?.key
        vendors.find { it.id == maxId }?.name ?: "—"
    } else {
        "—"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "PLATFORM CONTROL TOWER",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = LyoColors.AccentOrange,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "A consolidated live intelligence hub reflecting financial yield, logistic volume, and hot-zone vendor performance throughout the region.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 15.sp
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = LyoColors.VegGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TOTAL SALES GMV",
                            color = LyoColors.TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "₹${String.format("%.2f", totalRevenue)}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = "Share",
                            tint = LyoColors.AccentOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "LYO NET INCOME (15%)",
                            color = LyoColors.TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "₹${String.format("%.2f", platformEarned)}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AnalyticsStatCounter(label = "STORES", count = vendors.size.toString(), color = Color(0xFFA5F3FC))
                    AnalyticsStatCounter(label = "RIDERS", count = riders.size.toString(), color = Color(0xFFFDE047))
                    AnalyticsStatCounter(label = "CUSTOMERS", count = customers.size.toString(), color = Color(0xFFF472B6))
                    AnalyticsStatCounter(label = "ACTIVE ORDERS", count = pendingOrders.size.toString(), color = LyoColors.AccentOrange)
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF1E293B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "star",
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "LEADERBOARD MERCHANT OF THE WEEK",
                            color = LyoColors.AccentOrange,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = hotVendor,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Top volume driver with 99.1% kitchen cooking fulfillment speed",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE REGIONAL LOGISTICS DISPATCH FRACTIONS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val totalCount = orders.size.toDouble().coerceAtLeast(1.0)
                    val placedPct = orders.count { it.status == "PLACED" } / totalCount
                    val prepPct = orders.count { it.status == "PREPARING" || it.status == "ACCEPTED" || it.status == "COOKING" } / totalCount
                    val transitPct = orders.count { it.status == "OUT_FOR_DELIVERY" } / totalCount
                    val donePct = orders.count { it.status == "DELIVERED" } / totalCount

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF334155))
                    ) {
                        if (placedPct > 0) {
                            Box(modifier = Modifier.fillMaxHeight().weight(placedPct.toFloat()).background(Color(0xFFEA580C)))
                        }
                        if (prepPct > 0) {
                            Box(modifier = Modifier.fillMaxHeight().weight(prepPct.toFloat()).background(Color(0xFFD97706)))
                        }
                        if (transitPct > 0) {
                            Box(modifier = Modifier.fillMaxHeight().weight(transitPct.toFloat()).background(Color(0xFF0284C7)))
                        }
                        if (donePct > 0) {
                            Box(modifier = Modifier.fillMaxHeight().weight(donePct.toFloat()).background(LyoColors.VegGreen))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnalyticsProportionLegendItem(color = Color(0xFFEA580C), label = "Placed")
                        AnalyticsProportionLegendItem(color = Color(0xFFD97706), label = "Cooking")
                        AnalyticsProportionLegendItem(color = Color(0xFF0284C7), label = "Transit")
                        AnalyticsProportionLegendItem(color = LyoColors.VegGreen, label = "Delivered")
                    }
                }
            }
        }

        item {
            var broadcastText by remember { mutableStateOf("") }
            val promoBanners by viewModel.allPromoBanners.collectAsState()
            val activeBroadcast = promoBanners.find { it.code == "AI_BROADCAST_PROMO" }
            val context = androidx.compose.ui.platform.LocalContext.current
            
            val recordSpeechLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val data = result.data
                    val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                    val spokenText = results?.firstOrNull() ?: ""
                    if (spokenText.isNotBlank()) {
                        broadcastText = spokenText
                    }
                }
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = LyoColors.AccentOrange.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = "AI Broadcaster",
                                tint = LyoColors.AccentOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LYO AI ADMIN BROADCASTER 📢",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                        if (activeBroadcast != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE PINNED", color = Color(0xFF22C55E), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "உடன் பரிந்துரைக்க விரும்பும் அறிவிப்பு/சலுகையை இங்கே டைப் செய்து ஸ்பார்க் செய்யவும். Lyo AI கஸ்டமர்களுக்கு இதையே முதன்மைப்படுத்தி பரிந்துரைக்கும்!",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = broadcastText,
                        onValueChange = { broadcastText = it },
                        placeholder = { Text("எ.கா. சேலம் ஐயர் பேக்கரியில் இன்று 50% சலுகை! உடனே சொல்லுங்கள் Lyo AI", color = LyoColors.TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
                                            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "அறிவிப்பை பேசவும்...")
                                        }
                                        recordSpeechLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Voice input not supported", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = "voice input", tint = LyoColors.AccentOrange, modifier = Modifier.size(18.dp))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val text = broadcastText.trim()
                                if (text.isNotBlank()) {
                                    if (activeBroadcast != null) {
                                        viewModel.deletePromoBanner(activeBroadcast)
                                    }
                                    viewModel.insertPromoBanner(
                                        PromoBanner(
                                            code = "AI_BROADCAST_PROMO",
                                            description = text,
                                            imageUrl = ""
                                        )
                                    )
                                    broadcastText = ""
                                    android.widget.Toast.makeText(context, "Broadcast Sparked Successfully! 🚀", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                            shape = RoundedCornerShape(8.dp),
                            enabled = broadcastText.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🚀 SPARK BROADCAST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        
                        if (activeBroadcast != null) {
                            Button(
                                onClick = {
                                    viewModel.deletePromoBanner(activeBroadcast)
                                    android.widget.Toast.makeText(context, "Clear success", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("CLEAR PIN ❌", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    
                    if (activeBroadcast != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x0AFFFFFF), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Current Active AI recommendation pin:", color = LyoColors.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(activeBroadcast.description, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsStatCounter(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = LyoColors.TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnalyticsProportionLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

data class PromoPreset(val name: String, val code: String, val desc: String, val url: String)

@Composable
fun BannersManagementTab(
    viewModel: AdminViewModel,
    onBrowsePhoto: () -> Unit,
    selectedImageUriStr: String?,
    onClearSelectedImage: () -> Unit
) {
    val promoBanners by viewModel.allPromoBanners.collectAsState()

    var promoCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    LaunchedEffect(selectedImageUriStr) {
        if (selectedImageUriStr != null) {
            imageUrl = selectedImageUriStr
            onClearSelectedImage()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(20.dp)
    ) {
        // Form Checklist: preset options
        item {
            GlassCard(
                cornerRadius = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                borderColor = Color(0x66F59E0B),
                backgroundColor = Color(0xFF0F172A)
            ) {
                Column {
                    Text(
                        text = "⚡ PRESET HERO BANNER DECK CREATOR",
                        color = LyoColors.AmberYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "10 to 15+ பிரீமியம் பிக்சர்ஸ் உடனே சேர்க்கும் ஆப்ஷன். கீழே உள்ள பட்டனை தட்டினால் 15 விசேஷ சலுகைகள் ஆட்டோமேட்டிக்காக ஆப்பில் சேர்ந்து விடும்!",
                        color = LyoColors.TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    LyoButton(
                        text = "⚡ LOAD ALL 15 PREMIUM DEALS CAROUSELS AT ONCE",
                        onClick = {
                            val samples = listOf(
                                PromoBanner(code = "LYOBIRYANI", description = "எடப்பாடி ஸ்பெஷல் மட்டன் பிரியாணி • ₹80 தள்ளுபடி! 🍛", imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYODOSA", description = "சூடான நெய் ரோஸ்ட் தோசை மாம்பழ சட்னி • 30% சலுகை! 🥞", imageUrl = "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOIDLI", description = "ஆவியில் வெந்த பஞ்சு போன்ற மல்லிகைப்பூ இட்லி • இலவச டெலிவரி! ☕", imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOPAROTTA", description = "நறுக்கிய மொறுமொறுப்பான கொத்து பரோட்டாக்கள் • ₹50 தள்ளுபடி! 🍽️", imageUrl = "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOTEA", description = "நறுமணமுள்ள இஞ்சி ஏலக்காய் டீCombo • ஒன்று வாங்கினால் ஒன்று இலவசம்! ☕", imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOCAKE", description = "கொண்டாட்ட விசேஷ சாக்லேட் கேக்குகள் • இலவச ஸ்பெஷல் ஐஸ்கிரீம் ஸ்கூப் 🎂", imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOPIZZA", description = "சீஸ் நிறைந்த டபுள் சீஸ் லோடட் வெஜ் பிஸ்ஸா • ₹120 சேமிப்பு! 🍕", imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOSNACKS", description = "மாலை நேர காரசாரமான வெங்காய சமோசா மற்றும் வடை • 20% தள்ளுபடி! 🧆", imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOMEALS", description = "அசல் பாரம்பரிய தென்னிந்திய மதிய சாப்பாடு காம்போ • ₹40 தள்ளுபடி! 🍱", imageUrl = "https://images.unsplash.com/photo-1610192244261-3f33de3f55e4?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOSHAKES", description = "ஜில்லென்ற சுவையான பிரெஷ் மேங்கோ மில்க்ஷேக் • 15% உடனடி சலுகை! 🥭", imageUrl = "https://images.unsplash.com/photo-1579954115545-a95591f28bfc?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOCHINESE", description = "காரசாரமான கோபி மஞ்சூரியன் மற்றும் பிரைடு ரைஸ் • ₹40 சேமிப்பு! 🍜", imageUrl = "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOSWEETS", description = "பண்டிகை கால சுத்தமான நெய் மைசூர் பாக் காம்போ • ₹100 சலுகை 🍬", imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOBURGER", description = "செம காரமான சிக்கன் பர்கர் உடன் குளிர்பானங்கள் இலவசம் • ₹150 மட்டும்! 🍔", imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOCURRY", description = "சுவையான பட்டர் சிக்கன் மற்றும் பன்னீர் பட்டர் மசாலா • ₹100 சேமிப்பு! 🍛", imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=800&auto=format&fit=crop&q=80"),
                                PromoBanner(code = "LYOICECREAM", description = "சுவையான ஐஸ்கிரீம் சண்டே காம்போ பேக் • வெறும் ₹99 முதல்! 🍨", imageUrl = "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=800&auto=format&fit=crop&q=80")
                            )
                            samples.forEach { s ->
                                viewModel.insertPromoBanner(s)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("💡 CHOOSE A SINGLE PRESET DEAL TO PRE-POPULATE THE FORM:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val context = androidx.compose.ui.platform.LocalContext.current
                    val samples = listOf(
                        PromoPreset("Biryani 🍛", "LYOBIRYANI", "எடப்பாடி ஸ்பெஷல் மட்டன் பிரியாணி • ₹80 தள்ளுபடி! 🍛", "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Ghee Dosa 🥞", "LYODOSA", "சூடான நெய் ரோஸ்ட் தோசை மாம்பழ சட்னி • 30% சலுகை! 🥞", "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Idli Box ☕", "LYOIDLI", "ஆவியில் வெந்த பஞ்சு போன்ற மல்லிகைப்பூ இட்லி • இலவச டெலிவரி! ☕", "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Kothu Parotta 🍽️", "LYOPAROTTA", "நறுக்கிய மொறுமொறுப்பான கொத்து பரோட்டாக்கள் • ₹50 தள்ளுபடி! 🍽️", "https://images.unsplash.com/photo-1626132647523-66f5bf380027?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Tea/Chai ☕", "LYOTEA", "நறுமணமுள்ள இஞ்சி ஏலக்காய் டீCombo • ஒன்று வாங்கினால் ஒன்று இலவசம்! ☕", "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Choco Cake 🎂", "LYOCAKE", "கொண்டாட்ட விசேஷ சாக்லேட் கேக்குகள் • இலவச ஸ்பெஷல் ஐஸ்கிரீம் ஸ்கூப் 🎂", "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Pizza 🍕", "LYOPIZZA", "சீஸ் நிறைந்த டபுள் சீஸ் லோடட் வெஜ் பிஸ்ஸா • ₹120 சேமிப்பு! 🍕", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Samosa 🧆", "LYOSNACKS", "மாலை நேர காரசாரமான வெங்காய சமோசா மற்றும் வடை • 20% தள்ளுபடி! 🧆", "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Meals 🍱", "LYOMEALS", "அசல் பாரம்பரிய தென்னிந்திய மதிய சாப்பாடு காம்போ • ₹40 தள்ளுபடி! 🍱", "https://images.unsplash.com/photo-1610192244261-3f33de3f55e4?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Mango 🥭", "LYOSHAKES", "ஜில்லென்ற சுவையான பிரெஷ் மேங்கோ மில்க்ஷேக் • 15% உடனடி சலுகை! 🥭", "https://images.unsplash.com/photo-1579954115545-a95591f28bfc?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Chinese 🍜", "LYOCHINESE", "காரசாரமான கோபி மஞ்சூரியன் மற்றும் பிரைடு ரைஸ் • ₹40 சேமிப்பு! 🍜", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Sweets 🍬", "LYOSWEETS", "பண்டிகை கால சுத்தமான நெய் மைசூர் பாக் காம்போ • ₹100 சலுகை 🍬", "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Burger 🍔", "LYOBURGER", "செம காரமான சிக்கன் பர்கர் உடன் குளிர்பானங்கள் இலவசம் • ₹150 மட்டும்! 🍔", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Curry 🍛", "LYOCURRY", "சுவையான பட்டர் சிக்கன் மற்றும் பன்னீர் பட்டர் மசாலா • ₹100 சேமிப்பு! 🍛", "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=800&auto=format&fit=crop&q=80"),
                        PromoPreset("Icecream 🍨", "LYOICECREAM", "சுவையான ஐஸ்கிரீம் சண்டே காம்போ பேக் • வெறும் ₹99 முதல்! 🍨", "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=800&auto=format&fit=crop&q=80")
                    )

                    val chunks = samples.chunked(3)
                    chunks.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { sample ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0x1F38BDF8), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(8.dp))
                                        .clickable {
                                            promoCode = sample.code
                                            description = sample.desc
                                            imageUrl = sample.url
                                            android.widget.Toast.makeText(context, "${sample.name} loaded to form!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = sample.name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (rowItems.size < 3) {
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Form: Add New Dynamic Banner
        item {
            GlassCard(
                cornerRadius = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "✨ CREATE PROMOTIONAL CAROUSEL BANNER",
                        color = LyoColors.AmberYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = { promoCode = it.uppercase() },
                        label = { Text("Promo Coupon Code (e.g., WELCOME100)", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Short Description (e.g., ₹100 OFF on order above ₹499)", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Curate Promo Cover Image:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onBrowsePhoto,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BROWSE PHOTO", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Or Paste Internet Image URL") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Image preview if selected or typed
                    if (imageUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Image Preview:", color = LyoColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        ) {
                            val painterModel = remember(imageUrl) {
                                if (imageUrl.startsWith("/")) java.io.File(imageUrl) else imageUrl
                            }
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(painterModel),
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LyoButton(
                        text = "ADD PROMO SLIDER TO HOMEPAGE",
                        onClick = {
                            if (promoCode.isNotBlank() && description.isNotBlank()) {
                                viewModel.insertPromoBanner(
                                    PromoBanner(
                                        code = promoCode.trim().uppercase(),
                                        description = description.trim(),
                                        imageUrl = imageUrl.trim()
                                    )
                                )
                                // Reset inputs
                                promoCode = ""
                                description = ""
                                imageUrl = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Text(
                text = "ACTIVE CAROUSEL BANNERS (${promoBanners.size})",
                color = LyoColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 22.dp, bottom = 10.dp)
            )
        }

        if (promoBanners.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No promotion slides active. Add one above!", color = LyoColors.TextSecondary)
                }
            }
        }

        items(promoBanners, key = { it.id }) { banner ->
            Box(modifier = Modifier.padding(vertical = 8.dp)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Banner preview thumbnail on the left
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val isCustomImage = banner.imageUrl.isNotBlank() && (
                                banner.imageUrl.startsWith("http") || 
                                banner.imageUrl.startsWith("content://") || 
                                banner.imageUrl.startsWith("file://") || 
                                banner.imageUrl.startsWith("/storage") || 
                                banner.imageUrl.startsWith("/data") || 
                                banner.imageUrl.contains("/")
                            )
                            if (isCustomImage) {
                                val painterModel = remember(banner.imageUrl) {
                                    if (banner.imageUrl.startsWith("/")) java.io.File(banner.imageUrl) else banner.imageUrl
                                }
                                androidx.compose.foundation.Image(
                                    painter = coil.compose.rememberAsyncImagePainter(painterModel),
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.LocalOffer,
                                    contentDescription = null,
                                    tint = LyoColors.AccentOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CODE: ${banner.code}",
                                color = LyoColors.AmberYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = banner.description,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (banner.imageUrl.isNotBlank()) {
                                Text(
                                    text = "Custom Photo Cover Active",
                                    color = LyoColors.VegGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = { viewModel.deletePromoBanner(banner) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x1FDC2626))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "delete banner",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartMenuManagerTab(viewModel: AdminViewModel) {
    var rawInput by remember { mutableStateOf("") }
    val messages by viewModel.smartMenuMessages.collectAsState()
    val isLoading by viewModel.isSmartMenuLoading.collectAsState()
    val state by viewModel.smartMenuState.collectAsState()
    val rawJson by viewModel.lastParsedJson.collectAsState()
    
    var previewTab by remember { mutableStateOf("TABLE") } // "TABLE" or "JSON"
    val listState = rememberLazyListState()
    val previewListState = rememberLazyListState()
    
    // Auto-scroll chat to latest message on change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var showManualAddDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var mName by remember { mutableStateOf("") }
    var mNameTa by remember { mutableStateOf("") }
    var mType by remember { mutableStateOf("Restaurant") }
    var mAddress by remember { mutableStateOf("") }
    var mPhone by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    if (showHelpDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = LyoColors.AmberYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ஸ்மார்ட் மெனு வழிகாட்டி 🤖📖",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFF00)),
                        border = BorderStroke(1.dp, LyoColors.AmberYellow.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "ஏன் இந்த AI மேலாளர் சிறந்தவர்?",
                                fontWeight = FontWeight.Bold,
                                color = LyoColors.AmberYellow,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "கைமுறையாக ஒரு கடையையும் அதன் மெனுவையும் ஆப்பில் சேர்க்க 20 முதல் 30 நிமிடங்கள் வரை ஆகும். ஆனால் இந்த AI ஸ்மார்ட் மேலாளர் மூலம் வெறும் 1 நிமிடத்தில் கடையை நேரலையாக்கலாம்! 🚀",
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Text(
                        text = "💡 எப்படிப் பயன்படுத்துவது? (How to Use)",
                        fontWeight = FontWeight.Bold,
                        color = LyoColors.AccentOrange,
                        fontSize = 13.sp
                    )
                    
                    val steps = listOf(
                        "1. **மெனுவை நகலெடுத்து ஒட்டவும்**: வாட்ஸ்அப் அல்லது காகிதத்தில் இருக்கும் கடையின் மெனுவை அப்படியே நகலெடுத்து (Copy) கீழே உள்ள உள்ளீட்டுப் பெட்டியில் ஒட்டவும் (Paste).",
                        "2. **AI பகுப்பாய்வு**: கடையின் பெயர், முகவரி, தொலைபேசி மற்றும் உணவுகளின் விலைகள், வகைகளை (சைவம்/அசைவம்/மட்டன்/சிக்கன்) AI தானாகவே பிரித்தெடுத்துவிடும்.",
                        "3. **இருமொழி ஆதரவு (Bilingual)**: உணவுகளின் பெயர்களை தமிழ் மற்றும் ஆங்கிலத்தில் துல்லியமாக மொழிபெயர்க்கும்.",
                        "4. **சரிபார்த்தல் மற்றும் திருத்துதல்**: வலதுபுறம் (அல்லது மொபைலில் கீழே) தோன்றும் நேரடி அட்டவணையில் உள்ள 'Edit' ✏️ பொத்தானை அழுத்தி கடையின் முகவரி, விலை போன்றவற்றை எடிட் செய்து கொள்ளலாம்.",
                        "5. **டேட்டாபேஸில் சேமிக்க**: எல்லாம் சரியாக இருந்தால் 'Publish to DB 🚀' பட்டனை அழுத்தவும். உடனடியாக ஆப்பில் அந்த கடை நேரலையாகிவிடும்!"
                    )

                    steps.forEach { step ->
                        Text(
                            text = step,
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Text(
                        text = "📊 தினசரி சேர்க்கும் திறன் (Onboarding Capacity)",
                        fontWeight = FontWeight.Bold,
                        color = LyoColors.VegGreen,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• **அதிகபட்ச வரம்பு இல்லை**: டேட்டாபேஸ் மற்றும் சர்வர் லெவலில் எந்தக் கட்டுப்பாடும் கிடையாது.\n" +
                               "• **பரிந்துரைக்கப்படும் வேகம்**: ஒரு ஆபரேட்டர் ஒரு நாளைக்கு மிக எளிதாக **150 முதல் 200 கடைகள் வரை** துல்லியமாக இணைக்க முடியும். இது உங்கள் உற்பத்தித் திறனை 20 மடங்கு அதிகரிக்கிறது!",
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Text(
                        text = "⏰ உணவு கிடைக்கும் நேரம் (Timing Control)",
                        fontWeight = FontWeight.Bold,
                        color = LyoColors.LiveCyan,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• கடையை வெற்றிகரமாகப் பப்ளிஷ் செய்த பிறகு, 'Merchants' மெனுவில் அந்தக் கடைக்குச் சென்று, குறிப்பிட்ட உணவு வகைகளுக்கோ அல்லது தனித்தனி உணவுகளுக்கோ குறிப்பிட்ட நேரத்தை (autoOpenTime - autoCloseTime, எ.கா. 07:00 AM - 11:00 AM) அமைத்துக் கொள்ளலாம். இதனால் குறிப்பிட்ட நேரத்தில் மட்டும் அந்த உணவு வாடிக்கையாளர்களுக்குக் காட்டும்!",
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange)
                ) {
                    Text("சரி, புரிந்தது! 👍", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF141720),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showManualAddDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showManualAddDialog = false },
            title = {
                Text(
                    text = "கைமுறையாக கடையைச் சேர் 🏪",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = mName,
                        onValueChange = { mName = it },
                        label = { Text("Merchant Name (English)*", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = mNameTa,
                        onValueChange = { mNameTa = it },
                        label = { Text("Merchant Name (Tamil)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Business Type:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                         val typeEmojis by viewModel.customBusinessTypes.collectAsState()
                         typeEmojis.forEach { (bType, emoji) ->
                             val isSel = mType == bType
                             Box(
                                 modifier = Modifier
                                     .size(85.dp)
                                     .clip(RoundedCornerShape(12.dp))
                                     .background(
                                         if (isSel) {
                                             androidx.compose.ui.graphics.Brush.verticalGradient(
                                                 colors = listOf(Color(0xFFFF7849), Color(0xFFEA580C))
                                             )
                                         } else {
                                             androidx.compose.ui.graphics.Brush.verticalGradient(
                                                 colors = listOf(Color(0x1FFFFFFF), Color(0x0AFFFFFF))
                                             )
                                         }
                                     )
                                     .border(
                                         width = 1.dp,
                                         color = if (isSel) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f),
                                         shape = RoundedCornerShape(12.dp)
                                     )
                                     .clickable { mType = bType },
                                 contentAlignment = Alignment.Center
                             ) {
                                 Column(
                                     horizontalAlignment = Alignment.CenterHorizontally,
                                     verticalArrangement = Arrangement.Center,
                                     modifier = Modifier.padding(4.dp)
                                 ) {
                                     Text(
                                         text = emoji,
                                         fontSize = 24.sp,
                                         modifier = Modifier.padding(bottom = 2.dp)
                                     )
                                     Text(
                                         text = bType,
                                         color = Color.White,
                                         fontSize = 10.sp,
                                         fontWeight = FontWeight.Bold,
                                         maxLines = 1,
                                         overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                         textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                     )
                                 }
                             }
                         }

                         // Add Custom Type Button Box
                         var showCustomTypeDialog by remember { mutableStateOf(false) }
                         Box(
                             modifier = Modifier
                                 .size(85.dp)
                                 .clip(RoundedCornerShape(12.dp))
                                 .background(
                                     androidx.compose.ui.graphics.Brush.verticalGradient(
                                         colors = listOf(Color(0x1F22C55E), Color(0x0A22C55E))
                                     )
                                 )
                                 .border(
                                     width = 1.dp,
                                     color = Color(0x6622C55E),
                                     shape = RoundedCornerShape(12.dp)
                                 )
                                 .clickable { showCustomTypeDialog = true },
                             contentAlignment = Alignment.Center
                         ) {
                             Column(
                                 horizontalAlignment = Alignment.CenterHorizontally,
                                 verticalArrangement = Arrangement.Center,
                                 modifier = Modifier.padding(4.dp)
                             ) {
                                 Text(
                                     text = "➕",
                                     fontSize = 24.sp,
                                     modifier = Modifier.padding(bottom = 2.dp)
                                 )
                                 Text(
                                     text = "Add Custom\n(புதிய வகை)",
                                     color = Color(0xFF22C55E),
                                     fontSize = 9.sp,
                                     fontWeight = FontWeight.Bold,
                                     textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                     lineHeight = 11.sp
                                 )
                             }
                         }

                         if (showCustomTypeDialog) {
                             var customTypeName by remember { mutableStateOf("") }
                             var customTypeEmoji by remember { mutableStateOf("🏪") }
                             androidx.compose.material3.AlertDialog(
                                 onDismissRequest = { showCustomTypeDialog = false },
                                 title = { Text("Add Custom Business Type (புதிய தொழில் வகை)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                 text = {
                                     Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                         androidx.compose.material3.OutlinedTextField(
                                             value = customTypeName,
                                             onValueChange = { customTypeName = it },
                                             label = { Text("Type Name (e.g., Edappadi Mess, Bakery, Tea)", color = Color.Gray) },
                                             colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                         Text("Select Emoji Icon (ஐகான் தேர்வு):", color = Color.Gray, fontSize = 11.sp)
                                         Row(
                                             modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                                         ) {
                                             listOf("🏪", "🍵", "🍳", "🥤", "🌾", "🍿", "🍗", "🥣", "🧁", "🐟", "🌶️").forEach { em ->
                                                 val isSelected = customTypeEmoji == em
                                                 Box(
                                                     modifier = Modifier
                                                         .size(36.dp)
                                                         .clip(CircleShape)
                                                         .background(if (isSelected) LyoColors.AccentOrange else Color(0x1Fffffff))
                                                         .clickable { customTypeEmoji = em },
                                                     contentAlignment = Alignment.Center
                                                 ) {
                                                     Text(em, fontSize = 18.sp)
                                                 }
                                             }
                                         }
                                     }
                                 },
                                 confirmButton = {
                                     androidx.compose.material3.Button(
                                         onClick = {
                                             if (customTypeName.isNotBlank()) {
                                                 viewModel.addCustomBusinessType(customTypeName, customTypeEmoji)
                                                 mType = customTypeName
                                                 showCustomTypeDialog = false
                                             }
                                         },
                                         colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange)
                                     ) {
                                         Text("ADD (சேர்)", color = Color.White, fontWeight = FontWeight.Bold)
                                     }
                                 },
                                 dismissButton = {
                                     androidx.compose.material3.TextButton(onClick = { showCustomTypeDialog = false }) {
                                         Text("CANCEL", color = Color.LightGray)
                                     }
                                 },
                                 containerColor = Color(0xFF0F172A),
                                 shape = RoundedCornerShape(16.dp)
                             )
                         }
                    }
                    androidx.compose.material3.OutlinedTextField(
                        value = mAddress,
                        onValueChange = { mAddress = it },
                        label = { Text("Address", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = mPhone,
                        onValueChange = { mPhone = it },
                        label = { Text("Phone Number", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        if (mName.isBlank()) {
                            android.widget.Toast.makeText(context, "Merchant Name is required!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.insertManualVendor(
                                name = mName,
                                nameTa = mNameTa,
                                type = mType,
                                address = mAddress,
                                phone = mPhone,
                                onSuccess = {
                                    android.widget.Toast.makeText(context, "கடை வெற்றிகரமாக சேர்க்கப்பட்டது! 🎉", android.widget.Toast.LENGTH_LONG).show()
                                    showManualAddDialog = false
                                    mName = ""
                                    mNameTa = ""
                                    mAddress = ""
                                    mPhone = ""
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                ) {
                    Text("CREATE MERCHANT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualAddDialog = false }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Action Bar for Smart Menu & Manual Creation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏪 MERCHANT PLATFORM",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = LyoColors.AccentOrange,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "AI Smart Menu & Merchant Controls",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            
            OutlinedButton(
                onClick = { showManualAddDialog = true },
                border = BorderStroke(1.dp, LyoColors.VegGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LyoColors.VegGreen),
                shape = RoundedCornerShape(20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "add shop",
                    tint = LyoColors.VegGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "கைமுறையாகச் சேர்",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
        val isWide = maxWidth > 750.dp
        
        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left chat container
                Box(modifier = Modifier.weight(1f)) {
                    ChatInterfaceSection(
                        messages = messages,
                        isLoading = isLoading,
                        rawInput = rawInput,
                        onInputChange = { rawInput = it },
                        onSendMessage = {
                            viewModel.sendSmartMenuMessage(rawInput)
                            rawInput = ""
                        },
                        onQuickPublish = {
                            if (state != null) {
                                viewModel.publishCurrentDraftDirectly()
                            } else {
                                viewModel.sendSmartMenuMessage("PUBLISH")
                            }
                        },
                        onReset = {
                            viewModel.resetSmartMenu()
                        },
                        onShowHelp = {
                            showHelpDialog = true
                        },
                        listState = listState,
                        state = state
                    )
                }
                
                // Right live preview container
                Box(modifier = Modifier.weight(1.2f)) {
                    InteractivePreviewSection(
                        state = state,
                        rawJson = rawJson,
                        previewTab = previewTab,
                        onTabChange = { previewTab = it },
                        viewModel = viewModel,
                        listState = previewListState
                    )
                }
            }
        } else {
            // Mobile Tabbed view: eliminates nested scrolling completely for fluid UX!
            var mobileTab by remember { mutableStateOf("CHAT") } // "CHAT" or "REVIEW"
            
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                // High contrast tab switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (mobileTab == "CHAT") LyoColors.AccentOrange else Color.Transparent)
                            .clickable { mobileTab = "CHAT" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🤖 AI Chat Assistant (${messages.size})",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (mobileTab == "REVIEW") LyoColors.AccentOrange else Color.Transparent)
                            .clickable { mobileTab = "REVIEW" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.RateReview,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📋 Review & Publish",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Tab content box taking full height
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (mobileTab == "CHAT") {
                        ChatInterfaceSection(
                            messages = messages,
                            isLoading = isLoading,
                            rawInput = rawInput,
                            onInputChange = { rawInput = it },
                            onSendMessage = {
                                viewModel.sendSmartMenuMessage(rawInput)
                                rawInput = ""
                            },
                            onQuickPublish = {
                                if (state != null) {
                                    viewModel.publishCurrentDraftDirectly()
                                } else {
                                    viewModel.sendSmartMenuMessage("PUBLISH")
                                }
                            },
                            onReset = {
                                viewModel.resetSmartMenu()
                            },
                            onShowHelp = {
                                showHelpDialog = true
                            },
                            listState = listState,
                            state = state
                        )
                    } else {
                        InteractivePreviewSection(
                            state = state,
                            rawJson = rawJson,
                            previewTab = previewTab,
                            onTabChange = { previewTab = it },
                            viewModel = viewModel,
                            listState = previewListState
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun ChatInterfaceSection(
    messages: List<AdminViewModel.SmartMenuMessage>,
    isLoading: Boolean,
    rawInput: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onQuickPublish: () -> Unit,
    onReset: () -> Unit,
    onShowHelp: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    state: AdminViewModel.SmartMenuState? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0x33F8FAFC), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = LyoColors.AccentOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Smart Menu Manager",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onShowHelp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "help",
                        tint = LyoColors.AmberYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "reset",
                        tint = LyoColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        // Lyo AI Insights Collapsible Dashboard
        var showAiDashboard by remember { mutableStateOf(true) }
        if (state != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
                border = BorderStroke(1.dp, Color(0x1FFFFFFF))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showAiDashboard = !showAiDashboard },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = LyoColors.AmberYellow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lyo AI Active Audit Insights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Text(
                            text = if (showAiDashboard) "Hide Details 🔼" else "Show Details 🔽",
                            color = LyoColors.AccentOrange,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (showAiDashboard) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0x11FFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Detected Info Column
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("✅ DETECTED INFO:", color = LyoColors.VegGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("• Shop: ${state.restaurantName}", color = Color.White, fontSize = 9.sp)
                                Text("• Type: ${state.businessType}", color = Color.White, fontSize = 9.sp)
                                if (state.phone.isNotBlank()) Text("• Phone: ${state.phone}", color = Color.White, fontSize = 9.sp)
                                if (state.address.isNotBlank()) Text("• Address: ${state.address}", color = Color.White, fontSize = 9.sp)
                                Text("• Menu size: ${state.menuData.values.sumOf { it.size }} items", color = Color.White, fontSize = 9.sp)
                            }
                            
                            // Missing / Warning Column
                            Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val missingList = mutableListOf<String>()
                                val warningList = mutableListOf<String>()
                                
                                if (state.phone.isBlank()) missingList.add("Contact Phone")
                                if (state.address.isBlank()) missingList.add("Shop Address")
                                if (state.restaurantNameTa.isBlank()) warningList.add("Tamil Shop Name")
                                
                                val zeroPriceItems = state.menuData.values.flatten().count { it.price <= 0.0 }
                                if (zeroPriceItems > 0) warningList.add("$zeroPriceItems dishes with ₹0 price")
                                
                                val missingTamilItems = state.menuData.values.flatten().count { it.itemNameTa.isBlank() }
                                if (missingTamilItems > 0) warningList.add("$missingTamilItems dishes lack Tamil name")
                                
                                if (missingList.isNotEmpty() || warningList.isNotEmpty()) {
                                    Text("⚠️ VALIDATION WARNINGS:", color = LyoColors.AmberYellow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    missingList.forEach { Text("• Missing: $it", color = Color(0xFFFCA5A5), fontSize = 9.sp) }
                                    warningList.forEach { Text("• Warning: $it", color = Color(0xFFFDE047), fontSize = 9.sp) }
                                } else {
                                    Text("🌟 AUDIT COMPLIANCE:", color = LyoColors.LiveCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text("• All core details validated!", color = Color.White, fontSize = 9.sp)
                                    Text("• 100% complete bilingual metadata.", color = Color.White, fontSize = 9.sp)
                                    Text("• Ready for database publishing.", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Publishing Status: ${state.status.uppercase()}",
                                color = if (state.status.uppercase() == "PUBLISHED") LyoColors.VegGreen else LyoColors.AmberYellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.status.uppercase() != "PUBLISHED") {
                                Text(
                                    text = "Ready to publish 🚀",
                                    color = LyoColors.VegGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Chat Stream list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0AFFFFFF))
                .padding(8.dp)
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isAdmin = msg.sender == "admin"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAdmin) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isAdmin) 12.dp else 0.dp,
                                        bottomEnd = if (isAdmin) 0.dp else 12.dp
                                    )
                                )
                                .background(if (isAdmin) LyoColors.AccentOrange else Color(0xFF1E293B))
                                .border(
                                    1.dp,
                                    if (isAdmin) Color.Transparent else Color(0x1FFFFFFF),
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isAdmin) 12.dp else 0.dp,
                                        bottomEnd = if (isAdmin) 0.dp else 12.dp
                                    )
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (isAdmin) "Admin 👤" else "Lyo Smart Menu Manager 🤖",
                                color = if (isAdmin) Color(0xFFFFCCAA) else LyoColors.AmberYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            
            // Inline Non-Blocking Beautiful Loading State
            if (isLoading) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xEB0F172A)),
                    border = BorderStroke(1.dp, LyoColors.AccentOrange.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = LyoColors.AccentOrange,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lyo AI is processing menu data... ⚙️",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Current Step: Analyzing categories, translating to Tamil & validating prices.",
                                color = LyoColors.TextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Chat Action bar Shortcuts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { onInputChange("Merge both datasets and clear duplicates.") },
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0x3338BDF8),
                    contentColor = Color(0xFF38BDF8)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("Merge chunks", fontSize = 10.sp)
            }
            
            FilledTonalButton(
                onClick = onQuickPublish,
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0x3310B981),
                    contentColor = Color(0xFF10B981)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("Publish to DB 🚀", fontSize = 10.sp)
            }

            var sampleMenuExpanded by remember { mutableStateOf(false) }
            Box {
                FilledTonalButton(
                    onClick = { sampleMenuExpanded = true },
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0x33CA8A04),
                        contentColor = Color(0xFFEAB308)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("மாதிரி மெனு 📋", fontSize = 10.sp)
                }

                DropdownMenu(
                    expanded = sampleMenuExpanded,
                    onDismissRequest = { sampleMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("பிரியாணி மெனு", fontSize = 12.sp) },
                        onClick = {
                            sampleMenuExpanded = false
                            onInputChange(
                                "Lyo Royal Biryani, Restaurant, Salem bypass Road, Edappadi, 9876543210\n" +
                                "Menu:\n" +
                                "[Biryanis]\n" +
                                "Mutton Royal Dum Biryani ₹380 Mutton\n" +
                                "Chicken Special Biryani ₹260 Chicken\n" +
                                "Veg Kuska Biryani ₹140 Veg\n" +
                                "[Starters]\n" +
                                "Mutton Boti Fry ₹250 Mutton\n" +
                                "Chicken 65 Boneless ₹180 Chicken\n" +
                                "Spicy Cauliflower Manchurian ₹140 Veg"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("பேக்கரி மெனு", fontSize = 12.sp) },
                        onClick = {
                            sampleMenuExpanded = false
                            onInputChange(
                                "Lyo Sweet Palace & Bakery, Bakery, GH Road near Bypass, Edappadi, 9876543212\n" +
                                "Menu:\n" +
                                "[Hot Beverages]\n" +
                                "Ginger Tea ₹15 Veg\n" +
                                "Filter Coffee ₹20 Veg\n" +
                                "[Sweets & Snacks]\n" +
                                "Spl Ghee Mysorepak ₹320 Veg\n" +
                                "Hot Potato Samosa ₹12 Veg\n" +
                                "Veg Puff Crispy ₹18 Veg"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("சைவ மெனு", fontSize = 12.sp) },
                        onClick = {
                            sampleMenuExpanded = false
                            onInputChange(
                                "Lyo Pure Veg Saravana Mess, Hotel, South Car Street, Edappadi, 9876543211\n" +
                                "Menu:\n" +
                                "[Breakfast (காலை உணவு)]\n" +
                                "Ghee Podi Roast ₹90 Veg\n" +
                                "Spl Rava Dosa ₹80 Veg\n" +
                                "Soft Idly (2 Nos) ₹30 Veg\n" +
                                "[Lunch (மதிய உணவு)]\n" +
                                "Traditional South Indian Meals ₹130 Veg"
                            )
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Chat Input box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

            OutlinedTextField(
                value = rawInput,
                onValueChange = onInputChange,
                placeholder = { Text("Paste menu data block or type edit...", fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = LyoColors.AccentOrange,
                    unfocusedBorderColor = Color(0x33F8FAFC)
                ),
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
                maxLines = 4
            )

            Button(
                onClick = {
                    clipboardManager.getText()?.text?.let { text ->
                        if (text.isNotBlank()) {
                            val newValue = if (rawInput.isBlank()) {
                                text
                            } else {
                                rawInput + "\n" + text
                            }
                            onInputChange(newValue)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp)),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text("📋 Paste\n(ஒட்டு)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, lineHeight = 11.sp)
            }

            Button(
                onClick = onSendMessage,
                enabled = rawInput.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "send", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

fun fetchCurrentLocation(context: android.content.Context, onResult: (Double, Double) -> Unit) {
    try {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
        if (locationManager != null) {
            val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
            
            var location: android.location.Location? = null
            if (isGpsEnabled) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                }
            }
            if (location == null && isNetworkEnabled) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    location = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                }
            }
            
            if (location != null) {
                onResult(location.latitude, location.longitude)
                android.widget.Toast.makeText(context, "📍 GPS இருப்பிடம் பெறப்பட்டது: ${location.latitude}, ${location.longitude}", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                onResult(11.5812, 77.8465)
                android.widget.Toast.makeText(context, "📍 GPS இயங்கவில்லை, மாதிரி இருப்பிடம் பயன்படுத்தப்படுகிறது (Edappadi)", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        onResult(11.5812, 77.8465)
        android.widget.Toast.makeText(context, "GPS பிழை: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun InteractivePreviewSection(
    state: AdminViewModel.SmartMenuState?,
    rawJson: String,
    previewTab: String,
    onTabChange: (String) -> Unit,
    viewModel: AdminViewModel,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
) {
    var showEditMetaDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editNameTa by remember { mutableStateOf("") }
    var editBusinessType by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editPhotoUrl by remember { mutableStateOf("") }
    var editLat by remember { mutableStateOf(11.5812) }
    var editLng by remember { mutableStateOf(77.8465) }

    var showEditItemDialog by remember { mutableStateOf(false) }
    var editingCategoryKey by remember { mutableStateOf("") }
    var editingItemIndex by remember { mutableStateOf(-1) }
    var editItemNameEn by remember { mutableStateOf("") }
    var editItemNameTa by remember { mutableStateOf("") }
    var editItemPrice by remember { mutableStateOf("") }
    var editItemMeatType by remember { mutableStateOf("") }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var targetCategoryForNewItem by remember { mutableStateOf("") }
    var newItemNameEn by remember { mutableStateOf("") }
    var newItemNameTa by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }
    var newItemMeatType by remember { mutableStateOf("VEG") }

    var showRenameCategoryDialog by remember { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf("") }
    var newCategoryName by remember { mutableStateOf("") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInputName by remember { mutableStateOf("") }

    if (showEditMetaDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditMetaDialog = false },
            title = { Text("கடை விவரங்களை திருத்து ✏️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Shop Name (English)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editNameTa,
                        onValueChange = { editNameTa = it },
                        label = { Text("Shop Name (Tamil)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editBusinessType,
                        onValueChange = { editBusinessType = it },
                        label = { Text("Business Type", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Address", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Contact Phone", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Photo Input
                    androidx.compose.material3.OutlinedTextField(
                        value = editPhotoUrl,
                        onValueChange = { editPhotoUrl = it },
                        label = { Text("Photo URL / Theme name", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Presets
                    Text("Preset Banner Themes (கடையின் புகைப்படம்):", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("biryani", "southindian", "northindian", "bakery", "cafe", "fastfood", "dessert", "juice").forEach { theme ->
                            val themeUrl = when (theme) {
                                "biryani" -> "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80"
                                "southindian" -> "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=800&auto=format&fit=crop&q=80"
                                "northindian" -> "https://images.unsplash.com/photo-1585938338392-50a59970d2ee?w=800&auto=format&fit=crop&q=80"
                                "bakery" -> "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800&auto=format&fit=crop&q=80"
                                "cafe" -> "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800&auto=format&fit=crop&q=80"
                                "fastfood" -> "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80"
                                "dessert" -> "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=800&auto=format&fit=crop&q=80"
                                "juice" -> "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=800&auto=format&fit=crop&q=80"
                                else -> ""
                            }
                            val isSel = editPhotoUrl == themeUrl
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) LyoColors.AccentOrange else Color(0x1Fffffff))
                                    .clickable { editPhotoUrl = themeUrl }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(theme.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Map picker for shop location!
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Shop Location on Map (கடையின் இருப்பிடம் மேப்பில்):", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    
                    val gpsContext = androidx.compose.ui.platform.LocalContext.current
                    androidx.compose.material3.Button(
                        onClick = {
                            fetchCurrentLocation(gpsContext) { lat, lng ->
                                editLat = lat
                                editLng = lng
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("📍 USE CURRENT GPS LOCATION (இருப்பிடத்தை பெறுக)", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))

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
                androidx.compose.material3.Button(
                    onClick = {
                        viewModel.updateDraftRestaurantName(editName)
                        viewModel.updateDraftRestaurantNameTa(editNameTa)
                        viewModel.updateDraftBusinessType(editBusinessType)
                        viewModel.updateDraftAddress(editAddress)
                        viewModel.updateDraftPhone(editPhone)
                        viewModel.updateDraftPhotoUrl(editPhotoUrl)
                        viewModel.updateDraftLocation(editLat, editLng)
                        showEditMetaDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                ) {
                    Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditMetaDialog = false }) {
                    Text("CANCEL", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showEditItemDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditItemDialog = false },
            title = { Text("உணவுப் பொருளை திருத்து 🍔", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = editItemNameEn,
                        onValueChange = { editItemNameEn = it },
                        label = { Text("Dish Name (English)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editItemNameTa,
                        onValueChange = { editItemNameTa = it },
                        label = { Text("Dish Name (Tamil)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editItemPrice,
                        onValueChange = { editItemPrice = it },
                        label = { Text("Price (₹)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Category / Meat Type:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("VEG", "CHICKEN", "MUTTON", "EGG", "OTHER").forEach { type ->
                            val isSelected = editItemMeatType.uppercase() == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LyoColors.AccentOrange else Color(0x1Fffffff))
                                    .clickable { editItemMeatType = type }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(type, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        val parsedPrice = editItemPrice.toDoubleOrNull() ?: 0.0
                        viewModel.updateDraftMenuItem(
                            categoryKey = editingCategoryKey,
                            itemIndex = editingItemIndex,
                            updatedItem = AdminViewModel.SmartMenuItem(
                                itemName = editItemNameEn,
                                itemNameTa = editItemNameTa,
                                price = parsedPrice,
                                meatType = editItemMeatType
                            )
                        )
                        showEditItemDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                ) {
                    Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditItemDialog = false }) {
                    Text("CANCEL", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAddItemDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("பிரிவில் புதிய உணவுச் சேர்க்க ➕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Category: ${targetCategoryForNewItem.split("__AND__").first()}", color = LyoColors.AccentOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    androidx.compose.material3.OutlinedTextField(
                        value = newItemNameEn,
                        onValueChange = { newItemNameEn = it },
                        label = { Text("Dish Name (English)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = newItemNameTa,
                        onValueChange = { newItemNameTa = it },
                        label = { Text("Dish Name (Tamil)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = newItemPrice,
                        onValueChange = { newItemPrice = it },
                        label = { Text("Price (₹)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Category / Meat Type:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("VEG", "CHICKEN", "MUTTON", "EGG", "OTHER").forEach { type ->
                            val isSelected = newItemMeatType.uppercase() == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LyoColors.AccentOrange else Color(0x1Fffffff))
                                    .clickable { newItemMeatType = type }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(type, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        val parsedPrice = newItemPrice.toDoubleOrNull() ?: 0.0
                        viewModel.addDraftMenuItem(
                            categoryKey = targetCategoryForNewItem,
                            item = AdminViewModel.SmartMenuItem(
                                itemName = newItemNameEn,
                                itemNameTa = newItemNameTa,
                                price = parsedPrice,
                                meatType = newItemMeatType
                            )
                        )
                        showAddItemDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                ) {
                    Text("ADD", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAddItemDialog = false }) {
                    Text("CANCEL", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showRenameCategoryDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRenameCategoryDialog = false },
            title = { Text("பிரிவின் பெயரை மாற்றுக ✏️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Old Category: ${categoryToRename.split("__AND__").first()}", color = Color.Gray, fontSize = 12.sp)
                    androidx.compose.material3.OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("New Category Name (e.g., Soups__AND__சூப்)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.renameDraftCategory(categoryToRename, newCategoryName)
                        }
                        showRenameCategoryDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                ) {
                    Text("RENAME", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showRenameCategoryDialog = false }) {
                    Text("CANCEL", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAddCategoryDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("புதிய பிரிவு சேர்க்க ➕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = newCategoryInputName,
                        onValueChange = { newCategoryInputName = it },
                        label = { Text("Category Name (e.g., Desserts__AND__இனிப்புகள்)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        if (newCategoryInputName.isNotBlank()) {
                            viewModel.addDraftCategory(newCategoryInputName)
                        }
                        showAddCategoryDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                ) {
                    Text("CREATE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("CANCEL", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0x33F8FAFC), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Toggle tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("TABLE" to "Review Table", "JSON" to "Output Schema JSON").forEach { (tabId, label) ->
                val isSel = previewTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) LyoColors.AccentOrange else Color.Transparent)
                        .clickable { onTabChange(tabId) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Jump Navigation Chips for Table review
        if (state != null && previewTab == "TABLE") {
            val scope = rememberCoroutineScope()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1Fffffff))
                        .border(1.dp, Color(0x33ffffff), RoundedCornerShape(12.dp))
                        .clickable { scope.launch { listState.animateScrollToItem(0) } }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("🔝 Top", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                
                // Audit Score
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1Fffffff))
                        .border(1.dp, Color(0x33ffffff), RoundedCornerShape(12.dp))
                        .clickable { scope.launch { listState.animateScrollToItem(0) } }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("📊 Audit Score", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                
                // Voice Review
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1Fffffff))
                        .border(1.dp, Color(0x33ffffff), RoundedCornerShape(12.dp))
                        .clickable { scope.launch { listState.animateScrollToItem(1) } }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("🔊 Voice Review", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                
                // Shop Details
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1Fffffff))
                        .border(1.dp, Color(0x33ffffff), RoundedCornerShape(12.dp))
                        .clickable { scope.launch { listState.animateScrollToItem(2) } }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("🏪 Shop Details", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                
                // Menu Preview
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1Fffffff))
                        .border(1.dp, Color(0x33ffffff), RoundedCornerShape(12.dp))
                        .clickable { scope.launch { listState.animateScrollToItem(3) } }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("🍔 Menu Preview", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                
                // Bottom
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1Fffffff))
                        .border(1.dp, Color(0x33ffffff), RoundedCornerShape(12.dp))
                        .clickable { 
                            scope.launch { 
                                val total = listState.layoutInfo.totalItemsCount
                                if (total > 0) listState.animateScrollToItem(total - 1)
                            } 
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("⬇️ Bottom", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        if (state == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                    Icon(
                        imageVector = Icons.Filled.TableChart,
                        contentDescription = null,
                        tint = LyoColors.TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Parsed Menu Live Review Screen",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Metadata and categorized food card hierarchies appear here dynamically as the manager processes raw inputs.",
                        color = LyoColors.TextSecondary,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                // Success banner if published
                if (state.status.uppercase() == "PUBLISHED") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x3310B981)),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Published Successfully! Merchant and menu mapped to direct sqlite database.",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                if (previewTab == "JSON") {
                    // Raw Monospace JSON view
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = rawJson,
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                } else {
                    // Text To Speech Voice Review initialization
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = rememberCoroutineScope()
                    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
                    var ttsReady by remember { mutableStateOf(false) }
                    
                    var isPlaying by remember { mutableStateOf(false) }
                    var isPaused by remember { mutableStateOf(false) }
                    var currentSegment by remember { mutableStateOf(-1) }
                    
                    val segments = remember(state) {
                        if (state == null) emptyList()
                        else listOf(
                            "ஸ்மார்ட் மெனு பகுப்பாய்வு அறிக்கை. கடையின் பெயர்: ${state.restaurantNameTa.ifBlank { state.restaurantName }}. வணிக வகை: ${state.businessType}.",
                            "முகவரி: ${state.address.ifBlank { "விவரம் இல்லை" }}. தொலைபேசி எண்: ${state.phone.ifBlank { "விவரம் இல்லை" }}.",
                            "உணவுகள் பட்டியல்: இந்த கடையில் மொத்தம் ${state.menuData.size} பிரிவுகள் மற்றும் ${state.menuData.values.sumOf { it.size }} உணவுகள் கண்டறியப்பட்டுள்ளன.",
                            if (state.address.isBlank() || state.phone.isBlank()) {
                                "எச்சரிக்கை: கடையின் முகவரி அல்லது தொலைபேசி எண் விடுபட்டுள்ளது. சரிபார்க்கவும்."
                            } else {
                                "மெனு தரக் கட்டுப்பாடு வெற்றி பெற்றுள்ளது. வெளியிடுவதற்கு தயாராக உள்ளது."
                            }
                        )
                    }
                    
                    fun playSegment(index: Int) {
                        if (index !in segments.indices) {
                            isPlaying = false
                            isPaused = false
                            currentSegment = -1
                            return
                        }
                        currentSegment = index
                        val params = android.os.Bundle()
                        params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "segment_$index")
                        tts?.speak(segments[index], android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "segment_$index")
                    }
                    
                    DisposableEffect(context) {
                        var instance: android.speech.tts.TextToSpeech? = null
                        instance = android.speech.tts.TextToSpeech(context) { status ->
                            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                                val result = instance?.setLanguage(java.util.Locale("ta", "IN"))
                                if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                                    instance?.language = java.util.Locale.US
                                }
                                ttsReady = true
                            }
                        }
                        tts = instance
                        
                        instance.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {}
                            override fun onDone(utteranceId: String?) {
                                val idx = utteranceId?.substringAfter("segment_")?.toIntOrNull()
                                if (idx != null) {
                                    scope.launch {
                                        if (isPlaying && !isPaused) {
                                            playSegment(idx + 1)
                                        }
                                    }
                                }
                            }
                            override fun onError(utteranceId: String?) {
                                scope.launch {
                                    isPlaying = false
                                    isPaused = false
                                }
                            }
                        })
                        
                        onDispose {
                            instance.stop()
                            instance.shutdown()
                        }
                    }
                    
                    val progress = if (segments.isEmpty() || currentSegment == -1) 0f else (currentSegment + 1).toFloat() / segments.size.toFloat()

                    // Mapped Table view
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section: AI Quality Score & Compliance Audit Card (Index 0)
                        item {
                            val totalItems = remember(state.menuData) { state.menuData.values.sumOf { it.size } }
                            val categoryCount = remember(state.menuData) { state.menuData.size }
                            val muttonCount = remember(state.menuData) { state.menuData.values.flatten().count { it.meatType.uppercase() == "MUTTON" } }
                            val vegCount = remember(state.menuData) { state.menuData.values.flatten().count { it.meatType.uppercase() == "VEG" } }
                            val avgPrice = remember(state.menuData) { 
                                if (totalItems > 0) state.menuData.values.flatten().map { it.price }.average() else 0.0 
                            }
                            val (menuGrade, gradeColor) = remember(categoryCount, totalItems, muttonCount) {
                                when {
                                    categoryCount >= 4 && totalItems >= 12 && muttonCount > 0 -> "A+ (Launch Ready 🌟)" to Color(0xFF22C55E)
                                    categoryCount >= 3 && totalItems >= 8 -> "A (Structurally Complete ✅)" to Color(0xFF4ADE80)
                                    totalItems > 0 -> "B (Needs Category Variety ⚠️)" to Color(0xFFEAB308)
                                    else -> "C (Incomplete Menu ❌)" to Color(0xFFEF4444)
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, gradeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "AI MENU COMPLIANCE SCORE",
                                            color = LyoColors.AmberYellow,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Quality Audit & Evaluation",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(gradeColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = menuGrade,
                                            color = gradeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0x1FFFFFFF), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Categories count
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Categories", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("$categoryCount cats", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    // Items count
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Total Foods", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("$totalItems dishes", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    // Mutton counts
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text("🥩 Mutton Promoted", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("$muttonCount dishes", color = if (muttonCount > 0) Color(0xFFFEF08A) else Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                    // Average Price
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text("Avg Price Pt", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("₹${avgPrice.toInt()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // Section: AI Voice Review Panel (Index 1)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, LyoColors.AmberYellow.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Card Header
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = LyoColors.AmberYellow, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("🔊 AI Voice Review Assistant", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Please listen to the AI's pronunciation and menu details review before publishing to ensure high speech synthesis quality.",
                                        color = LyoColors.TextSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Progress Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Speaking Progress:", color = LyoColors.TextSecondary, fontSize = 9.sp)
                                        Text(
                                            text = if (currentSegment == -1) "Not Playing" else "${currentSegment + 1} / ${segments.size}",
                                            color = LyoColors.AmberYellow,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                        color = LyoColors.AccentOrange,
                                        trackColor = Color(0x33FFFFFF)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Spoken content preview bubble
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x1Fffffff))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = if (currentSegment in segments.indices) segments[currentSegment] else "Click play to start the voice audit review.",
                                            color = if (currentSegment != -1) Color.White else Color.LightGray,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            fontStyle = if (currentSegment == -1) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Controls Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Play / Resume
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (!isPlaying || isPaused) {
                                                Button(
                                                    onClick = {
                                                        isPlaying = true
                                                        isPaused = false
                                                        if (currentSegment == -1) {
                                                            playSegment(0)
                                                        } else {
                                                            playSegment(currentSegment)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen),
                                                    shape = RoundedCornerShape(18.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (isPaused) "Resume" else "Play Audit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            } else {
                                                // Pause
                                                Button(
                                                    onClick = {
                                                        isPaused = true
                                                        tts?.stop()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AmberYellow),
                                                    shape = RoundedCornerShape(18.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(Icons.Filled.Pause, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Pause", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                }
                                            }
                                            
                                            // Stop
                                            if (isPlaying || currentSegment != -1) {
                                                OutlinedButton(
                                                    onClick = {
                                                        isPlaying = false
                                                        isPaused = false
                                                        currentSegment = -1
                                                        tts?.stop()
                                                    },
                                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                                    shape = RoundedCornerShape(18.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(Icons.Filled.Stop, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Stop", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        
                                        // Replay
                                        if (currentSegment != -1) {
                                            IconButton(
                                                onClick = {
                                                    isPlaying = true
                                                    isPaused = false
                                                    playSegment(0)
                                                },
                                                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0x1Fffffff))
                                            ) {
                                                Icon(Icons.Filled.Replay, contentDescription = "replay", tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section: Restaurant Metadata (Index 2)
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "RESTAURANT METADATA",
                                        color = LyoColors.AmberYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            editName = state.restaurantName
                                            editNameTa = state.restaurantNameTa
                                            editBusinessType = state.businessType
                                            editAddress = state.address
                                            editPhone = state.phone
                                            editPhotoUrl = state.photoUrl
                                            editLat = state.lat
                                            editLng = state.lng
                                            showEditMetaDialog = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Edit Metadata",
                                            tint = LyoColors.AccentOrange,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // Image Preview section
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x11FFFFFF))
                                        .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.photoUrl.isNotBlank()) {
                                        androidx.compose.foundation.Image(
                                            painter = coil.compose.rememberAsyncImagePainter(state.photoUrl),
                                            contentDescription = "restaurant image",
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.TableChart, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("No photo mapped. Use Edit above to bind photo.", color = Color.Gray, fontSize = 9.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                MetadataRow("Restaurant ID", state.restaurantId)
                                MetadataRow("Shop Name (EN)", state.restaurantName)
                                MetadataRow("Shop Name (TA)", state.restaurantNameTa)
                                MetadataRow("Business Type", state.businessType)
                                MetadataRow("Address", state.address)
                                MetadataRow("Contact Phone", state.phone)
                                MetadataRow("Photo URL", state.photoUrl)
                                MetadataRow("Latitude (அட்சரேகை)", state.lat.toString())
                                MetadataRow("Longitude (தீர்க்கரேகை)", state.lng.toString())
                            }
                        }
                        
                        // Section: Categories & Menu items (Index 3)
                        item {
                            Text(
                                "CATEGORIZED MENU CORES",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        state.menuData.forEach { (categoryName, itemList) ->
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x1F000000))
                                        .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            categoryName.split("__AND__").first().uppercase(),
                                            color = LyoColors.AccentOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Add item under this category
                                            androidx.compose.material3.IconButton(
                                                onClick = {
                                                    targetCategoryForNewItem = categoryName
                                                    newItemNameEn = ""
                                                    newItemNameTa = ""
                                                    newItemPrice = ""
                                                    newItemMeatType = "VEG"
                                                    showAddItemDialog = true
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = "Add Item to Category",
                                                    tint = LyoColors.VegGreen,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }

                                            // Rename category
                                            androidx.compose.material3.IconButton(
                                                onClick = {
                                                    categoryToRename = categoryName
                                                    newCategoryName = categoryName
                                                    showRenameCategoryDialog = true
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = "Rename Category",
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }

                                            // Delete category
                                            androidx.compose.material3.IconButton(
                                                onClick = {
                                                    viewModel.deleteDraftCategory(categoryName)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Delete Category",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    val catTa = categoryName.split("__AND__").getOrNull(1) ?: ""
                                    if (catTa.isNotBlank()) {
                                        Text(
                                            catTa,
                                            color = LyoColors.TextSecondary,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    
                                    // Map items with their original index to safely edit/delete
                                    val itemsWithIdx = itemList.mapIndexed { idx, it -> idx to it }
                                    // Mutton items prioritized first!
                                    val sortedItems = itemsWithIdx.sortedWith(compareByDescending { it.second.meatType.uppercase() == "MUTTON" })
                                    
                                    sortedItems.forEach { (origIdx, item) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    item.itemName,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (item.itemNameTa.isNotBlank()) {
                                                    Text(
                                                        item.itemNameTa,
                                                        color = LyoColors.AmberYellow,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(top = 1.dp)
                                                    )
                                                }
                                                
                                                val isMutton = item.meatType.uppercase() == "MUTTON"
                                                val isChicken = item.meatType.uppercase() == "CHICKEN"
                                                val isVeg = item.meatType.uppercase() == "VEG"
                                                
                                                val badgeText = if (isMutton) "🥩 Mutton" else if (isChicken) "🍗 Chicken" else if (isVeg) "🟢 Veg" else "⚪ Other"
                                                val badgeBg = if (isMutton) Color(0x4DCA8A04) else if (isChicken) Color(0x33F97316) else if (isVeg) Color(0x3322C55E) else Color(0x1FFFFFFF)
                                                val badgeColor = if (isMutton) Color(0xFFFEF08A) else if (isChicken) Color(0xFFFFCCAA) else if (isVeg) Color(0xFF86EFAC) else LyoColors.TextSecondary
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 2.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(badgeBg)
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        badgeText,
                                                        color = badgeColor,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    "₹${item.price.toInt()}",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                
                                                // Edit food item button
                                                IconButton(
                                                    onClick = {
                                                        editingCategoryKey = categoryName
                                                        editingItemIndex = origIdx
                                                        editItemNameEn = item.itemName
                                                        editItemNameTa = item.itemNameTa
                                                        editItemPrice = item.price.toString()
                                                        editItemMeatType = item.meatType
                                                        showEditItemDialog = true
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Edit,
                                                        contentDescription = "Edit Item",
                                                        tint = Color(0xFF38BDF8),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                                
                                                // Delete food item button
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteDraftMenuItem(categoryName, origIdx)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "Delete Item",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                        }
                                        HorizontalDivider(color = Color(0x0FFFFFFF), thickness = 1.dp)
                                    }
                                }
                            }
                        }

                        // Section: Add New Category button
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    newCategoryInputName = ""
                                    showAddCategoryDialog = true
                                },
                                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ADD NEW CATEGORY (புதிய பிரிவு)", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Section: Final Publish Button
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    viewModel.publishCurrentDraftDirectly()
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CONFIRM & PUBLISH MENU LIVE 🚀 (வெளியிடு)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = LyoColors.TextSecondary, fontSize = 11.sp)
        Text(
            text = if (value.isBlank()) "--" else value,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
