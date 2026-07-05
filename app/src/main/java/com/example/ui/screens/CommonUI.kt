package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset

// Central Premium Theme Colors (Section 1)
object LyoColors {
    val DarkCyanBg = Color(0xFF0B1120) // Deep Navy Background (#0B1120)
    val CardSlate = Color(0xFF131B2E) // Deep Premium Slate Card Surface
    val AccentOrange = Color(0xFFFF7622) // Vibrant high-contrast Premium Coral/Orange Accent (#FFFF7622)
    val AmberYellow = Color(0xFF00D9FF) // Electric Cyan Accent (#00D9FF)
    val GlassBorder = Color(0x3300D9FF) // Translucent Electric Cyan border stroke
    val TranslucentSlate = Color(0x1F131B2E) // Light glass translucent overlay
    val TranslucentBlack = Color(0x99000000)
    val TextPrimary = Color(0xFFFFFFFF) // Crisp White for maximum dark contrast
    val TextSecondary = Color(0xFFCBD5E1) // Brilliant Platinum/Slate muted text for perfect dark contrast
    val VegGreen = Color(0xFF10B981) // Modern Emerald Veg Green
    val NonVegRed = Color(0xFFEF4444) // Premium Coral Red
    val WarningYellow = Color(0xFFF59E0B) // Warning Amber Gold
    val LiveCyan = Color(0xFF00D9FF) // Electric Cyan Accent
}

// ============================================
// LIQUID GLASS DESIGN TOKENS
// ============================================
object LyoGlassDesignTokens {
    val SurfaceAlpha = 0.25f
    val HighlightAlpha = 0.60f
    
    // Core background colors with different transparency levels to replicate glass
    val GlassCardBg = Color(0xCC131B2E) // Rich translucent deep slate card background
    val GlassCardBgGlow = Color(0xDD1E293B) // Rich glowing card variant
    
    // Specular / Neon Gradient colors for the 3D Liquid border
    val EdgeColorOrange = Color(0xFF1E3A8A)
    val EdgeColorCyan = Color(0xFF00D9FF)
    val EdgeColorGold = Color(0xFF00D9FF)
    
    // Gradient definitions for borders
    val LiquidOrangeCyanBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00D9FF).copy(alpha = 0.6f), // Electric Cyan top-left
            Color(0xFF1E3A8A).copy(alpha = 0.4f), // Deep Indigo bottom
            Color(0xFF00D9FF).copy(alpha = 0.1f)  // Fading bottom-right
        )
    )

    val LiquidGoldOrangeBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00D9FF).copy(alpha = 0.7f), // Electric Cyan
            Color(0xFF1E3A8A).copy(alpha = 0.4f), // Deep Indigo
            Color(0x11FFFFFF)
        )
    )
    
    val Standard3DElevation = 3.dp
    val Active3DElevation = 5.dp
    
    @Composable
    fun Modifier.liquidGlass3D(
        cornerRadius: Dp = 20.dp,
        elevation: Dp = Standard3DElevation,
        borderBrush: Brush = LiquidOrangeCyanBorder,
        borderWidth: Dp = 1.2.dp,
        glowColor: Color = Color(0xFF00D9FF),
        backgroundColor: Color = GlassCardBg
    ): Modifier {
        return this
            .graphicsLayer {
                shape = RoundedCornerShape(cornerRadius)
                clip = false
            }
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = glowColor.copy(alpha = 0.15f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.85f),
                        backgroundColor.copy(alpha = 0.65f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(borderWidth, borderBrush, RoundedCornerShape(cornerRadius))
    }
}

@Composable
fun LyoBackground(
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lyo_bg_anim")
    
    // Pulsating factors
    val pulseOrange by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_orange"
    )
    
    val pulseCyan by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.26f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_cyan"
    )

    val pulseMagenta by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_magenta"
    )

    val pulseGreen by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_green"
    )

    // Spatial movement offsets for orbs
    val offsetOrangeX by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset_orange_x"
    )

    val offsetCyanY by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = 140f,
        animationSpec = infiniteRepeatable(
            animation = tween(8500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset_cyan_y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1120), // Ultimate Deep Navy background
                        Color(0xFF0B1120),
                        Color(0xFF1E3A8A)  // Futuristic Deep Indigo base glow
                    )
                )
            )
    ) {
        // 1. Futuristic Indigo Soft Aura (Top-Right)
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = offsetOrangeX.dp, y = (-70).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A).copy(alpha = pulseOrange * 0.35f),
                            Color(0xFF00D9FF).copy(alpha = pulseOrange * 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 2. High-Trust Cyan Intelligent Aura (Mid-Left)
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-90).dp, y = offsetCyanY.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00D9FF).copy(alpha = pulseCyan * 0.28f),
                            Color(0xFF1E3A8A).copy(alpha = pulseCyan * 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. Tech-Minded Indigo Aura (Bottom-Right)
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A).copy(alpha = pulseMagenta * 0.25f),
                            Color(0xFF00D9FF).copy(alpha = pulseMagenta * 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 4. Subtle Minimal Cyan Aura (Bottom-Left)
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 140.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00D9FF).copy(alpha = pulseGreen * 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Subtly lit central overlay texture for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x08FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.2.dp,
    borderColor: Color = LyoColors.GlassBorder,
    backgroundColor: Color = LyoGlassDesignTokens.GlassCardBg,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    innerPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val finalBorderColor = glowColor ?: borderColor
    val finalBorderWidth = if (glowColor != null) 1.5.dp else borderWidth
    val finalElevation = if (glowColor != null) LyoGlassDesignTokens.Active3DElevation else LyoGlassDesignTokens.Standard3DElevation
    val finalSpotColor = glowColor ?: LyoColors.GlassBorder

    // Tap/Press scale micro-interaction
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_press"
    )

    // Dynamic inner glass body with rich vertical gradient (Apple multi-layer refraction approximation)
    val glassBackgroundBrush = remember(backgroundColor) {
        Brush.verticalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.95f), // High opacity to prevent text and image bleed
                backgroundColor.copy(alpha = 0.88f),
                backgroundColor.copy(alpha = 0.92f)
            )
        )
    }

    // Specular border gradient (high-light at top-left edge, translucent glow at bottom-right edge)
    val specularBorderBrush = remember(finalBorderColor) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.45f), // highlight specular notch (Refraction edge)
                finalBorderColor.copy(alpha = 0.35f),
                finalBorderColor.copy(alpha = 0.08f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    // Specular highlight "shine" streak animation (Part 4)
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_shine")
    val shineProgress by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine_progress"
    )

    var baseModifier = modifier
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            shape = RoundedCornerShape(cornerRadius),
            clip = false
        )
        .shadow(
            elevation = finalElevation,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = Color.Black.copy(alpha = 0.3f),
            spotColor = finalSpotColor.copy(alpha = 0.3f)
        )
        .clip(RoundedCornerShape(cornerRadius))

    if (onClick != null) {
        baseModifier = baseModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }

    val cardModifier = baseModifier
        .background(glassBackgroundBrush)
        .border(finalBorderWidth, specularBorderBrush, RoundedCornerShape(cornerRadius))
        .drawWithContent {
            // Render the items nested in the card first
            drawContent()

            // Draw premium specular shine sweep that maps the "Liquid Glass" refraction
            val width = size.width
            val height = size.height
            val lineOffset = width * shineProgress

            val shineBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.005f),
                    Color.White.copy(alpha = 0.020f),
                    Color.White.copy(alpha = 0.040f), // Extremely subtle specular peak intensity to avoid hiding texts/graphics
                    Color.White.copy(alpha = 0.020f),
                    Color.White.copy(alpha = 0.005f),
                    Color.Transparent
                ),
                start = Offset(lineOffset - width * 0.4f, 0f),
                end = Offset(lineOffset + width * 0.4f, height)
            )

            drawRect(
                brush = shineBrush,
                blendMode = BlendMode.Screen
            )
        }
        .padding(innerPadding)

    Column(
        modifier = cardModifier,
        content = content
    )
}

