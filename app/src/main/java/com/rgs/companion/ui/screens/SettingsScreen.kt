package com.rgs.companion.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.rgs.companion.control.CompanionAccessibilityService
import com.rgs.companion.memory.MemoryStore
import com.rgs.companion.overlay.FloatingCompanionService
import com.rgs.companion.ui.theme.DaylightBg
import com.rgs.companion.ui.theme.DaylightInkOn
import com.rgs.companion.ui.theme.DaylightMutedOn
import com.rgs.companion.ui.theme.DaylightSurface
import com.rgs.companion.ui.theme.DaylightTeal
import com.rgs.companion.ui.theme.DaylightViolet
import com.rgs.companion.ui.theme.InkOn
import com.rgs.companion.ui.theme.LocalCompanionThemeState
import com.rgs.companion.ui.theme.MutedOn
import com.rgs.companion.ui.theme.NeonCyan
import com.rgs.companion.ui.theme.NeonViolet
import com.rgs.companion.ui.theme.ObsidianBg
import com.rgs.companion.ui.theme.ObsidianSurface
import com.rgs.companion.ui.theme.ThemeMode
import com.rgs.companion.voice.SpeechEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    speech: SpeechEngine? = null,
    memoryStore: MemoryStore? = null,
    onOpenAccessibility: (() -> Unit)? = null,
    onToggleOverlay: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeState = LocalCompanionThemeState.current
    val isDark = themeState.isDark

    var factCount by remember { mutableIntStateOf(-1) }
    LaunchedEffect(Unit) {
        factCount = runCatching { memoryStore?.allFacts()?.size ?: 0 }.getOrDefault(0)
    }

    val textPrimary = if (isDark) InkOn else DaylightInkOn
    val textSecondary = if (isDark) MutedOn else DaylightMutedOn
    val cardBg = if (isDark) Color(0xFF131318) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x1F161324)
    val accentColor = if (isDark) NeonCyan else DaylightTeal
    val secondaryAccent = if (isDark) NeonViolet else DaylightViolet

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        // ── Top Header ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textPrimary,
                )
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text = "SETTINGS & PREFERENCES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = textPrimary,
                )
                Text(
                    text = "Theme, voice synthesis & system preferences",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary,
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── THEME & INTERFACE SECTION (PRIMARY USER REQUEST) ─────
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, if (isDark) secondaryAccent.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("theme_settings_card"),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) NeonViolet.copy(alpha = 0.2f) else DaylightViolet.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                    contentDescription = "Theme Mode",
                                    tint = secondaryAccent,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "INTERFACE THEME",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = secondaryAccent,
                                )
                                Text(
                                    text = if (isDark) "Cyber Obsidian Dark" else "Daylight Silver Light",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textPrimary,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // ── Simple Toggle Row (Explicitly Requested) ──
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF1B1B22) else Color(0xFFF3F1FA),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        themeState.toggle()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isDark) "Dark Mode Enabled" else "Light Mode Enabled",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = textPrimary,
                                    )
                                    Text(
                                        text = if (isDark) "Deep obsidian surfaces with neon accents" else "Clean, high-contrast daylight aesthetic",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary,
                                    )
                                }
                                Switch(
                                    checked = isDark,
                                    onCheckedChange = { checked ->
                                        themeState.setDarkMode(checked)
                                    },
                                    modifier = Modifier.testTag("theme_toggle_switch"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = secondaryAccent,
                                        uncheckedThumbColor = DaylightTeal,
                                        uncheckedTrackColor = Color(0xFFD6D0E8),
                                    ),
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // ── Mode Selector Segmented Pills ─────────────
                        Text(
                            text = "PRESET SELECTION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                            ),
                            color = textSecondary,
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ThemeModePill(
                                title = "Dark",
                                icon = Icons.Filled.DarkMode,
                                isSelected = themeState.mode == ThemeMode.DARK,
                                accent = secondaryAccent,
                                textPrimary = textPrimary,
                                cardBorder = cardBorder,
                                modifier = Modifier.weight(1f),
                                onClick = { themeState.setMode(ThemeMode.DARK) },
                            )
                            ThemeModePill(
                                title = "Light",
                                icon = Icons.Filled.LightMode,
                                isSelected = themeState.mode == ThemeMode.LIGHT,
                                accent = accentColor,
                                textPrimary = textPrimary,
                                cardBorder = cardBorder,
                                modifier = Modifier.weight(1f),
                                onClick = { themeState.setMode(ThemeMode.LIGHT) },
                            )
                            ThemeModePill(
                                title = "System",
                                icon = Icons.Outlined.BrightnessAuto,
                                isSelected = themeState.mode == ThemeMode.SYSTEM,
                                accent = secondaryAccent,
                                textPrimary = textPrimary,
                                cardBorder = cardBorder,
                                modifier = Modifier.weight(1f),
                                onClick = { themeState.setMode(ThemeMode.SYSTEM) },
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // ── Live Palette Swatches Preview ─────────────
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF09090B) else Color(0xFFF7F6FA),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Active Palette",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        color = textSecondary,
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    PaletteCircle(if (isDark) ObsidianBg else DaylightBg, "Base", isDark)
                                    PaletteCircle(if (isDark) ObsidianSurface else DaylightSurface, "Surface", isDark)
                                    PaletteCircle(accentColor, "Primary", isDark)
                                    PaletteCircle(secondaryAccent, "Accent", isDark)
                                }
                            }
                        }
                    }
                }
            }

            // ── VOICE & SPEECH ENGINE SECTION ────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = "Voice",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "VOICE ENGINE & SYNTHESIS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = accentColor,
                                )
                                Text(
                                    text = "Neural TTS & Speech-to-Text",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textPrimary,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        SettingItemRow(
                            title = "Voice Synthesis (TTS)",
                            subtitle = if (speech?.isReady == true) "Engine initialized and active" else "Initializing speech engine...",
                            status = if (speech?.isReady == true) "READY" else "STARTING",
                            statusColor = if (speech?.isReady == true) Color(0xFF34D399) else Color(0xFFFFB84D),
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                        )

                        Spacer(Modifier.height(10.dp))

                        SettingItemRow(
                            title = "Speech Recognition (STT)",
                            subtitle = if (speech?.isRecognitionAvailable == true) "Google Speech Recognizer available" else "Using system speech dialog",
                            status = if (speech?.isRecognitionAvailable == true) "AVAILABLE" else "FALLBACK",
                            statusColor = if (speech?.isRecognitionAvailable == true) Color(0xFF34D399) else Color(0xFF60A5FA),
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                speech?.speak("Sab set hai. Zaiphra voice engine is operating smoothly.")
                                Toast.makeText(context, "Testing voice output...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(imageVector = Icons.Filled.VolumeUp, contentDescription = "Test Voice", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Test Voice Synthesis",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }

            // ── MEMORY & RECALL SECTION ──────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(secondaryAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Storage,
                                    contentDescription = "Memory",
                                    tint = secondaryAccent,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LONG-TERM MEMORY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = secondaryAccent,
                                )
                                Text(
                                    text = "Room-Backed Cognitive Vault",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textPrimary,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        SettingItemRow(
                            title = "Retained Facts & Profile",
                            subtitle = if (factCount < 0) "Reading memory records..." else "$factCount persistent facts remembered",
                            status = if (factCount < 0) "LOADING" else "$factCount FACTS",
                            statusColor = if (factCount > 0) secondaryAccent else textSecondary,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    runCatching { memoryStore?.clearAll() }
                                    factCount = 0
                                    Toast.makeText(context, "Companion memory cleared", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, cardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(imageVector = Icons.Outlined.CleaningServices, contentDescription = "Clear Memory", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Clear Saved Memory",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }

            // ── SYSTEM SUBSYSTEMS & OVERLAY ──────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Widgets,
                                    contentDescription = "System",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SYSTEM ENCLAVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = accentColor,
                                )
                                Text(
                                    text = "Overlays & Device Control",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textPrimary,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        val a11yActive = CompanionAccessibilityService.instance != null
                        val overlayActive = FloatingCompanionService.canDrawOverlays(context)

                        SettingItemRow(
                            title = "Accessibility Device Control",
                            subtitle = if (a11yActive) "Automations and navigation granted" else "Tap to open system settings",
                            status = if (a11yActive) "ACTIVE" else "NOT GRANTED",
                            statusColor = if (a11yActive) Color(0xFF34D399) else Color(0xFFFFB84D),
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = onOpenAccessibility,
                        )

                        Spacer(Modifier.height(10.dp))

                        SettingItemRow(
                            title = "Floating Companion Window",
                            subtitle = if (overlayActive) "Draw over other apps allowed" else "Tap to grant permission",
                            status = if (overlayActive) "PERMITTED" else "NEEDS PERMISSION",
                            statusColor = if (overlayActive) Color(0xFF34D399) else Color(0xFFFFB84D),
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = onToggleOverlay,
                        )

                        Spacer(Modifier.height(10.dp))

                        SettingItemRow(
                            title = "Neural Model",
                            subtitle = BuildConfig.LLM_MODEL,
                            status = if (BuildConfig.LLM_API_KEY.isNotBlank()) "CONFIGURED" else "STANDBY",
                            statusColor = if (BuildConfig.LLM_API_KEY.isNotBlank()) Color(0xFF34D399) else Color(0xFFFFB84D),
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeModePill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accent: Color,
    textPrimary: Color,
    cardBorder: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accent.copy(alpha = 0.2f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) accent else cardBorder),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("theme_pill_${title.lowercase()}"),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) accent else textPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                ),
                color = if (isSelected) accent else textPrimary,
            )
        }
    }
}

@Composable
private fun PaletteCircle(color: Color, name: String, isDark: Boolean) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.2f), CircleShape)
    )
}

@Composable
private fun SettingItemRow(
    title: String,
    subtitle: String,
    status: String,
    statusColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = textPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary,
            )
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = statusColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                ),
                color = statusColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}
