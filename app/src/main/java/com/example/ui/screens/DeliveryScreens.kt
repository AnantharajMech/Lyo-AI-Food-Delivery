package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DeliveryRide
import com.example.ui.viewmodels.DeliveryViewModel
import kotlinx.coroutines.launch
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

// Overrides for Light Operational Theme (DeliveryScreens)
private val LocalTextColor = staticCompositionLocalOf<Color?> { null }

private object RiderThemeColors {
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

// ==========================================
// 1. DELIVERY MOBILE VIEWPORT & DIRECTIONS
// ==========================================
@Composable
fun DeliveryPartnerDashboardScreen(
    viewModel: DeliveryViewModel,
    onLogoutClick: () -> Unit
) {
    val activeRides by viewModel.deliveryRides.collectAsState()
    val completedRidesList by viewModel.completedRides.collectAsState()
    val earningsToday by viewModel.totalEarningsToday.collectAsState()
    val completedTrips by viewModel.completedRidesCount.collectAsState()

    val otpInput by viewModel.otpInputVal.collectAsState()
    val otpError by viewModel.otpErrorState.collectAsState()

    var showOtpDialogForRide by remember { mutableStateOf<DeliveryRide?>(null) }

    val currentUserState by viewModel.currentUser.collectAsState()
    val isDeactivated = currentUserState?.isActiveRider == false

    val todayDistanceCovered by viewModel.todayDistanceCovered.collectAsState()
    val averageDeliveryTimeMinutes by viewModel.averageDeliveryTimeMinutes.collectAsState()

    var activeHudTheme by remember { mutableStateOf("ORANGE") }
    var hasAutoSelected by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("MY_JOB") }
    LaunchedEffect(activeRides) {
        if (!hasAutoSelected && activeRides.isNotEmpty()) {
            val hasReady = activeRides.any { it.status == "PENDING_RIDER_ACCEPT" }
            if (hasReady) {
                selectedTab = "READY"
            }
            hasAutoSelected = true
        }
    }
    var activeVehicleClass by remember { mutableStateOf("SCOOTER") }
    val hudColor = when (activeHudTheme) {
        "EMERALD" -> Color(0xFF00E676)
        "CYAN" -> Color(0xFF00E5FF)
        "GOLD" -> Color(0xFFFFB347)
        else -> Color(0xFFFF6B00)
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    var shiftActiveTimeStr by remember { mutableStateOf("0.1 Hrs") }

    var tickerIndex by remember { mutableStateOf(0) }
    val tickerPrompts = listOf(
        "🔥 Surge Bonus +₹30 Active in Idappadi Busstand",
        "📈 Live Yield: 1.8x multiplier in Salem zones",
        "🕒 Handoff Verification OTP required for secure payouts",
        "⚡ Extra ₹15.00 credited for lightning compliance"
    )
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            tickerIndex = (tickerIndex + 1) % tickerPrompts.size
        }
    }

    LaunchedEffect(currentUserState) {
        currentUserState?.let { user ->
            val sharedPrefs = context.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
            val key = "login_session_start_${user.phone}"
            var loginTime = sharedPrefs.getLong(key, 0L)
            if (loginTime == 0L) {
                loginTime = System.currentTimeMillis()
                sharedPrefs.edit().putLong(key, loginTime).apply()
            }
            
            while (true) {
                val elapsed = System.currentTimeMillis() - loginTime
                val hours = elapsed / (1000.0 * 60.0 * 60.0)
                shiftActiveTimeStr = String.format(java.util.Locale.US, "%.1f Hrs", if (hours < 0.1) 0.1 else hours)
                kotlinx.coroutines.delay(60000)
            }
        }
    }

    if (isDeactivated) {
        LyoBackground {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "locked",
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "கணக்கு முடக்கப்பட்டது / Account Deactivated",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "மதிப்பிற்குரிய டெலிவரி பார்ட்னர்,\n\nநிர்வாகியால் உங்கள் கணக்கு தற்காலிகமாக முடக்கப்பட்டுள்ளது. புதிய ஆர்டர்களைப் பெறவோ, ஏற்கனவே உள்ள ஆர்டர்களைத் தொடரவோ முடியாது. தயவுசெய்து எங்களை தொடர்பு கொள்ளவும்.\n\nYour driver dispatch account is inactive or suspended by administrator.",
                            fontSize = 13.sp,
                            color = LyoColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.stopRealGpsTracking()
                                onLogoutClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LOG OUT SECURELY", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        return
    }

    val activeDeliveringRide = activeRides.firstOrNull { it.status == "DELIVERING" }

    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            android.widget.Toast.makeText(context, "டெலிவரி GPS ட்ராக்கிங் வேலை செய்ய லொகேஷன் அனுமதி தேவை!", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (activeDeliveringRide != null) {
        DisposableEffect(activeDeliveringRide.id) {
            val hasFineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            val hasCoarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            if (hasFineLocation || hasCoarseLocation) {
                try {
                    // Start Background/Foreground GPS LocationTrackingService
                    val intent = android.content.Intent(context, com.example.LocationTrackingService::class.java).apply {
                        putExtra(com.example.LocationTrackingService.EXTRA_RIDE_ID, activeDeliveringRide.id)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                android.util.Log.w("DeliveryGPS", "Rider hardware GPS tracking permissions are not granted.")
            }
            onDispose {
                try {
                    val intent = android.content.Intent(context, com.example.LocationTrackingService::class.java)
                    context.stopService(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LyoBackground {
        val filteredRides = when (selectedTab) {
            "MY_JOB" -> activeRides.filter { ride ->
                ride.status != "PENDING_RIDER_ACCEPT"
            }
            "READY" -> activeRides.filter { ride ->
                ride.status == "PENDING_RIDER_ACCEPT"
            }
            else -> emptyList()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                // Header Stats Dashboard
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = LyoColors.CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LyoColors.GlassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Row 1: Brand Logo, App Name, and Logout Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                // Lyo AI Food Delivery Custom Logo Icon / Avatar
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(LyoColors.VegGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Text(
                                        text = "Lyo AI",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    androidx.compose.material3.Text(
                                        text = "Lyo AI",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = LyoColors.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    androidx.compose.material3.Text(
                                        text = "Rider Portal",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LyoColors.TextSecondary
                                    )
                                }
                            }

                            // Logout Button - Clear, Prominent, Safe
                            IconButton(
                                onClick = onLogoutClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .testTag("rider_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Logout,
                                    contentDescription = "Logout",
                                    tint = LyoColors.NonVegRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Divider Line
                        HorizontalDivider(
                            color = LyoColors.GlassBorder,
                            thickness = 1.dp
                        )

                        // Row 2: Active Availability Status and Online/Offline Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status indicator: Available / Busy / Offline
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val statusText = if (currentUserState?.isActiveRider == true) {
                                    if (activeDeliveringRide != null) "BUSY" else "AVAILABLE"
                                } else {
                                    "OFFLINE"
                                }
                                val statusColor = when (statusText) {
                                    "AVAILABLE" -> LyoColors.VegGreen
                                    "BUSY" -> LyoColors.WarningYellow
                                    else -> LyoColors.TextSecondary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                androidx.compose.material3.Text(
                                    text = statusText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Toggle Status Switch Pill
                            currentUserState?.let { user ->
                                val isOnline = user.isActiveRider
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isOnline) LyoColors.VegGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                        .clickable { viewModel.toggleRiderStatus() }
                                        .border(
                                            1.dp,
                                            if (isOnline) LyoColors.VegGreen else LyoColors.GlassBorder,
                                            RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (isOnline) LyoColors.VegGreen else LyoColors.TextSecondary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isOnline) "ஆன்லைன் (Online)" else "ஆஃப்லைன் (Offline)",
                                        color = if (isOnline) LyoColors.VegGreen else LyoColors.TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LyoColors.CardSlate)
                        .padding(4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val myJobCount = activeRides.count { ride -> 
                        ride.status != "PENDING_RIDER_ACCEPT"
                    }
                    val readyCount = activeRides.count { ride -> 
                        ride.status == "PENDING_RIDER_ACCEPT"
                    }
                    val historyCount = completedRidesList.size

                    val tabItems = listOf(
                        Triple("MY_JOB", "🏍️ My Job", myJobCount),
                        Triple("READY", "📦 Ready", readyCount),
                        Triple("HISTORY", "📜 History", historyCount)
                    )

                    tabItems.forEach { (tabId, label, count) ->
                        val isSelected = selectedTab == tabId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LyoColors.VegGreen else Color.Transparent)
                                .clickable { selectedTab = tabId }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$label ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isSelected) Color.White else LyoColors.TextSecondary
                                )
                                if (tabId == "READY" && count > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Rider Financial Performance Dashboard Summary
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("DAILY DRIVER EARNINGS", color = LyoColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "₹${String.format("%.2f", earningsToday)}",
                            color = LyoColors.VegGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("TOTAL TRIPS DONE", color = LyoColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$completedTrips rides",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Real-time Live Earnings Ticker marquee banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF6B00).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFF6B00).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp, 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00E5FF), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.animation.AnimatedContent(
                        targetState = tickerPrompts[tickerIndex],
                        transitionSpec = {
                            androidx.compose.animation.slideInVertically { h -> h } + androidx.compose.animation.fadeIn() togetherWith
                            androidx.compose.animation.slideOutVertically { h -> -h } + androidx.compose.animation.fadeOut()
                        },
                        label = "live_ticker"
                    ) { promptText ->
                        Text(
                            text = promptText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                // Interactive Fleet Controls: Theme Customizer & Vehicle Class Selectors
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎨 HUD THEME CUSTOMIZATION / இடைமுக வண்ணங்கள்",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "ORANGE" to "🌋 Lava",
                                "EMERALD" to "🍀 Emerald",
                                "CYAN" to "🌌 Aurora",
                                "GOLD" to "⚡ Gold"
                            ).forEach { (code, label) ->
                                val isSelected = activeHudTheme == code
                                val btnColor = when (code) {
                                    "EMERALD" -> Color(0xFF00E676)
                                    "CYAN" -> Color(0xFF00E5FF)
                                    "GOLD" -> Color(0xFFFFB347)
                                    else -> Color(0xFFFF6B00)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) btnColor.copy(alpha = 0.25f) else Color(0x0FFFFFFF))
                                        .border(1.dp, if (isSelected) btnColor else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable { activeHudTheme = code }
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) btnColor else Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "🛵 ACTIVE LOGISTICS VEHICLE / பயண வாகனம்",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "BICYCLE" to "🚲 Bicycle\n(0% Gas)",
                                "ELECTRIC_BIKE" to "⚡ E-Bike\n(Eco Green)",
                                "SCOOTER" to "🛵 Scooter\n(High Range)"
                            ).forEach { (code, label) ->
                                val isSelected = activeVehicleClass == code
                                val themeColor = hudColor
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) themeColor.copy(alpha = 0.15f) else Color(0x0FFFFFFF))
                                        .border(1.dp, if (isSelected) themeColor else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable { activeVehicleClass = code }
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Rider Performance, Earnings & Completed Order History Ledger (Replaces simulated 3D active engine)
            item {
                RiderPerformanceDashboardCard(
                    completedTripsCount = completedTrips,
                    earningsToday = earningsToday,
                    completedRides = completedRidesList,
                    viewModel = viewModel,
                    onSupportClick = {
                        try {
                            val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:18004190300")
                            }
                            context.startActivity(dialIntent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Cannot dial support", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            // Dynamic Tab Header Title
            item {
                val headerText = when (selectedTab) {
                    "MY_JOB" -> "ACTIVE DISPATCHES IN TRANSIT (${filteredRides.size})"
                    "READY" -> "INCOMING/READY DISPATCHES (${filteredRides.size})"
                    else -> "COMPLETED DISPATCH HISTORY (${completedRidesList.size})"
                }
                Text(
                    text = headerText,
                    color = LyoColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                )
            }

            if (selectedTab != "HISTORY" && filteredRides.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HourglassEmpty,
                            contentDescription = "none",
                            tint = LyoColors.TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (selectedTab) {
                                "MY_JOB" -> "No active deliveries in progress.\nGo to the READY tab to accept a trip!"
                                "READY" -> "No pending pickup assignments nearby currently.\nKeep the app open to receive alerts."
                                else -> ""
                            },
                            color = LyoColors.TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (selectedTab == "HISTORY" && completedRidesList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircleOutline,
                            contentDescription = "none",
                            tint = LyoColors.TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No completed trips yet.\nDeliver orders to build your history!",
                            color = LyoColors.TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (selectedTab != "HISTORY") {
                items(filteredRides, key = { it.id }) { ride ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        DeliveryJobCard(
                            ride = ride,
                            viewModel = viewModel,
                            onShowOtpVerify = { showOtpDialogForRide = ride }
                        )
                    }
                }
            } else {
                items(completedRidesList, key = { "history_${it.id}" }) { ride ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("ORDER #LYO-${ride.orderId}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Completed at: ${java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(System.currentTimeMillis()))}", color = LyoColors.TextSecondary, fontSize = 10.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0x1F10B981))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "COMPLETED ✅",
                                            color = LyoColors.VegGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.material3.HorizontalDivider(color = Color(0x1AFFFFFF))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Distance: ${String.format(java.util.Locale.US, "%.1f", ride.totalDistance)} km", color = Color.White, fontSize = 12.sp)
                                    Text("Payout: ₹${String.format(java.util.Locale.US, "%.2f", ride.earnings)}", color = LyoColors.VegGreen, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            // Stats / Advice section is shown below list in My Job tab for rider guide
            if (selectedTab == "MY_JOB") {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = "⚡ RIDER SHIFT STATISTICS",
                        color = LyoColors.AccentOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Icon(Icons.Filled.DirectionsBike, contentDescription = "Distance", tint = Color(0xFFFDE047), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("TODAY'S DISTANCE", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text(String.format(java.util.Locale.US, "%.1f km", todayDistanceCovered), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Icon(Icons.Filled.Schedule, contentDescription = "Time", tint = Color(0xFFA5F3FC), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("SHIFT ACTIVE TIME", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text(shiftActiveTimeStr, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Icon(Icons.Filled.Timer, contentDescription = "Time", tint = LyoColors.VegGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("AVG DELIVERY TIME", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text(if (averageDeliveryTimeMinutes == 0) "--" else "$averageDeliveryTimeMinutes Mins", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "🛰️ AI ROUTE SAFETY & EXTRA PAYOUT RADAR",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x22EA580C))
                                .border(1.dp, Color(0x66EA580C), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalFireDepartment, contentDescription = "fire", tint = Color(0xFFF97316), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("HIGH SURGE HOTSPOT ACTIVE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    Text("Jalakandapuram Road & Sangagiri Main Road zone is witnessing extreme demand. Earn ₹20 extra per trip instantly!", color = Color(0xFFE2E8F0), fontSize = 11.sp, lineHeight = 14.sp)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x22FCD34D))
                                .border(1.dp, Color(0x66FCD34D), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Cloud, contentDescription = "cloud", tint = Color(0xFFFBBF24), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("WEATHER SPEED ADVISORY CRITERIA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    Text("Light drizzle reported. Maximum recommended transition speed set at 40 km/h for rider safety.", color = Color(0xFFE2E8F0), fontSize = 11.sp, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // OTP HANDOFF SECURITY INPUT POP-UP MODAL PANEL
        if (showOtpDialogForRide != null) {
            val currRide = showOtpDialogForRide!!
            AlertDialog(
                onDismissRequest = { showOtpDialogForRide = null },
                containerColor = Color(0xFF1E293B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = "sec", tint = LyoColors.AccentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Secure Receipt Verification", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "To guarantee the parcel reaches the rightful client, please get the 4-digit security code printed on their in-app invoice.",
                            color = LyoColors.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (otpError != null) {
                            Text(
                                text = otpError ?: "",
                                color = LyoColors.NonVegRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { viewModel.otpInputVal.value = it },
                            label = { Text("Enter 4-Digit Handoff Code") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("otp_input")
                        )
                        
                        Text(
                            text = "💡 HANDOFF SECURE GUIDE:\n" +
                                    "Customer's invoice shows this security Code inside the track page. Obtain this code from the customer to finalize the transaction.",
                            color = LyoColors.TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.verifyDeliveryOTP(currRide) {
                                showOtpDialogForRide = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                    ) {
                        Text("VALIDATE SECURITY CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOtpDialogForRide = null }) {
                        Text("BACK", color = Color.White)
                    }
                }
            )
        }
    }

@Composable
fun RiderPerformanceDashboardCard(
    completedTripsCount: Int,
    earningsToday: Double,
    completedRides: List<DeliveryRide>,
    viewModel: DeliveryViewModel,
    onSupportClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, Color(0x33F8FAFC)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color(0xFFFF6B00),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "வருவாய் & ஆர்டர் அறிக்கை",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                    Text(
                        text = "SHIFT ACTIVE",
                        fontSize = 10.sp,
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "மொத்த வருவாய் (Earnings)",
                        fontSize = 9.sp,
                        color = LyoColors.TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${String.format(java.util.Locale.US, "%.2f", earningsToday)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF22C55E)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val base = earningsToday * 0.8
                    val bonus = earningsToday * 0.2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("அடிப்படை கட்டணம்:", fontSize = 10.5.sp, color = LyoColors.TextSecondary)
                        Text("₹${String.format(java.util.Locale.US, "%.1f", base)}", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ஊக்கத்தொகை/டிப்ஸ்:", fontSize = 10.5.sp, color = LyoColors.TextSecondary)
                        Text("₹${String.format(java.util.Locale.US, "%.1f", bonus)}", fontSize = 10.5.sp, color = Color(0xFFFF6B00), fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "வழங்கப்பட்டவை",
                        fontSize = 11.sp,
                        color = LyoColors.TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$completedTripsCount ஆர்டர்கள்",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Deliveries Closed",
                        fontSize = 11.sp,
                        color = LyoColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = "rating", tint = Color(0xFFFDE047), modifier = Modifier.size(12.dp))
                        Text("⭐ 4.90 Rating", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (earningsToday > 0.0) {
                var showCashoutDialog by remember { mutableStateOf(false) }
                var upiInputVal by remember { mutableStateOf("") }
                val isCashoutRequested by viewModel.isCashoutRequested.collectAsState()
                val showCashoutSuccess by viewModel.showCashoutSuccess.collectAsState()

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showCashoutDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AmberYellow),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalance,
                            contentDescription = "bank",
                            tint = LyoColors.DarkCyanBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "வங்கி மாற்றக் கோரிக்கை (Instant Cashout 💸)",
                            color = LyoColors.DarkCyanBg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (showCashoutDialog) {
                    AlertDialog(
                        onDismissRequest = { 
                            showCashoutDialog = false
                            viewModel.showCashoutSuccess.value = false
                        },
                        containerColor = Color(0xFF1E293B),
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AccountBalance, contentDescription = "bank", tint = LyoColors.AccentOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("வங்கிப் பரிமாற்றக் கோரிக்கை", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        text = {
                            Column {
                                Text(
                                    text = "இன்றைய வருவாய் ₹${String.format(java.util.Locale.US, "%.2f", earningsToday)} உங்கள் வங்கி கணக்கு அல்லது UPI ID-க்கு உடனே அனுப்பப்படும்.",
                                    color = LyoColors.TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (showCashoutSuccess) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF22C55E).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🎉 கோரிக்கை ஏற்கப்பட்டது! ₹${String.format(java.util.Locale.US, "%.2f", earningsToday)} உங்கள் UPI கணக்கிற்கு அடுத்த 5 நிமிடங்களில் வரவு வைக்கப்படும்.",
                                            color = Color(0xFF22C55E),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = upiInputVal,
                                        onValueChange = { upiInputVal = it },
                                        label = { Text("UPI ID (GPay / PhonePe / Paytm)") },
                                        placeholder = { Text("e.g. 9876543210@ybl") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color.White, unfocusedLabelColor = LyoColors.TextSecondary),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            if (!showCashoutSuccess) {
                                Button(
                                    onClick = {
                                        if (upiInputVal.trim().isNotEmpty()) {
                                            viewModel.requestCashout(upiInputVal)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen),
                                    enabled = !isCashoutRequested && upiInputVal.trim().isNotEmpty()
                                ) {
                                    if (isCashoutRequested) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text("CONFIRM WITHDRAWAL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { 
                                        showCashoutDialog = false
                                        viewModel.showCashoutSuccess.value = false
                                        upiInputVal = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange)
                                ) {
                                    Text("நன்றி (Done)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        dismissButton = {
                            if (!showCashoutSuccess) {
                                TextButton(onClick = { showCashoutDialog = false }) {
                                    Text("CANCEL", color = Color.White)
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val target = 10
            val progressPercent = (completedTripsCount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "இலக்கு முன்னேற்றம்: $completedTripsCount/$target முடிக்கப்பட்டது",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progressPercent * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = Color(0xFFFF6B00),
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressPercent },
                    color = Color(0xFFFF6B00),
                    trackColor = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (completedTripsCount >= target) {
                        "🎉 வாழ்த்துகள்! இன்றைய போனஸ் இலக்கு ₹50 எட்டப்பட்டது!"
                    } else {
                        "💡 ₹50 கூடுதல் போனஸ் பெற இன்னும் ${target - completedTripsCount} ஆர்டர்கள் தேவை!"
                    },
                    fontSize = 9.sp,
                    color = LyoColors.TextSecondary,
                    lineHeight = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "சமீபத்திய ஆர்டர் அறிக்கை (Completed Ledger)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = LyoColors.TextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (completedRides.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "இன்று இன்னும் எந்த ஆர்டர்களும் முடிக்கப்படவில்லை.\nஆக்டிவ் ஆர்டர்களை முடித்தவுடன் விவரங்கள் இங்கு தோன்றும்.",
                        fontSize = 10.sp,
                        color = LyoColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    completedRides.take(4).forEach { completedRide ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                .padding(10.dp, 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = "done", tint = Color(0xFF22C55E), modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "ஆர்டர் #${completedRide.orderId}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(text = "தூரம்: ${String.format(java.util.Locale.US, "%.1f km", completedRide.totalDistance)}", fontSize = 9.sp, color = LyoColors.TextSecondary)
                                }
                            }
                            Text(
                                text = "₹${completedRide.earnings.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF22C55E)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSupportClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x2238BDF8)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SupportAgent, contentDescription = "telephony", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "விநியோகஸ்தர் உதவிக்கு தொடர்பு கொள்ள (Call Admin/Help 📞)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                }
            }
        }
    }
}

@Composable
fun DeliveryJobCard(
    ride: DeliveryRide,
    viewModel: DeliveryViewModel,
    onShowOtpVerify: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentStep = viewModel.getStepForRide(ride.id, ride.status)
    val isPendingAccept = ride.status == "PENDING_RIDER_ACCEPT"
    var isExpanded by remember(ride.id) { mutableStateOf(false) }

    val orderPairState = produceState<Pair<com.example.data.database.Order?, List<com.example.data.database.OrderItem>>>(initialValue = Pair(null, emptyList()), key1 = ride.orderId) {
        value = viewModel.repository.getOrderWithItems(ride.orderId)
    }
    val order = orderPairState.value.first
    val orderItems = orderPairState.value.second

    val customerState = produceState<com.example.data.database.User?>(initialValue = null, key1 = order?.userId) {
        order?.userId?.let { uId ->
            value = viewModel.repository.userDao.getUserByPhone(uId)
        }
    }
    val customer = customerState.value

    val vendorState = produceState<com.example.data.database.Vendor?>(initialValue = null, key1 = order?.vendorId) {
        order?.vendorId?.let { vId ->
            value = viewModel.repository.vendorDao.getVendorById(vId)
        }
    }
    val vendor = vendorState.value

    fun makePhoneCall(phone: String) {
        if (phone.isNotEmpty()) {
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:$phone")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Cannot make call", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        borderColor = if (isPendingAccept) LyoColors.AccentOrange else Color(0x33F8FAFC)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = "டெலிவரி ஆர்டர் #${ride.id}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (order != null && com.example.data.repository.LyoLiveTestTracker.isTestOrder(order)) {
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
                    Text(
                        text = "Linked Order ID: #LYO-${ride.orderId}",
                        color = LyoColors.TextSecondary,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "உணவகம்/Hub: ${vendor?.name ?: "..."} ➔ வாடிக்கையாளர்: ${customer?.name ?: "..."}",
                        color = LyoColors.TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "நிலை / Status: " + when (currentStep) {
                            "ARRIVING_AT_STORE" -> "கடைக்குச் செல்கிறார் 🛵"
                            "STORE_ARRIVED" -> "கடையை அடைந்தார் 🏪"
                            "COLLECTED" -> "விநியோகத்தில் உள்ளார் 🏍️"
                            "ARRIVED_AT_CUSTOMER" -> "வாடிக்கையாளரிடம் உள்ளார் 🏡"
                            else -> "தயாராகிறது..."
                        },
                        color = LyoColors.AmberYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LyoColors.VegGreen.copy(alpha = 0.2f))
                            .border(1.dp, LyoColors.VegGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Payout: ₹${ride.earnings.toInt()}",
                            color = LyoColors.VegGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isExpanded) {
                Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 6.dp))

            vendor?.let { v ->
                val transition = rememberInfiniteTransition(label = "store_pulse")
                val pulseGlow by transition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.82f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, LyoColors.GlassBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFF8A00),
                                            Color(0xFFE11D48)
                                        )
                                    )
                                )
                                .border(1.5.dp, LyoColors.AmberYellow.copy(alpha = pulseGlow), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "APPROVED CULINARY HUB 🍽️",
                                color = LyoColors.AccentOrange,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = v.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = v.address,
                                color = LyoColors.TextSecondary,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "ஆர்டரைத் துரிதமாகத் தயார் செய்யச் சொல்லுங்கள் 🛵",
                                color = LyoColors.AmberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { makePhoneCall(v.phone) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x1F22C55E))
                                .border(1.dp, Color(0x4D22C55E), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call Restaurant",
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            customer?.let { c ->
                val transition = rememberInfiniteTransition(label = "home_pulse")
                val pulsingColor by transition.animateColor(
                    initialValue = Color(0xFF38BDF8).copy(alpha = 0.15f),
                    targetValue = Color(0xFF38BDF8).copy(alpha = 0.45f),
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = androidx.compose.animation.core.LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_color"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, LyoColors.GlassBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0EA5E9),
                                            Color(0xFF2563EB)
                                        )
                                    )
                                )
                                .border(1.dp, pulsingColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CLIENT'S SECURE DESTINATION 🏡",
                                color = Color(0xFF38BDF8),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = c.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (c.address.isNotEmpty()) c.address else "East Car Street, Idappadi, Salem",
                                color = LyoColors.TextSecondary,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "உங்கள் இல்லம் தேடி! / Delivered safely to home 🏡",
                                color = Color(0xFFA5F3FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { makePhoneCall(c.phone) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x1F38BDF8))
                                .border(1.dp, Color(0x4D38BDF8), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call Customer",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (orderItems.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "ITEMS SUMMARY:",
                            color = LyoColors.TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val summaryText = orderItems.joinToString(", ") { "${it.quantity}x ${it.nameEn}" }
                        Text(
                            text = summaryText,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isPendingAccept) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1AF97316), RoundedCornerShape(8.dp))
                        .border(1.dp, LyoColors.AccentOrange, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("⚡ LOCAL SENSORS DETECTED READY PICKUP!", color = LyoColors.AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Distance: ${ride.totalDistance} km coordinates radius threshold meta logic applied.", color = Color.White, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.riderAcceptAssignment(ride.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ACCEPT ASSIGNMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val scope = rememberCoroutineScope()
                var customMessage by remember { mutableStateOf("") }

                if (false) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Chat, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "வாடிக்கையாளருக்கு செய்தி அனுப்பவும் • CUSTOMER QUICK CHAT",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick suggestion chips with equal weight and min-height to prevent wrapping/overflow
                        val suggestions = listOf("வழியில் வருகிறேன் 🏍️", "5 நிமிடம் ⏳", "கடையில் காத்திருக்கிறேன் 🏪")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            suggestions.forEach { suggestion ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x3338BDF8))
                                        .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable {
                                            scope.launch {
                                                com.example.data.repository.LyoFirebaseHelper.sendOrderMessage(
                                                    orderId = ride.orderId,
                                                    senderId = ride.riderPhone,
                                                    senderRole = "RIDER",
                                                    text = suggestion
                                                )
                                                android.widget.Toast.makeText(context, "செய்தி அனுப்பப்பட்டது", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = suggestion, 
                                        fontSize = 10.sp, 
                                        color = Color(0xFF38BDF8), 
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 12.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                                value = customMessage,
                                onValueChange = { customMessage = it },
                                placeholder = { Text("செய்தி தட்டச்சு செய்யவும்...", fontSize = 11.sp, color = Color.Gray) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0x33FFFFFF),
                                    unfocusedContainerColor = Color(0x11FFFFFF),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    if (customMessage.isNotBlank()) {
                                        val text = customMessage.trim()
                                        customMessage = ""
                                        scope.launch {
                                            com.example.data.repository.LyoFirebaseHelper.sendOrderMessage(
                                                orderId = ride.orderId,
                                                senderId = ride.riderPhone,
                                                senderRole = "RIDER",
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

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 ROUTE MAP",
                            color = Color(0xFFFBBF24),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF10B981))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LIVE ACTIVE GPS",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    val centerLat = vendor?.lat ?: 11.5850
                    val centerLng = vendor?.lng ?: 77.8420
                    val storeLat = vendor?.lat ?: 11.5850
                    val storeLng = vendor?.lng ?: 77.8420
                    val customerLat = order?.customerLat ?: 11.5812
                    val customerLng = order?.customerLng ?: 77.8465

                    LeafletMapView(
                        centerLat = centerLat,
                        centerLng = centerLng,
                        riderLat = ride.currentLat,
                        riderLng = ride.currentLng,
                        storeLat = storeLat,
                        storeLng = storeLng,
                        customerLat = customerLat,
                        customerLng = customerLng,
                        zoom = 16,
                        screenTag = "delivery_screens_map",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .padding(bottom = 8.dp)
                    )

                    // Gorgeous, clean, outside-the-map Compose HUD for Distance & ETA
                    val rLat = ride.currentLat
                    val rLng = ride.currentLng
                    if (rLat != 0.0 && rLng != 0.0) {
                        val distanceToCust = com.example.ui.viewmodels.calculateDistance(rLat, rLng, customerLat, customerLng)
                        val distStoreToCust = if (storeLat != 0.0 && storeLng != 0.0) {
                            com.example.ui.viewmodels.calculateDistance(storeLat, storeLng, customerLat, customerLng)
                        } else {
                            distanceToCust
                        }
                        
                        var progressPercent = 0f
                        if (distStoreToCust > 0.01) {
                            progressPercent = (1.0 - (distanceToCust / distStoreToCust)).toFloat()
                            if (progressPercent < 0f) progressPercent = 0f
                            if (progressPercent > 1f) progressPercent = 1f
                        }
                        
                        // ETA calculation: 1km takes ~2.5 mins
                        val etaMinutes = (distanceToCust * 2.5).toInt().coerceAtLeast(1)
                        // Simple timezone/formatted string compatible with API 24
                        val calendar = java.util.Calendar.getInstance()
                        calendar.add(java.util.Calendar.MINUTE, etaMinutes)
                        val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                        val etaString = formatter.format(calendar.time)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.TwoWheeler,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B00),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "தொலைவு: ${String.format(java.util.Locale.US, "%.1f", distanceToCust)} கி.மீ",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "வருகை: $etaString",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressPercent)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFFF6B00), Color(0xFFFFA500))
                                            )
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = "🏪 Store ➔ 📍 Customer: ${customer?.name ?: "Customer"} (${customer?.address?.ifEmpty { "Idappadi, Salem" } ?: "Idappadi, Salem"})",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Start Google Maps Navigation Button (Orange)
                    Button(
                        onClick = {
                            try {
                                val gmmIntentUri = android.net.Uri.parse("google.navigation:q=$customerLat,$customerLng")
                                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                try {
                                    val mapIntent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$customerLat,$customerLng")
                                    )
                                    context.startActivity(mapIntent)
                                } catch (ex: Exception) {
                                    android.widget.Toast.makeText(context, "No map application found", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_google_navigation_button")
                    ) {
                        Icon(Icons.Filled.Directions, contentDescription = "directions", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🗺️ Start Google Maps Navigation", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Call Customer
                        Button(
                            onClick = {
                                val phone = customer?.phone ?: ""
                                if (phone.isNotEmpty()) {
                                    makePhoneCall(phone)
                                } else {
                                    android.widget.Toast.makeText(context, "No customer phone available", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("call_customer_button")
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "call", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📞 Call Customer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // WhatsApp
                        Button(
                            onClick = {
                                val phone = customer?.phone ?: ""
                                if (phone.isNotEmpty()) {
                                    try {
                                        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "WhatsApp is not installed", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "No customer phone available", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF115E59)),
                            border = BorderStroke(1.dp, Color(0xFF14B8A6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("whatsapp_button")
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = "whatsapp", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("💬 WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Rider Easy Help Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x1F10B981))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "🏍️ RIDER EASY HELP / எளிய உதவிக்குறிப்புகள்",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            val bullets = listOf(
                                "• Deliver within 35 minutes for high feedback stars / விரைவான விநியோகம்.",
                                "• Match route layout coordinates in live active satellite / செயற்கைக்கோள் வழித்தடம்.",
                                "• Call customer immediately upon store arrival / உணவு சேகரித்த பின் அழைக்கவும்.",
                                "• Verify customer identity with dynamic invoice handoff OTP / பாதுகாப்பான OTP சரிபார்ப்பு."
                            )
                            bullets.forEach { bullet ->
                                Text(
                                    text = bullet,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Progress Details
                    Text(
                        text = "🛰️ COORDINATES WAYPOINTS MAP PROGRESS",
                        color = LyoColors.AccentOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = "loc",
                                tint = LyoColors.VegGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(45.dp)
                                    .background(if (currentStep != "ARRIVING_AT_STORE") LyoColors.VegGreen else Color.Gray)
                            )
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = "store",
                                tint = if (currentStep != "ARRIVING_AT_STORE") LyoColors.VegGreen else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(45.dp)
                                    .background(if (currentStep == "ARRIVED_AT_CUSTOMER") LyoColors.VegGreen else Color.Gray)
                            )
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "home",
                                tint = if (currentStep == "ARRIVED_AT_CUSTOMER") LyoColors.VegGreen else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("RIDER GPS CURRENT POSITION", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("Rider LatLng (${String.format(java.util.Locale.US, "%.5f", ride.currentLat)}, ${String.format(java.util.Locale.US, "%.5f", ride.currentLng)})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("RESTAURANT PICKUP GEOFENCE", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(vendor?.name ?: "Kitchen Outlets", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(vendor?.address ?: "Lyo AI Partner Merchant", color = LyoColors.TextSecondary, fontSize = 10.sp)

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("CUSTOMER DOORSTEP HANDOFF", color = LyoColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(customer?.name ?: "Customer Destination", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(order?.customerLat?.let { cLat -> "LatLng ($cLat, ${order.customerLng})" } ?: "Secure Handoff Zone", color = LyoColors.TextSecondary, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "தற்போதைய நிலை / Status: " + when (currentStep) {
                                "ARRIVING_AT_STORE" -> "கடைக்குச் செல்கிறார் 🛵 (Heading to kitchen depot)"
                                "STORE_ARRIVED" -> "கடையை அடைந்தார்! உணவைச் சேகரிக்கவும் 🏪 (Arrived at store - pick up order)"
                                "COLLECTED" -> "விநியோகத்தில் உள்ளார் 🏍️ (Out for delivery to customer)"
                                "ARRIVED_AT_CUSTOMER" -> "வாடிக்கையாளரிடம் உள்ளார் 🏡 (Reached doorstep - handoff)"
                                else -> "தயாராகிறது... (Dispatch loading)"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentStep != "ARRIVED_AT_CUSTOMER") {
                            Button(
                                onClick = {
                                    viewModel.performSimulatedTransit(ride)
                                    if (currentStep == "STORE_ARRIVED") {
                                        viewModel.startRealGpsTracking(context, ride)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Text(
                                    text = when (currentStep) {
                                        "ARRIVING_AT_STORE" -> "கடைக்கு வந்தாச்சு • ARRIVED AT STORE"
                                        "STORE_ARRIVED" -> "உணவு சேகரிக்கப்பட்டது • ITEMS COLLECTED"
                                        "COLLECTED" -> "வாடிக்கையாளரிடம் வந்தாச்சு • ARRIVED AT CUSTOMER"
                                        else -> "நிலை தொடரவும் • PROGRESS ROUTE"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = onShowOtpVerify,
                                colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Icon(Icons.Filled.VpnKey, contentDescription = "otp", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("பாதுகாப்பான OTP சரிபார்ப்பு • SECURE OTP VERIFY", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