@Composable
fun LyoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = LyoColors.AccentOrange,
        contentColor = Color.White,
        disabledContainerColor = Color(0x33F97316),
        disabledContentColor = LyoColors.TextSecondary
    )
) {
    val baseColor = if (enabled) LyoColors.AccentOrange else Color(0x22F97316)
    val contentColor = if (enabled) Color.White else LyoColors.TextSecondary
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btn_press_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 10.dp,
        animationSpec = tween(120),
        label = "btn_elevation"
    )

    // Shine animation sweep
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_btn_shine")
    val shineProgress by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "btn_shine_progress"
    )

    val shape = RoundedCornerShape(14.dp)
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation.toPx()
                clip = false
            }
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = baseColor.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.95f),
                        baseColor.copy(alpha = 0.75f),
                        baseColor.copy(alpha = 0.90f)
                    )
                )
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else Modifier
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        baseColor.copy(alpha = 0.15f)
                    )
                ),
                shape = shape
            )
            .drawWithContent {
                drawContent()
                
                // Oval glass refraction highlight at the top of the button
                drawOval(
                    color = Color.White.copy(alpha = 0.12f),
                    topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.9f, size.height * 0.35f)
                )

                // 3D specular shine sweep
                val width = size.width
                val height = size.height
                val lineOffset = width * shineProgress
                
                val shineBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.04f),
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    start = Offset(lineOffset - width * 0.2f, 0f),
                    end = Offset(lineOffset + width * 0.2f, height)
                )
                
                drawRect(
                    brush = shineBrush,
                    blendMode = BlendMode.Screen
                )
            }
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Rating",
            tint = LyoColors.AmberYellow,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format("%.1f", rating),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = LyoColors.TextPrimary
        )
    }
}

@Composable
fun VegIndicator(isVeg: Boolean, modifier: Modifier = Modifier) {
    val mainColor = if (isVeg) Color(0xFF10B981) else Color(0xFFF43F5E) // Premium Emerald vs Bright Rose/Red
    Box(
        modifier = modifier
            .size(13.dp)
            .background(
                Brush.linearGradient(
                    colors = if (isVeg) {
                        listOf(Color(0x2610B981), Color(0x0A10B981))
                    } else {
                        listOf(Color(0x26EF4444), Color(0x0AEF4444))
                    }
                ),
                RoundedCornerShape(3.dp)
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        mainColor.copy(alpha = 0.9f),
                        mainColor.copy(alpha = 0.3f)
                    )
                ),
                RoundedCornerShape(3.dp)
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White, mainColor),
                        radius = 8f
                    )
                )
        )
    }
}

@Composable
fun LyoLogo(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.lyo_logo),
        contentDescription = "Lyo Brand Logo",
        modifier = modifier
    )
}

@Composable
fun LyoLogoDeprecated(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val scale = width / 100f // 100x100 reference coordinate grid
            val centerPt = center

            // 1. Hot Gastronomic Ambient Glow (Fire Red to Golden Honey)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x55FF4500),  // Fire Red heat focus
                        Color(0x2BFFD166),  // Royal Gold culinary aura
                        Color.Transparent
                    ),
                    center = centerPt,
                    radius = width * 0.75f
                ),
                radius = width * 0.75f
            )

            // 2. High-Tech Mathematical & Geometrical Alignment Rings
            val outerRingStroke = Stroke(width = 1.2.dp.toPx())
            val dashRingStroke = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f)
            )

            // Outer perfect golden Ratio frame
            drawCircle(
                color = Color(0x60FFD166),
                radius = width * 0.44f,
                style = outerRingStroke
            )

            // Dynamic dashed speed track ring (lightning-fast transport)
            drawCircle(
                color = Color(0x6000E5FF), // Living Cyber Cyan
                radius = width * 0.38f,
                style = dashRingStroke
            )

            // 3. Precise Navigation/Telemetry Crosshair Indicators (North, South, East, West ticks)
            val crosshairBrush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF6B35)))
            // Top Indicator
            drawLine(
                brush = crosshairBrush,
                start = Offset(width * 0.5f, height * 0.04f),
                end = Offset(width * 0.5f, height * 0.09f),
                strokeWidth = 2.dp.toPx()
            )
            // Bottom Indicator
            drawLine(
                brush = crosshairBrush,
                start = Offset(width * 0.5f, height * 0.91f),
                end = Offset(width * 0.5f, height * 0.96f),
                strokeWidth = 2.dp.toPx()
            )
            // Left Indicator
            drawLine(
                brush = crosshairBrush,
                start = Offset(width * 0.04f, height * 0.5f),
                end = Offset(width * 0.09f, height * 0.5f),
                strokeWidth = 2.dp.toPx()
            )
            // Right Indicator
            drawLine(
                brush = crosshairBrush,
                start = Offset(width * 0.91f, height * 0.5f),
                end = Offset(width * 0.96f, height * 0.5f),
                strokeWidth = 2.dp.toPx()
            )

            // 4. Elegant Heat-Waves & Flavor Evaporation Trails
            val steam1 = Path().apply {
                moveTo(38f * scale, 24f * scale)
                cubicTo(35f * scale, 17f * scale, 41f * scale, 14f * scale, 39f * scale, 7f * scale)
            }
            val steam2 = Path().apply {
                moveTo(50f * scale, 20f * scale)
                cubicTo(47f * scale, 13f * scale, 53f * scale, 10f * scale, 51f * scale, 3f * scale)
            }
            val steam3 = Path().apply {
                moveTo(62f * scale, 24f * scale)
                cubicTo(59f * scale, 17f * scale, 65f * scale, 14f * scale, 63f * scale, 7f * scale)
            }

            val steamBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFD166).copy(alpha = 0.9f), Color(0xFFFF4500).copy(alpha = 0.05f))
            )
            val steamStroke = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawPath(steam1, brush = steamBrush, style = steamStroke)
            drawPath(steam2, brush = steamBrush, style = steamStroke)
            drawPath(steam3, brush = steamBrush, style = steamStroke)

            // 5. The Geometrically Perfect Gourmet Cloche (Upgraded to Radiant Fire Gradient Cover)
            val clochePath = Path().apply {
                // Cloche Dome Arc left side
                moveTo(50f * scale, 29f * scale)
                cubicTo(
                    30f * scale, 29f * scale,
                    24f * scale, 45f * scale,
                    24f * scale, 62f * scale
                )
                // Dome Base Rim left flare
                lineTo(18f * scale, 62f * scale)
                // Left rim thickness
                quadraticTo(18f * scale, 66f * scale, 23f * scale, 66f * scale)
                // Tray base line
                lineTo(77f * scale, 66f * scale)
                // Right rim thickness
                quadraticTo(82f * scale, 66f * scale, 82f * scale, 62f * scale)
                // Right base rim flare
                lineTo(76f * scale, 62f * scale)
                // Sweep right dome
                cubicTo(
                    76f * scale, 45f * scale,
                    70f * scale, 29f * scale,
                    50f * scale, 29f * scale
                )
            }

            // Draw Cloche Outer Shiny Rim and Dome - 3D Fire Orange Ribbon Gradient
            drawPath(
                path = clochePath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD166), Color(0xFFFF6B35), Color(0xFFFF4500)),
                    start = Offset(width * 0.25f, height * 0.25f),
                    end = Offset(width * 0.75f, height * 0.75f)
                ),
                style = Stroke(width = 3.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )

            // Precise Dome Glass highlight reflection
            drawArc(
                color = Color.White.copy(alpha = 0.5f),
                startAngle = 145f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(27f * scale, 31f * scale),
                size = androidx.compose.ui.geometry.Size(46f * scale, 46f * scale),
                style = Stroke(width = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Cloche Platter base plate inner light
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color(0x10FFFFFF), Color(0xCCFFFFFF), Color(0x10FFFFFF))
                ),
                start = Offset(24f * scale, 61f * scale),
                end = Offset(76f * scale, 61f * scale),
                strokeWidth = 1.2.dp.toPx()
            )

            // Top Cloche Sphere Handle (Mathematical Sphere representing nucleus & culinary garnish)
            drawCircle(
                color = Color(0xFFFFD166),
                radius = 4.2f * scale,
                center = Offset(50f * scale, 26f * scale)
            )
            drawCircle(
                color = Color.White,
                radius = 1.8f * scale,
                center = Offset(48.5f * scale, 24.5f * scale)
            )

            // 6. The Scientifically Aligned "L" Signature Inside the Dome
            val lCorePath = Path().apply {
                // Top node of L starting around upper center-left
                moveTo(43f * scale, 38f * scale)
                // Downward vertical spine
                lineTo(43f * scale, 52f * scale)
                // Smooth aesthetic curve
                quadraticTo(
                    43f * scale, 58f * scale,
                    49f * scale, 58f * scale
                )
                // Horizontal right base leg
                lineTo(64f * scale, 58f * scale)
            }

            // Thick neon laser glow behind the letter L
            drawPath(
                path = lCorePath,
                color = Color(0x55FF4500),
                style = Stroke(width = 14.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )

            // Main Radiant "L" Core Path (Highly responsive white-to-gold-to-red gradient)
            drawPath(
                path = lCorePath,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, Color(0xFFFFD166), Color(0xFFFF6B35)),
                    start = Offset(width * 0.4f, height * 0.35f),
                    end = Offset(width * 0.65f, height * 0.6f)
                ),
                style = Stroke(width = 6.2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )

            // Superfine core light thread inside "L"
            drawPath(
                path = lCorePath,
                color = Color.White,
                style = Stroke(width = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )

            // Golden terminal nodes at L ends
            drawCircle(
                color = Color(0xFFFF6B35),
                radius = 3.5f * scale,
                center = Offset(43f * scale, 38f * scale)
            )
            drawCircle(
                color = Color.White,
                radius = 1.5f * scale,
                center = Offset(43f * scale, 38f * scale)
            )

            drawCircle(
                color = Color(0xFFFFD166),
                radius = 3.5f * scale,
                center = Offset(64f * scale, 58f * scale)
            )
            drawCircle(
                color = Color.White,
                radius = 1.5f * scale,
                center = Offset(64f * scale, 58f * scale)
            )

            // 7. Shimmering Triple Quality Star Sparkles
            val starPath = Path().apply {
                val cx = 73f * scale
                val cy = 34f * scale
                val rMax = 7.5f * scale
                val rMin = 2f * scale
                
                moveTo(cx, cy - rMax)
                quadraticTo(cx, cy, cx + rMin, cy)
                lineTo(cx + rMax, cy)
                quadraticTo(cx, cy, cx, cy + rMin)
                lineTo(cx, cy + rMax)
                quadraticTo(cx, cy, cx - rMin, cy)
                lineTo(cx - rMax, cy)
                quadraticTo(cx, cy, cx, cy - rMin)
                close()
            }
            drawPath(path = starPath, color = Color(0xFFFFD166))
            drawCircle(color = Color.White, radius = 1.2f * scale, center = Offset(73f * scale, 34f * scale))

            // Micro cyan spark on the opposite corner
            val microStar = Path().apply {
                val cx = 26f * scale
                val cy = 46f * scale
                val rMax = 4f * scale
                val rMin = 1.1f * scale
                
                moveTo(cx, cy - rMax)
                quadraticTo(cx, cy, cx + rMin, cy)
                lineTo(cx + rMax, cy)
                quadraticTo(cx, cy, cx, cy + rMin)
                lineTo(cx, cy + rMax)
                quadraticTo(cx, cy, cx - rMin, cy)
                lineTo(cx - rMax, cy)
                quadraticTo(cx, cy, cx, cy - rMin)
                close()
            }
            drawPath(path = microStar, color = Color(0xCC00E5FF))
        }
    }
}

