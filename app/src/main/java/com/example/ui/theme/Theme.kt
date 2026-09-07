package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ========================================================
// Theme Mode Enums & Configuration State
// ========================================================

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Stable
class ThemeState(
    initialMode: ThemeMode = ThemeMode.SYSTEM,
    initialDynamicColor: Boolean = false
) {
    var themeMode by mutableStateOf(initialMode)
    var dynamicColor by mutableStateOf(initialDynamicColor)

    fun isDark(systemInDark: Boolean): Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    fun toggleTheme(systemInDark: Boolean) {
        val currentlyDark = isDark(systemInDark)
        themeMode = if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
    }

    fun setMode(mode: ThemeMode) {
        themeMode = mode
    }

    fun toggleDynamicColor() {
        dynamicColor = !dynamicColor
    }
}

val LocalThemeState = compositionLocalOf<ThemeState> {
    error("No ThemeState provided. Wrap your composable hierarchy in RgsAiTheme or ProvideThemeState.")
}

// ========================================================
// Material 3 Dark Color Scheme (Cyber Obsidian / Neon RGS)
// ========================================================
val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = RgsCyan,
    onPrimary = ObsidianBackground,
    primaryContainer = Color(0xFF004D56),
    onPrimaryContainer = Color(0xFF9CF7FF),
    inversePrimary = Color(0xFF006874),

    secondary = RgsPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A0099),
    onSecondaryContainer = Color(0xFFE9D5FF),

    tertiary = RgsTerminalGreen,
    onTertiary = ObsidianBackground,
    tertiaryContainer = Color(0xFF00522B),
    onTertiaryContainer = Color(0xFF86FFB9),

    error = RgsRedAlert,
    onError = Color.White,
    errorContainer = Color(0xFF8C0020),
    onErrorContainer = Color(0xFFFFD9DF),

    background = ObsidianBackground,
    onBackground = TextPrimary,

    surface = CyberDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    surfaceTint = RgsCyan,

    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = ObsidianBackground,

    outline = GlassBorder,
    outlineVariant = Color(0xFF243656),
    scrim = Color.Black
)

// ========================================================
// Material 3 Light Color Scheme (Polished Clean Light Theme)
// ========================================================
val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9CF7FF),
    onPrimaryContainer = Color(0xFF001F24),
    inversePrimary = RgsCyan,

    secondary = Color(0xFF6750A4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DDFF),
    onSecondaryContainer = Color(0xFF22005D),

    tertiary = Color(0xFF006D3B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF92F9B4),
    onTertiaryContainer = Color(0xFF00210E),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = Color(0xFF006874),

    inverseSurface = Color(0xFF1E293B),
    inverseOnSurface = Color(0xFFF8FAFC),

    outline = LightBorder,
    outlineVariant = Color(0xFFE2E8F0),
    scrim = Color.Black
)

/**
 * Material 3 Application Theme supporting dynamic color palettes (Android 12+),
 * automatic system light/dark mode toggling, and CompositionLocal configuration state.
 */
@Composable
fun RgsAiTheme(
    themeState: ThemeState = remember { ThemeState() },
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = themeState.isDark(systemInDark)
    val context = LocalContext.current

    val colorScheme = when {
        themeState.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalThemeState provides themeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun RgsAiTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val initialMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT
    val themeState = remember(darkTheme, dynamicColor) {
        ThemeState(initialMode = initialMode, initialDynamicColor = dynamicColor)
    }
    RgsAiTheme(themeState = themeState, content = content)
}

// Backward-compatibility and template alias wrappers
@Composable
fun AppTheme(
    themeState: ThemeState = remember { ThemeState() },
    content: @Composable () -> Unit
) {
    RgsAiTheme(themeState = themeState, content = content)
}

@Composable
fun IrisXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val initialMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT
    val themeState = remember(darkTheme, dynamicColor) {
        ThemeState(initialMode = initialMode, initialDynamicColor = dynamicColor)
    }
    RgsAiTheme(themeState = themeState, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val initialMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT
    val themeState = remember(darkTheme, dynamicColor) {
        ThemeState(initialMode = initialMode, initialDynamicColor = dynamicColor)
    }
    RgsAiTheme(themeState = themeState, content = content)
}

private val ZaiphraDarkColorScheme = darkColorScheme(
    primary = ZaiphraPrimary,
    secondary = ZaiphraSecondary,
    tertiary = ZaiphraTertiary,
    background = ZaiphraBackground,
    surface = ZaiphraSurface,
    surfaceVariant = ZaiphraSurfaceVariant,
    onPrimary = ZaiphraBackground,
    onSecondary = ZaiphraBackground,
    onTertiary = ZaiphraBackground,
    onBackground = ZaiphraOnBackground,
    onSurface = ZaiphraOnSurface,
    onSurfaceVariant = ZaiphraOnSurfaceVariant
)

@Composable
fun ZaiphraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ZaiphraDarkColorScheme,
        typography = Typography,
        content = content
    )
}
