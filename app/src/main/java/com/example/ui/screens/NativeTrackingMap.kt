package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LyoOrangeExpress
import com.example.ui.theme.LyoVegGreen
import com.example.ui.theme.LyoNonVegRed

/**
 * Pure coordinate utility translating geographical coordinates (latitude, longitude)
 * into screen coordinates (Offset) inside the specified map canvas pixel viewport.
 * Uses a robust equirectangular projection mapping.
 */
fun latLngToPixel(
    lat: Double,
    lng: Double,
    imageWidthPx: Float,
    imageHeightPx: Float
): Offset {
    // Highly accurate calibration bounds for the local Idappadi town service area (approx 15km)
    val topLat = 11.62
    val bottomLat = 11.54
    val leftLng = 77.78
    val rightLng = 77.90

    val xPortion = (lng - leftLng) / (rightLng - leftLng)
    val yPortion = (topLat - lat) / (topLat - bottomLat)

    // Coerce coordinates slightly inside bounds to guarantee markers stay visible on mapping canvas
    val marginFraction = 0.02f
    val minX = imageWidthPx * marginFraction
    val maxX = imageWidthPx * (1f - marginFraction)
    val minY = imageHeightPx * marginFraction
    val maxY = imageHeightPx * (1f - marginFraction)

    val x = (xPortion * imageWidthPx).toFloat().coerceIn(minX, maxX)
    val y = (yPortion * imageHeightPx).toFloat().coerceIn(minY, maxY)

    return Offset(x, y)
}

/**
 * Native Compose Canvas-based hyperlocal map displaying vendor node, customer address,
 * and live delivery partner vehicle telemetry smoothly glided on physical bounds.
 */
@Composable
fun NativeTrackingMap(
    riderLat: Double?,
    riderLng: Double?,
    customerLat: Double,
    customerLng: Double,
    vendorLat: Double,
    vendorLng: Double,
    modifier: Modifier = Modifier,
    riderLastUpdatedEpoch: Long? = null
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(LyoColors.DarkCyanBg) // Luxury slate fallback backing
            .border(1.dp, LyoColors.GlassBorder, RoundedCornerShape(24.dp))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // Assess if the telemetry is alive, valid, and recent (under 2 minutes)
        val isRiderStale = remember(riderLastUpdatedEpoch) {
            riderLastUpdatedEpoch != null && (System.currentTimeMillis() - riderLastUpdatedEpoch) > 120_000
        }
        val isRiderActive = riderLat != null && riderLng != null && !isRiderStale

        // 1. Calculate static point coordinate offsets
        val vendorOffset = remember(vendorLat, vendorLng, widthPx, heightPx) {
            if (widthPx > 0f && heightPx > 0f) {
                latLngToPixel(vendorLat, vendorLng, widthPx, heightPx)
            } else {
                Offset.Zero
            }
        }

        val customerOffset = remember(customerLat, customerLng, widthPx, heightPx) {
            if (widthPx > 0f && heightPx > 0f) {
                latLngToPixel(customerLat, customerLng, widthPx, heightPx)
            } else {
                Offset.Zero
            }
        }

        // 2. Animate the live telemetry coordinate using a robust Animatable<Offset, ...>
        val riderAnimOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

        LaunchedEffect(riderLat, riderLng, widthPx, heightPx, isRiderActive) {
            if (isRiderActive && riderLat != null && riderLng != null && widthPx > 0f && heightPx > 0f) {
                val targetOffset = latLngToPixel(riderLat, riderLng, widthPx, heightPx)
                if (riderAnimOffset.value == Offset.Zero) {
                    riderAnimOffset.snapTo(targetOffset)
                } else {
                    riderAnimOffset.animateTo(
                        targetValue = targetOffset,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    )
                }
            } else if (!isRiderActive) {
                riderAnimOffset.snapTo(Offset.Zero)
            }
        }

        // 3. Static background map image of the Idappadi service area
        Image(
            painter = painterResource(id = R.drawable.idappadi_service_map),
            contentDescription = "Idappadi Service Map",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // 4. Overlay connection vector lines layer (straight dashed trajectory indicator)
        if (isRiderActive && widthPx > 0f && heightPx > 0f) {
            val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f) }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rOff = riderAnimOffset.value
                val cOff = customerOffset

                if (rOff != Offset.Zero && cOff != Offset.Zero) {
                    // Straight course trajectory path
                    drawLine(
                        color = LyoOrangeExpress.copy(alpha = 0.5f),
                        start = rOff,
                        end = cOff,
                        strokeWidth = 3.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }
            }
        }

        // 5. High-fidelity floating markers
        if (widthPx > 0f && heightPx > 0f) {
            val markerSize = 44.dp
            val halfSizePx = with(density) { (markerSize / 2).toPx() }

            // A. Vendor Node Marker (Storefront)
            if (vendorOffset != Offset.Zero) {
                val vx = with(density) { (vendorOffset.x - halfSizePx).toDp() }
                val vy = with(density) { (vendorOffset.y - halfSizePx).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = vx, y = vy)
                        .size(markerSize),
                    contentAlignment = Alignment.Center
                ) {
                    // Accent pulsing halo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LyoVegGreen.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, LyoVegGreen.copy(alpha = 0.35f), CircleShape)
                    )
                    // Solid marker body
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(LyoVegGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = "விற்பனையாளர்",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // B. Customer Node Marker (Home)
            if (customerOffset != Offset.Zero) {
                val cx = with(density) { (customerOffset.x - halfSizePx).toDp() }
                val cy = with(density) { (customerOffset.y - halfSizePx).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = cx, y = cy)
                        .size(markerSize),
                    contentAlignment = Alignment.Center
                ) {
                    // Accent pulsing halo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LyoNonVegRed.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, LyoNonVegRed.copy(alpha = 0.35f), CircleShape)
                    )
                    // Solid marker body
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(LyoNonVegRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "வாடிக்கையாளர்",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // C. Smooth Telemetric Rider Express Marker
            if (isRiderActive && riderAnimOffset.value != Offset.Zero) {
                val rx = with(density) { (riderAnimOffset.value.x - halfSizePx).toDp() }
                val ry = with(density) { (riderAnimOffset.value.y - halfSizePx).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = rx, y = ry)
                        .size(markerSize),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing/rippling signal aura radiating outwards to represent moving telemetry
                    val pulseTransition = rememberInfiniteTransition(label = "rider_pulse")
                    val pulseScale by pulseTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.45f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = EaseOutQuad),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "pulse_scale"
                    )
                    val pulseAlpha by pulseTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 0.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = EaseOutQuad),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "pulse_alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(markerSize)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = pulseAlpha
                            }
                            .background(LyoOrangeExpress.copy(alpha = 0.4f), CircleShape)
                    )

                    // Solid circular rider token
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(LyoOrangeExpress, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsBike,
                            contentDescription = "விநியோகஸ்தர்",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 6. Graceful dormant telemetry indicator card if rider coordinates are stale / unavailable
        if (!isRiderActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(LyoColors.DarkCyanBg.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .border(1.dp, LyoNonVegRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = LyoNonVegRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ரைடர் இருப்பிடம் கிடைக்கவில்லை (Rider location dormant)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