object WebViewPool {
    private val pool = mutableMapOf<String, android.webkit.WebView>()

    fun acquire(context: android.content.Context, tag: String): android.webkit.WebView {
        synchronized(pool) {
            val existing = pool.remove(tag)
            if (existing != null) {
                (existing.parent as? android.view.ViewGroup)?.removeView(existing)
                (existing.context as? android.content.MutableContextWrapper)?.baseContext = context
                return existing
            }
        }
        val wrapper = android.content.MutableContextWrapper(context)
        return android.webkit.WebView(wrapper).apply {
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            webViewClient = android.webkit.WebViewClient()
            webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    android.util.Log.d("WebViewMap", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                    return true
                }
            }
        }
    }

    fun release(webView: android.webkit.WebView, tag: String) {
        synchronized(pool) {
            (webView.parent as? android.view.ViewGroup)?.removeView(webView)
            webView.stopLoading()
            webView.clearHistory()
            pool[tag] = webView
        }
    }
}

class LeafletCoordsHolder {
    var coords: List<Double>? = null
}

@Composable
fun LeafletMapView(
    centerLat: Double = 11.5875,
    centerLng: Double = 77.8232,
    riderLat: Double? = null,
    riderLng: Double? = null,
    storeLat: Double? = null,
    storeLng: Double? = null,
    customerLat: Double? = null,
    customerLng: Double? = null,
    zoom: Int = 16,
    screenTag: String = "general_map",
    storeName: String = "உணவகம் (Hotel Hub)",
    riderPhone: String? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val webView = remember(screenTag) { WebViewPool.acquire(context, screenTag) }
    var isFirstUpdate by remember { mutableStateOf(true) }
    var isPageLoadedState by remember { mutableStateOf(false) }

    LaunchedEffect(webView) {
        webView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN,
                android.view.MotionEvent.ACTION_MOVE -> {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    val html = remember {
        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    html, body, #map {
                        height: 100%;
                        width: 100%;
                        margin: 0;
                        padding: 0;
                        background-color: #0c0f17;
                        background-image: 
                            radial-gradient(circle at 50% 50%, #171d31 0%, #080a0f 100%),
                            linear-gradient(rgba(252, 159, 56, 0.04) 1px, transparent 1px),
                            linear-gradient(90deg, rgba(252, 159, 56, 0.04) 1px, transparent 1px);
                        background-size: 100% 100%, 25px 25px, 25px 25px;
                    }
                    .pulse {
                        width: 14px;
                        height: 14px;
                        border: 3.5px solid #00E5FF;
                        border-radius: 50%;
                        background: #FFFFFF;
                        cursor: pointer;
                        box-shadow: 0 0 10px rgba(0,229,255, 0.8);
                        animation: pulse_anim 1.5s infinite;
                    }
                    @keyframes pulse_anim {
                        0% { box-shadow: 0 0 0 0 rgba(0,229,255, 0.8); }
                        70% { box-shadow: 0 0 0 15px rgba(0,229,255, 0); }
                        100% { box-shadow: 0 0 0 0 rgba(0,229,255, 0); }
                    }
                    @keyframes bounce {
                        0% { transform: translateY(0); }
                        100% { transform: translateY(-6px); }
                    }
                    
                    /* Custom Rider Icon CSS */
                    .rider-container {
                        position: relative;
                        width: 44px;
                        height: 44px;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                    }
                    .rider-pulse-ring {
                        position: absolute;
                        width: 40px;
                        height: 40px;
                        border-radius: 50%;
                        background: rgba(16, 185, 129, 0.35);
                        animation: rider_pulse_anim 1.8s infinite ease-out;
                    }
                    @keyframes rider_pulse_anim {
                        0% { transform: scale(0.6); opacity: 0.9; }
                        100% { transform: scale(1.6); opacity: 0; }
                    }
                    .rider-circle {
                        position: absolute;
                        width: 28px;
                        height: 28px;
                        border-radius: 50%;
                        background: #10B981;
                        border: 2px solid #FFFFFF;
                        box-shadow: 0 4px 8px rgba(0,0,0,0.4);
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        font-size: 16px;
                        z-index: 2;
                    }
                    .rider-emoji {
                        display: inline-block;
                        transition: transform 0.3s ease;
                    }
                    .rider-label {
                        position: absolute;
                        top: -18px;
                        background: rgba(15, 23, 42, 0.9);
                        border: 1px solid #10B981;
                        color: #FFFFFF;
                        font-size: 8px;
                        font-weight: bold;
                        padding: 1px 5px;
                        border-radius: 4px;
                        white-space: nowrap;
                        pointer-events: none;
                        box-shadow: 0 2px 6px rgba(0,0,0,0.5);
                        z-index: 3;
                    }
                    
                    /* Floating Map Style Toggle styling from the user's design image */
                    .style-toggle {
                        position: absolute;
                        top: 12px;
                        right: 12px;
                        z-index: 1000;
                        background: rgba(15, 23, 42, 0.85);
                        border: 1.5px solid rgba(255, 255, 255, 0.15);
                        border-radius: 8px;
                        padding: 3px;
                        display: flex;
                        gap: 4px;
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
                    }
                    .style-toggle button {
                        background: rgba(30, 41, 59, 0.9);
                        border: none;
                        color: #FFFFFF;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                        font-size: 10px;
                        font-weight: bold;
                        padding: 5px 10px;
                        cursor: pointer;
                        border-radius: 6px;
                        display: flex;
                        align-items: center;
                        gap: 4px;
                        transition: all 0.2s ease;
                    }
                    .style-toggle button.active {
                        background: #FBBF24 !important;
                        color: #0F172A !important;
                    }
                    
                    /* Distance/ETA HUD Strip Overlay */
                    #hud-strip {
                        position: absolute;
                        bottom: 12px;
                        left: 12px;
                        right: 12px;
                        z-index: 1000;
                        background: rgba(15, 23, 42, 0.95);
                        border: 1.5px solid #FF6B00;
                        border-radius: 12px;
                        padding: 10px 14px;
                        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.7);
                        color: #FFFFFF;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    }
                    .hud-content {
                        display: flex;
                        flex-direction: column;
                        gap: 6px;
                    }
                    .hud-row {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        font-size: 11px;
                        font-weight: bold;
                    }
                    .hud-item {
                        display: flex;
                        align-items: center;
                        gap: 4px;
                    }
                    .hud-progress-container {
                        height: 6px;
                        background: rgba(255,255,255,0.1);
                        border-radius: 3px;
                        overflow: hidden;
                        width: 100%;
                    }
                    .hud-progress-fill {
                        height: 100%;
                        width: 0%;
                        background: linear-gradient(90deg, #FF6B00, #FFA500);
                        border-radius: 3px;
                        transition: width 0.4s ease;
                    }

                    /* Tap-to-Center Action Badge */
                    #action-badge {
                        position: absolute;
                        top: 60px;
                        left: 12px;
                        right: 12px;
                        z-index: 1000;
                        background: rgba(15, 23, 42, 0.95);
                        border: 2px solid #FF6B00;
                        border-radius: 12px;
                        padding: 12px;
                        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.7);
                        color: #FFFFFF;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        font-size: 12px;
                    }
                    .badge-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        font-weight: bold;
                        font-size: 12px;
                        border-bottom: 1px solid rgba(255,107,0,0.3);
                        padding-bottom: 6px;
                        margin-bottom: 8px;
                    }
                    .badge-close-btn {
                        background: transparent;
                        border: none;
                        color: #94A3B8;
                        font-size: 14px;
                        font-weight: bold;
                        cursor: pointer;
                    }
                    .badge-buttons {
                        display: flex;
                        gap: 8px;
                        width: 100%;
                    }
                    .badge-btn {
                        flex: 1;
                        text-align: center;
                        padding: 8px;
                        border-radius: 8px;
                        font-weight: bold;
                        font-size: 11px;
                        text-decoration: none;
                        color: white;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        gap: 6px;
                    }
                    .badge-btn.call {
                        background: #10B981;
                    }
                    .badge-btn.whatsapp {
                        background: #25D366;
                    }
                    
                    /* Customized marching path animation */
                    .flowing-path-anim {
                        stroke-dasharray: 10, 10;
                        animation: flow-dash 1.2s linear infinite !important;
                    }
                    @keyframes flow-dash {
                        to { stroke-dashoffset: -20; }
                    }
                    
                    /* Custom Popup styling to match our dark theme */
                    .leaflet-popup-content-wrapper {
                        background: #0F172A !important;
                        color: #FFFFFF !important;
                        border: 1.5px solid #FF6B00 !important;
                        border-radius: 10px !important;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.6) !important;
                    }
                    .leaflet-popup-tip {
                        background: #0F172A !important;
                        border-left: 1.5px solid #FF6B00 !important;
                        border-bottom: 1.5px solid #FF6B00 !important;
                    }
                </style>
            </head>
            <body onload="onPageLoaded()">
                <div id="map"></div>
                
                <!-- Floating Map Mode Controls -->
                <div class="style-toggle">
                    <button class="btn-standard" onclick="setTileStyle('voyager')">🗺️ standard</button>
                    <button class="btn-satellite active" onclick="setTileStyle('satellite')">🟡 satellite</button>
                </div>
                
                <script>
                    var map, storeMarker, customerMarker, riderMarker, routePolyline;
                    var activeTileLayer;
                    var hybridLabelLayer = null;
                    window.isPageLoaded = false;
                    window.pendingArgs = null;

                    // Rider interpolation state
                    var targetRiderLat = null;
                    var targetRiderLng = null;
                    var displayedRiderLat = null;
                    var displayedRiderLng = null;
                    
                    var storeIcon = L.divIcon({
                        className: 'custom-store-leaflet-icon',
                        html: '<div style="position: relative; width: 44px; height: 44px; display: flex; justify-content: center; align-items: center;">' +
                              '  <div style="position: absolute; width: 34px; height: 34px; border-radius: 50%; background: #F97316; border: 2.5px solid #FFFFFF; box-shadow: 0 4px 10px rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; font-size: 16px;">🏪</div>' +
                              '</div>',
                        iconSize: [44, 44],
                        iconAnchor: [22, 22]
                    });
                    
                    var customerIcon = L.divIcon({
                        className: 'custom-customer-leaflet-icon',
                        html: '<div style="position: relative; width: 44px; height: 44px; display: flex; justify-content: center; align-items: center;">' +
                              '  <div style="position: absolute; width: 34px; height: 34px; border-radius: 50%; background: #3B82F6; border: 2.5px solid #FFFFFF; box-shadow: 0 4px 10px rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; font-size: 16px;">🏠</div>' +
                              '</div>',
                        iconSize: [44, 44],
                        iconAnchor: [22, 22]
                    });

                    function getHaversineDistance(lat1, lon1, lat2, lon2) {
                        var R = 6371; // Radius of the earth in km
                        var dLat = (lat2 - lat1) * Math.PI / 180;
                        var dLon = (lon2 - lon1) * Math.PI / 180;
                        var a = 
                            Math.sin(dLat/2) * Math.sin(dLat/2) +
                            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
                            Math.sin(dLon/2) * Math.sin(dLon/2);
                        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                        return R * c;
                    }
                    
                    function onPageLoaded() {
                        window.isPageLoaded = true;
                        map = L.map('map', {
                            zoomControl: false,
                            attributionControl: false
                        }).setView([11.5875, 77.8232], 16);
                        
                        setTileStyle('satellite');

                        // PERSISTENT Smooth Rider Animation Interpolation Loop
                        function animateRider() {
                            if (riderMarker && targetRiderLat !== null && targetRiderLng !== null && displayedRiderLat !== null && displayedRiderLng !== null) {
                                var dLat = targetRiderLat - displayedRiderLat;
                                var dLng = targetRiderLng - displayedRiderLng;
                                if (Math.abs(dLat) > 0.000005 || Math.abs(dLng) > 0.000005) {
                                    displayedRiderLat += dLat * 0.05;
                                    displayedRiderLng += dLng * 0.05;
                                    riderMarker.setLatLng([displayedRiderLat, displayedRiderLng]);
                                } else {
                                    displayedRiderLat = targetRiderLat;
                                    displayedRiderLng = targetRiderLng;
                                    riderMarker.setLatLng([displayedRiderLat, displayedRiderLng]);
                                }
                            }
                            requestAnimationFrame(animateRider);
                        }
                        requestAnimationFrame(animateRider);

                        // Background click doesn't pop up any messy badge now, keeping it clean
                        map.on('click', function(e) {
                            // Empty to keep the map clean and interactive
                        });
                        
                        if (window.pendingArgs) {
                            window.updateMap.apply(null, window.pendingArgs);
                            window.pendingArgs = null;
                        }
                    }

                    function centerOnRiderAndShowBadge() {
                        var centerCoords;
                        var riderValid = (targetRiderLat !== null && targetRiderLng !== null && targetRiderLat !== 0.0 && targetRiderLng !== 0.0);
                        
                        if (riderValid) {
                            centerCoords = [targetRiderLat, targetRiderLng];
                        } else {
                            var sLat = window.lastSLat, sLng = window.lastSLng;
                            var cLat = window.lastCLat, cLng = window.lastCLng;
                            if (sLat && sLng && cLat && cLng) {
                                centerCoords = [(sLat + cLat)/2, (sLng + cLng)/2];
                            } else if (cLat && cLng) {
                                centerCoords = [cLat, cLng];
                            } else {
                                centerCoords = [11.5875, 77.8232];
                            }
                        }
                        
                        if (map) {
                            map.flyTo(centerCoords, 16, { animate: true, duration: 1.2 });
                        }
                        showActionBadge(riderValid);
                    }

                    function showActionBadge(riderValid) {
                        var badge = document.getElementById('action-badge');
                        var etaTextEl = document.getElementById('badge-eta-text');
                        var actionsBtnRow = document.getElementById('badge-actions-row');
                        
                        if (!badge) return;
                        
                        if (riderValid && window.lastRLat && window.lastRLng && window.lastCLat && window.lastCLng) {
                            var rDist = getHaversineDistance(window.lastRLat, window.lastRLng, window.lastCLat, window.lastCLng);
                            var etaSeconds = rDist * 150;
                            var etaDate = new Date();
                            etaDate.setSeconds(etaDate.getSeconds() + etaSeconds);
                            var etaString = etaDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                            
                            etaTextEl.innerHTML = "🏍️ ரைடர் வருகை நேரம்: <strong>" + etaString + "</strong> (" + rDist.toFixed(1) + " km)";
                        } else {
                            etaTextEl.innerHTML = "🏍️ ரைடர் இன்னும் கிளம்பவில்லை / இருப்பிடம் கிடைக்கவில்லை.";
                        }
                        
                        var phone = window.riderPhoneNum;
                        if (phone && phone.trim().length > 0 && phone !== "null") {
                            var cleanPhone = phone.replace(/[^0-9]/g, '');
                            if (cleanPhone.length === 10) {
                                cleanPhone = "91" + cleanPhone;
                            }
                            actionsBtnRow.innerHTML = 
                                '<a href="tel:' + phone + '" class="badge-btn call">📞 அழைப்பு (Call)</a>' +
                                '<a href="https://wa.me/' + cleanPhone + '" class="badge-btn whatsapp" target="_blank">💬 வாட்ஸ்அப் (WhatsApp)</a>';
                        } else {
                            actionsBtnRow.innerHTML = '<span style="color: #94A3B8; font-style: italic; font-size: 11px;">தொலைபேசி எண் கிடைக்கவில்லை (No contact info)</span>';
                        }
                        
                        badge.style.display = 'block';
                    }

                    function hideActionBadge(event) {
                        if (event) {
                            event.stopPropagation();
                        }
                        var badge = document.getElementById('action-badge');
                        if (badge) {
                            badge.style.display = 'none';
                        }
                    }
                    
                    function setTileStyle(style) {
                        if (!map) return;
                        if (activeTileLayer) {
                            map.removeLayer(activeTileLayer);
                        }
                        if (hybridLabelLayer) {
                            map.removeLayer(hybridLabelLayer);
                            hybridLabelLayer = null;
                        }
                        var url = '';
                        var attr = '';
                        
                        if (style === 'dark') {
                            url = 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png';
                            attr = '&copy; CARTO';
                        } else if (style === 'voyager') {
                            url = 'https://mt{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}';
                            attr = '&copy; Google';
                        } else if (style === 'satellite') {
                            url = 'https://mt{s}.google.com/vt/lyrs=y&x={x}&y={y}&z={z}';
                            attr = '&copy; Google';
                        }
                        
                        activeTileLayer = L.tileLayer(url, {
                            attribution: attr,
                            maxZoom: 22,
                            maxNativeZoom: 20,
                            subdomains: '0123'
                        }).addTo(map);
                        
                        // Update button selection visual state
                        document.querySelectorAll('.style-toggle button').forEach(function(btn) {
                            btn.classList.remove('active');
                        });
                        var buttonClass = (style === 'voyager' ? 'btn-standard' : 'btn-satellite');
                        var targetBtn = document.querySelector('.' + buttonClass);
                        if (targetBtn) {
                            targetBtn.classList.add('active');
                        }
                    }
                    
                    window.updateMap = function(sLat, sLng, cLat, cLng, rLat, rLng, firstLoad, storeName, centerLatVal, centerLngVal, zoomVal, rPhone) {
                        if (!window.isPageLoaded) {
                            window.pendingArgs = arguments;
                            return;
                        }

                        // Save arguments for overlay interactions
                        window.lastSLat = sLat; window.lastSLng = sLng;
                        window.lastCLat = cLat; window.lastCLng = cLng;
                        window.lastRLat = rLat; window.lastRLng = rLng;
                        window.riderPhoneNum = rPhone;
                        
                        var points = [];
                        
                        if (sLat && sLng && sLat !== 0.0 && sLng !== 0.0) {
                            if (!storeMarker) {
                                storeMarker = L.marker([sLat, sLng], {icon: storeIcon}).addTo(map)
                                    .bindPopup("<div style='color: #ffffff; padding: 4px; font-family: sans-serif; font-size: 11px; line-height: 1.4; min-width: 130px;'>" +
                                               "  <strong style='color: #FF8A00;'>🏪 " + storeName + "</strong><br/>" +
                                               "  <span style='color: #94A3B8; font-size: 9px;'>கடை (Store)</span>" +
                                               "</div>", { closeButton: false });
                            } else {
                                storeMarker.setLatLng([sLat, sLng]);
                            }
                            points.push([sLat, sLng]);
                        } else if (storeMarker) {
                            map.removeLayer(storeMarker);
                            storeMarker = null;
                        }
                        
                        if (cLat && cLng && cLat !== 0.0 && cLng !== 0.0) {
                            if (!customerMarker) {
                                customerMarker = L.marker([cLat, cLng], {icon: customerIcon}).addTo(map)
                                    .bindPopup("<div style='color: #ffffff; padding: 4px; font-family: sans-serif; font-size: 11px; line-height: 1.4; min-width: 130px;'>" +
                                               "  <strong style='color: #38BDF8;'>🏠 உங்கள் இல்லம் (Home)</strong><br/>" +
                                               "  <span style='color: #94A3B8; font-size: 9px;'>வாடிக்கையாளர் (Customer)</span>" +
                                               "</div>", { closeButton: false });
                            } else {
                                customerMarker.setLatLng([cLat, cLng]);
                            }
                            points.push([cLat, cLng]);
                        } else if (customerMarker) {
                            map.removeLayer(customerMarker);
                            customerMarker = null;
                        }
                        
                        if (rLat && rLng && rLat !== 0.0 && rLng !== 0.0) {
                            targetRiderLat = rLat;
                            targetRiderLng = rLng;
                            if (displayedRiderLat === null || displayedRiderLng === null) {
                                displayedRiderLat = rLat;
                                displayedRiderLng = rLng;
                            }

                            // Eastbound vs westbound flip transition dynamic calculation
                            var flipTransform = (cLng && (cLng - rLng > 0)) ? 'transform: scaleX(-1); display: inline-block;' : 'display: inline-block;';
                            var upgradedRiderIcon = L.divIcon({
                                className: 'custom-rider-leaflet-icon',
                                html: '<div id="rider-marker-el" class="rider-container">' +
                                      '  <div class="rider-pulse-ring"></div>' +
                                      '  <div class="rider-circle"><span class="rider-emoji" style="' + flipTransform + '">🏍️</span></div>' +
                                      '</div>',
                                iconSize: [44, 44],
                                iconAnchor: [22, 22]
                            });

                            if (!riderMarker) {
                                riderMarker = L.marker([displayedRiderLat, displayedRiderLng], {icon: upgradedRiderIcon}).addTo(map)
                                    .bindPopup("<div style='color: #ffffff; padding: 4px; font-family: sans-serif; font-size: 11px; line-height: 1.4; min-width: 130px;'>" +
                                               "  <strong style='color: #10B981;'>🏍️ டெலிவரி தம்பி (Rider)</strong><br/>" +
                                               "  <span style='color: #94A3B8; font-size: 9px;'>தற்போது வரும் வழியில்</span>" +
                                               "</div>", { closeButton: false });
                            } else {
                                riderMarker.setIcon(upgradedRiderIcon);
                            }
                            points.push([rLat, rLng]);
                        } else {
                            if (riderMarker) {
                                map.removeLayer(riderMarker);
                                riderMarker = null;
                            }
                            targetRiderLat = null;
                            targetRiderLng = null;
                            displayedRiderLat = null;
                            displayedRiderLng = null;
                        }
                        
                        // Update Distance/ETA HUD Strip Overlay
                        var hud = document.getElementById('hud-strip');
                        if (hud) {
                            if (rLat && rLng && rLat !== 0.0 && rLng !== 0.0 && cLat && cLng && cLat !== 0.0 && cLng !== 0.0) {
                                var distToCust = getHaversineDistance(rLat, rLng, cLat, cLng);
                                var distStoreToCust = (sLat && sLng && sLat !== 0.0 && sLng !== 0.0) ? getHaversineDistance(sLat, sLng, cLat, cLng) : distToCust;
                                
                                var progressPercent = 0;
                                if (distStoreToCust > 0.01) {
                                    progressPercent = (1 - (distToCust / distStoreToCust)) * 100;
                                    if (progressPercent < 0) progressPercent = 0;
                                    if (progressPercent > 100) progressPercent = 100;
                                }

                                var totalSecondsToAdd = distToCust * 150;
                                var etaDate = new Date();
                                etaDate.setSeconds(etaDate.getSeconds() + totalSecondsToAdd);
                                var etaString = etaDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

                                document.getElementById('hud-dist').innerText = distToCust.toFixed(1) + " கி.மீ (km)";
                                document.getElementById('hud-eta').innerText = etaString;
                                document.getElementById('hud-progress-bar').style.width = progressPercent.toFixed(1) + "%";
                                hud.style.display = 'block';
                            } else {
                                hud.style.display = 'none';
                            }
                        }

                        if (points.length > 1) {
                            if (!routePolyline) {
                                routePolyline = L.polyline(points, {
                                    color: '#FF6B00',
                                    weight: 5,
                                    opacity: 0.95,
                                    className: 'flowing-path-anim'
                                }).addTo(map);
                            } else {
                                routePolyline.setLatLngs(points);
                            }
                            map.fitBounds(routePolyline.getBounds(), { padding: [50, 50], animate: true, duration: 1.2 });
                        } else {
                            if (routePolyline) {
                                map.removeLayer(routePolyline);
                                routePolyline = null;
                            }
                            if (rLat && rLng && rLat !== 0.0 && rLng !== 0.0) {
                                map.setView([rLat, rLng], 15, { animate: true, duration: 1.2 });
                            } else if (firstLoad && centerLatVal && centerLngVal) {
                                map.setView([centerLatVal, centerLngVal], zoomVal || 16);
                            }
                        }
                    };
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    LaunchedEffect(webView) {
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
                isPageLoadedState = true
            }
        }
        isPageLoadedState = false
        webView.clearHistory()
        webView.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
        webView.loadDataWithBaseURL("https://lyofresh-map-service.com/", html, "text/html", "UTF-8", null)
        kotlinx.coroutines.delay(80)
        webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        webView.invalidate()
        webView.requestLayout()
    }

    LaunchedEffect(
        isPageLoadedState,
        storeLat,
        storeLng,
        customerLat,
        customerLng,
        riderLat,
        riderLng,
        riderPhone,
        storeName,
        centerLat,
        centerLng,
        zoom
    ) {
        if (isPageLoadedState) {
            val sLat = storeLat ?: 0.0
            val sLng = storeLng ?: 0.0
            val cLat = customerLat ?: 0.0
            val cLng = customerLng ?: 0.0
            val rLat = riderLat ?: 0.0
            val rLng = riderLng ?: 0.0
            val phoneArg = riderPhone?.let { "'$it'" } ?: "null"
            
            webView.evaluateJavascript(
                "if (window.updateMap) { window.updateMap($sLat, $sLng, $cLat, $cLng, $rLat, $rLng, $isFirstUpdate, '$storeName', $centerLat, $centerLng, $zoom, $phoneArg); }",
                null
            )
            isFirstUpdate = false
        }
    }

    DisposableEffect(webView, screenTag) {
        onDispose {
            WebViewPool.release(webView, screenTag)
        }
    }

    AndroidView(
        factory = { ctx ->
            webView.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
            webView.post {
                webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                webView.invalidate()
                webView.requestLayout()
            }
            android.widget.FrameLayout(ctx).apply {
                addView(webView, android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                ))
            }
        },
        update = { parentLayout ->
            webView.invalidate()
            webView.requestLayout()
        },
        modifier = modifier
    )
}

