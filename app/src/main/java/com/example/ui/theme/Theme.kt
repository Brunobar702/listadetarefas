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

private val SleekBlue = Color(0xFF0061A4)
private val SleekBlueLight = Color(0xFFD1E4FF)
private val SleekBlueDarkText = Color(0xFF001D36)
private val SleekBg = Color(0xFFFDFCFF)
private val SleekSlateDark = Color(0xFF1A1C1E)
private val SleekSlateBody = Color(0xFF1E293B)
private val SleekSlateMuted = Color(0xFF64748B)
private val SleekSlateContainer = Color(0xFFF1F5F9)
private val SleekBorder = Color(0xFFF1F5F9)

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7D0)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekBlue,
    onPrimary = Color.White,
    primaryContainer = SleekBlueLight,
    onPrimaryContainer = SleekBlueDarkText,
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    background = SleekBg,
    onBackground = SleekSlateDark,
    surface = Color.White,
    onSurface = SleekSlateBody,
    surfaceVariant = SleekSlateContainer,
    onSurfaceVariant = SleekSlateMuted,
    outline = Color(0xFFE2E8F0)
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
