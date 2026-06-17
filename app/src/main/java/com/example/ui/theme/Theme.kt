package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF6366F1), // Indigo 500
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4F46E5), // Indigo 600
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFA1A1AA), // Zinc 400
    onSecondary = Color(0xFF18181B), // Zinc 900
    background = Color(0xFF09090B), // Zinc 950
    surface = Color(0xFF18181B), // Zinc 900
    onBackground = Color(0xFFFAFAFA), // Zinc 50
    onSurface = Color(0xFFFAFAFA), // Zinc 50
    surfaceVariant = Color(0xFF27272A), // Zinc 800
    onSurfaceVariant = Color(0xFFD4D4D8), // Zinc 300
    outline = Color(0xFF27272A), // Zinc 800 (border)
    outlineVariant = Color(0xFF3F3F46), // Zinc 700
    error = Color(0xFFEF4444)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF6366F1), // Indigo 500
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4F46E5), // Indigo 600
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF71717A), // Zinc 500
    onSecondary = Color.White, 
    background = Color(0xFFFFFFFF), // White
    surface = Color(0xFFF4F4F5), // Zinc 100
    onBackground = Color(0xFF09090B), // Zinc 950
    onSurface = Color(0xFF09090B), // Zinc 950
    surfaceVariant = Color(0xFFE4E4E7), // Zinc 200
    onSurfaceVariant = Color(0xFF3F3F46), // Zinc 700
    outline = Color(0xFFD4D4D8), // Zinc 300 (border)
    outlineVariant = Color(0xFFA1A1AA), // Zinc 400
    error = Color(0xFFEF4444)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