@Composable
fun Simulated3DBikeAnimation(
    modifier: Modifier = Modifier,
    speedKmh: Int = 42
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bike_3d")
    val enginePulse by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "engine_pulse"
    )
    
    val roadOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "road_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            
            // Draw converging 3D perspective lines
            val vanishingPointY = cy - 40f
            val vanishingPointX = cx
            
            // 3D Grid floor
            val roadColor = Color(0x22FF6B00)
            for (i in -4..4) {
                val startX = cx + i * 80f
                drawLine(
                    color = roadColor,
                    start = Offset(vanishingPointX, vanishingPointY),
                    end = Offset(startX, h),
                    strokeWidth = 2f
                )
            }
            
            // Moving horizontal segment indicators (road stripes in perspective)
            val strokeStep = 30f
            var offset = roadOffset % strokeStep
            while (offset < h) {
                val progressY = offset / h
                val lineY = vanishingPointY + progressY * (h - vanishingPointY)
                val lineW = progressY * w * 0.6f
                drawLine(
                    color = Color(0x33FFB347),
                    start = Offset(cx - lineW, lineY),
                    end = Offset(cx + lineW, lineY),
                    strokeWidth = (progressY * 4f).coerceAtLeast(1f)
                )
                offset += strokeStep
            }

            // Glowing track lines (Neon cyan/orange)
            drawCircle(
                color = Color(0x4400E5FF),
                radius = 40f + enginePulse * 2,
                center = Offset(cx, cy + 20f)
            )

            // Draw shadow of the bike
            drawOval(
                color = Color(0xAA000000),
                topLeft = Offset(cx - 30f, cy + 30f),
                size = androidx.compose.ui.geometry.Size(60f + enginePulse, 12f)
            )
            
            // Render the delivery rider in isometric view
            val riderCenter = Offset(cx, cy + 10f + enginePulse)
            
            // Helmet (Orange glow)
            drawCircle(
                color = Color(0xFFFF6B00),
                radius = 12f,
                center = Offset(riderCenter.x, riderCenter.y - 25f)
            )
            // Helmet Visor
            drawArc(
                color = Color(0xFF00E5FF),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(riderCenter.x - 12f, riderCenter.y - 37f),
                size = androidx.compose.ui.geometry.Size(24f, 24f),
                style = Stroke(width = 4f)
            )
            
            // Bike body
            val bikePath = Path().apply {
                moveTo(riderCenter.x - 45f, riderCenter.y + 15f) // Back Wheel
                lineTo(riderCenter.x - 10f, riderCenter.y - 5f) // Frame
                lineTo(riderCenter.x + 35f, riderCenter.y + 15f) // Front Wheel
                lineTo(riderCenter.x + 10f, riderCenter.y - 12f) // Handlebars
                lineTo(riderCenter.x - 20f, riderCenter.y - 3f) // Seat
                close()
            }
            drawPath(
                path = bikePath,
                color = Color(0xFFF8FAFC),
                style = Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Neon wheels (glowing spinning indicators)
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 12f,
                center = Offset(riderCenter.x - 38f, riderCenter.y + 15f),
                style = Stroke(width = 4f)
            )
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 12f,
                center = Offset(riderCenter.x + 28f, riderCenter.y + 15f),
                style = Stroke(width = 4f)
            )

            // Delivery box on back (Golden Amber Cargo)
            drawRect(
                color = Color(0xFFFFB347),
                topLeft = Offset(riderCenter.x - 35f, riderCenter.y - 12f),
                size = androidx.compose.ui.geometry.Size(22f, 20f)
            )
            drawRect(
                color = Color.White,
                topLeft = Offset(riderCenter.x - 35f, riderCenter.y - 12f),
                size = androidx.compose.ui.geometry.Size(22f, 20f),
                style = Stroke(width = 2f)
            )
        }
        
        // Stats Overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color(0xBB0D0F14), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(8.dp))
                .padding(8.dp, 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF00E5FF), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ENGINE ACTIVE • ${speedKmh} KM/H",
                color = Color(0xFF00E5FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = "3D TRANSMISSION STATUS",
            color = Color(0x44FFFFFF),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
        )
    }
}

