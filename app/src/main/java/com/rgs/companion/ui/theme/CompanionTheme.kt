package com.rgs.companion.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * "Cyber Obsidian" & "Daylight Silver" — the app's visual identity.
 *
 * Dark: Near-black base, neon cyan for "system/live" accents, violet for "her" accents.
 * Light: Refined pearl/lavender-silver canvas, deep cyber teal and royal violet accents.
 */
val ObsidianBg = Color(0xFF09090B.toInt())
val ObsidianSurface = Color(0xFF131318.toInt())
val NeonCyan = Color(0xFF00E5FF.toInt())
val NeonViolet = Color(0xFFB53DFF.toInt())
val InkOn = Color(0xFFE8E3F5.toInt())
val MutedOn = Color(0xFFA89FC6.toInt())

// Light Palette Colors
val DaylightBg = Color(0xFFF6F5FA.toInt())
val DaylightSurface = Color(0xFFFFFFFF.toInt())
val DaylightSurfaceVariant = Color(0xFFECE9F5.toInt())
val DaylightInkOn = Color(0xFF161324.toInt())
val DaylightMutedOn = Color(0xFF5F587A.toInt())
val DaylightTeal = Color(0xFF007585.toInt())
val DaylightViolet = Color(0xFF7A1CA8.toInt())

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

@Stable
class CompanionThemeState(
    private val context: Context? = null,
    initialMode: ThemeMode = ThemeMode.DARK
) {
    private val prefs = context?.getSharedPreferences("zaiphra_theme_prefs", Context.MODE_PRIVATE)

    var mode: ThemeMode by mutableStateOf(
        prefs?.getString("theme_mode", null)?.let { saved ->
            try { ThemeMode.valueOf(saved) } catch (_: Exception) { initialMode }
        } ?: initialMode
    )

    val isDark: Boolean
        @Composable
        get() = when (mode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

    fun setDarkMode(dark: Boolean) {
        setMode(if (dark) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    fun setMode(newMode: ThemeMode) {
        mode = newMode
        prefs?.edit()?.putString("theme_mode", newMode.name)?.apply()
    }

    fun toggle(systemInDark: Boolean = true) {
        val currentlyDark = when (mode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> systemInDark
        }
        setDarkMode(!currentlyDark)
    }
}

val LocalCompanionThemeState = staticCompositionLocalOf { CompanionThemeState() }

@Composable
fun rememberCompanionThemeState(
    context: Context = LocalContext.current,
    initialMode: ThemeMode = ThemeMode.DARK
): CompanionThemeState {
    return remember(context) {
        CompanionThemeState(context = context, initialMode = initialMode)
    }
}

val CompanionDark: ColorScheme = darkColorScheme(
    background = ObsidianBg,
    onBackground = InkOn,
    surface = ObsidianSurface,
    onSurface = InkOn,
    surfaceVariant = Color(0xFF1C1A28.toInt()),
    onSurfaceVariant = MutedOn,
    primary = NeonCyan,
    onPrimary = Color(0xFF04262B.toInt()),
    primaryContainer = Color(0xFF07333C.toInt()),
    onPrimaryContainer = Color(0xFFBFF6FF.toInt()),
    secondary = NeonViolet,
    onSecondary = Color(0xFF2B0A38.toInt()),
    secondaryContainer = Color(0xFF331245.toInt()),
    onSecondaryContainer = Color(0xFFEFC8FF.toInt()),
    tertiary = Color(0xFF4DD0C8.toInt()),
    onTertiary = Color(0xFF003834.toInt()),
    error = Color(0xFFCB4B5B.toInt()),
    errorContainer = Color(0xFF3A1520.toInt()),
    onError = Color(0xFFFFF1F2.toInt()),
    onErrorContainer = Color(0xFFFFD9DE.toInt()),
    outline = Color(0x33FFFFFF.toInt()),
    outlineVariant = Color(0x1AFFFFFF.toInt()),
)

val CompanionLight: ColorScheme = lightColorScheme(
    background = DaylightBg,
    onBackground = DaylightInkOn,
    surface = DaylightSurface,
    onSurface = DaylightInkOn,
    surfaceVariant = DaylightSurfaceVariant,
    onSurfaceVariant = DaylightMutedOn,
    primary = DaylightTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAF0F8.toInt()),
    onPrimaryContainer = Color(0xFF002025.toInt()),
    secondary = DaylightViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0D5FF.toInt()),
    onSecondaryContainer = Color(0xFF2D0044.toInt()),
    tertiary = Color(0xFF007F70.toInt()),
    onTertiary = Color.White,
    error = Color(0xFFBA1A1A.toInt()),
    errorContainer = Color(0xFFFFDAD6.toInt()),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002.toInt()),
    outline = Color(0x2E161324.toInt()),
    outlineVariant = Color(0x14161324.toInt()),
)

@Composable
fun CompanionTheme(
    themeState: CompanionThemeState = rememberCompanionThemeState(),
    content: @Composable () -> Unit
) {
    val isDark = themeState.isDark
    val colorScheme = if (isDark) CompanionDark else CompanionLight

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

    CompositionLocalProvider(LocalCompanionThemeState provides themeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

