package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LyoGoldPrimary, // Electric Cyan Accent
    secondary = LyoOrangeExpress, // Electric Cyan Accent
    tertiary = LyoCyanActive, // Electric Cyan Accent
    background = LyoMidnightBg, // Deep Navy Background
    surface = LyoSurfaceSlate, // Deep Slate Card Surface
    onPrimary = Color(0xFF0B1120), // Dark Navy text on Electric Cyan
    onSecondary = Color(0xFF0B1120),
    onTertiary = Color(0xFF0B1120),
    onBackground = LyoTextWhite, // Pure White text
    onSurface = LyoTextWhite, // Pure White text
    surfaceVariant = Color(0xFF1E293B), // Dark Slate variant
    onSurfaceVariant = LyoTextSlate, // Platinum/Silver-Gray subtext
    outline = Color(0x3300D9FF), // Electric Cyan Translucent Outline
    error = LyoNonVegRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LyoGoldPrimary,
    secondary = LyoOrangeExpress,
    tertiary = LyoCyanActive,
    background = LyoMidnightBg,
    surface = LyoSurfaceSlate,
    onPrimary = Color(0xFF0B1120),
    onSecondary = Color(0xFF0B1120),
    onTertiary = Color(0xFF0B1120),
    onBackground = LyoTextWhite,
    onSurface = LyoTextWhite,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = LyoTextSlate,
    outline = Color(0x3300D9FF),
    error = LyoNonVegRed,
    onError = Color.White
)

private val LyoShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set to false by default to protect authentic Lyo premium branded colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = LyoShapes,
    content = content
  )
}