@Composable
fun Lyo3DSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
    ) {
        // 3D Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 2.dp)
                .background(Color(0x2200D9FF), shape = RoundedCornerShape(8.dp))
        )
        
        // Active searching body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp) // Slim height design
                .background(LyoColors.CardSlate, shape = RoundedCornerShape(8.dp))
                .border(1.dp, LyoColors.GlassBorder, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = LyoColors.TextPrimary,
                    fontSize = 12.sp
                ),
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(LyoColors.AmberYellow),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = LyoColors.AmberYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                        if (trailingIcon != null) {
                            trailingIcon()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun Lyo3DDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF0F172A),
    borderColor: Color = Color(0xFF334155),
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .padding(12.dp)
        ) {
            // Shadow Layer (3D Effect)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 2.dp, y = 5.dp)
                    .background(Color(0x111E3A8A), shape = RoundedCornerShape(24.dp))
            )
            // Foreground Dialog Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(containerColor, shape = RoundedCornerShape(24.dp))
                    .border(1.5.dp, borderColor, shape = RoundedCornerShape(24.dp))
                    .padding(14.dp)
            ) {
                content()
            }
        }
    }
}

class InteractiveMapAppInterface(
    private val onPageLoaded: (() -> Unit)? = null,
    private val onPicked: (Double, Double) -> Unit
) {
    @android.webkit.JavascriptInterface
    fun onPageLoaded() {
        onPageLoaded?.invoke()
    }

    @android.webkit.JavascriptInterface
    fun onLocationPicked(lat: Double, lng: Double) {
        onPicked(lat, lng)
    }
}

