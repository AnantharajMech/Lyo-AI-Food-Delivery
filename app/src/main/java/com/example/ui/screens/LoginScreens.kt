package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.AuthViewModel
import com.example.data.database.User
import androidx.compose.ui.res.painterResource
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// 1. SPLASH SCREEN (LOADING TRANSITION)
// ==========================================
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LyoBackground {
        var animateScale by remember { mutableStateOf(0.7f) }
        var animateProgress by remember { mutableStateOf(0.0f) }

        LaunchedEffect(Unit) {
            animateScale = 1.0f
            while (animateProgress < 1.0f) {
                delay(30)
                animateProgress += 0.02f
            }
            delay(300)
            onSplashFinished()
        }

        // 3D/Glass brand pulsing variables
        val dotTransition = rememberInfiniteTransition(label = "dot_pulse")
        val dot1Alpha by dotTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = 0, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot1"
        )
        val dot2Alpha by dotTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot2"
        )
        val dot3Alpha by dotTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot3"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Central Planet/Orb glowing sphere matching the mockup
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(animateScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF0F1B35),
                                    Color(0xFF060B16)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF).copy(alpha = 0.4f))
                            ),
                            shape = CircleShape
                        )
                        .shadow(
                            elevation = 30.dp,
                            ambientColor = Color(0xFF00E5FF).copy(alpha = 0.4f),
                            spotColor = Color(0xFF7C4DFF).copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Planet texture glow aura
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Lyo AI",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // FOOD DELIVERY Subtitle
                Text(
                    text = "FOOD DELIVERY",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Brand Tamil Tagline Locale
                Text(
                    text = "உணவு விநியோகம் • எடப்பாடி",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Brand secondary slogan string
                Text(
                    text = "Connecting fine local\nculinary kitchens with you",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Horizontal loading progress bar matching the image (cyan line)
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animateProgress)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                                )
                            )
                    )
                }
            }

            // Version info at the bottom
            Text(
                text = "Version 2.5.0",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}


