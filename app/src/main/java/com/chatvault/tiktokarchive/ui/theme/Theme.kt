package com.chatvault.tiktokarchive.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.chatvault.tiktokarchive.domain.model.ThemeMode

// Own palette (rose + teal). Intentionally not TikTok's brand assets, so the app is not mistaken for the official one.
private val LightColors = lightColorScheme(
    primary = Color(0xFFE0245E),
    onPrimary = Color.White,
    secondary = Color(0xFF0F9D8F),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F1F3),
    onSurfaceVariant = Color(0xFF6B6B73),
    error = Color(0xFFC62828),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF4D7A),
    onPrimary = Color.White,
    secondary = Color(0xFF2DD4BF),
    background = Color(0xFF0B0B0D),
    surface = Color(0xFF0B0B0D),
    surfaceVariant = Color(0xFF2A2A2F),
    onSurfaceVariant = Color(0xFFA3A3AD),
    error = Color(0xFFFF6B6B),
)

@Composable
fun ChatVaultTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