@Composable
fun InteractiveMapPickerView(
    initialLat: Double = 11.5812,
    initialLng: Double = 77.8465,
    onLocationPicked: (Double, Double) -> Unit,
    screenTag: String = "interactive_map_picker",
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val webView = remember(screenTag) { WebViewPool.acquire(context, screenTag) }
    var isPageLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(webView) {
        webView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN,
                android.view.MotionEvent.ACTION_MOVE -> {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    val html = remember(initialLat, initialLng) {
        """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" onerror="this.onerror=null;this.href='https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.css';"/>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" onload="initMap()" onerror="this.onerror=null;var s=document.createElement('script');s.src='https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.js';s.onload=initMap;document.head.appendChild(s);"></script>
<style>
html,body {height:100%;width:100%;margin:0;padding:0;background:#0c0f17;overflow:hidden;}
#map {position:absolute;top:0;bottom:0;left:0;right:0;background:#0c0f17;}
#offline-container {height:100%;width:100%;background:#0F172A;display:none;position:relative;}
#offline-canvas {display:block;width:100%;height:100%;}
.info-bar{position:absolute;top:8px;left:8px;right:8px;background:rgba(15,23,42,0.95);border:1px solid rgba(255,107,0,0.5);border-radius:8px;padding:6px 10px;font-size:11px;color:#FFB347;font-weight:bold;z-index:1001;text-align:center;box-shadow:0 2px 4px rgba(0,0,0,0.5);}
.controls-container{position:absolute;bottom:8px;left:8px;right:8px;display:flex;flex-direction:column;gap:6px;z-index:1001;}
.zoom-row{display:flex;gap:6px;width:100%;}
.center-row{display:flex;width:100%;}
.btn{flex:1;background:#FF6B00;border:none;color:white;font-size:11px;font-weight:bold;padding:6px 4px;border-radius:6px;cursor:pointer;box-shadow:0 2px 4px rgba(0,0,0,0.5);min-height:34px;height:auto;display:flex;align-items:center;justify-content:center;}
.btn-sec{background:#1E293B;border:1px solid #FF6B00;}
</style>
</head>
<body onload="initMap()">
<div class="info-bar" id="info">📍 தொட்டு location தேர்வு செய்யுங்கள்</div>

<div id="map"></div>

<div id="offline-container">
  <canvas id="offline-canvas"></canvas>
</div>

<div class="controls-container">
  <div class="zoom-row">
    <button class="btn btn-sec" onclick="zoomIn()">➕ Zoom In</button>
    <button class="btn btn-sec" onclick="zoomOut()">➖ Zoom Out</button>
  </div>
  <div class="center-row">
    <button class="btn" onclick="pinCenter()">📍 PICK CENTER (நடுவில் வை)</button>
  </div>
</div>

<script>
var mapInitialized = false;
var map;

function initMap() {
  if (mapInitialized) return;
  if (typeof L !== 'undefined') {
    mapInitialized = true;
    document.getElementById('offline-container').style.display = 'none';
    document.getElementById('map').style.display = 'block';

    map = L.map('map',{zoomControl:false,attributionControl:false}).setView([$initialLat,$initialLng],17);
    
    // OpenStreetMap primary layer - extremely fast, never blocked
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{
      maxZoom:19,
      attribution:'© OpenStreetMap'
    }).addTo(map);

    // Google Maps Layer fallback
    L.tileLayer('https://mt{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',{
      maxZoom:22,
      maxNativeZoom:20,
      subdomains:'0123'
    }).addTo(map);

    var marker = L.marker([$initialLat,$initialLng],{draggable:true}).addTo(map);
    marker.bindPopup("📍 இங்கே").openPopup();
    
    function notify(lat,lng){
      document.getElementById('info').textContent = '✅ Lat: '+lat.toFixed(6)+', Lng: '+lng.toFixed(6);
      if(window.AndroidApp) window.AndroidApp.onLocationPicked(lat,lng);
    }

    // Call notify immediately to send default coordinate values on start
    notify($initialLat, $initialLng);

    marker.on('dragend',function(e){var p=e.target.getLatLng();notify(p.lat,p.lng);});
    map.on('click',function(e){marker.setLatLng(e.latlng);map.panTo(e.latlng);notify(e.latlng.lat,e.latlng.lng);});
    
    window.zoomIn = function(){map.zoomIn();};
    window.zoomOut = function(){map.zoomOut();};
    window.pinCenter = function(){var c=map.getCenter();marker.setLatLng(c);notify(c.lat,c.lng);};
    
    window.setCoords = function(lat,lng){
      marker.setLatLng([lat,lng]);
      map.setView([lat,lng],17);
      notify(lat,lng);
    };

    setTimeout(function(){map.invalidateSize();},300);
    
    if (window.AndroidApp && window.AndroidApp.onPageLoaded) {
      window.AndroidApp.onPageLoaded();
    }
  } else {
    setupOfflineMap();
  }
}

// Fallback to offline map if Leaflet fails to load after 1.5 seconds
setTimeout(function() {
  if (typeof L === 'undefined' && !mapInitialized) {
    setupOfflineMap();
  }
}, 1500);

var offlineLat = $initialLat;
var offlineLng = $initialLng;

function setupOfflineMap() {
  if (mapInitialized) return;
  mapInitialized = true;
  document.getElementById('map').style.display = 'none';
  document.getElementById('offline-container').style.display = 'block';
  document.getElementById('info').textContent = '⚠️ Offline Mode (வரைபடம் ஆஃப்லைனில் உள்ளது)';
  
  var canvas = document.getElementById('offline-canvas');
  var ctx = canvas.getContext('2d');
  
  function resizeCanvas() {
    canvas.width = canvas.parentElement.clientWidth || 360;
    canvas.height = canvas.parentElement.clientHeight || 400;
    drawOfflineMap();
  }
  
  window.addEventListener('resize', resizeCanvas);
  setTimeout(resizeCanvas, 100);
  setTimeout(resizeCanvas, 500);
  setTimeout(resizeCanvas, 1000);
  
  function drawOfflineMap() {
    var w = canvas.width;
    var h = canvas.height;
    
    // Background Slate
    ctx.fillStyle = '#0F172A';
    ctx.fillRect(0, 0, w, h);
    
    // Agricultural green patches
    ctx.fillStyle = '#14532D';
    ctx.fillRect(20, 20, w * 0.3, h * 0.25);
    ctx.fillRect(w * 0.65, h * 0.55, w * 0.3, h * 0.3);
    
    // Hills
    ctx.fillStyle = '#292524';
    ctx.beginPath(); ctx.arc(w * 0.2, h * 0.75, 40, 0, Math.PI*2); ctx.fill();
    ctx.fillStyle = '#1C1917';
    ctx.beginPath(); ctx.arc(w * 0.2, h * 0.75, 25, 0, Math.PI*2); ctx.fill();
    
    // Lake Big Blue
    ctx.fillStyle = '#0F766E';
    ctx.beginPath(); ctx.arc(w * 0.8, h * 0.25, 45, 0, Math.PI*2); ctx.fill();
    ctx.fillStyle = '#0D9488';
    ctx.beginPath(); ctx.arc(w * 0.8, h * 0.25, 30, 0, Math.PI*2); ctx.fill();
    
    // Roads (Main and side streets)
    ctx.lineWidth = 14;
    ctx.strokeStyle = '#334155';
    // Main road
    ctx.beginPath(); ctx.moveTo(0, h * 0.5); ctx.lineTo(w, h * 0.5); ctx.stroke();
    // Bazaar street
    ctx.beginPath(); ctx.moveTo(w * 0.5, 0); ctx.lineTo(w * 0.5, h); ctx.stroke();
    
    ctx.lineWidth = 2;
    ctx.strokeStyle = '#E2E8F0';
    ctx.setLineDash([6, 6]);
    // Dash lines
    ctx.beginPath(); ctx.moveTo(0, h * 0.25); ctx.lineTo(w, h * 0.25); ctx.stroke();
    ctx.beginPath(); ctx.moveTo(w * 0.25, 0); ctx.lineTo(w * 0.25, h); ctx.stroke();
    ctx.setLineDash([]); // Reset
    
    // Labels
    ctx.fillStyle = '#E2E8F0';
    ctx.font = 'bold 9px sans-serif';
    ctx.fillText('🛣️ சேலம் மெயின் ரோடு (Salem Road)', 10, h * 0.5 - 12);
    ctx.fillText('🛒 இடப்பாடி கடைவீதி (Bazaar St)', w * 0.5 + 8, 20);
    ctx.fillText('⛰️ சிவகிரி திருமலை (Sivagiri Hill)', w * 0.1, h * 0.75 + 50);
    ctx.fillText('🌊 பெரிய ஏரி (Big Lake)', w * 0.7, h * 0.25 - 55);
    
    // Draw Pin Drop Marker
    var latMin = 11.5700, latMax = 11.6000;
    var lngMin = 77.8300, lngMax = 77.8700;
    
    var pctX = (offlineLng - lngMin) / (lngMax - lngMin);
    var pctY = (latMax - offlineLat) / (latMax - latMin);
    
    var px = pctX * w;
    var py = pctY * h;
    
    px = Math.max(20, Math.min(w - 20, px));
    py = Math.max(20, Math.min(h - 20, py));
    
    var grad = ctx.createRadialGradient(px, py, 2, px, py, 20);
    grad.addColorStop(0, 'rgba(255, 107, 0, 0.6)');
    grad.addColorStop(1, 'rgba(255, 107, 0, 0)');
    ctx.fillStyle = grad;
    ctx.beginPath(); ctx.arc(px, py, 20, 0, Math.PI*2); ctx.fill();
    
    // Pin Center
    ctx.fillStyle = '#FF6B00';
    ctx.beginPath();
    ctx.arc(px, py - 15, 8, 0, Math.PI*2);
    ctx.fill();
    // Pin stem
    ctx.strokeStyle = '#FF6B00';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(px, py - 15);
    ctx.lineTo(px, py);
    ctx.stroke();
    // Pin center dot
    ctx.fillStyle = '#FFFFFF';
    ctx.beginPath();
    ctx.arc(px, py - 15, 3, 0, Math.PI*2);
    ctx.fill();
  }
  
  function handleCanvasInteraction(e) {
    var rect = canvas.getBoundingClientRect();
    var x = (e.clientX || e.touches[0].clientX) - rect.left;
    var y = (e.clientY || e.touches[0].clientY) - rect.top;
    
    var w = canvas.width;
    var h = canvas.height;
    
    var pctX = x / w;
    var pctY = y / h;
    
    var latMin = 11.5700, latMax = 11.6000;
    var lngMin = 77.8300, lngMax = 77.8700;
    
    offlineLat = latMax - (pctY * (latMax - latMin));
    offlineLng = lngMin + (pctX * (lngMax - lngMin));
    
    drawOfflineMap();
    notifyOffline(offlineLat, offlineLng);
  }
  
  canvas.addEventListener('mousedown', function(e) {
    handleCanvasInteraction(e);
    canvas.addEventListener('mousemove', handleCanvasInteraction);
  });
  
  canvas.addEventListener('mouseup', function() {
    canvas.removeEventListener('mousemove', handleCanvasInteraction);
  });
  
  canvas.addEventListener('touchstart', function(e) {
    handleCanvasInteraction(e);
  });
  
  canvas.addEventListener('touchmove', function(e) {
    handleCanvasInteraction(e);
    e.preventDefault();
  });
  
  function notifyOffline(lat, lng) {
    document.getElementById('info').textContent = '✅ Lat: '+lat.toFixed(6)+', Lng: '+lng.toFixed(6);
    if(window.AndroidApp) window.AndroidApp.onLocationPicked(lat,lng);
  }
  
  notifyOffline(offlineLat, offlineLng);
  
  window.zoomIn = function() {
    document.getElementById('info').textContent = 'ℹ️ Zoom In (Offline Map Static)';
  };
  window.zoomOut = function() {
    document.getElementById('info').textContent = 'ℹ️ Zoom Out (Offline Map Static)';
  };
  window.pinCenter = function() {
    offlineLat = 11.5812;
    offlineLng = 77.8465;
    drawOfflineMap();
    notifyOffline(offlineLat, offlineLng);
  };

  if (window.AndroidApp && window.AndroidApp.onPageLoaded) {
    window.AndroidApp.onPageLoaded();
  }
}
</script>
</body>
</html>
        """.trimIndent()
    }

    LaunchedEffect(webView) {
        webView.clearHistory()
        webView.loadDataWithBaseURL("https://lyofresh-map-service.com/", html, "text/html", "UTF-8", null)
    }

    var lastSentLat by remember { mutableStateOf(0.0) }
    var lastSentLng by remember { mutableStateOf(0.0) }

    DisposableEffect(webView, screenTag) {
        webView.addJavascriptInterface(InteractiveMapAppInterface(
            onPageLoaded = {
                isPageLoaded = true
            },
            onPicked = { lat, lng ->
                lastSentLat = lat
                lastSentLng = lng
                onLocationPicked(lat, lng)
            }
        ), "AndroidApp")
        onDispose {
            webView.removeJavascriptInterface("AndroidApp")
            WebViewPool.release(webView, screenTag)
        }
    }

    LaunchedEffect(isPageLoaded, initialLat, initialLng) {
        if (isPageLoaded) {
            webView.evaluateJavascript(
                "if (window.setCoords) { window.setCoords($initialLat, $initialLng); }",
                null
            )
        }
    }

    AndroidView(
        factory = { ctx ->
            webView.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
            webView.post {
                webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                webView.invalidate()
                webView.requestLayout()
            }
            android.widget.FrameLayout(ctx).apply {
                addView(webView, android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                ))
            }
        },
        update = { parentLayout ->
            webView.invalidate()
            webView.requestLayout()
        },
        modifier = modifier
    )
}


