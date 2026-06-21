package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SleekTeal,
    secondary = SleekTealDark,
    tertiary = SleekGreen,
    background = Color(0xFF111315),
    surface = Color(0xFF1E2124),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE1E2E5),
    onSurface = Color(0xFFE1E2E5),
    outlineVariant = Color(0xFF2C2F33),
    primaryContainer = SleekTealDark,
    onPrimaryContainer = SleekTealLight
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekTeal,
    secondary = SleekTealDark,
    tertiary = SleekGreen,
    background = SleekBackground,
    surface = SleekSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SleekTextDark,
    onSurface = SleekTextDark,
    outlineVariant = SleekOutline,
    primaryContainer = SleekTealLight,
    onPrimaryContainer = SleekTealDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamicColor by default to guarantee the brand's Sleek Interface colors
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