// ==========================================
// 2. UNIFIED LOGIN SCREEN
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (String, String?) -> Unit, // returns role: CUSTOMER, ADMIN, DELIVERY and passwordOrHash
    onNavigateToAdminLogin: () -> Unit,
    onNavigateToDeliveryLogin: () -> Unit,
    onBackToStore: (() -> Unit)? = null,
    initialMode: String = "CUSTOMER"
) {
    var loginMode by remember { mutableStateOf(initialMode) }

    if (loginMode != "CUSTOMER") {
        androidx.activity.compose.BackHandler {
            loginMode = "CUSTOMER"
            viewModel.clearError()
        }
    } else if (onBackToStore != null) {
        androidx.activity.compose.BackHandler {
            onBackToStore()
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_logo")
    val logoPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val errorMsg by viewModel.loginError.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()

    // Forgot Password Simulation States (Tamil localized)
    var showForgotDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSandboxGoogleDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var forgotPhone by remember { mutableStateOf("") }
    var forgotStep by remember { mutableStateOf(1) }
    var generatedOtp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var forgotError by remember { mutableStateOf("") }
    var newResetPassword by remember { mutableStateOf("") }
    var resetOtpMethod by remember { mutableStateOf("SMS") }

    val mappedError = remember(errorMsg) {
        if (errorMsg != null) {
            val err = errorMsg!!.lowercase()
            if (err.contains("incorrect") || err.contains("wrong") || err.contains("invalid") || err.contains("failed") || err.contains("no user") || err.contains("not found") || err.contains("password")) {
                "Incorrect password. Please try again."
            } else if (err.contains("deactivated")) {
                "Sorry, your delivery partner account has been deactivated by the administrator."
            } else {
                errorMsg
            }
        } else {
            null
        }
    }

    LyoBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Compact Brand Logo & Heading Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0x13FFFFFF), CircleShape)
                        .border(1.dp, Color(0x3300E5FF), CircleShape)
                        .padding(6.dp)
                ) {
                    LyoLogo(modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lyo AI",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                                )
                            ),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(16.dp)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "உணவு விநியோகம்",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    }
                    Text(
                        text = "FOOD DELIVERY • EDAPPADI SELECTION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Form Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                if (loginMode == "CUSTOMER") {
                    Text(
                        text = "Lyo Account Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Enter your registered credentials to access your account.",
                        fontSize = 12.sp,
                        color = LyoColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Error Banner
                    if (mappedError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                                .border(1.dp, LyoColors.NonVegRed, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = mappedError ?: "",
                                fontSize = 13.sp,
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Phone/Username/Email ID
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Username / Email / Mobile") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "username", tint = LyoColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("username_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Masked Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "pass", tint = LyoColors.TextSecondary) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, contentDescription = "Toggle password", tint = LyoColors.TextSecondary)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Forgot Password Button / கடவுச்சொல்லை மறந்துவிட்டீர்களா?
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = LyoColors.AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .testTag("forgot_password_button")
                                .clickable {
                                    forgotPhone = phone
                                    forgotStep = 1
                                    forgotError = ""
                                    generatedOtp = ""
                                    enteredOtp = ""
                                    showForgotDialog = true
                                }
                                .padding(vertical = 4.dp)
                        )
                    }

                    if (loginMode != "CUSTOMER") {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Remember Me
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { viewModel.setRememberMe(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LyoColors.AccentOrange,
                                    uncheckedColor = LyoColors.TextSecondary
                                )
                            )
                            Text(
                                text = "Keep me authorized on this terminal",
                                color = LyoColors.TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Login Button
                    LyoButton(
                        text = "Authorize Session",
                        onClick = {
                            viewModel.loginWithPhoneAndPassword(phone, password) { detectedRole ->
                                if (detectedRole == "ADMIN") {
                                    viewModel.setLoginError("Access Denied: Admin accounts cannot log in through this portal. Please use the Admin Console login.")
                                } else if (detectedRole == "DELIVERY" || detectedRole == "RIDER") {
                                    viewModel.setLoginError("Access Denied: Delivery Partner accounts cannot log in through this portal. Please use the Delivery Partner Login.")
                                } else {
                                    onLoginSuccess(detectedRole, password)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("submit_button")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real Authenticating Google Provider Button
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val googleSignInLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result: ActivityResult ->
                        val intent = result.data
                        if (result.resultCode == android.app.Activity.RESULT_OK && intent != null) {
                            try {
                                val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
                                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                                account?.idToken?.let { token ->
                                    viewModel.loginWithGoogle(token) { detectedRole ->
                                        if (detectedRole == "ADMIN") {
                                            viewModel.setLoginError("Access Denied: Admin accounts cannot log in through this portal.")
                                        } else if (detectedRole == "DELIVERY" || detectedRole == "RIDER") {
                                            viewModel.setLoginError("Access Denied: Delivery Partner accounts cannot log in through this portal.")
                                        } else {
                                            onLoginSuccess(detectedRole, null)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                showSandboxGoogleDialog = true
                            }
                        } else {
                            showSandboxGoogleDialog = true
                        }
                    }

                    Button(
                        onClick = {
                            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken("604469873807-example.apps.googleusercontent.com")
                                .requestEmail()
                                .build()
                            val mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(mGoogleSignInClient.signInIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("google_login_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "google icon",
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                color = Color(0xFF1F1F1F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (loginMode == "ADMIN") {
                    val admins by viewModel.allAdmins.collectAsState(initial = emptyList())
                    val activeAdmins = remember(admins) {
                        admins.filter { it.role == "ADMIN" }.distinctBy { it.phone }
                    }
                    val finalAdmins = remember(activeAdmins) {
                        val baseList = mutableListOf<User>()
                        baseList.add(User("Anantharajmech", "Super Admin", "AnantharajEinstein@gmail.com", "Lyo Salem HQ, Salem Road, Idappadi", 11.5812, 77.8465, false, "ADMIN"))
                        baseList.add(User("8778148899", "Anantharaj R (CEO)", "AnantharajEinstein@gmail.com", "Lyo Salem HQ, Salem Road, Idappadi", 11.5812, 77.8465, false, "ADMIN"))
                        for (admin in activeAdmins) {
                            if (admin.phone != "Anantharajmech" && admin.phone != "8778148899" && admin.phone != "AnanthEinstein") {
                                baseList.add(admin)
                            }
                        }
                        baseList.distinctBy { it.phone }
                    }
                    var selectedAdmin by remember { mutableStateOf<User?>(null) }
                    var isAdminExpanded by remember { mutableStateOf(false) }

                    Text(
                        text = "🛡️ Lyo Admin Console Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LyoColors.AccentOrange,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Please select your Administrator profile below to authenticate into the Control Tower.",
                        fontSize = 12.sp,
                        color = LyoColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Error Banner
                    if (mappedError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                                .border(1.dp, LyoColors.NonVegRed, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Incorrect password. Please try again.",
                                fontSize = 13.sp,
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Dropdown for Admin selection
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAdminExpanded = !isAdminExpanded }
                        ) {
                            OutlinedTextField(
                                value = if (selectedAdmin != null) selectedAdmin!!.name else "",
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Select Admin") },
                                placeholder = { Text("Click to select Admin") },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = LyoColors.TextSecondary) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (isAdminExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Toggle Dropdown",
                                        tint = LyoColors.TextSecondary
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.White,
                                    disabledLabelColor = LyoColors.TextSecondary,
                                    disabledBorderColor = Color(0x33F8FAFC),
                                    disabledLeadingIconColor = LyoColors.TextSecondary,
                                    disabledTrailingIconColor = LyoColors.TextSecondary
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("admin_select_dropdown")
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { isAdminExpanded = !isAdminExpanded }
                            )
                        }

                        DropdownMenu(
                            expanded = isAdminExpanded,
                            onDismissRequest = { isAdminExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(LyoColors.DarkCyanBg)
                                .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(8.dp))
                        ) {
                            finalAdmins.forEach { admin ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(admin.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Authorized Admin", color = LyoColors.TextSecondary, fontSize = 12.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedAdmin = admin
                                        isAdminExpanded = false
                                        viewModel.clearError()
                                    }
                                )
                            }
                        }
                    }

                    if (selectedAdmin != null) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Admin Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "pass", tint = LyoColors.TextSecondary) },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(image, contentDescription = "Toggle password", tint = LyoColors.TextSecondary)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LyoColors.AccentOrange,
                                unfocusedBorderColor = Color(0x33F8FAFC)
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("admin_password_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Remember Me
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { viewModel.setRememberMe(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LyoColors.AccentOrange,
                                    uncheckedColor = LyoColors.TextSecondary
                                )
                            )
                            Text(
                                text = "Keep me authorized on this terminal",
                                color = LyoColors.TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Primary Admin Login Button
                        LyoButton(
                            text = "Authenticate Admin Console",
                            onClick = {
                                val loginInput = if (selectedAdmin != null && selectedAdmin!!.email.isNotBlank() && selectedAdmin!!.email.contains("@")) selectedAdmin!!.email else selectedAdmin!!.phone
                                viewModel.loginWithPhoneAndPassword(loginInput, password) { detectedRole ->
                                    if (detectedRole == "ADMIN") {
                                        onLoginSuccess("ADMIN", password)
                                    } else {
                                        viewModel.logout()
                                        viewModel.setLoginError("Access Denied: Your account does not have Admin privileges.")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("admin_submit_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Small Back Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "← Customer Login",
                            color = LyoColors.AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .testTag("back_to_customer_login")
                                .clickable {
                                    loginMode = "CUSTOMER"
                                    viewModel.clearError()
                                    phone = ""
                                    password = ""
                                    selectedAdmin = null
                                }
                                .padding(8.dp)
                        )
                    }
                } else if (loginMode == "DELIVERY") {
                    val riders by viewModel.allRiders.collectAsState(initial = emptyList())
                    val activeRiders = remember(riders) {
                        riders.filter { (it.role == "DELIVERY" || it.role == "RIDER") && it.isActiveRider && !it.phone.startsWith("999991") && it.phone != "9000000002" && it.phone != "9000000003" }.distinctBy { it.phone }
                    }
                    var selectedRider by remember { mutableStateOf<User?>(null) }
                    var searchText by remember { mutableStateOf("") }
                    var isExpanded by remember { mutableStateOf(false) }

                    val searchFiltered = remember(activeRiders, searchText) {
                        activeRiders.filter {
                            it.name.contains(searchText, ignoreCase = true) || it.phone.contains(searchText)
                        }
                    }

                    Text(
                        text = "🏍️ Lyo Delivery Partner Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LyoColors.AccentOrange,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Please select your name from the active delivery partners list below, enter your password, and log in.",
                        fontSize = 12.sp,
                        color = LyoColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Error Banner
                    if (mappedError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                                .border(1.dp, LyoColors.NonVegRed, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Incorrect password. Please try again.",
                                fontSize = 13.sp,
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Dropdown/Search selection field
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (selectedRider != null) selectedRider!!.name else searchText,
                            onValueChange = {
                                searchText = it
                                selectedRider = null
                                isExpanded = true
                            },
                            label = { Text("Select Delivery Partner") },
                            placeholder = { Text("Type name to search") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = LyoColors.TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { isExpanded = !isExpanded }) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Toggle Dropdown",
                                        tint = LyoColors.TextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LyoColors.AccentOrange,
                                unfocusedBorderColor = Color(0x33F8FAFC)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("rider_select_dropdown")
                        )

                        DropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(LyoColors.DarkCyanBg)
                                .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(8.dp))
                        ) {
                            if (searchFiltered.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No active delivery partners found", color = Color.Gray, fontSize = 13.sp) },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                searchFiltered.forEach { rider ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(rider.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        },
                                        onClick = {
                                            selectedRider = rider
                                            searchText = rider.name
                                            isExpanded = false
                                            viewModel.clearError()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedRider != null) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Masked Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "pass", tint = LyoColors.TextSecondary) },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(image, contentDescription = "Toggle password", tint = LyoColors.TextSecondary)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LyoColors.AccentOrange,
                                unfocusedBorderColor = Color(0x33F8FAFC)
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("rider_password_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Remember Me
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { viewModel.setRememberMe(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LyoColors.AccentOrange,
                                    uncheckedColor = LyoColors.TextSecondary
                                )
                            )
                            Text(
                                text = "Keep me authorized on this terminal",
                                color = LyoColors.TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Primary Rider Login Button
                        LyoButton(
                            text = "Authenticate Rider Session",
                            onClick = {
                                viewModel.loginWithPhoneAndPassword(selectedRider!!.phone, password) { detectedRole ->
                                    if (detectedRole == "DELIVERY" || detectedRole == "RIDER") {
                                        onLoginSuccess(detectedRole, password)
                                    } else {
                                        viewModel.logout()
                                        viewModel.setLoginError("Access Denied: You do not have Rider privileges.")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("rider_submit_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Small Back Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "← Customer Login",
                            color = LyoColors.AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .testTag("back_to_customer_login")
                                .clickable {
                                    loginMode = "CUSTOMER"
                                    viewModel.clearError()
                                    phone = ""
                                    password = ""
                                    selectedRider = null
                                    searchText = ""
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }

            if (loginMode == "CUSTOMER") {
                Spacer(modifier = Modifier.height(16.dp))

                // Premium CTA box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LyoColors.CardSlate // subtle dark-blue surface
                    ),
                    border = BorderStroke(1.dp, LyoColors.AccentOrange), // thin orange border
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New to Lyo AI?",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onNavigateToRegister() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LyoColors.AccentOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("register_link")
                        ) {
                            Text(
                                text = "Register Now",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                onNavigateToAdminLogin()
                            }
                            .padding(8.dp)
                            .testTag("admin_portal_link"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👑 Admin Login",
                            color = LyoColors.AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clickable {
                                onNavigateToDeliveryLogin()
                            }
                            .padding(8.dp)
                            .testTag("delivery_portal_link"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏍️ Delivery Partner Login",
                            color = LyoColors.AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Elegant Interactive Sizable Forgot Password Dialog (Firebase-Auth based)
    if (showForgotDialog) {
        val dialogContext = androidx.compose.ui.platform.LocalContext.current
        var forgotEmail by remember { mutableStateOf("") }
        var forgotStatusMessage by remember { mutableStateOf("") }
        var isResetSending by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = LyoColors.NonVegRed
                    )
                    Text(
                        text = if (loginMode == "CUSTOMER") "கடவுச்சொல் மீட்பு (Password Reset)" else "பாதுகாப்பான கடவுச்சொல் மீட்பு (Secure Password Reset)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (loginMode == "CUSTOMER") {
                        // Tamil instruction for Customer
                        Text(
                            text = "உங்கள் கணக்கில் பதிவு செய்யப்பட்ட மின்னஞ்சல் முகவரியை (Email Address) கீழே உள்ளிடவும். உங்கள் ஜிமெயில் முகவரிக்கு கடவுச்சொல்லை மீட்டமைப்பதற்கான இணைப்பு உடனடியாக அனுப்பப்படும்.",
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // English instruction
                        Text(
                            text = "Please enter your registered email address below. A password reset link will be sent to your Gmail inbox instantly.",
                            color = LyoColors.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Email Text Field
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { 
                                forgotEmail = it
                                forgotError = ""
                                forgotStatusMessage = ""
                            },
                            label = { Text("மின்னஞ்சல் முகவரி (Email Address)", color = Color.Gray) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B),
                                focusedLabelColor = LyoColors.AccentOrange,
                                unfocusedLabelColor = Color.Gray,
                                focusedIndicatorColor = LyoColors.AccentOrange,
                                unfocusedIndicatorColor = Color.DarkGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_email_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (isResetSending) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(color = LyoColors.AccentOrange)
                            }
                        }

                        if (forgotStatusMessage.isNotEmpty()) {
                            Text(
                                text = forgotStatusMessage,
                                color = LyoColors.VegGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (forgotError.isNotEmpty()) {
                            Text(
                                text = forgotError,
                                color = LyoColors.NonVegRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                val emailVal = forgotEmail.trim()
                                if (emailVal.isEmpty()) {
                                    forgotError = "தயவுசெய்து மின்னஞ்சல் முகவரியை உள்ளிடவும். (Please enter your email.)"
                                    return@Button
                                }
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()) {
                                    forgotError = "தவறான மின்னஞ்சல் வடிவம். (Invalid email format.)"
                                    return@Button
                                }

                                isResetSending = true
                                forgotError = ""
                                forgotStatusMessage = ""
                                viewModel.sendFirebasePasswordReset(emailVal) { success, errMsg ->
                                    isResetSending = false
                                    if (success) {
                                        forgotStatusMessage = "வெற்றி! கடவுச்சொல் மீட்பு இணைப்பு உங்கள் மின்னஞ்சலுக்கு அனுப்பப்பட்டுள்ளது. (Success! Reset link has been sent to your email.)"
                                    } else {
                                        forgotError = errMsg ?: "மின்னஞ்சல் அனுப்புவதில் தோல்வி. (Failed to send reset email.)"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_send_email_button"),
                            enabled = !isResetSending
                        ) {
                            Text(
                                text = "மின்னஞ்சல் அனுப்பவும் (Send Reset Link)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Divider or WhatsApp option for Customer as fallback
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.DarkGray)
                            Text(
                                text = " அல்லது (OR) ",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.DarkGray)
                        }

                        // Fallback Contact CTA Button
                        Button(
                            onClick = {
                                val msg = "வணக்கம் Lyo, எனது கஸ்டமர் கணக்கின் கடவுச்சொல்லை மாற்ற உதவி தேவை. (Hello Lyo, I need help resetting my customer password.)"
                                com.example.WhatsAppHelper.sendMessage(dialogContext, "8778148899", msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)), // Whatsapp green
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_whatsapp_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "WhatsApp",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "வாட்ஸ்அப்பில் தொடர்பு கொள்ள (WhatsApp Support)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Admin / Rider mode: Automated reset is disabled, contact WhatsApp
                        Text(
                            text = "பாதுகாப்பு காரணங்களுக்காக, இந்த செயலியில் அட்மின் / விநியோகஸ்தர் கணக்குகளுக்கான தானியங்கி கடவுச்சொல் மீட்டமைப்பு மின்னஞ்சல் வசதி முடக்கப்பட்டுள்ளது. " +
                                    "உங்கள் கடவுச்சொல்லை மீட்டமைக்க, தயவுசெய்து எங்களது வாட்ஸ்அப் நிர்வாகியை நேரடியாகத் தொடர்பு கொள்ளவும். உங்கள் அடையாளம் சரிபார்க்கப்பட்ட பின் உடனடியாக புதிய கடவுச்சொல் வழங்கப்படும்.",
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "For security reasons, automated password reset email is disabled for Admin / Delivery roles. " +
                                    "To reset your account password, please contact our administrator directly on WhatsApp. Our support team will verify your identity and restore access.",
                            color = LyoColors.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                val msg = "வணக்கம் Lyo, எனது அட்மின்/விநியோகஸ்தர் கணக்கின் கடவுச்சொல்லை மாற்ற உதவி தேவை. (Hello Lyo, I need help resetting my admin/rider password.)"
                                com.example.WhatsAppHelper.sendMessage(dialogContext, "8778148899", msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)), // Whatsapp green
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_whatsapp_admin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "WhatsApp",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "வாட்ஸ்அப்பில் தொடர்பு கொள்ள (WhatsApp Admin)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showForgotDialog = false },
                    modifier = Modifier.testTag("forgot_cancel_button")
                ) {
                    Text("மூடு (Close)", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp)
        )
    }
    
    if (showSandboxGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showSandboxGoogleDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = LyoColors.AccentOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Google Sandbox", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "We detected that Google Services are inactive on this sandbox environment. To verify and execute the Google Sign-In pipeline immediately, proceed with a secure Simulated Google Sandbox Account.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSandboxGoogleDialog = false
                        viewModel.loginWithGoogle("mock_sandbox_token_123456") { role ->
                            onLoginSuccess(role, null)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                ) {
                    Text("FAST LOG IN WITH GOOGLE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSandboxGoogleDialog = false }) {
                    Text("BACK", color = Color.White)
                }
            }
        )
    }

    if (showAboutDialog) {
        val aboutContext = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OrbitalGlowLogo()
                    
                    Text(
                        text = "COSC∞M",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "CREATIVE TECH SOLUTIONS",
                        color = Color(0xFF06B6D4), // Bright cyan
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Anantharaj.R",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "Lead Architect & Software Engineer",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PinDrop,
                            contentDescription = "location",
                            tint = Color(0xFFEF4444), // Red
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Idappadi, Salem, Tamil Nadu, 637101.", color = Color.White, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mail,
                            contentDescription = "email",
                            tint = Color(0x99FFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AnantharajEinstein@gmail.com", color = Color.White, fontSize = 13.sp, maxLines = 1)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = "link",
                            tint = Color(0x99FFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Instagram: idappadi_creators",
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    com.example.WhatsAppHelper.sendMessage(
                                        aboutContext,
                                        "8778148899",
                                        "வணக்கம் அனந்தராஜ் சார், லியோ ஏ ஐ செயலி (Lyo AI App) தொடர்பாக தங்களை தொடர்பு கொள்கிறேன்."
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Send, contentDescription = "whatsapp", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Button(
                            onClick = {
                                try {
                                    val dialIntent = android.content.Intent(
                                        android.content.Intent.ACTION_DIAL,
                                        android.net.Uri.parse("tel:8778148899")
                                    )
                                    aboutContext.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Phone, contentDescription = "call", tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Text(
                        text = "Privacy Policy",
                        color = Color(0xFFEC4899), // Pink text
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clickable {
                                showPrivacyDialog = true
                            }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), // Purple-Indigo
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Privacy Policy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { showPrivacyDialog = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "close", tint = Color.White)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Lyo AI App Privacy Policy\n\n" +
                                    "1. Data Collection:\n" +
                                    "• We collect your name, phone number, and email to securely set up your personal account.\n" +
                                    "• To offer high precision, we secure geo-fence locations utilizing high-fidelity GPS pin drops.\n\n" +
                                    "2. Location Mapping & Safety:\n" +
                                    "• Users can pin-point delivery locations directly on the integrated map inside the system for extreme accuracy.\n\n" +
                                    "3. WhatsApp Opt-In:\n" +
                                    "• Opting in grants real-time shipping notifications delivered straight to your registered device via WhatsApp.\n\n" +
                                    "4. Storage & Integrity:\n" +
                                    "• Your records are hosted in secure cloud firestore databases. Customers hold full control and deleting profiles completely scrubs any related trails instantly.",
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun AdminLoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String, String?) -> Unit, // returns role "ADMIN" and password
    onBackToCustomerLogin: () -> Unit
) {
    androidx.activity.compose.BackHandler {
        onBackToCustomerLogin()
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    val admins by viewModel.allAdmins.collectAsState(initial = emptyList())
    val activeAdmins = remember(admins) {
        admins.filter { it.role == "ADMIN" }.distinctBy { it.phone }
    }
    val finalAdmins = remember(activeAdmins) {
        val baseList = mutableListOf<User>()
        baseList.add(User("Anantharajmech", "Super Admin", "AnantharajEinstein@gmail.com", "Lyo Salem HQ, Salem Road, Idappadi", 11.5812, 77.8465, false, "ADMIN"))
        baseList.add(User("8778148899", "Anantharaj R (CEO)", "AnantharajEinstein@gmail.com", "Lyo Salem HQ, Salem Road, Idappadi", 11.5812, 77.8465, false, "ADMIN"))
        for (admin in activeAdmins) {
            if (admin.phone != "Anantharajmech" && admin.phone != "8778148899" && admin.phone != "AnanthEinstein") {
                baseList.add(admin)
            }
        }
        baseList.add(User("custom_admin_manual_input", "Other Admin (Manually Enter ID) 👤", "", "", 11.5812, 77.8465, false, "ADMIN"))
        baseList.distinctBy { it.phone }
    }
    var selectedAdmin by remember { mutableStateOf<User?>(null) }
    var isAdminExpanded by remember { mutableStateOf(false) }
    var customAdminInput by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val errorMsg by viewModel.loginError.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()

    val mappedError = remember(errorMsg) {
        errorMsg
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result: androidx.activity.result.ActivityResult ->
        val intent = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && intent != null) {
            try {
                val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                account?.idToken?.let { token ->
                    viewModel.loginWithGoogle(token) { detectedRole ->
                        if (detectedRole == "ADMIN") {
                            onLoginSuccess("ADMIN", null)
                        } else {
                            viewModel.logout()
                            viewModel.setLoginError("Access Denied: Your account does not have Admin privileges.")
                        }
                    }
                }
            } catch (e: Exception) {
                viewModel.setLoginError("Google Sign-In failed: ${e.message}")
            }
        } else {
            viewModel.setLoginError("Google Sign-In was cancelled or failed.")
        }
    }

    LyoBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToCustomerLogin,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Icon
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = "Admin Console",
                tint = LyoColors.AccentOrange,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Input Form Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Text(
                    text = "🛡️ Lyo Admin Console Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LyoColors.AccentOrange,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Please select your Administrator profile below to authenticate into the Control Tower. Standard customer and rider accounts cannot log in here.",
                    fontSize = 12.sp,
                    color = LyoColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Error Banner
                if (mappedError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                            .border(1.dp, LyoColors.NonVegRed, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = mappedError,
                            fontSize = 13.sp,
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Dropdown for Admin selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAdminExpanded = !isAdminExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (selectedAdmin != null) selectedAdmin!!.name else "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Select Admin") },
                            placeholder = { Text("Click to select Admin") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = LyoColors.TextSecondary) },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (isAdminExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Toggle Dropdown",
                                    tint = LyoColors.TextSecondary
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.White,
                                disabledLabelColor = LyoColors.TextSecondary,
                                disabledBorderColor = Color(0x33F8FAFC),
                                disabledLeadingIconColor = LyoColors.TextSecondary,
                                disabledTrailingIconColor = LyoColors.TextSecondary
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_select_dropdown")
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { isAdminExpanded = !isAdminExpanded }
                        )
                    }

                    DropdownMenu(
                        expanded = isAdminExpanded,
                        onDismissRequest = { isAdminExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(LyoColors.DarkCyanBg)
                            .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(8.dp))
                    ) {
                        finalAdmins.forEach { admin ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(admin.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Authorized Admin", color = LyoColors.TextSecondary, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    selectedAdmin = admin
                                    isAdminExpanded = false
                                    viewModel.clearError()
                                }
                            )
                        }
                    }
                }

                if (selectedAdmin != null) {
                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedAdmin!!.phone == "custom_admin_manual_input") {
                        OutlinedTextField(
                            value = customAdminInput,
                            onValueChange = { customAdminInput = it },
                            label = { Text("Admin Email or Phone Number") },
                            placeholder = { Text("Enter authorized Admin Email or Phone") },
                            leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = LyoColors.TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LyoColors.AccentOrange,
                                unfocusedBorderColor = Color(0x33F8FAFC)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_custom_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Admin Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "pass", tint = LyoColors.TextSecondary) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, contentDescription = "Toggle password", tint = LyoColors.TextSecondary)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("admin_password_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Remember Me
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { viewModel.setRememberMe(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = LyoColors.AccentOrange,
                                uncheckedColor = LyoColors.TextSecondary
                            )
                        )
                        Text(
                            text = "Keep me authorized on this terminal",
                            color = LyoColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Login Button
                    LyoButton(
                        text = "Authenticate Admin Console",
                        onClick = {
                            val loginInput = if (selectedAdmin!!.phone == "custom_admin_manual_input") {
                                customAdminInput.trim()
                            } else if (selectedAdmin!!.email.isNotBlank() && selectedAdmin!!.email.contains("@")) {
                                selectedAdmin!!.email
                            } else {
                                selectedAdmin!!.phone
                            }
                            
                            if (loginInput.isBlank()) {
                                viewModel.setLoginError("Please enter your Admin Email or Phone Number.")
                            } else {
                                viewModel.loginWithPhoneAndPassword(loginInput, password) { detectedRole ->
                                    if (detectedRole == "ADMIN") {
                                        onLoginSuccess("ADMIN", password)
                                    } else {
                                        viewModel.logout()
                                        viewModel.setLoginError("Access Denied: Your account does not have Admin privileges.")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("admin_submit_button")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "— OR —",
                        color = LyoColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken("604469873807-example.apps.googleusercontent.com")
                                .requestEmail()
                                .build()
                            val mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(mGoogleSignInClient.signInIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("admin_google_login_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "google icon",
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Admin Sign in with Google",
                                color = Color(0xFF1F1F1F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onBackToCustomerLogin()
                        }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Customer Login",
                        tint = LyoColors.AccentOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Switch to Customer / Rider Login",
                        color = LyoColors.AccentOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ==========================================
// 3. ACCOUNT CREATION & ADDRESS ENGINE
// ==========================================
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: (String?) -> Unit
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    val regName by viewModel.regName.collectAsState()
    val regPhone by viewModel.regPhone.collectAsState()
    val regEmail by viewModel.regEmail.collectAsState()
    val regAddress by viewModel.regAddress.collectAsState()
    val regLat by viewModel.regLat.collectAsState()
    val regLng by viewModel.regLng.collectAsState()
    val regWhatsAppOptIn by viewModel.regWhatsAppOptIn.collectAsState()
    val regRole by viewModel.regRole.collectAsState()
    val errorMsg by viewModel.loginError.collectAsState()
    val hasUserSetLocation by viewModel.hasUserSetLocation.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.regRole.value = "CUSTOMER"
    }

    var showMapPicker by remember { mutableStateOf(false) }
    var showSandboxGoogleRegDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var latInputStr by remember(regLat) { mutableStateOf(regLat.toString()) }
    var lngInputStr by remember(regLng) { mutableStateOf(regLng.toString()) }

    // Salem / Idappadi junctions for Map Picker
    val salemJunctions = listOf(
        Triple("Idappadi Bus Stand Ring Road", 11.5812, 77.8465),
        Triple("Nedungulam Junction", 11.5724, 77.8285),
        Triple("Vellarithi Street Market", 11.5848, 77.8412),
        Triple("Konganapuram Bypass Corner", 11.5950, 77.8620),
        Triple("Chinnappampatti Main Road", 11.6025, 77.8710)
    )

    LyoBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Customer Registration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {

                Text(
                    text = "Enter Personal Details",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateBack() }
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Login redirect",
                        tint = LyoColors.AccentOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Already registered? Click here to Log In",
                        color = LyoColors.AccentOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFFFCA5A5),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Full Name
                OutlinedTextField(
                    value = regName,
                    onValueChange = { viewModel.regName.value = it },
                    label = { Text("Full Name (Primary Name)") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "person", tint = LyoColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange,
                        unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone
                OutlinedTextField(
                    value = regPhone,
                    onValueChange = { viewModel.regPhone.value = it },
                    label = { Text("10-Digit Mobile Number") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "phone", tint = LyoColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange,
                        unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { viewModel.regEmail.value = it },
                    label = { Text("Email ID (Optional)") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "email", tint = LyoColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange,
                        unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (regRole == "DELIVERY") {
                    val regVehicleNo by viewModel.regVehicleNo.collectAsState()
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = regVehicleNo,
                        onValueChange = { viewModel.regVehicleNo.value = it },
                        label = { Text("Vehicle Registration No (e.g. TN-30-X-1234)") },
                        leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = "vehicle", tint = LyoColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reg_vehicle_input")
                    )
                }

                val regPassword by viewModel.regPassword.collectAsState()
                val regConfirmPassword by viewModel.regConfirmPassword.collectAsState()
                var passwordVisible by remember { mutableStateOf(false) }
                var confirmPasswordVisible by remember { mutableStateOf(false) }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = regPassword,
                    onValueChange = { viewModel.regPassword.value = it },
                    label = { Text("Create Login Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "pass", tint = LyoColors.TextSecondary) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, contentDescription = "Toggle password", tint = LyoColors.TextSecondary)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange,
                        unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = regConfirmPassword,
                    onValueChange = { viewModel.regConfirmPassword.value = it },
                    label = { Text("Confirm Login Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "confirm_pass", tint = LyoColors.TextSecondary) },
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(image, contentDescription = "Toggle confirm password", tint = LyoColors.TextSecondary)
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LyoColors.AccentOrange,
                        unfocusedBorderColor = Color(0x33F8FAFC)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // DUAL ENGINE ADDRESS SELECTOR
                Text(
                    text = "Delivery Geo-Fence Location",
                    fontSize = 15.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Method A: GPS geocoding
                    val scope = rememberCoroutineScope()
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            scope.launch {
                                val result = fetchCurrentLocationAndReverseGeocode(context)
                                if (result != null) {
                                    viewModel.setManualCoordinates(result.third, result.first, result.second)
                                    android.widget.Toast.makeText(context, "📍 GPS வெற்றிகரமாக பெறப்பட்டது!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.triggerAutoGPS()
                                    android.widget.Toast.makeText(context, "📍 GPS லொகேஷன் பெறப்பட்டது! (இடப்பாடி)", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)), // Sky 700
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(45.dp)
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = "gps", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Method B: Manual Picker Map overlay
                    Button(
                        onClick = { showMapPicker = !showMapPicker },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LyoColors.GlassBorder),
                        modifier = Modifier.weight(1f).height(45.dp)
                    ) {
                        Icon(Icons.Filled.Map, contentDescription = "map", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Map Pin Drop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Interactive Map picker Panel
                AnimatedVisibility(visible = showMapPicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(Color(0xE60F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🗺️ SELECT DELIVER ZONE (மேப் தேர்வு)",
                            color = LyoColors.AccentOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "🛰️ HIGH-RESOLUTION SATELLITE HYBRID GPS MAP (தொட்டு மேப்பை நகர்த்திப் பின Drop செய்யவும்)",
                            color = LyoColors.AmberYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        InteractiveMapPickerView(
                            initialLat = regLat,
                            initialLng = regLng,
                            onLocationPicked = { lat, lng ->
                                coroutineScope.launch {
                                    val finalAddr = resolveHighFidelityReverseGeocoding(context, lat, lng)
                                    viewModel.setManualCoordinates(finalAddr, lat, lng)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        salemJunctions.forEach { (addressName, lat, lng) ->
                            val isSelected = regLat == lat && regLng == lng
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0x33F97316) else Color.Transparent)
                                    .clickable {
                                        viewModel.setManualCoordinates(addressName, lat, lng)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.PinDrop else Icons.Filled.LocationOn,
                                    contentDescription = "location",
                                    tint = if (isSelected) LyoColors.AccentOrange else LyoColors.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(addressName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Coord: $lat, $lng", color = LyoColors.TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fully Editable Delivery Address box
                OutlinedTextField(
                    value = regAddress,
                    onValueChange = { viewModel.regAddress.value = it },
                    readOnly = false,
                    label = { Text("Secured Delivery Address") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = LyoColors.AccentOrange,
                        focusedBorderColor = LyoColors.AccentOrange
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Editable Coordinates (Latitude and Longitude)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latInputStr,
                        onValueChange = {
                            latInputStr = it
                            it.toDoubleOrNull()?.let { d -> viewModel.regLat.value = d }
                        },
                        label = { Text("Latitude") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = lngInputStr,
                        onValueChange = {
                            lngInputStr = it
                            it.toDoubleOrNull()?.let { d -> viewModel.regLng.value = d }
                        },
                        label = { Text("Longitude") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // WhatsApp opt-in checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = regWhatsAppOptIn,
                        onCheckedChange = { viewModel.regWhatsAppOptIn.value = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LyoColors.AccentOrange,
                            uncheckedColor = LyoColors.TextSecondary
                        )
                    )
                    Icon(
                        imageVector = Icons.Filled.Textsms,
                        contentDescription = "whatsapp",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Opt-in to real-time WhatsApp updates",
                        color = LyoColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Apply registration
                LyoButton(
                    text = "Create Account & Start Ordering",
                    onClick = {
                        if (!hasUserSetLocation) {
                            android.widget.Toast.makeText(
                                context,
                                "தயவுசெய்து உங்கள் வீட்டு இருப்பிடத்தை Map-ல் select பண்ணவும் அல்லது GPS Auto-detect பயன்படுத்தவும் — இது இல்லாமல் டெலிவரி நபர் உங்கள் வீட்டைக் கண்டுபிடிக்க முடியாது!",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            viewModel.createAccount {
                                onRegistrationSuccess(viewModel.regPassword.value)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("submit_button")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                    Text(
                        text = "  OR  ",
                        color = LyoColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real Registering Google Provider Button
                val googleSignInRegLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                ) { result: androidx.activity.result.ActivityResult ->
                    val intent = result.data
                    if (result.resultCode == android.app.Activity.RESULT_OK && intent != null) {
                        try {
                            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
                            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                            account?.idToken?.let { token ->
                                if (!hasUserSetLocation) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "தயவுசெய்து உங்கள் வீட்டு இருப்பிடத்தை Map-ல் select பண்ணவும் அல்லது GPS Auto-detect பயன்படுத்தவும் — இது இல்லாமல் டெலிவரி நபர் உங்கள் வீட்டைக் கண்டுபிடிக்க முடியாது!",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    viewModel.loginWithGoogle(
                                        idToken = token,
                                        overrideRole = regRole,
                                        vehicleNo = if (regRole == "DELIVERY") viewModel.regVehicleNo.value.trim() else "",
                                        address = regAddress,
                                        lat = regLat,
                                        lng = regLng
                                    ) { detectedRole ->
                                        onRegistrationSuccess(null)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("GoogleSignInReg", "Google registration failed, opening sandbox fallback: ${e.message}")
                            showSandboxGoogleRegDialog = true
                        }
                    } else {
                        showSandboxGoogleRegDialog = true
                    }
                }

                Button(
                    onClick = {
                        if (!hasUserSetLocation) {
                            android.widget.Toast.makeText(
                                context,
                                "தயவுசெய்து உங்கள் வீட்டு இருப்பிடத்தை Map-ல் select பண்ணவும் அல்லது GPS Auto-detect பயன்படுத்தவும் — இது இல்லாமல் டெலிவரி நபர் உங்கள் வீட்டைக் கண்டுபிடிக்க முடியாது!",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken("604469873807-example.apps.googleusercontent.com")
                                .requestEmail()
                                .build()
                            val mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                            googleSignInRegLauncher.launch(mGoogleSignInClient.signInIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                    modifier = Modifier.fillMaxWidth().height(45.dp).testTag("google_register_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "google icon",
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Register seamlessly with Google",
                            color = Color(0xFF1F1F1F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "பதிவு செய்வதன் மூலம் எங்கள் Privacy Policy மற்றும் Terms of Service ஐ ஏற்கிறீர்கள்.",
                    color = LyoColors.TextSecondary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (showSandboxGoogleRegDialog) {
        AlertDialog(
            onDismissRequest = { showSandboxGoogleRegDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = LyoColors.AccentOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Google Sandbox Register", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "We detected that Google Services are inactive on this sandbox environment. Proceed with a secure Simulated Google Sandbox Account to register immediately.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!hasUserSetLocation) {
                            android.widget.Toast.makeText(
                                context,
                                "தயவுசெய்து உங்கள் வீட்டு இருப்பிடத்தை Map-ல் select பண்ணவும் அல்லது GPS Auto-detect பயன்படுத்தவும் — இது இல்லாமல் டெலிவரி நபர் உங்கள் வீட்டைக் கண்டுபிடிக்க முடியாது!",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            showSandboxGoogleRegDialog = false
                        } else {
                            showSandboxGoogleRegDialog = false
                            viewModel.loginWithGoogle(
                                idToken = "mock_sandbox_token_123456",
                                overrideRole = regRole,
                                vehicleNo = if (regRole == "DELIVERY") viewModel.regVehicleNo.value.trim() else "",
                                address = regAddress,
                                lat = regLat,
                                lng = regLng
                            ) { role ->
                                onRegistrationSuccess(null)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.VegGreen)
                ) {
                    Text("FAST REGISTER WITH GOOGLE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSandboxGoogleRegDialog = false }) {
                    Text("BACK", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun OrbitalGlowLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbital_glow")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing/rotating neon ring
        Canvas(modifier = Modifier.size(90.dp)) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFEC4899), // Pink
                        Color(0xFF8B5CF6), // Purple
                        Color(0xFF3B82F6), // Blue
                        Color(0xFF06B6D4), // Cyan
                        Color(0xFFEC4899)  // Pink loop
                    )
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.dp.toPx())
            )

            // Draw glowing orbiting dot on the ring orbit
            val radius = 90.dp.toPx() / 2
            val radians = Math.toRadians(angle.toDouble())
            val x = (center.x + radius * Math.cos(radians)).toFloat()
            val y = (center.y + radius * Math.sin(radians)).toFloat()

            // Outer blur glow circle
            drawCircle(
                color = Color(0xFF22D3EE),
                radius = 10.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y),
                alpha = 0.6f
            )
            // Inner core dot circle
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

@Composable
fun DeliveryPartnerLoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String, String?) -> Unit,
    onBackToPortal: () -> Unit
) {
    androidx.activity.compose.BackHandler {
        onBackToPortal()
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    val riders by viewModel.allRiders.collectAsState(initial = emptyList())
    val activeRiders = remember(riders) {
        riders.filter { (it.role == "DELIVERY" || it.role == "RIDER") && it.isActiveRider && !it.phone.startsWith("999991") && it.phone != "9000000002" && it.phone != "9000000003" }.distinctBy { it.phone }
    }

    var selectedRider by remember { mutableStateOf<User?>(null) }
    var searchText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val errorMsg by viewModel.loginError.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()

    val searchFiltered = remember(activeRiders, searchText) {
        activeRiders.filter {
            it.name.contains(searchText, ignoreCase = true) || it.phone.contains(searchText)
        }
    }

    val mappedError = remember(errorMsg) {
        if (errorMsg != null) {
            if (errorMsg!!.contains("deactivated", ignoreCase = true)) {
                "Sorry, your delivery partner account has been deactivated by the administrator."
            } else {
                "Incorrect password. Please try again."
            }
        } else {
            null
        }
    }

    LyoBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToPortal,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rider Icon
            Icon(
                imageVector = Icons.Filled.TwoWheeler,
                contentDescription = "Delivery Partner",
                tint = LyoColors.AccentOrange,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Text(
                    text = "🏍️ Lyo Delivery Partner Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LyoColors.AccentOrange,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Please select your name from the active delivery partners list below, enter your password, and log in to access your Delivery Dashboard.",
                    fontSize = 12.sp,
                    color = LyoColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Error Banner
                if (mappedError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                            .border(1.dp, LyoColors.NonVegRed, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = mappedError,
                            fontSize = 13.sp,
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Dropdown/Search selection field
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedRider != null) selectedRider!!.name else searchText,
                        onValueChange = {
                            searchText = it
                            selectedRider = null
                            isExpanded = true
                        },
                        label = { Text("Select Delivery Partner") },
                        placeholder = { Text("Type name to search") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = LyoColors.TextSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { isExpanded = !isExpanded }) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Toggle Dropdown",
                                    tint = LyoColors.TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("rider_select_dropdown")
                    )

                    DropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(LyoColors.DarkCyanBg)
                            .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(8.dp))
                    ) {
                        if (searchFiltered.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No active delivery partners found", color = Color.Gray, fontSize = 13.sp) },
                                onClick = {},
                                enabled = false
                            )
                        } else {
                            searchFiltered.forEach { rider ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(rider.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedRider = rider
                                        searchText = rider.name
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedRider != null) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Masked Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "pass", tint = LyoColors.TextSecondary) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, contentDescription = "Toggle password", tint = LyoColors.TextSecondary)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LyoColors.AccentOrange,
                            unfocusedBorderColor = Color(0x33F8FAFC)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("rider_password_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Remember Me
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { viewModel.setRememberMe(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = LyoColors.AccentOrange,
                                uncheckedColor = LyoColors.TextSecondary
                            )
                        )
                        Text(
                            text = "Keep me authorized on this terminal",
                            color = LyoColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Login Button
                    LyoButton(
                        text = "Authenticate Rider Session",
                        onClick = {
                            viewModel.loginWithPhoneAndPassword(selectedRider!!.phone, password) { detectedRole ->
                                if (detectedRole == "DELIVERY" || detectedRole == "RIDER") {
                                    onLoginSuccess(detectedRole, password)
                                } else {
                                    viewModel.logout()
                                    viewModel.setLoginError("Access Denied: You do not have Rider privileges.")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("rider_submit_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onBackToPortal()
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Switch Portal",
                    tint = LyoColors.AccentOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Switch Login Portal",
                    color = LyoColors.AccentOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OfflineAuthorizationPendingScreen(
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    isRetrying: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LyoColors.DarkCyanBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            OrbitalGlowLogo()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "இணைப்பு துண்டிக்கப்பட்டது • Verification Pending",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "We could not verify your session with our servers. Your authorization is pending a valid network connection. Please check your internet connection and retry.\n\n(உங்கள் கணக்கு விவரங்களை சர்வரில் இருந்து சரிபார்க்க முடியவில்லை. இணைய இணைப்பைச் சரிபார்த்து மீண்டும் முயற்சிக்கவும்.)",
                color = LyoColors.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isRetrying) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = LyoColors.AccentOrange,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                androidx.compose.material3.Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = LyoColors.AccentOrange),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                        .testTag("retry_offline_auth_button")
                ) {
                    Text(
                        text = "மீண்டும் முயல்க (Retry Verification)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                androidx.compose.material3.TextButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("logout_offline_auth_button")
                ) {
                    Text(
                        text = "வெளியேறவும் (Logout)",
                        color = Color.Red.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

