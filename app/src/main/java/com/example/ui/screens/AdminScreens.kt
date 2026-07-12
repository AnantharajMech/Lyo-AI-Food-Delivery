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
import androidx.compose.ui.draw.shadow
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
import android.util.Log
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonElevation
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

// Overrides for Light Operational Theme (AdminScreens)
private val LocalTextColor = staticCompositionLocalOf<Color?> { null }

private object AdminThemeColors {
    val DarkCyanBg = LyoColors.DarkCyanBg
    val CardSlate = LyoColors.CardSlate
    val AccentOrange = LyoColors.AccentOrange
    val AmberYellow = LyoColors.AmberYellow
    val GlassBorder = LyoColors.GlassBorder
    val TranslucentSlate = LyoColors.TranslucentSlate
    val TranslucentBlack = LyoColors.TranslucentBlack
    val TextPrimary = LyoColors.TextPrimary
    val TextSecondary = LyoColors.TextSecondary
    val VegGreen = LyoColors.VegGreen
    val NonVegRed = LyoColors.NonVegRed
    val WarningYellow = LyoColors.WarningYellow
    val LiveCyan = LyoColors.LiveCyan
}

@Composable
private fun Color(color: Long): Color {
    return androidx.compose.ui.graphics.Color(color)
}

@Composable
private fun Color(color: Int): Color {
    return androidx.compose.ui.graphics.Color(color)
}

@Composable
private fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val overrideColor = LocalTextColor.current
    val finalColor = when {
        overrideColor != null -> overrideColor
        else -> color
    }
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = finalColor,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
private fun Text(
    text: androidx.compose.ui.text.AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val overrideColor = LocalTextColor.current
    val finalColor = when {
        overrideColor != null -> overrideColor
        else -> color
    }
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = finalColor,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
private fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val finalTint = tint
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = finalTint
    )
}

@Composable
private fun Icon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val finalTint = tint
    androidx.compose.material3.Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = finalTint
    )
}

@Composable
private fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle.copy(color = LyoColors.TextPrimary),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LyoColors.TextPrimary,
            unfocusedTextColor = LyoColors.TextPrimary,
            focusedBorderColor = LyoColors.AmberYellow,
            unfocusedBorderColor = LyoColors.GlassBorder,
            focusedLabelColor = LyoColors.AmberYellow,
            unfocusedLabelColor = LyoColors.TextSecondary
        )
    )
}

@Composable
private fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(LocalTextColor provides Color.White) {
            content()
        }
    }
}

