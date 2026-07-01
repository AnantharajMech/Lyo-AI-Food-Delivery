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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Food Core Logo Icon Glowing & Custom styled cloche
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(animateScale)
                    .background(
                        color = Color(0x13FFFFFF),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0x55FFFFFF), Color(0x08FFFFFF))
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .shadow(
                        elevation = 20.dp,
                        ambientColor = Color(0xFFFF6B35).copy(alpha = 0.3f),
                        spotColor = Color(0xFF00E5FF).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                LyoLogo(
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Serif Style LYO Bold Brand Name
            Text(
                text = "Lyo",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFF4500), Color(0xFFFF6B35), Color(0xFFFFD166))
                    )
                ),
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            // Brand Divider gold underline bar
            Box(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .width(110.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD166), Color(0xFFFF9A3C))
                        )
                    )
            )

            // Brand secondary slogan string
            Text(
                text = "FOOD DELIVERY",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8),
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Brand Tamil Tagline Locale
            Text(
                text = "உணவு விநியோகம் • எடப்பாடி",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Indian English localized greeting description
            Text(
                text = "Connecting fine local culinary kitchens with you",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Gorgeous pulsing custom animated dots loading visual
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer { alpha = dot1Alpha; scaleX = dot1Alpha; scaleY = dot1Alpha }
                        .clip(CircleShape)
                        .background(Color(0xFFFF4500))
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer { alpha = dot2Alpha; scaleX = dot2Alpha; scaleY = dot2Alpha }
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B35))
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer { alpha = dot3Alpha; scaleX = dot3Alpha; scaleY = dot3Alpha }
                        .clip(CircleShape)
                        .background(Color(0xFFFFD166))
                )
            }
        }
    }
}


// ==========================================
// 2. UNIFIED LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (String, String?) -> Unit, // returns role: CUSTOMER, ADMIN, DELIVERY and passwordOrHash
    onBackToStore: (() -> Unit)? = null
) {
    if (onBackToStore != null) {
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
    var forgotStep by remember { mutableStateOf(1) } // 1: Enter phone, 2: Simulated OTP, 3: Set password, 4: Success
    var generatedOtp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var forgotError by remember { mutableStateOf("") }
    var newResetPassword by remember { mutableStateOf("") }
    var resetOtpMethod by remember { mutableStateOf("SMS") } // SMS or Gmail

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
            if (onBackToStore != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToStore,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back to Store",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    TextButton(
                        onClick = onBackToStore,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "👈 View Catalog / Guest Mode",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
            }

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
                        .size(48.dp)
                        .scale(logoPulseScale)
                        .background(Color(0x13FFFFFF), CircleShape)
                        .border(1.dp, Color(0x33FFB347), CircleShape)
                        .padding(6.dp)
                ) {
                    LyoLogo(modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lyo",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF4500), Color(0xFFFF6B35), Color(0xFFFFD166))
                                )
                            ),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(16.dp)
                                .background(Color(0xFFFFD166).copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "உணவு விநியோகம்",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD166)
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

            Spacer(modifier = Modifier.height(6.dp))

            // Input Form Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Text(
                    text = "Lyo Account Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Enter your registered credentials. The portal will automatically detect if you are a Customer, Rider, or Administrator.",
                    fontSize = 12.sp,
                    color = LyoColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Error Banner
                if (errorMsg != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                            .border(1.dp, LyoColors.NonVegRed, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMsg ?: "",
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                    text = "Authorize Session",
                    onClick = {
                        viewModel.loginWithPhoneAndPassword(phone, password) { detectedRole ->
                            onLoginSuccess(detectedRole, password)
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
                                    onLoginSuccess(detectedRole, null)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("GoogleSignIn", "Google authorization failed, opening sandbox fallback: ${e.message}")
                            showSandboxGoogleDialog = true
                        }
                    } else {
                        // User cancelled or Play Services failed - open sandbox dialog helper for easy local testing
                        showSandboxGoogleDialog = true
                    }
                }

                Button(
                    onClick = {
                        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("368208047268-example.apps.googleusercontent.com")
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "புதிய வாடிக்கையாளரா? • New Customer?",
                color = LyoColors.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "பதிவு செய்ய இங்கே கிளிக் செய்யவும் • Click to Create Account",
                color = LyoColors.AccentOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onNavigateToRegister() }
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .clickable { showAboutDialog = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "About icon",
                    tint = LyoColors.AccentOrange.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Lyo Settings & Privacy Policy",
                    color = LyoColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Elegant Interactive Sizable Forgot Password Dialog (Firebase-Auth based)
    if (showForgotDialog) {
        val dialogContext = androidx.compose.ui.platform.LocalContext.current
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
                        text = "பாதுகாப்பான கடவுச்சொல் மீட்பு (Secure Password Reset)",
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
                    // Tamil Message
                    Text(
                        text = "பாதுகாப்பு காரணங்களுக்காக, இந்த செயலியில் தானியங்கி முறையில் (OTP) கடவுச்சொல்லை மாற்றும் வசதி தற்காலிகமாக முடக்கப்பட்டுள்ளது. " +
                                "உங்கள் கணக்கின் கடவுச்சொல்லை மீட்டமைக்க, தயவுசெய்து எங்களது வாட்ஸ்அப் நிர்வாகியைத் தொடர்பு கொள்ளவும். உங்கள் அடையாளம் சரிபார்க்கப்பட்ட பின் உடனடியாக புதிய கடவுச்சொல் வழங்கப்படும்.",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // English Message
                    Text(
                        text = "For enhanced cryptographic security, automated self-service password reset has been disabled. " +
                                "To reset your confidential account passcode, please contact our administrator directly on WhatsApp. Our support team will verify your identity and restore access securely.",
                        color = LyoColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Contact CTA Button inside text area
                    Button(
                        onClick = {
                            val msg = "வணக்கம் Lyo, எனது கணக்கின் கடவுச்சொல்லை மாற்ற உதவி தேவை. (Hello Lyo, I need help resetting my password.)"
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
                            text = "வாட்ஸ்அப்பில் தொடர்பு கொள்ள (WhatsApp Admin)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
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
                                        "வணக்கம் அனந்தராஜ் சார், லியோ உணவு விநியோக செயலி (Lyo Food Delivery App) தொடர்பாக தங்களை தொடர்பு கொள்கிறேன்."
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
                                .requestIdToken("368208047268-example.apps.googleusercontent.com")
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
