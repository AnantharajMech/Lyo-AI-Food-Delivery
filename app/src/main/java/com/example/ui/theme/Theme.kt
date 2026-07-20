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
    primary = LyoGoldPrimary, // Neon Cyan Accent
    secondary = LyoOrangeExpress, // Neon Purple Accent
    tertiary = LyoCyanActive,
    background = LyoMidnightBg, // Background #060B16
    surface = LyoSurfaceSlate, // Surface #121A33
    onPrimary = Color(0xFF060B16),
    onSecondary = Color(0xFF060B16),
    onTertiary = Color(0xFF060B16),
    onBackground = LyoTextWhite, // Platinum text #F5F7FA
    onSurface = LyoTextWhite,
    surfaceVariant = LyoSecondaryBg, // Secondary Background #0D1424
    onSurfaceVariant = LyoTextSlate, // Secondary text #9AA6C4
    outline = LyoBorder, // Border rgba(255,255,255,0.08)
    error = LyoNonVegRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LyoGoldPrimary,
    secondary = LyoOrangeExpress,
    tertiary = LyoCyanActive,
    background = LyoMidnightBg,
    surface = LyoSurfaceSlate,
    onPrimary = Color(0xFF060B16),
    onSecondary = Color(0xFF060B16),
    onTertiary = Color(0xFF060B16),
    onBackground = LyoTextWhite,
    onSurface = LyoTextWhite,
    surfaceVariant = LyoSecondaryBg,
    onSurfaceVariant = LyoTextSlate,
    outline = LyoBorder,
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