@Composable
private fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(contentColor = LyoColors.AccentOrange),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(LocalTextColor provides LyoColors.AccentOrange) {
            content()
        }
    }
}

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogoutClick: () -> Unit,
    onSwitchToCustomer: (() -> Unit)? = null
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
    var dispatchCustomerNameState by remember { mutableStateOf("") }
    var dispatchCustomerPhoneState by remember { mutableStateOf("") }
    var dispatchCustomerAddressState by remember { mutableStateOf("") }
    var confirmRiderToAssign by remember { mutableStateOf<com.example.data.database.User?>(null) }
    var confirmStatusChangeOrder by remember { mutableStateOf<Pair<com.example.data.database.Order, String>?>(null) }

    if (confirmStatusChangeOrder != null) {
        val (ord, status) = confirmStatusChangeOrder!!
        val statusTamil = when (status) {
            "ACCEPTED" -> "ஏற்கப்பட்டது (ACCEPTED)"
            "PREPARING" -> "சமையலறையில் தயாரிக்கப்படுகிறது (PREPARING)"
            "READY_FOR_PICKUP" -> "விநியோகிக்க தயாராக உள்ளது (READY FOR PICKUP)"
            "CANCELLED" -> "ரத்து செய்யப்பட்டது (CANCELLED)"
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

    var showLogoutConfirmation by remember { mutableStateOf(false) }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = {
                androidx.compose.material3.Text(
                    text = "வெளியேறவும் (Logout Confirmation)",
                    color = Color(0xFF172033),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                androidx.compose.material3.Text(
                    text = "நிர்வாகி பேனலில் இருந்து வெளியேற விரும்புகிறீர்களா?\n\nAre you sure you want to log out from the Admin Panel?",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmation = false
                        onLogoutClick()
                        android.widget.Toast.makeText(context, "Logged out successfully", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    androidx.compose.material3.Text("YES, LOGOUT", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    androidx.compose.material3.Text("CANCEL", color = Color(0xFF64748B), fontSize = 13.sp)
                }
            },
            containerColor = Color.White
        )
    }

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
                    .padding(20.dp, 12.dp)
                    .background(LyoColors.CardSlate, RoundedCornerShape(12.dp))
                    .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Lyo AI Food Delivery Custom Logo Icon / Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LyoColors.VegGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = "Lyo",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        androidx.compose.material3.Text(
                            text = "Lyo AI Food Delivery",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LyoColors.TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Text(
                                text = "Admin Panel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LyoColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Online Status Dot
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(LyoColors.VegGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            androidx.compose.material3.Text(
                                text = "ONLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = LyoColors.VegGreen
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onSwitchToCustomer != null) {
                        IconButton(
                            onClick = onSwitchToCustomer,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LyoColors.CardSlate)
                        ) {
                            Icon(Icons.Filled.Store, contentDescription = "Storefront", tint = LyoColors.AmberYellow, modifier = Modifier.size(18.dp))
                        }
                    }

                    IconButton(
                        onClick = { showLogoutConfirmation = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LyoColors.CardSlate)
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = "logout", tint = LyoColors.NonVegRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LyoColors.CardSlate)
                    .padding(4.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                val tabs = mutableListOf(
                    "ANALYTICS" to "Analytics",
                    "LIVE_TEST" to "Live Test 🧪",
                    "SMART_MENU" to "Smart Menu 🤖",
                    "VENDORS" to "Stores",
                    "BANNERS" to "Banners",
                    "LOGISTICS" to "Orders",
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
                            .background(if (isSel) Color(0xFF15803D) else Color.Transparent)
                            .clickable {
                                activeTab = tabId
                                viewModel.selectedAdminVendor.value = null
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(LocalTextColor provides (if (isSel) Color.White else Color(0xFF64748B))) {
                            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab View Contents
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                "ANALYTICS" -> {
                    AnalyticsDashboardTab(viewModel = viewModel)
                }

                "LIVE_TEST" -> {
                    LiveTestMonitorTab(viewModel = viewModel)
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
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Onboard Premium Merchant",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    IconButton(
                                        onClick = { activeTab = "VENDORS" },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = LyoColors.AccentOrange
                                        )
                                    }
                                }

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
                                     text = if (isOnboarding) "ONBOARDING IN PROGRESS..." else "ONBOARD BRAND NEW VENTURE",
                                     onClick = {
                                         val localContext = context
                                         viewModel.onboardVendor(
                                             onSuccess = {
                                                 android.widget.Toast.makeText(localContext, "✅ New venue onboarded successfully!", android.widget.Toast.LENGTH_LONG).show()
                                             },
                                             onError = { err ->
                                                 android.widget.Toast.makeText(localContext, err, android.widget.Toast.LENGTH_LONG).show()
                                             }
                                         )
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
                    var logisticsSubTab by remember { mutableStateOf("ACTIVE") }
                    val filteredOrders = remember(orders, logisticsSubTab) {
                        orders.filter { ord ->
                            when (logisticsSubTab) {
                                "ACTIVE" -> ord.status != "DELIVERED" && ord.status != "CANCELLED"
                                "COMPLETED" -> ord.status == "DELIVERED"
                                "CANCELLED" -> ord.status == "CANCELLED"
                                else -> true
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        item {
                            Text(
                                text = "SYSTEM ORDER TRAFFIC (${filteredOrders.size} listed)",
                                color = LyoColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // SEGMENTED CONTROL FOR ACTIVE / COMPLETED / CANCELLED ORDERS
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val subTabs = listOf(
                                    Triple("ACTIVE", "Active", "செயலில்"),
                                    Triple("COMPLETED", "Completed", "முடிந்தது"),
                                    Triple("CANCELLED", "Cancelled", "ரத்து")
                                )
                                subTabs.forEach { tab ->
                                    val isSelected = logisticsSubTab == tab.first
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) LyoColors.AccentOrange else Color(0x11FFFFFF))
                                            .border(1.dp, if (isSelected) LyoColors.AccentOrange else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                            .clickable { logisticsSubTab = tab.first }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = tab.second, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(text = tab.third, color = Color.White.copy(alpha = 0.6f), fontSize = 10.5.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredOrders.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                    Text("No orders match this status classification.", color = LyoColors.TextSecondary)
                                }
                            }
                        }

                        items(filteredOrders, key = { it.id }) { ord ->
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
                                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                                    Text("Order ID: #LYO-${ord.id}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                    if (com.example.data.repository.LyoLiveTestTracker.isTestOrder(ord)) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFF8B5CF6))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("🧪 TEST ORDER", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
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

                                        val itemsListState = remember(ord.id) {
                                            viewModel.repository.orderItemDao.getItemsForOrderFlow(ord.id)
                                        }.collectAsState(initial = emptyList())
                                        val itemsList = itemsListState.value

                                        if (itemsList.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "ITEMS IN ORDER (ஆர்டர் செய்யப்பட்டவை):",
                                                color = LyoColors.TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0x0AFFFFFF), RoundedCornerShape(8.dp))
                                                    .padding(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                itemsList.forEach { item ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "• ${item.nameEn} ${if (item.nameTa.isNotBlank() && item.nameTa != item.nameEn) "(${item.nameTa})" else ""} x${item.quantity}",
                                                            color = Color.White,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Text(
                                                            text = "₹${(item.price * item.quantity).toInt()}",
                                                            color = LyoColors.AmberYellow,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                
                                                Divider(color = Color(0x0DFFFFFF), modifier = Modifier.padding(vertical = 4.dp))
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Subtotal", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                                    Text("₹${ord.subtotal.toInt()}", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                                }
                                                if (ord.deliveryFee > 0) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text("Delivery Fee", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                                        Text("₹${ord.deliveryFee.toInt()}", color = LyoColors.TextSecondary, fontSize = 11.sp)
                                                    }
                                                }
                                                if (ord.couponDiscount > 0) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text("Discount Coupon", color = Color(0xFFEF4444), fontSize = 11.sp)
                                                        Text("-₹${ord.couponDiscount.toInt()}", color = Color(0xFFEF4444), fontSize = 11.sp)
                                                    }
                                                }
                                                if (ord.tipAmount > 0) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text("Rider Tip", color = LyoColors.VegGreen, fontSize = 11.sp)
                                                        Text("+₹${ord.tipAmount.toInt()}", color = LyoColors.VegGreen, fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }

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
                                            if (ord.status != "DELIVERED" && ord.status != "CANCELLED") {
                                                val nextStatus = when (ord.status) {
                                                    "PENDING" -> "ACCEPTED"
                                                    "ACCEPTED" -> "PREPARING"
                                                    "PREPARING" -> "READY_FOR_PICKUP"
                                                    else -> "READY_FOR_PICKUP"
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (ord.status != "READY_FOR_PICKUP" && ord.status != "OUT_FOR_DELIVERY") {
                                                        Button(
                                                            onClick = {
                                                                confirmStatusChangeOrder = ord to nextStatus
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("MARK $nextStatus", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    } else {
                                                        Text(
                                                            text = "Rider Transit Active",
                                                            color = LyoColors.TextSecondary,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }

                                                    // Cancel Button
                                                    Button(
                                                        onClick = {
                                                            confirmStatusChangeOrder = ord to "CANCELLED"
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("CANCEL ORDER ரத்து", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            } else if (ord.status == "CANCELLED") {
                                                Text("Cancelled (ரத்து செய்யப்பட்டது)", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                                    val customerUser = viewModel.repository.findUser(ord.userId)
                                                    val customerName = customerUser?.name?.ifBlank { null } ?: "Lyo AI Food Delivery Customer"
                                                    val customerPhone = customerUser?.phone?.ifBlank { null } ?: ord.userId
                                                    val customerAddress = customerUser?.address?.ifBlank { null } ?: "Coordinates (${ord.customerLat}, ${ord.customerLng})"
                                                    LyoNotificationHelper.generateOrderPdfAndShare(context, ord, items, customerName, customerPhone, customerAddress)
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
                                                         append("🏪 *உணவகம் (Shop):* ${vendor?.name ?: "Lyo AI Food Delivery Partner"}\n")
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

                                         Button(
                                              onClick = {
                                                  coroutineScope.launch {
                                                      val items = viewModel.getOrderItems(ord.id)
                                                      val customerUser = viewModel.repository.findUser(ord.userId)
                                                      val customerName = customerUser?.name ?: "Lyo AI Food Delivery Customer"
                                                      LyoNotificationHelper.generateKitchenKotPdfAndShare(context, ord, items, customerName)
                                                  }
                                              },
                                              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                              shape = RoundedCornerShape(10.dp),
                                              modifier = Modifier.fillMaxWidth()
                                          ) {
                                              Icon(
                                                  imageVector = Icons.Filled.Receipt,
                                                  contentDescription = null,
                                                  tint = Color.White,
                                                  modifier = Modifier.size(16.dp)
                                              )
                                              Spacer(modifier = Modifier.width(8.dp))
                                              Text(
                                                  text = "KOT அச்சிடு (Print KOT PDF)",
                                                  color = Color.White,
                                                  fontSize = 11.sp,
                                                  fontWeight = FontWeight.Bold
                                              )
                                          }

                                          Spacer(modifier = Modifier.height(8.dp))
                                          Spacer(modifier = Modifier.height(8.dp))
                                         Button(
                                             onClick = {
                                                 coroutineScope.launch {
                                                     val items = viewModel.getOrderItems(ord.id)
                                                     val customerUser = viewModel.repository.findUser(ord.userId)
                                                     val customerName = customerUser?.name?.ifBlank { null } ?: "Lyo AI Food Delivery Customer"
                                                     val customerPhone = customerUser?.phone?.ifBlank { null } ?: ord.userId
                                                     val customerAddress = customerUser?.address?.ifBlank { null } ?: "Coordinates (${ord.customerLat}, ${ord.customerLng})"
                                                     LyoNotificationHelper.generateOrderPdfAndShare(context, ord, items, customerName, customerPhone, customerAddress)
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
                            val coroutineScope = rememberCoroutineScope()
                            val globalCategoriesFlow = remember { viewModel.repository.categoryDao.getCategoriesForVendor(-1L) }
                            val globalCategories by globalCategoriesFlow.collectAsState(initial = emptyList())

                            val newCatNameEn by viewModel.newCategoryNameEn.collectAsState()
                            val newCatNameTa by viewModel.newCategoryNameTa.collectAsState()
                            val newCatIconKey by viewModel.newCategoryIconKey.collectAsState()
                            val newCatAccentColor by viewModel.newCategoryAccentColor.collectAsState()
                            val newCatIsActive by viewModel.newCategoryIsActive.collectAsState()
                            val newCatSortOrder by viewModel.newCategorySortOrder.collectAsState()

                            AlertDialog(
                                onDismissRequest = { showClassificationsDialog = false },
                                title = {
                                    Text(
                                        "முகப்பு கேட்டகிரி மேலாண்மை (Home Categories)",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                },
                                text = {
                                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                        // Section 1: Add Category Form
                                        Text(
                                            "சேர் / ADD NEW CLASSIFICATION",
                                            color = Color(0xFFFF7A1A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        OutlinedTextField(
                                            value = newCatNameEn,
                                            onValueChange = { viewModel.newCategoryNameEn.value = it },
                                            label = { Text("English Name (e.g., Chinese)", fontSize = 11.sp) },
                                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color(0xFF334155),
                                                focusedBorderColor = Color(0xFFFF7A1A),
                                                unfocusedLabelColor = Color.LightGray,
                                                focusedLabelColor = Color(0xFFFF7A1A)
                                            ),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = newCatNameTa,
                                            onValueChange = { viewModel.newCategoryNameTa.value = it },
                                            label = { Text("Tamil Name (e.g., சைனீஸ்)", fontSize = 11.sp) },
                                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color(0xFF334155),
                                                focusedBorderColor = Color(0xFFFF7A1A),
                                                unfocusedLabelColor = Color.LightGray,
                                                focusedLabelColor = Color(0xFFFF7A1A)
                                            ),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                            singleLine = true
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                         ) {
                                             OutlinedTextField(
                                                 value = newCatSortOrder,
                                                 onValueChange = { viewModel.newCategorySortOrder.value = it },
                                                 label = { Text("Sort Order", fontSize = 11.sp) },
                                                 textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                                 colors = OutlinedTextFieldDefaults.colors(
                                                     unfocusedBorderColor = Color(0xFF334155),
                                                     focusedBorderColor = Color(0xFFFF7A1A),
                                                     unfocusedLabelColor = Color.LightGray
                                                 ),
                                                 modifier = Modifier.width(100.dp),
                                                 singleLine = true
                                             )

                                             Row(verticalAlignment = Alignment.CenterVertically) {
                                                 Text("Active", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                                                 Switch(
                                                     checked = newCatIsActive,
                                                     onCheckedChange = { viewModel.newCategoryIsActive.value = it },
                                                     colors = SwitchDefaults.colors(
                                                         checkedThumbColor = Color(0xFF22C55E),
                                                         checkedTrackColor = Color(0x3322C55E)
                                                     )
                                                 )
                                             }
                                         }

                                         // Icon Picker
                                         Text(
                                             "ஐகான் தேர்வு / CHOOSE ICON",
                                             color = Color.LightGray,
                                             fontSize = 11.sp,
                                             fontWeight = FontWeight.Bold,
                                             modifier = Modifier.padding(bottom = 6.dp)
                                         )
                                         Row(
                                             modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                             horizontalArrangement = Arrangement.spacedBy(6.dp)
                                         ) {
                                             val iconOptions = listOf("Restaurant", "Coffee", "LocalDining", "Cake", "LocalPizza", "Store", "Icecream", "Fastfood")
                                             iconOptions.forEach { iconName ->
                                                 val isSelected = newCatIconKey.equals(iconName, ignoreCase = true)
                                                 Box(
                                                     modifier = Modifier
                                                         .size(34.dp)
                                                         .clip(RoundedCornerShape(8.dp))
                                                         .background(if (isSelected) Color(0xFFFF7A1A) else Color(0xFF1E293B))
                                                         .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                                         .clickable { viewModel.newCategoryIconKey.value = iconName },
                                                     contentAlignment = Alignment.Center
                                                 ) {
                                                     Icon(
                                                         imageVector = getIconForCategoryKey(iconName),
                                                         contentDescription = iconName,
                                                         tint = if (isSelected) Color.White else Color.LightGray,
                                                         modifier = Modifier.size(16.dp)
                                                     )
                                                 }
                                             }
                                         }

                                         // Accent Glow Color
                                         Text(
                                             "வண்ணம் / CHOOSE GLOW COLOR",
                                             color = Color.LightGray,
                                             fontSize = 11.sp,
                                             fontWeight = FontWeight.Bold,
                                             modifier = Modifier.padding(bottom = 6.dp)
                                         )
                                         Row(
                                             modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                                         ) {
                                             val colorOptions = listOf("#16C7E8", "#FF7A1A", "#16A56B", "#D94A52", "#A855F7", "#EC4899", "#EAB308")
                                             colorOptions.forEach { hexColor ->
                                                 val isSelected = newCatAccentColor.equals(hexColor, ignoreCase = true)
                                                 val colorObj = Color(android.graphics.Color.parseColor(hexColor))
                                                 Box(
                                                     modifier = Modifier
                                                         .size(28.dp)
                                                         .clip(CircleShape)
                                                         .background(colorObj)
                                                         .border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                                                         .clickable { viewModel.newCategoryAccentColor.value = hexColor }
                                                 )
                                             }
                                         }

                                         Button(
                                             onClick = {
                                                 viewModel.createCategory(-1L, onSuccess = {
                                                     android.widget.Toast.makeText(context, "கேட்டகிரி சேர்க்கப்பட்டது! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                                                 }, onError = { err ->
                                                     android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show()
                                                 })
                                             },
                                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A1A)),
                                             modifier = Modifier.fillMaxWidth().height(36.dp),
                                             shape = RoundedCornerShape(8.dp)
                                         ) {
                                             Text("CREATE CLASSIFICATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                         }

                                         Spacer(modifier = Modifier.height(16.dp))
                                         Divider(color = Color(0xFF334155))
                                         Spacer(modifier = Modifier.height(16.dp))

                                         // Section 2: Current List
                                         Text(
                                             "தற்போதுள்ள கேட்டகிரிகள் / CURRENT SYSTEM CATEGORIES",
                                             color = Color(0xFF16C7E8),
                                             fontWeight = FontWeight.Bold,
                                             fontSize = 12.sp,
                                             modifier = Modifier.padding(bottom = 8.dp)
                                         )

                                         Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                             globalCategories.sortedBy { it.sortOrder }.forEach { cat ->
                                                 Row(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .clip(RoundedCornerShape(10.dp))
                                                         .background(Color(0xFF1E293B))
                                                         .border(1.dp, if (cat.isActive) Color(0x3316C7E8) else Color.Transparent, RoundedCornerShape(10.dp))
                                                         .padding(horizontal = 10.dp, vertical = 8.dp),
                                                     horizontalArrangement = Arrangement.SpaceBetween,
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                         val catColor = try { Color(android.graphics.Color.parseColor(cat.accentColor)) } catch(e: Exception) { Color(0xFF16C7E8) }
                                                         Box(
                                                             modifier = Modifier
                                                                 .size(36.dp)
                                                                 .clip(RoundedCornerShape(8.dp))
                                                                 .background(catColor.copy(alpha = 0.15f))
                                                                 .border(1.dp, catColor, RoundedCornerShape(8.dp)),
                                                             contentAlignment = Alignment.Center
                                                         ) {
                                                             Icon(
                                                                 imageVector = getIconForCategoryKey(cat.iconKey),
                                                                 contentDescription = cat.nameEn,
                                                                 tint = catColor,
                                                                 modifier = Modifier.size(18.dp)
                                                             )
                                                         }
                                                         Spacer(modifier = Modifier.width(8.dp))
                                                         Column {
                                                             Row(verticalAlignment = Alignment.CenterVertically) {
                                                                 Text(cat.nameEn, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                                 Spacer(modifier = Modifier.width(6.dp))
                                                                 Box(
                                                                     modifier = Modifier
                                                                         .size(6.dp)
                                                                         .background(if (cat.isActive) Color(0xFF22C55E) else Color(0xFFEF4444), CircleShape)
                                                                 )
                                                             }
                                                             Text(cat.nameTa, color = Color.LightGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                             Text("Sort Order: ${cat.sortOrder}", color = Color.Gray, fontSize = 9.sp)
                                                         }
                                                     }

                                                     Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                         IconButton(
                                                             onClick = {
                                                                 coroutineScope.launch {
                                                                     viewModel.repository.categoryDao.updateCategory(cat.copy(sortOrder = cat.sortOrder - 1))
                                                                 }
                                                             },
                                                             modifier = Modifier.size(24.dp)
                                                         ) {
                                                             Icon(Icons.Filled.ArrowUpward, contentDescription = "Up", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                                         }
                                                         IconButton(
                                                             onClick = {
                                                                 coroutineScope.launch {
                                                                     viewModel.repository.categoryDao.updateCategory(cat.copy(sortOrder = cat.sortOrder + 1))
                                                                 }
                                                             },
                                                             modifier = Modifier.size(24.dp)
                                                         ) {
                                                             Icon(Icons.Filled.ArrowDownward, contentDescription = "Down", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                                         }
                                                         IconButton(
                                                             onClick = {
                                                                 coroutineScope.launch {
                                                                     viewModel.repository.categoryDao.updateCategory(cat.copy(isActive = !cat.isActive))
                                                                 }
                                                             },
                                                             modifier = Modifier.size(24.dp)
                                                         ) {
                                                             Icon(
                                                                 imageVector = if (cat.isActive) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                                 contentDescription = "Toggle Active",
                                                                 tint = if (cat.isActive) Color(0xFF22C55E) else Color.Gray,
                                                                 modifier = Modifier.size(16.dp)
                                                             )
                                                         }
                                                         IconButton(
                                                             onClick = {
                                                                 coroutineScope.launch {
                                                                     viewModel.repository.categoryDao.deleteCategory(cat)
                                                                 }
                                                             },
                                                             modifier = Modifier.size(24.dp)
                                                         ) {
                                                             Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
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
                                             showClassificationsDialog = false
                                         }
                                     ) {
                                         Text("முடிந்தது (DONE)", color = LyoColors.VegGreen, fontWeight = FontWeight.Bold)
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
                                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "புதிய உணவகம்",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            lineHeight = 13.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }

                                    Button(
                                        onClick = { showClassificationsDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        border = BorderStroke(1.dp, Color(0xFF334155)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.Sort, contentDescription = null, tint = LyoColors.AccentOrange, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "முகப்பு கேட்டகிரி",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            lineHeight = 13.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    val st = item.status
                                                    val (stColor, label) = when (st) {
                                                        "ACTIVE" -> LyoColors.VegGreen to "ACTIVE"
                                                        "DRAFT" -> Color.Gray to "DRAFT"
                                                        "PAUSED" -> LyoColors.AmberYellow to "PAUSED"
                                                        "ARCHIVED" -> Color.LightGray to "ARCHIVED"
                                                        "REJECTED" -> Color.Red to "REJECTED"
                                                        "REVIEW_REQUIRED" -> LyoColors.NonVegRed to "REVIEW REQUIRED"
                                                        "READY_TO_PUBLISH" -> Color.Cyan to "READY TO PUBLISH"
                                                        else -> LyoColors.AccentOrange to st
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(stColor.copy(alpha = 0.2f))
                                                            .border(0.5.dp, stColor, RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(label, color = stColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    if (item.isOnHoliday) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color.Red.copy(alpha = 0.2f))
                                                                .border(0.5.dp, Color.Red, RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("HOLIDAY/CLOSED", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
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
                        var editStatus by remember(partner.id) { mutableStateOf(partner.status) }

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
                            editStatus = partner.status
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
                        var selectedFilterCategoryId by remember(partner.id) { mutableStateOf<Long?>(null) }

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

                                        Text("Store Lifecycle Status:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val statusList = listOf("DRAFT", "REVIEW_REQUIRED", "READY_TO_PUBLISH", "ACTIVE", "PAUSED", "REJECTED", "ARCHIVED")
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(statusList) { st ->
                                                val isSelected = editStatus == st
                                                val stColor = when (st) {
                                                    "ACTIVE" -> LyoColors.VegGreen
                                                    "DRAFT" -> Color.Gray
                                                    "PAUSED" -> LyoColors.AmberYellow
                                                    "ARCHIVED" -> Color.LightGray
                                                    "REJECTED" -> Color.Red
                                                    "REVIEW_REQUIRED" -> LyoColors.NonVegRed
                                                    "READY_TO_PUBLISH" -> Color.Cyan
                                                    else -> LyoColors.AccentOrange
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) stColor.copy(alpha = 0.3f) else Color(0x15FFFFFF))
                                                        .border(1.dp, if (isSelected) stColor else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                                        .clickable { editStatus = st }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(st, color = if (isSelected) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

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
                                                    val uRole = currentUser?.role ?: ""
                                                    val uPhone = currentUser?.phone ?: ""
                                                    val isUserAdmin = uRole == "ADMIN"

                                                    // 1. Manager / assigned shop check
                                                    if (!isUserAdmin) {
                                                        val cleanUPhone = uPhone.replace(Regex("[^0-9]"), "")
                                                        val cleanVPhone = partner.phone.replace(Regex("[^0-9]"), "")
                                                        val isAssigned = cleanUPhone.isNotBlank() && cleanVPhone.isNotBlank() && (cleanUPhone.endsWith(cleanVPhone) || cleanVPhone.endsWith(cleanUPhone))
                                                        if (!isAssigned) {
                                                            android.widget.Toast.makeText(context, "⚠️ நீங்கள் உங்கள் சொந்த கடையில் மட்டுமே மாற்றம் செய்ய முடியும்! (Manager must only edit/publish their own assigned shop)", android.widget.Toast.LENGTH_LONG).show()
                                                            return@Button
                                                        }
                                                    }

                                                    // 2. Field validations
                                                    val nameVal = editName.trim()
                                                    val addrVal = editAddress.trim()
                                                    val phoneVal = editPhone.trim()
                                                    val openVal = editAutoOpenTime.trim()
                                                    val closeVal = editAutoCloseTime.trim()

                                                    if (nameVal.isBlank()) {
                                                        android.widget.Toast.makeText(context, "⚠️ கடையின் பெயர் தேவை! (Store Name is required)", android.widget.Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    if (addrVal.isBlank()) {
                                                        android.widget.Toast.makeText(context, "⚠️ முகவரி தேவை! (Address is required)", android.widget.Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    if (phoneVal.isBlank()) {
                                                        android.widget.Toast.makeText(context, "⚠️ தொலைபேசி எண் தேவை! (Phone number is required)", android.widget.Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    if (openVal.isBlank() || closeVal.isBlank()) {
                                                        android.widget.Toast.makeText(context, "⚠️ திறக்கும் மற்றும் மூடும் நேரம் தேவை! (Opening and Closing times are required)", android.widget.Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }

                                                    // 3. Active menu item check
                                                    val activeItemsCount = itemsList.count { it.isAvailable }
                                                    if (activeItemsCount == 0 && editStatus == "ACTIVE") {
                                                        android.widget.Toast.makeText(context, "⚠️ உணவகத்தை வெளியிட (Active செய்ய) குறைந்தது ஒரு மெனு ஐட்டமாவது (Active Menu Item) இருக்க வேண்டும்!", android.widget.Toast.LENGTH_LONG).show()
                                                        return@Button
                                                    }

                                                    val updated = partner.copy(
                                                        name = nameVal,
                                                        type = editType,
                                                        address = addrVal,
                                                        isOnHoliday = editIsOnHoliday,
                                                        autoOpenTime = openVal,
                                                        autoCloseTime = closeVal,
                                                        deliveryFee = editDeliveryFee.toDoubleOrNull() ?: partner.deliveryFee,
                                                        minOrderAmount = editMinOrder.toDoubleOrNull() ?: partner.minOrderAmount,
                                                        bannerUrl = editBannerUrl.trim(),
                                                        phone = phoneVal,
                                                        visibilityRadiusKm = editVisibilityRadiusKm,
                                                        lat = editLat,
                                                        lng = editLng,
                                                        sortOrder = editSortOrder.toIntOrNull() ?: partner.sortOrder,
                                                        isDynamicDelivery = editIsDynamicDelivery,
                                                        status = editStatus
                                                    )
                                                    val localContext = context
                                                    viewModel.updateVendor(updated) {
                                                        android.widget.Toast.makeText(localContext, "✅ கடையின் விவரங்கள் சேமிக்கப்பட்டன! (Store details saved!)", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
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
                                                                val localContext = context
                                                                viewModel.deleteVendor(partner) {
                                                                    android.widget.Toast.makeText(localContext, "🗑️ கடை முற்றிலும் நீக்கப்பட்டது! (Store deleted!)", android.widget.Toast.LENGTH_SHORT).show()
                                                                }
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
                                                        val localContext = context
                                                        viewModel.updateVendor(updated) {
                                                            android.widget.Toast.makeText(localContext, "✅ பேனர் புதுப்பிக்கப்பட்டது! (Banner updated!)", android.widget.Toast.LENGTH_SHORT).show()
                                                        }
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
                                                val localContext = context
                                                viewModel.updateVendor(updated) {
                                                    android.widget.Toast.makeText(localContext, if (it) "✅ கூப்பன் செயல்படுத்தப்பட்டது! (Coupon enabled!)" else "⚠️ கூப்பன் முடக்கப்பட்டது! (Coupon disabled!)", android.widget.Toast.LENGTH_SHORT).show()
                                                }
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
                                                val localContext = context
                                                viewModel.updateVendor(updated) {
                                                    android.widget.Toast.makeText(localContext, "✅ பேனர் புதுப்பிக்கப்பட்டது! (Banner updated!)", android.widget.Toast.LENGTH_SHORT).show()
                                                }
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
                                        onClick = {
                                            val localContext = context
                                            viewModel.createCategory(
                                                vendorId = partner.id,
                                                onSuccess = {
                                                    android.widget.Toast.makeText(localContext, "✅ வகை வெற்றிகரமாக உருவாக்கப்பட்டது! (Category created!)", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { msg ->
                                                    android.widget.Toast.makeText(localContext, msg, android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        },
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
                                            onClick = {
                                                val localContext = context
                                                viewModel.createMenuItem(
                                                    vendorId = partner.id,
                                                    onSuccess = {
                                                        android.widget.Toast.makeText(localContext, "✅ உணவு வெற்றிகரமாக சேர்க்கப்பட்டது! (Dish added!)", android.widget.Toast.LENGTH_SHORT).show()
                                                    },
                                                    onError = { msg ->
                                                        android.widget.Toast.makeText(localContext, msg, android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            },
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
                                val totalItemsCount = itemsList.size
                                val uncategorizedItemsCount = itemsList.count { item -> categoriesList.none { it.id == item.categoryId } }
                                val categoryCounts = categoriesList.associate { cat ->
                                    cat.id to itemsList.count { it.categoryId == cat.id }
                                }

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
                                    Spacer(modifier = Modifier.height(12.dp))

                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. "All Items" chip
                                        item {
                                            val isSelected = selectedFilterCategoryId == null
                                            Box(
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) LyoColors.AccentOrange else Color(0x11FFFFFF))
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) LyoColors.AccentOrange else Color(0x33FFFFFF),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { selectedFilterCategoryId = null }
                                                    .padding(horizontal = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "All Items ($totalItemsCount)",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // 2. Dynamic Categories
                                        items(categoriesList, key = { it.id }) { cat ->
                                            val count = categoryCounts[cat.id] ?: 0
                                            val isSelected = selectedFilterCategoryId == cat.id
                                            Box(
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) LyoColors.AccentOrange else Color(0x11FFFFFF))
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) LyoColors.AccentOrange else Color(0x33FFFFFF),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { selectedFilterCategoryId = cat.id }
                                                    .padding(horizontal = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${cat.nameEn} ($count)",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // 3. Uncategorized chip (if there are uncategorized items)
                                        if (uncategorizedItemsCount > 0) {
                                            item {
                                                val isSelected = selectedFilterCategoryId == -1L
                                                Box(
                                                    modifier = Modifier
                                                        .height(48.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (isSelected) LyoColors.AccentOrange else Color(0x11FFFFFF))
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) LyoColors.AccentOrange else Color(0x33FFFFFF),
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable { selectedFilterCategoryId = -1L }
                                                        .padding(horizontal = 16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Uncategorized ($uncategorizedItemsCount)",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            val filteredMenuItems = itemsList.filter { item ->
                                // Category filtering
                                val matchesCategory = when (selectedFilterCategoryId) {
                                    null -> true // All Items
                                    -1L -> categoriesList.none { it.id == item.categoryId } // Uncategorized
                                    else -> item.categoryId == selectedFilterCategoryId
                                }

                                // Search filtering
                                val itemCat = categoriesList.find { it.id == item.categoryId }
                                val catNameEn = itemCat?.nameEn ?: "Uncategorized"
                                val catNameTa = itemCat?.nameTa ?: "Uncategorized"

                                val matchesSearch = if (menuSearchQuery.isBlank()) {
                                    true
                                } else {
                                    item.nameEn.contains(menuSearchQuery, ignoreCase = true) ||
                                    item.nameTa.contains(menuSearchQuery, ignoreCase = true) ||
                                    item.descEn.contains(menuSearchQuery, ignoreCase = true) ||
                                    item.descTa.contains(menuSearchQuery, ignoreCase = true) ||
                                    catNameEn.contains(menuSearchQuery, ignoreCase = true) ||
                                    catNameTa.contains(menuSearchQuery, ignoreCase = true)
                                }

                                matchesCategory && matchesSearch
                            }

                            if (filteredMenuItems.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 40.dp, horizontal = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "No items found in this category",
                                            color = LyoColors.TextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LyoButton(
                                            text = "Clear filters",
                                            onClick = {
                                                selectedFilterCategoryId = null
                                                menuSearchQuery = ""
                                            }
                                        )
                                    }
                                }
                            } else {
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
                            var showNewCategoryFields by remember(item.id) { mutableStateOf(false) }
                            var newCatEn by remember(item.id) { mutableStateOf("") }
                            var newCatTa by remember(item.id) { mutableStateOf("") }

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
                                        Text("Category (MANDATORY) • வகை:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                        val selectedCat = categoriesList.find { it.id == selectedCatId }
                                        val selectedCatName = selectedCat?.let { "${it.nameEn} / ${it.nameTa}" } ?: "Uncategorized (வகைப்படுத்தப்படாதது)"

                                        Text(
                                            text = "Current: $selectedCatName",
                                            color = LyoColors.AccentOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )

                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
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

                                        Spacer(modifier = Modifier.height(4.dp))

                                        if (!showNewCategoryFields) {
                                            TextButton(
                                                onClick = { showNewCategoryFields = true },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Icon(Icons.Filled.Add, contentDescription = "add", tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Create New Category Inline", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0x0AFFFFFF), RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
                                                    .padding(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("Create New Category", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                                OutlinedTextField(
                                                    value = newCatEn,
                                                    onValueChange = { newCatEn = it },
                                                    label = { Text("Category Name (English)", fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                OutlinedTextField(
                                                    value = newCatTa,
                                                    onValueChange = { newCatTa = it },
                                                    label = { Text("Category Name (Tamil)", fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            if (newCatEn.isNotBlank() && newCatTa.isNotBlank()) {
                                                                scope.launch {
                                                                    val cat = Category(vendorId = partner.id, nameEn = newCatEn.trim(), nameTa = newCatTa.trim())
                                                                    val newId = viewModel.repository.categoryDao.insertCategory(cat)
                                                                    val updatedCat = cat.copy(id = newId)
                                                                    try {
                                                                        com.example.data.repository.LyoFirebaseHelper.syncCategoryToFirestore(updatedCat)
                                                                    } catch (e: Exception) {
                                                                        Log.e("AdminScreens", "Firestore createCategory error: ${e.message}")
                                                                    }
                                                                    selectedCatId = newId
                                                                    newCatEn = ""
                                                                    newCatTa = ""
                                                                    showNewCategoryFields = false
                                                                }
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen),
                                                        modifier = Modifier.weight(1.5f)
                                                    ) {
                                                        Text("Create & Select", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    TextButton(
                                                        onClick = { showNewCategoryFields = false },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Cancel", color = Color.LightGray, fontSize = 10.sp)
                                                    }
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
                                            val localContext = context
                                            viewModel.updateMenuItem(updated) {
                                                android.widget.Toast.makeText(localContext, "✅ உணவின் விவரங்கள் சேமிக்கப்பட்டன! (Dish updated!)", android.widget.Toast.LENGTH_SHORT).show()
                                            }
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
                                                val localContext = context
                                                 viewModel.updateCategory(
                                                     catNode.copy(
                                                         nameEn = nameEn.trim(),
                                                         nameTa = nameTa.trim(),
                                                         autoOpenTime = autoOpen.trim(),
                                                         autoCloseTime = autoClose.trim()
                                                     )
                                                 ) {
                                                     android.widget.Toast.makeText(localContext, "✅ வகைப்பாடு சேமிக்கப்பட்டது! (Category saved!)", android.widget.Toast.LENGTH_SHORT).show()
                                                 }
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
                                            val localContext = context
                                            viewModel.deleteCategory(catNode) {
                                                android.widget.Toast.makeText(localContext, "🗑️ வகைப்பாடு நீக்கப்பட்டது! (Category deleted!)", android.widget.Toast.LENGTH_SHORT).show()
                                            }
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
                                            val localContext = context
                                            viewModel.deleteMenuItem(dish) {
                                                android.widget.Toast.makeText(localContext, "🗑️ உணவு நீக்கப்பட்டது! (Dish deleted!)", android.widget.Toast.LENGTH_SHORT).show()
                                            }
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
                        val activePlatformRiders = riders.filter { (it.role == "DELIVERY" || it.role == "RIDER") && it.isActiveRider && !it.phone.startsWith("999991") && it.phone != "9000000002" && it.phone != "9000000003" }.distinctBy { it.phone }
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
                                 LyoNotificationHelper.generateOrderPdfAndShare(
                                     context = context,
                                     order = ord,
                                     items = dispatchOrderItemsState,
                                     customerName = dispatchCustomerNameState.ifBlank { "Lyo AI Food Delivery Customer" },
                                     customerPhone = dispatchCustomerPhoneState.ifBlank { ord.userId },
                                     customerAddress = dispatchCustomerAddressState.ifBlank { "Coordinates (${ord.customerLat}, ${ord.customerLng})" }
                                 )
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
                                         append("🏪 *உணவகம் (Shop):* ${dispatchVendorState?.name ?: "Lyo AI Food Delivery Partner"}\n")
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
                          
                          Spacer(modifier = Modifier.height(10.dp))
                          
                          // Action 3: Print KOT (Premium Printable PDF with large kitchen typography)
                          Button(
                              onClick = {
                                  try {
                                      val customerName = dispatchCustomerNameState.ifBlank { "Lyo AI Food Delivery Customer" }
                                      LyoNotificationHelper.generateKitchenKotPdfAndShare(context, ord, dispatchOrderItemsState, customerName)
                                  } catch (e: Exception) {
                                      e.printStackTrace()
                                  }
                              },
                              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                              shape = RoundedCornerShape(10.dp),
                              modifier = Modifier.fillMaxWidth()
                          ) {
                              Icon(Icons.Filled.Receipt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                              Spacer(modifier = Modifier.width(8.dp))
                              Text("PRINT KOT (PREMIUM PDF)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                                 dispatchCustomerNameState = customer?.name?.ifBlank { null } ?: "Lyo AI Food Delivery Customer"
                                 dispatchCustomerPhoneState = customer?.phone?.ifBlank { null } ?: order.userId
                                 dispatchCustomerAddressState = customer?.address?.ifBlank { null } ?: "Coordinates (${order.customerLat}, ${order.customerLng})"
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
                Spacer(modifier = Modifier.height(8.dp))

                val selectedSalaryType by viewModel.newRiderSalaryType.collectAsState()
                val salaryRateStr by viewModel.newRiderSalaryRate.collectAsState()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Salary Type Interactive Cards Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. MONTHLY BASE CARD
                        val isMonthly = selectedSalaryType == "MONTHLY"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isMonthly) Color(0x33FF7622) else Color(0x1F1E293B)
                                )
                                .border(
                                    width = if (isMonthly) 1.5.dp else 1.dp,
                                    color = if (isMonthly) LyoColors.AccentOrange else Color(0x1FFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.newRiderSalaryType.value = "MONTHLY" }
                                .padding(10.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isMonthly) LyoColors.AccentOrange.copy(alpha = 0.25f) else Color(0x11FFFFFF),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DateRange,
                                        contentDescription = null,
                                        tint = if (isMonthly) LyoColors.AccentOrange else LyoColors.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Monthly Base",
                                    color = if (isMonthly) Color.White else LyoColors.TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "மாதச் சம்பளம் 💰",
                                    color = if (isMonthly) LyoColors.AccentOrange else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // 2. PER_KM CARD
                        val isPerKm = selectedSalaryType == "PER_KM"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isPerKm) Color(0x33FF7622) else Color(0x1F1E293B)
                                )
                                .border(
                                    width = if (isPerKm) 1.5.dp else 1.dp,
                                    color = if (isPerKm) LyoColors.AccentOrange else Color(0x1FFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.newRiderSalaryType.value = "PER_KM" }
                                .padding(10.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isPerKm) LyoColors.AccentOrange.copy(alpha = 0.25f) else Color(0x11FFFFFF),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DirectionsBike,
                                        contentDescription = null,
                                        tint = if (isPerKm) LyoColors.AccentOrange else LyoColors.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Per Km Pay",
                                    color = if (isPerKm) Color.White else LyoColors.TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "கி.மீ சம்பளம் 🏍️",
                                    color = if (isPerKm) LyoColors.AccentOrange else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Salary Rate input field with premium border
                    OutlinedTextField(
                        value = salaryRateStr,
                        onValueChange = { viewModel.newRiderSalaryRate.value = it },
                        label = { Text(if (selectedSalaryType == "MONTHLY") "Monthly Base Pay (மாதச் சம்பளம்) - ₹" else "Rate per Kilometer (கி.மீ கட்டணம்) - ₹") },
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

                        // Row 3: Premium Action Buttons (WhatsApp Chat, Edit Form, Decline/Delete)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // WhatsApp Chat Button
                            Row(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .heightIn(min = 38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x1522C55E))
                                    .border(1.dp, Color(0x3322C55E), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val strippedPhone = rider.phone.replace(" ", "").replace("+", "")
                                        val finalPhone = if (strippedPhone.startsWith("91")) strippedPhone else "91$strippedPhone"
                                        com.example.WhatsAppHelper.sendMessage(
                                            context,
                                            finalPhone,
                                            "Hello ${rider.name}, Lyo Fresh Admin here. Are you available for pickup?"
                                        )
                                    }
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Chat,
                                    contentDescription = "chat on WhatsApp",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Chat / வாட்ஸ்அப்",
                                    color = Color(0xFF22C55E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            // Edit Button
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x1538BDF8))
                                    .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(10.dp))
                                    .clickable { riderToEdit = rider }
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit details",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit / திருத்து",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            // Delete Button
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x12EF4444))
                                    .border(1.dp, Color(0x26EF4444), RoundedCornerShape(10.dp))
                                    .clickable { riderToDelete = rider }
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Decline rider",
                                    tint = LyoColors.NonVegRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Delete / நீக்கு",
                                    color = LyoColors.NonVegRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    Spacer(modifier = Modifier.height(8.dp))

                    // Salary Type Interactive Cards Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. MONTHLY BASE CARD
                        val isMonthly = editSalaryType == "MONTHLY"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isMonthly) Color(0x33FF7622) else Color(0x1F1E293B)
                                )
                                .border(
                                    width = if (isMonthly) 1.5.dp else 1.dp,
                                    color = if (isMonthly) LyoColors.AccentOrange else Color(0x1FFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { editSalaryType = "MONTHLY" }
                                .padding(10.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isMonthly) LyoColors.AccentOrange.copy(alpha = 0.25f) else Color(0x11FFFFFF),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DateRange,
                                        contentDescription = null,
                                        tint = if (isMonthly) LyoColors.AccentOrange else LyoColors.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Monthly Base",
                                    color = if (isMonthly) Color.White else LyoColors.TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "மாதச் சம்பளம் 💰",
                                    color = if (isMonthly) LyoColors.AccentOrange else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // 2. PER_KM CARD
                        val isPerKm = editSalaryType == "PER_KM"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isPerKm) Color(0x33FF7622) else Color(0x1F1E293B)
                                )
                                .border(
                                    width = if (isPerKm) 1.5.dp else 1.dp,
                                    color = if (isPerKm) LyoColors.AccentOrange else Color(0x1FFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { editSalaryType = "PER_KM" }
                                .padding(10.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isPerKm) LyoColors.AccentOrange.copy(alpha = 0.25f) else Color(0x11FFFFFF),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DirectionsBike,
                                        contentDescription = null,
                                        tint = if (isPerKm) LyoColors.AccentOrange else LyoColors.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Per Km Pay",
                                    color = if (isPerKm) Color.White else LyoColors.TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "கி.மீ சம்பளம் 🏍️",
                                    color = if (isPerKm) LyoColors.AccentOrange else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Salary Rate input field
                    OutlinedTextField(
                        value = editSalaryRate,
                        onValueChange = { editSalaryRate = it },
                        label = { Text(if (editSalaryType == "MONTHLY") "Monthly Base Pay (மாதச் சம்பளம்) - ₹" else "Rate per Kilometer (கி.மீ கட்டணம்) - ₹") },
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
                Text(
                    text = "ADMINISTRATORS LIST",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = LyoColors.AccentOrange,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Slots: ${admins.size} / 6 Active Helpers",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color(0x3310B981), Color(0x4F059669))
                                ),
                                shape = RoundedCornerShape(100.dp)
                            )
                            .border(
                                width = 1.2.dp,
                                color = Color(0xFF10B981),
                                shape = RoundedCornerShape(100.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // Pulsing glowing circle
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "SUPER ADMIN ACTIVE ⚡",
                            color = Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "As Super Admin Anantharajmech (Anantharaj), you can manage and create custom secondary helper admin logins with distinct password codes to operate the supply portal simultaneously.",
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.8f),
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
                                    text = if (isSuper) "Anantharaj Super Admin (Anantharajmech)" else "${admin.name} (${admin.phone})",
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

    LaunchedEffect(Unit) {
        while (true) {
            isSyncing = true
            viewModel.syncWithFirestore { msg ->
                isSyncing = false
                syncMessage = msg
                lastSyncedTime = "Just now (Auto-synced)"
            }
            // Sync periodically every 25 seconds
            kotlinx.coroutines.delay(25000)
        }
    }

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
            val context = androidx.compose.ui.platform.LocalContext.current
            val isPausedState by viewModel.isAppPaused.collectAsState()
            val pauseMsgEnState by viewModel.appPauseMessageEn.collectAsState()
            val pauseMsgTaState by viewModel.appPauseMessageTa.collectAsState()

            var localIsPaused by remember(isPausedState) { mutableStateOf(isPausedState) }
            var localMsgEn by remember(pauseMsgEnState) { mutableStateOf(pauseMsgEnState) }
            var localMsgTa by remember(pauseMsgTaState) { mutableStateOf(pauseMsgTaState) }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsOff,
                            contentDescription = "App Pause Status",
                            tint = if (localIsPaused) Color.Red else LyoColors.VegGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📴 APP SUSPENSION & LEAVE CONTROLLER",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "விடுமுறை மற்றும் அவசர காலங்களில் ஆர்டர்களை தற்காலிகமாக நிறுத்தவும்",
                                color = if (localIsPaused) Color.Red else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pause Application (முழுவதுமாக நிறுத்து)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (localIsPaused) "Status: APPLICATION PAUSED 🔴" else "Status: SYSTEM RUNNING ONLINE 🟢",
                                color = if (localIsPaused) Color.Red else LyoColors.VegGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = localIsPaused,
                            onCheckedChange = { checked ->
                                localIsPaused = checked
                                viewModel.updateAppPauseSettings(context, checked, localMsgEn, localMsgTa)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Red,
                                checkedTrackColor = Color.Red.copy(alpha = 0.5f),
                                uncheckedThumbColor = LyoColors.VegGreen,
                                uncheckedTrackColor = LyoColors.VegGreen.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Text(
                        text = "Quick Leave Presets (விரைவு விடுப்பு தேர்வுகள்):",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    localIsPaused = true
                                    localMsgEn = "We are closed today for holiday. Will resume service tomorrow! Thank you."
                                    localMsgTa = "இன்று விடுமுறை காரணமாக சேவை கிடையாது. நாளை முதல் வழக்கம் போல் செயல்படும்! நன்றி."
                                    viewModel.updateAppPauseSettings(context, true, localMsgEn, localMsgTa)
                                    android.widget.Toast.makeText(context, "🔴 இன்று ஒரு நாள் மட்டும் விடுமுறைக்காக தற்காலிகமாக நிறுத்தப்பட்டது! (Paused for Today Only)", android.widget.Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (localIsPaused && localMsgTa.contains("இன்று விடுமுறை")) LyoColors.AccentOrange else Color(0x15FFFFFF)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Today,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "1 Day (இன்று)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        lineHeight = 13.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    localIsPaused = true
                                    localMsgEn = "We are currently on a short leave for 2 days. Service will resume shortly. Thank you."
                                    localMsgTa = "நாங்கள் 2 நாட்களுக்கு விடுப்பில் உள்ளோம். service விரைவில் மீண்டும் தொடங்கும். நன்றி."
                                    viewModel.updateAppPauseSettings(context, true, localMsgEn, localMsgTa)
                                    android.widget.Toast.makeText(context, "🔴 2 நாட்களுக்கு விடுமுறைக்காக தற்காலிகமாக நிறுத்தப்பட்டது! (Paused for 2 Days)", android.widget.Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (localIsPaused && localMsgTa.contains("2 நாட்களுக்கு")) LyoColors.AccentOrange else Color(0x15FFFFFF)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DateRange,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "2 Days (2 நாட்கள்)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        lineHeight = 13.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    localIsPaused = true
                                    localMsgEn = "We are currently on a short leave for 3 days. Thank you for your patience."
                                    localMsgTa = "நாங்கள் 3 நாட்களுக்கு விடுப்பில் உள்ளோம். உங்கள் பொறுமைக்கு நன்றி."
                                    viewModel.updateAppPauseSettings(context, true, localMsgEn, localMsgTa)
                                    android.widget.Toast.makeText(context, "🔴 3 நாட்களுக்கு விடுமுறைக்காக தற்காலிகமாக நிறுத்தப்பட்டது! (Paused for 3 Days)", android.widget.Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (localIsPaused && localMsgTa.contains("3 நாட்களுக்கு")) LyoColors.AccentOrange else Color(0x15FFFFFF)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Event,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "3 Days (3 நாட்கள்)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        lineHeight = 13.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    localIsPaused = false
                                    viewModel.updateAppPauseSettings(context, false, localMsgEn, localMsgTa)
                                    android.widget.Toast.makeText(context, "🟢 கடை வெற்றிகரமாக மீண்டும் ஆன் செய்யப்பட்டது! (System Online & Resumed)", android.widget.Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!localIsPaused) LyoColors.VegGreen else Color(0x15FFFFFF)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Resume (ஆன் செய்க)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        lineHeight = 13.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = localMsgEn,
                        onValueChange = {
                            localMsgEn = it
                            viewModel.updateAppPauseSettings(context, localIsPaused, it, localMsgTa)
                        },
                        label = { Text("English Notice Message", fontSize = 11.sp, color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = localMsgTa,
                        onValueChange = {
                            localMsgTa = it
                            viewModel.updateAppPauseSettings(context, localIsPaused, localMsgEn, it)
                        },
                        label = { Text("தமிழ் அறிவிப்பு செய்தி", fontSize = 11.sp, color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            viewModel.updateAppPauseSettings(context, localIsPaused, localMsgEn, localMsgTa)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text("Save Configurations (சேமி)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                    FirebaseKeyRow(label = "Messaging Sender ID", value = "604469873807")
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
                            text = "LIVE CLOUD DATABASE SYNC ENGINE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        // Glowing Pulsing Auto-sync active pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .background(Color(0x1522C55E), RoundedCornerShape(100.dp))
                                .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                            Text(
                                text = "AUTO-SYNC ON",
                                color = Color(0xFF22C55E),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "தானியங்கி ஒத்திசைவு பின்னணியில் செயலில் உள்ளது. நீங்கள் எந்த பொத்தானையும் அழுத்த வேண்டியதில்லை. Local database changes are auto-synced securely with remote Cloud Firestore.",
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
                                text = "Last Auto-Synced: $lastSyncedTime",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val isFirebaseConnected = com.example.data.repository.LyoFirebaseHelper.auth != null
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Firebase status: ",
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

                        // Auto-Running Engine Status Badge (Non-clickable, fully automatic status)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1F10B981))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AUTOMATIC ENGINE ACTIVE ⚙️",
                                fontSize = 10.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "info",
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSyncing) "Auto-sync engine synchronizing all databases..." else syncMessage,
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
    val menuItems by viewModel.allMenuItems.collectAsState()

    var selectedRange by remember { mutableStateOf("ALL") }

    val now = System.currentTimeMillis()
    val filteredOrdersByDate = remember(orders, selectedRange) {
        when (selectedRange) {
            "TODAY" -> {
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                orders.filter { it.timestamp >= cal.timeInMillis }
            }
            "7_DAYS" -> {
                orders.filter { it.timestamp >= now - (7 * 24 * 60 * 60 * 1000L) }
            }
            "30_DAYS" -> {
                orders.filter { it.timestamp >= now - (30 * 24 * 60 * 60 * 1000L) }
            }
            else -> orders
        }
    }

    val completedOrders = filteredOrdersByDate.filter { it.status == "DELIVERED" }
    val pendingOrders = filteredOrdersByDate.filter { it.status != "DELIVERED" && it.status != "CANCELLED" }
    
    val totalRevenue = completedOrders.sumOf { it.totalAmount }
    val platformEarned = totalRevenue * 0.15

    val hotVendor = if (filteredOrdersByDate.isNotEmpty() && vendors.isNotEmpty()) {
        val group = filteredOrdersByDate.groupBy { it.vendorId }
        val maxId = group.maxByOrNull { it.value.size }?.key
        vendors.find { it.id == maxId }?.name ?: "—"
    } else {
        "—"
    }

    val lowStockCount = menuItems.count { !it.isAvailable }
    val missingMetadataCount = vendors.count { 
        it.phone.isBlank() || it.address.isBlank() || it.nameTa.isBlank() || (it.lat == 11.5812 && it.lng == 77.8465)
    }
    val pendingRiderVerifications = customers.count { 
        (it.role == "DELIVERY" || it.role == "RIDER") && !it.isActiveRider 
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "ALL" to "All Time",
                    "TODAY" to "Today",
                    "7_DAYS" to "7 Days",
                    "30_DAYS" to "30 Days"
                ).forEach { (key, label) ->
                    val isSel = selectedRange == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) LyoColors.AccentOrange else Color(0x15FFFFFF))
                            .clickable { selectedRange = key }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color.White else Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (lowStockCount > 0 || missingMetadataCount > 0 || pendingRiderVerifications > 0) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0x33EF4444)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "⚠️ SYSTEM WARNINGS & PENDING ACTIONS",
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (lowStockCount > 0) {
                            Text(
                                "• Low Stock Warning: $lowStockCount menu items are marked unavailable (out of stock).",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                        if (missingMetadataCount > 0) {
                            Text(
                                "• Missing Venue Metadata: $missingMetadataCount stores have incomplete address, phone, Tamil localization or map pins.",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                        if (pendingRiderVerifications > 0) {
                            Text(
                                "• Rider Approvals Needed: $pendingRiderVerifications delivery agents are currently inactive, awaiting admin verification.",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
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
    val selectedStoreId by viewModel.selectedStoreIdForSmartMenu.collectAsState()
    
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
                        viewModel = viewModel,
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
                        isLoading = isLoading,
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
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (mobileTab == "CHAT") LyoColors.AccentOrange else Color(0xFF0F172A))
                            .clickable { mobileTab = "CHAT" }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                text = "🤖 AI Chat (${messages.size})",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (mobileTab == "REVIEW") LyoColors.AccentOrange else Color(0xFF0F172A))
                            .clickable { mobileTab = "REVIEW" }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                text = "📋 Review",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Tab content box taking full height
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (mobileTab == "CHAT") {
                        ChatInterfaceSection(
                            viewModel = viewModel,
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
                            state = state,
                            showInputArea = false
                        )
                    } else {
                        InteractivePreviewSection(
                            state = state,
                            rawJson = rawJson,
                            previewTab = previewTab,
                            onTabChange = { previewTab = it },
                            viewModel = viewModel,
                            isLoading = isLoading,
                            listState = previewListState
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                SmartMenuInputBar(
                    rawInput = rawInput,
                    onInputChange = { rawInput = it },
                    onSendMessage = { text ->
                        viewModel.sendSmartMenuMessage(text)
                        rawInput = ""
                    },
                    isLoading = isLoading,
                    selectedStoreId = selectedStoreId
                )
            }
        }
    }
    }
}

@Composable
fun ChatInterfaceSection(
    viewModel: AdminViewModel,
    messages: List<AdminViewModel.SmartMenuMessage>,
    isLoading: Boolean,
    rawInput: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onQuickPublish: () -> Unit,
    onReset: () -> Unit,
    onShowHelp: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    state: AdminViewModel.SmartMenuState? = null,
    showInputArea: Boolean = true
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var storeSearchQuery by remember { mutableStateOf("") }
    val vendors by viewModel.allVendors.collectAsState()
    val selectedStoreId by viewModel.selectedStoreIdForSmartMenu.collectAsState()
    val selectedStore = vendors.find { it.id == selectedStoreId }

    val context = androidx.compose.ui.platform.LocalContext.current
    val missingWords by viewModel.missingDictionaryWords.collectAsState()
    val selectedStoreHasBackup by viewModel.selectedStoreHasBackup.collectAsState()
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showMissingWordsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedStoreId) {
        selectedStoreId?.let { id ->
            viewModel.checkIfBackupExists(id)
        }
    }

    if (showRestoreConfirmDialog && selectedStore != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = {
                Text(
                    text = "மீட்டமைப்பை உறுதிப்படுத்துக 🔄",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "நீங்கள் உறுதியாக இந்த கடையின் கடைசியாக பேக்கப் எடுக்கப்பட்ட மெனுவை மீட்டமைக்க விரும்புகிறீர்களா?\n\nஎச்சரிக்கை: இது தற்போதைய கடையின் மெனுவை முழுமையாக அழித்து புதிய தரவை எழுதும்.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        viewModel.restoreLastMenuBackup(
                            selectedStore.id,
                            onSuccess = {
                                android.widget.Toast.makeText(context, "Menu restored successfully! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { err ->
                                android.widget.Toast.makeText(context, "Restore failed: $err ❌", android.widget.Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                ) {
                    Text("ஆம் (Yes, Restore)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("ரத்து செய் (Cancel)", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showMissingWordsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMissingWordsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = null,
                        tint = LyoColors.AmberYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Missing Dictionary Words 📖",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "The following English/Tamil terms were parsed but not found in the bilingual dictionary. They have been logged for future translation enhancements:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    
                    if (missingWords.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No missing words logged! All terms matched the dictionary. ✨", color = LyoColors.VegGreen, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .padding(8.dp)
                        ) {
                            androidx.compose.foundation.lazy.LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(missingWords.size) { index ->
                                    val word = missingWords[index]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(word.word, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        val df = java.text.SimpleDateFormat("MMM dd HH:mm", java.util.Locale.getDefault())
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0x33FFCCAA))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(df.format(java.util.Date(word.firstSeenAt)), color = Color(0xFFFFCCAA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    HorizontalDivider(color = Color(0x1FFFFFFF), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (missingWords.isNotEmpty()) {
                        Button(
                            onClick = {
                                viewModel.clearMissingWords()
                                showMissingWordsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Clear Log 🧹", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Button(
                        onClick = { showMissingWordsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange)
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0x33F8FAFC), RoundedCornerShape(12.dp))
            .padding(10.dp)
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Smart Menu Manager",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onShowHelp,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "help",
                        tint = LyoColors.AmberYellow,
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "reset",
                        tint = LyoColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))

        // 🎯 SELECT ACTIVE STORE (உணவகம் தேர்வு)*
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, if (selectedStoreId == null) LyoColors.AmberYellow.copy(alpha = 0.5f) else Color(0x33FFFFFF)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "🎯 SELECT ACTIVE STORE (உணவகம் தேர்வு)*",
                    color = if (selectedStoreId == null) LyoColors.AmberYellow else LyoColors.LiveCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .border(
                                1.dp,
                                if (selectedStoreId == null) LyoColors.AmberYellow else Color(0x33F8FAFC),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedStore?.let { "${it.name} - ${it.nameTa} (${it.phone})" } ?: "உணவகத்தைத் தேர்வு செய்க... (Select Restaurant)",
                                color = if (selectedStore == null) Color.Gray else Color.White,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (dropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = "toggle dropdown",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                    ) {
                        // Search bar inside Dropdown
                        OutlinedTextField(
                            value = storeSearchQuery,
                            onValueChange = { storeSearchQuery = it },
                            placeholder = { Text("Search by name or phone (தேடுக)...", fontSize = 10.sp, color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LyoColors.AccentOrange,
                                unfocusedBorderColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 10.sp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = "search", tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        )
                        
                        HorizontalDivider(color = Color(0x1FFFFFFF))
                        
                        val filteredVendors = vendors.filter {
                            it.name.contains(storeSearchQuery, ignoreCase = true) ||
                            it.nameTa.contains(storeSearchQuery, ignoreCase = true) ||
                            it.phone.contains(storeSearchQuery)
                        }
                        
                        if (filteredVendors.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("கடை எதுவும் கிடைக்கவில்லை (No matches)", color = Color.Gray, fontSize = 10.sp) },
                                onClick = {}
                            )
                        } else {
                            Box(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                                Column {
                                    filteredVendors.forEach { vendor ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(vendor.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Text("${vendor.nameTa} • ${vendor.type} • ${vendor.phone}", color = Color.Gray, fontSize = 9.sp)
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectedStoreIdForSmartMenu.value = vendor.id
                                                viewModel.initializeDraftWithStore(vendor)
                                                dropdownExpanded = false
                                                storeSearchQuery = ""
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedStore != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "💾 MENU BACKUP & RECOVERY (பேக்கப் & ரீஸ்டோர்)",
                        color = LyoColors.LiveCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Backup button
                        FilledTonalButton(
                            onClick = {
                                viewModel.createManualMenuBackup(
                                    selectedStore.id,
                                    selectedStore.name,
                                    onSuccess = {
                                        android.widget.Toast.makeText(context, "Menu backed up to cloud successfully! 💾🎉", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = { err ->
                                        android.widget.Toast.makeText(context, "Backup failed: $err ❌", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0x1F22C55E),
                                contentColor = Color(0xFF22C55E)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Backup Menu", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        // Restore button
                        FilledTonalButton(
                            onClick = {
                                showRestoreConfirmDialog = true
                            },
                            enabled = selectedStoreHasBackup,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (selectedStoreHasBackup) Color(0x1F38BDF8) else Color(0x0FFFFFFF),
                                contentColor = if (selectedStoreHasBackup) Color(0xFF38BDF8) else Color.Gray
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (selectedStoreHasBackup) "Restore Menu" else "No Cloud Backup", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Dictionary warnings collapsible section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clickable { showMissingWordsDialog = true },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, if (missingWords.isNotEmpty()) LyoColors.AmberYellow.copy(alpha = 0.4f) else Color(0x1FFFFFFF)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = null,
                        tint = if (missingWords.isNotEmpty()) LyoColors.AmberYellow else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Bilingual Dictionary Log", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text(
                            text = if (missingWords.isNotEmpty()) "${missingWords.size} words need translation review" else "All terms translated successfully!",
                            color = if (missingWords.isNotEmpty()) LyoColors.AmberYellow else Color.Gray,
                            fontSize = 8.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "view",
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Lyo AI Insights Collapsible Dashboard
        var showAiDashboard by remember { mutableStateOf(true) }
        if (state != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
                border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showAiDashboard = !showAiDashboard },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = LyoColors.AmberYellow, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lyo AI Active Audit Insights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Text(
                            text = if (showAiDashboard) "Hide Details 🔼" else "Show Details 🔽",
                            color = LyoColors.AccentOrange,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (showAiDashboard) {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0x11FFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Detected Info Column
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("✅ DETECTED INFO:", color = LyoColors.VegGreen, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                Text("• Shop: ${state.restaurantName}", color = Color.White, fontSize = 8.sp)
                                Text("• Type: ${state.businessType}", color = Color.White, fontSize = 8.sp)
                                if (state.phone.isNotBlank()) Text("• Phone: ${state.phone}", color = Color.White, fontSize = 8.sp)
                                if (state.address.isNotBlank()) Text("• Address: ${state.address}", color = Color.White, fontSize = 8.sp)
                                Text("• Menu size: ${state.menuData.values.sumOf { it.size }} items", color = Color.White, fontSize = 8.sp)
                            }
                            
                            // Missing / Warning Column
                            Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                                    Text("⚠️ VALIDATION WARNINGS:", color = LyoColors.AmberYellow, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    missingList.forEach { Text("• Missing: $it", color = Color(0xFFFCA5A5), fontSize = 8.sp) }
                                    warningList.forEach { Text("• Warning: $it", color = Color(0xFFFDE047), fontSize = 8.sp) }
                                } else {
                                    Text("🌟 AUDIT COMPLIANCE:", color = LyoColors.LiveCyan, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    Text("• All core details validated!", color = Color.White, fontSize = 8.sp)
                                    Text("• 100% complete bilingual metadata.", color = Color.White, fontSize = 8.sp)
                                    Text("• Ready for database publishing.", color = Color.White, fontSize = 8.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Publishing Status: ${state.status.uppercase()}",
                                color = if (state.status.uppercase() == "PUBLISHED") LyoColors.VegGreen else LyoColors.AmberYellow,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.status.uppercase() != "PUBLISHED") {
                                Text(
                                    text = "Ready to publish 🚀",
                                    color = LyoColors.VegGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Chat Stream list
        val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
        val maxHeightConstraint = screenHeight * 0.55f
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 320.dp, max = maxHeightConstraint)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x0AFFFFFF))
                .padding(4.dp)
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                        topStart = 8.dp,
                                        topEnd = 8.dp,
                                        bottomStart = if (isAdmin) 8.dp else 0.dp,
                                        bottomEnd = if (isAdmin) 0.dp else 8.dp
                                    )
                                )
                                .background(if (isAdmin) LyoColors.AccentOrange else Color(0xFF1E293B))
                                .border(
                                    1.dp,
                                    if (isAdmin) Color.Transparent else Color(0x1FFFFFFF),
                                    RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp,
                                        bottomStart = if (isAdmin) 8.dp else 0.dp,
                                        bottomEnd = if (isAdmin) 0.dp else 8.dp
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isAdmin) "Admin 👤" else "Lyo Smart Menu Manager 🤖",
                                color = if (isAdmin) Color(0xFFFFCCAA) else LyoColors.AmberYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
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
                        .padding(6.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xEB0F172A)),
                    border = BorderStroke(1.dp, LyoColors.AccentOrange.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            color = LyoColors.AccentOrange,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lyo AI is processing menu data... ⚙️",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Current Step: Analyzing categories, translating to Tamil & validating prices.",
                                color = LyoColors.TextSecondary,
                                fontSize = 8.sp
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
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { onInputChange("Merge both datasets and clear duplicates.") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0x3338BDF8),
                    contentColor = Color(0xFF38BDF8)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Merge chunks",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            
            FilledTonalButton(
                onClick = onQuickPublish,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0x3310B981),
                    contentColor = Color(0xFF10B981)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Publish to DB 🚀",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            var sampleMenuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                FilledTonalButton(
                    onClick = { sampleMenuExpanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0x33CA8A04),
                        contentColor = Color(0xFFEAB308)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "மாதிரி மெனு 📋",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                DropdownMenu(
                    expanded = sampleMenuExpanded,
                    onDismissRequest = { sampleMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("பிரியாணி மெனு 🍛", fontSize = 12.sp) },
                        onClick = {
                            sampleMenuExpanded = false
                            val txt = "Lyo Royal Biryani, Restaurant, Salem bypass Road, Edappadi, 9876543210\n" +
                                      "Menu:\n" +
                                      "[Biryanis]\n" +
                                      "Mutton Royal Dum Biryani ₹380 Mutton\n" +
                                      "Chicken Special Biryani ₹260 Chicken\n" +
                                      "Veg Kuska Biryani ₹140 Veg\n" +
                                      "[Starters]\n" +
                                      "Mutton Boti Fry ₹250 Mutton\n" +
                                      "Chicken 65 Boneless ₹180 Chicken\n" +
                                      "Spicy Cauliflower Manchurian ₹140 Veg"
                            onInputChange("")
                            viewModel.sendSmartMenuMessage(txt)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("பேக்கரி மெனு 🍰", fontSize = 12.sp) },
                        onClick = {
                            sampleMenuExpanded = false
                            val txt = "Lyo Sweet Palace & Bakery, Bakery, GH Road near Bypass, Edappadi, 9876543212\n" +
                                      "Menu:\n" +
                                      "[Hot Beverages]\n" +
                                      "Ginger Tea ₹15 Veg\n" +
                                      "Filter Coffee ₹20 Veg\n" +
                                      "[Sweets & Snacks]\n" +
                                      "Spl Ghee Mysorepak ₹320 Veg\n" +
                                      "Hot Potato Samosa ₹12 Veg\n" +
                                      "Veg Puff Crispy ₹18 Veg"
                            onInputChange("")
                            viewModel.sendSmartMenuMessage(txt)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("சைவ மெனு 🥬", fontSize = 12.sp) },
                        onClick = {
                            sampleMenuExpanded = false
                            val txt = "Lyo Pure Veg Saravana Mess, Hotel, South Car Street, Edappadi, 9876543211\n" +
                                      "Menu:\n" +
                                      "[Breakfast (காலை உணவு)]\n" +
                                      "Ghee Podi Roast ₹90 Veg\n" +
                                      "Spl Rava Dosa ₹80 Veg\n" +
                                      "Soft Idly (2 Nos) ₹30 Veg\n" +
                                      "[Lunch (மதிய உணவு)]\n" +
                                      "Traditional South Indian Meals ₹130 Veg"
                            onInputChange("")
                            viewModel.sendSmartMenuMessage(txt)
                        }
                    )
                }
            }
        }
        
        if (showInputArea) {
            Spacer(modifier = Modifier.height(10.dp))

            // Inline Warning if store not selected
            if (selectedStoreId == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x26F59E0B)),
                    border = BorderStroke(1.dp, LyoColors.AmberYellow.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = LyoColors.AmberYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "⚠️ மெனு அனுப்ப முதலில் உணவகத்தைத் தேர்வு செய்யவும்! (Select a store first above to enable sending.)",
                            color = LyoColors.AmberYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
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
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
                    maxLines = 4
                )

                val isStoreSelected = selectedStoreId != null

                Button(
                    onClick = {
                        clipboardManager.getText()?.text?.let { text ->
                            if (text.isNotBlank()) {
                                if (isStoreSelected) {
                                    onInputChange("")
                                    viewModel.sendSmartMenuMessage(text)
                                } else {
                                    onInputChange(text)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("📋 Paste\n(ஒட்டு)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, lineHeight = 11.sp)
                }
                Button(
                    onClick = {
                        if (isStoreSelected) {
                            onSendMessage()
                        }
                    },
                    enabled = rawInput.isNotBlank() && !isLoading && isStoreSelected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStoreSelected) LyoColors.AccentOrange else Color.Gray,
                        disabledContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "send",
                        tint = if (isStoreSelected) Color.White else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SmartMenuInputBar(
    rawInput: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isLoading: Boolean,
    selectedStoreId: Long?,
    modifier: Modifier = Modifier
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val isStoreSelected = selectedStoreId != null
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
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
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
            maxLines = 4
        )

        Button(
            onClick = {
                clipboardManager.getText()?.text?.let { text ->
                    if (text.isNotBlank()) {
                        if (isStoreSelected) {
                            onSendMessage(text)
                        } else {
                            onInputChange(text)
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            Text("📋 Paste\n(ஒட்டு)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, lineHeight = 11.sp)
        }

        Button(
            onClick = {
                if (isStoreSelected) {
                    onSendMessage(rawInput)
                }
            },
            enabled = rawInput.isNotBlank() && !isLoading && isStoreSelected,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isStoreSelected) LyoColors.AccentOrange else Color.Gray,
                disabledContainerColor = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp),
            contentPadding = PaddingValues(horizontal = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "send",
                tint = if (isStoreSelected) Color.White else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
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
    isLoading: Boolean = false,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
) {
    var showEditMetaDialog by remember { mutableStateOf(false) }
    val priceChangeChecked by viewModel.priceChangeChecked.collectAsState()
    val moveCategoryChecked by viewModel.moveCategoryChecked.collectAsState()
    val addTamilNameChecked by viewModel.addTamilNameChecked.collectAsState()
    val removeNeedsReviewLineChecked by viewModel.removeNeedsReviewLineChecked.collectAsState()
    val saveDraftChecked by viewModel.saveDraftChecked.collectAsState()
    val closeReopenChecked by viewModel.closeReopenChecked.collectAsState()

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
    var editItemCategory by remember { mutableStateOf("") }
    var editItemNeedsReview by remember { mutableStateOf(false) }

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
                        modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("📍 USE CURRENT GPS LOCATION (இருப்பிடத்தை பெறுக)", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                    androidx.compose.material3.OutlinedTextField(
                        value = editItemCategory,
                        onValueChange = { editItemCategory = it },
                        label = { Text("Category (பிரிவு)", color = Color.Gray) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (editItemNeedsReview) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { editItemNeedsReview = !editItemNeedsReview }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Needs Review (சரிபார்க்க தேவை)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Unresolved price-range line", color = Color.Gray, fontSize = 9.sp)
                                }
                            }
                            androidx.compose.material3.TextButton(
                                onClick = { editItemNeedsReview = false },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color(0xFF38BDF8))
                            ) {
                                Text("RESOLVE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x0Fffffff))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Review Status: Resolved ✅", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Price has been successfully verified", color = Color.Gray, fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    Text("Meat Type:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                meatType = editItemMeatType,
                                category = editItemCategory,
                                needsReview = editItemNeedsReview
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
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0x33F8FAFC), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Toggle tabs
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("TABLE" to "Review Table", "JSON" to "Output Schema JSON").forEach { (tabId, label) ->
                val isSel = previewTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) LyoColors.AccentOrange else Color(0xFF0F172A))
                        .clickable { onTabChange(tabId) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                    CircularProgressIndicator(
                        color = LyoColors.AccentOrange,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "பகுப்பாய்வு செய்யப்படுகிறது... ⚙️",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Lyo AI is parsing your menu paragraph into interactive schemas...",
                        color = LyoColors.TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else if (state == null) {
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
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (item.needsReview) Color(0x26EF4444) else Color.Transparent)
                                                .padding(horizontal = 4.dp, vertical = 6.dp),
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

                                                if (item.needsReview) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(top = 2.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                                            .border(0.5.dp, Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            "⚠️ Price Range - Needs Review",
                                                            color = Color(0xFFFCA5A5),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
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
                                                        editItemCategory = item.category
                                                        editItemNeedsReview = item.needsReview
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

                        // Section: Smart Menu Manager Compliance Checklist G
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.List,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "MANUAL COMPLIANCE CHECKLIST (சரிபார்ப்பு பட்டியல்)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    "Please complete all interface checks (A-F) to unlock the live publishing button.",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )
                                
                                val checks = listOf(
                                    Triple("Check A: Edit Price", "Edit any item and change its price.", priceChangeChecked),
                                    Triple("Check B: Move Category", "Edit any item and change its Category (e.g. Chicken Varieties) to move it.", moveCategoryChecked),
                                    Triple("Check C: Add/Edit Tamil Name", "Edit any item and update its Tamil name.", addTamilNameChecked),
                                    Triple("Check D: Resolve Needs Review", "Delete or resolve/fix at least one 'Needs Review' line.", removeNeedsReviewLineChecked),
                                    Triple("Check E: Save Draft", "Save your current progress as a draft.", saveDraftChecked),
                                    Triple("Check F: Close & Reopen Screen", "Close and reopen the review screen.", closeReopenChecked)
                                )
                                
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    checks.forEach { (title, desc, isChecked) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isChecked) Color(0xFF16A34A).copy(alpha = 0.1f) else Color(0x0Fffffff))
                                                .border(
                                                    1.dp, 
                                                    if (isChecked) Color(0xFF16A34A).copy(alpha = 0.4f) else Color.Transparent, 
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text(desc, color = Color.Gray, fontSize = 9.sp)
                                            }
                                            if (isChecked) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Passed",
                                                    tint = Color(0xFF22C55E),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.Warning,
                                                    contentDescription = "Pending",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Save Draft button
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            viewModel.saveDraft()
                                        },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SAVE DRAFT 💾", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    // Simulate Close/Reopen button
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            viewModel.markClosedAndReopened()
                                        },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("CLOSE & REOPEN 🔄", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Section: Final Publish Button (Unlocked only when all 6 checks are passed)
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (priceChangeChecked && moveCategoryChecked && addTamilNameChecked && removeNeedsReviewLineChecked && saveDraftChecked && closeReopenChecked) {
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
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "🔒 Publish Locked — Complete Checklist Above to Unlock",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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

@Composable
fun LiveTestMonitorTab(viewModel: AdminViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val orders by viewModel.allOrders.collectAsState()
    val activeRides by viewModel.activeDeliveryRides.collectAsState()

    val testPhones = listOf("9999900001", "9999900002", "9999900003", "9999900004", "9999900005")

    var reportsState by remember { mutableStateOf<List<com.example.data.repository.TestOrderReport>>(emptyList()) }

    LaunchedEffect(orders, activeRides) {
        val list = mutableListOf<com.example.data.repository.TestOrderReport>()
        for (phone in testPhones) {
            val order = orders.firstOrNull { it.userId == phone }
            if (order != null) {
                val ride = activeRides.firstOrNull { it.orderId == order.id } ?: viewModel.repository.getRideForOrder(order.id)
                val items = viewModel.getOrderItems(order.id)
                val report = com.example.data.repository.LyoLiveTestTracker.getReportForOrder(order, ride, items)
                list.add(report)
            } else {
                list.add(
                    com.example.data.repository.TestOrderReport(
                        orderId = 0L,
                        customerPhone = phone,
                        customerName = when (phone) {
                            "9999900001" -> "Test Customer 1"
                            "9999900002" -> "Test Customer 2"
                            "9999900003" -> "Test Customer 3"
                            "9999900004" -> "Test Customer 4"
                            "9999900005" -> "Test Customer 5"
                            else -> "Test Customer"
                        },
                        shopName = "None",
                        itemsText = "None",
                        price = 0.0,
                        riderName = "Unassigned",
                        riderPhone = "Unassigned",
                        placementTimeStr = "Pending",
                        adminAcceptanceTimeStr = "Pending",
                        riderAssignmentTimeStr = "Pending",
                        departureTimeStr = "Pending",
                        completionTimeStr = "Pending",
                        durationMinutes = 0,
                        gpsCoordinatesLog = "None",
                        notificationLogsStr = "None",
                        checklist = mapOf(
                            "A" to "NOT TESTED",
                            "B" to "NOT TESTED",
                            "C" to "NOT TESTED",
                            "D" to "NOT TESTED",
                            "E" to "NOT TESTED",
                            "F" to "NOT TESTED"
                        ),
                        finalStatus = "NOT TESTED"
                    )
                )
            }
        }
        reportsState = list
    }

    val totalOrdersCount = reportsState.count { it.orderId > 0L }
    val completedCount = reportsState.count { it.finalStatus == "PASS" }
    val overallStatus = when {
        completedCount == 5 -> "PASS (COMPLETE) 🎉"
        totalOrdersCount > 0 -> "RUNNING (IN PROGRESS) 🧪"
        else -> "READY (NOT TESTED) ⚡"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Master Banner
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Live Test Monitor 🧪", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("5-Customer Concurrent Multi-Device Live Order Flow", color = LyoColors.TextSecondary, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    completedCount == 5 -> Color(0x3322C55E)
                                    totalOrdersCount > 0 -> Color(0x33F97316)
                                    else -> Color(0x3364748B)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = overallStatus,
                            color = when {
                                completedCount == 5 -> Color(0xFF22C55E)
                                totalOrdersCount > 0 -> Color(0xFFF97316)
                                else -> Color(0xFF94A3B8)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Active Test Devices", color = LyoColors.TextSecondary, fontSize = 10.sp)
                        Text("5", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Orders Placed", color = LyoColors.TextSecondary, fontSize = 10.sp)
                        Text("$totalOrdersCount / 5", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Full Flows Passed", color = LyoColors.TextSecondary, fontSize = 10.sp)
                        Text("$completedCount / 5", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Export buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = {
                            val textBuilder = StringBuilder()
                            textBuilder.append("=== Lyo AI Food Delivery System Test Report ===\n")
                            textBuilder.append("Timestamp: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())} IST\n")
                            textBuilder.append("Overall Status: $overallStatus\n")
                            textBuilder.append("Passed Flows: $completedCount / 5\n\n")
                            for (rep in reportsState) {
                                textBuilder.append("Slot ${rep.customerName} (${rep.customerPhone}):\n")
                                textBuilder.append("  Order ID: ${if (rep.orderId > 0L) "#" + rep.orderId else "Pending"}\n")
                                textBuilder.append("  Status: ${rep.finalStatus}\n")
                                textBuilder.append("  Shop: ${rep.shopName} | Rider: ${rep.riderName}\n")
                                textBuilder.append("  Timestamps: Placement: ${rep.placementTimeStr} | Acceptance: ${rep.adminAcceptanceTimeStr} | Rider assignment: ${rep.riderAssignmentTimeStr} | Dispatch: ${rep.departureTimeStr} | Delivery: ${rep.completionTimeStr}\n")
                                textBuilder.append("  Checklist: A:${rep.checklist["A"]} B:${rep.checklist["B"]} C:${rep.checklist["C"]} D:${rep.checklist["D"]} E:${rep.checklist["E"]} F:${rep.checklist["F"]}\n")
                                textBuilder.append("  GPS Log: ${rep.gpsCoordinatesLog}\n")
                                textBuilder.append("  Device Push Notifications: ${rep.notificationLogsStr}\n")
                                textBuilder.append("--------------------------------------------------\n")
                            }
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Lyo Test Report", textBuilder.toString()))
                            android.widget.Toast.makeText(context, "Plain Text report copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📋 Copy Text Report", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    androidx.compose.material3.Button(
                        onClick = {
                            LyoNotificationHelper.generateTestReportPdfAndShare(context, reportsState)
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📄 Export PDF Report", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Concurrent Test Channels (5 Devices)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        for (i in reportsState.indices) {
            val report = reportsState[i]
            var isExpanded by remember { mutableStateOf(true) }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                borderColor = when (report.finalStatus) {
                    "PASS" -> Color(0xFF22C55E).copy(alpha = 0.3f)
                    "IN PROGRESS" -> LyoColors.AccentOrange.copy(alpha = 0.3f)
                    else -> Color(0xFF64748B).copy(alpha = 0.1f)
                }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(LyoColors.AccentOrange)
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                Text((i + 1).toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(report.customerName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Device Contact: ${report.customerPhone}", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (report.finalStatus) {
                                        "PASS" -> Color(0x3322C55E)
                                        "IN PROGRESS" -> Color(0x33F97316)
                                        else -> Color(0x3364748B)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = report.finalStatus,
                                color = when (report.finalStatus) {
                                    "PASS" -> Color(0xFF22C55E)
                                    "IN PROGRESS" -> Color(0xFFF97316)
                                    else -> Color(0xFF94A3B8)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.material3.HorizontalDivider(color = Color(0x11FFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Step statuses checklist row
                        Text("Checklist Evaluation (A to F):", color = LyoColors.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val steps = listOf("A", "B", "C", "D", "E", "F")
                            val stepsTa = listOf("Placed", "Accepted", "Rider Assign", "Live GPS", "Notifications", "Delivered")
                            for (j in steps.indices) {
                                val stepKey = steps[j]
                                val stepStatus = report.checklist[stepKey] ?: "NOT TESTED"
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (stepStatus) {
                                                    "PASS" -> Color(0x3322C55E)
                                                    "IN PROGRESS" -> Color(0x33F97316)
                                                    else -> Color(0x3364748B)
                                                }
                                            )
                                            .wrapContentSize(Alignment.Center)
                                    ) {
                                        Text(
                                            text = stepKey,
                                            color = when (stepStatus) {
                                                "PASS" -> Color(0xFF22C55E)
                                                "IN PROGRESS" -> Color(0xFFF97316)
                                                else -> Color(0xFF94A3B8)
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(stepsTa[j], color = LyoColors.TextSecondary, fontSize = 7.sp, maxLines = 1)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.material3.HorizontalDivider(color = Color(0x11FFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Order info
                        Text("Order details:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Linked Order:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            Text(if (report.orderId > 0L) "#LYO-${report.orderId}" else "Not created yet", color = Color.White, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Selected Store:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            Text(report.shopName, color = Color.White, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order Items & Subtotal:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            Text("${report.itemsText} (₹${report.price.toInt()})", color = Color.White, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Selected Delivery Partner:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            Text("${report.riderName} (${report.riderPhone})", color = Color.White, fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Flow Milestones Timestamps:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("A • Order Creation Timestamp:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            Text(report.placementTimeStr, color = Color.White, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("B • Admin Acceptance Timestamp:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            Text(report.adminAcceptanceTimeStr, color = Color.White, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("C • Partner Dispatch Timestamp:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            Text(report.riderAssignmentTimeStr, color = Color.White, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("D • Rider Out For Delivery Timestamp:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            Text(report.departureTimeStr, color = Color.White, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("F • Successful Handover Timestamp:", color = LyoColors.TextSecondary, fontSize = 10.sp)
                            Text(report.completionTimeStr, color = Color.White, fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Live GPS Logs (Salem Road, Idappadi):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(report.gpsCoordinatesLog, color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Device Notification Receipts Logs:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(report.notificationLogsStr, color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}