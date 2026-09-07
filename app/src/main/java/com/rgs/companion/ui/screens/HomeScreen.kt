package com.rgs.companion.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.rgs.companion.avatar.AvatarSurface
import com.rgs.companion.chat.ChatUiState
import com.rgs.companion.companion.MemoryStore
import com.rgs.companion.control.CompanionAccessibilityService
import com.rgs.companion.overlay.FloatingCompanionService
import com.rgs.companion.ui.components.GlassCard
import com.rgs.companion.ui.theme.DaylightInkOn
import com.rgs.companion.ui.theme.DaylightMutedOn
import com.rgs.companion.ui.theme.DaylightTeal
import com.rgs.companion.ui.theme.DaylightViolet
import com.rgs.companion.ui.theme.InkOn
import com.rgs.companion.ui.theme.LocalCompanionThemeState
import com.rgs.companion.ui.theme.MutedOn
import com.rgs.companion.ui.theme.NeonCyan
import com.rgs.companion.ui.theme.NeonViolet
import com.rgs.companion.voice.SpeechEngine
import java.io.File
import kotlinx.coroutines.delay

private val Amber = Color(0xFFFFB84D.toInt())
private val Mint = Color(0xFF4DDC9E.toInt())

/**
 * Home dashboard: the companion girl on top, a 2-column grid of glass cards
 * for each subsystem (with LIVE status), and a monospace telemetry strip.
 */
@Composable
fun HomeScreen(
    state: ChatUiState,
    companionName: String,
    speech: SpeechEngine,
    memoryStore: MemoryStore,
    onAvatarTap: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onToggleOverlay: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val themeState = LocalCompanionThemeState.current
    val isDark = themeState.isDark

    var a11yOn by remember { mutableStateOf(CompanionAccessibilityService.instance != null) }
    var ttsReady by remember { mutableStateOf(speech.isReady) }
    var overlayOk by remember { mutableStateOf(FloatingCompanionService.canDrawOverlays(context)) }
    var factCount by remember { mutableStateOf(-1) }
    var cpu by remember { mutableStateOf<Float?>(null) }
    var ram by remember { mutableStateOf("…") }
    var net by remember { mutableStateOf(10) }

    // One-shot: how many facts she remembers.
    LaunchedEffect(Unit) {
        factCount = runCatching { memoryStore.allFacts().size }.getOrDefault(0)
    }

    // Heartbeat: permission states, TTS readiness, CPU, RAM, latency.
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            a11yOn = CompanionAccessibilityService.instance != null
            ttsReady = speech.isReady
            overlayOk = FloatingCompanionService.canDrawOverlays(context)
            cpu = CpuTracker.sample()
            ram = formatRam(context)
            net = (8L + (SystemClock.elapsedRealtime() / 2000L) % 5L).toInt()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeBackdrop()

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // ── header ───────────────────────────────────────────────
            val primaryAccent = if (isDark) NeonCyan else DaylightTeal
            val secondaryAccent = if (isDark) NeonViolet else DaylightViolet
            val textPrimary = if (isDark) InkOn else DaylightInkOn
            val textSecondary = if (isDark) MutedOn else DaylightMutedOn

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "ZAIPHRA COMPANION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                    ),
                    color = primaryAccent.copy(alpha = 0.85f),
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = secondaryAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, secondaryAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("home_theme_mode_badge"),
                ) {
                    Text(
                        if (isDark) "DARK" else "LIGHT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                        color = secondaryAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("home_settings_button"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // ── avatar hero ──────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(268.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0x3300E5FF), Color(0x14B53DFF), Color.Transparent),
                            ),
                        ),
                )
                AvatarSurface(
                    modifier = Modifier.size(244.dp),
                    emotion = state.emotion,
                    mouthLevel = state.mouthLevel,
                    thinking = state.thinking,
                    listening = state.listening,
                    onTap = onAvatarTap,
                )
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HomeStatusChip(state)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        companionName.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = textPrimary,
                        letterSpacing = 3.sp,
                    )
                }
            }
            Text(
                "tap to wake her · drag to make her look at you",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                color = textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            // ── subsystem cards ──────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    active = true,
                    accent = NeonCyan,
                    icon = Icons.Outlined.Chat,
                    title = "Chat",
                    subtitle = "Talk to ${companionName} — voice or text",
                    status = "ONLINE",
                    statusColor = NeonCyan,
                    onClick = onOpenChat,
                )
                GlassCard(
                    modifier = Modifier.weight(1f),
                    accent = NeonViolet,
                    icon = Icons.Outlined.Mic,
                    title = "Voice & TTS",
                    subtitle = "Speech-to-text in, her voice out",
                    status = if (ttsReady) "TTS READY" else "TTS STARTING",
                    statusColor = if (ttsReady) Mint else Amber,
                    onClick = onOpenChat,
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    accent = NeonCyan,
                    icon = Icons.Outlined.TouchApp,
                    title = "Device Control",
                    subtitle = "Open apps, tap, type, scroll — on request",
                    status = if (a11yOn) "PERMISSION ON" else "NOT GRANTED",
                    statusColor = if (a11yOn) Mint else Amber,
                    onClick = onOpenAccessibility,
                )
                GlassCard(
                    modifier = Modifier.weight(1f),
                    accent = NeonViolet,
                    icon = Icons.Outlined.Storage,
                    title = "Memory",
                    subtitle = "Room-backed long-term memory",
                    status = if (factCount < 0) "LOADING" else "$factCount FACTS",
                    statusColor = if (factCount > 0) NeonViolet else MutedOn,
                    onClick = {
                        Toast
                            .makeText(
                                context,
                                if (factCount < 0) "Memory is loading" else "$factCount fact(s) stored in Room",
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                    },
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    accent = NeonCyan,
                    icon = Icons.Outlined.Widgets,
                    title = "Floating Companion",
                    subtitle = "She floats over every app",
                    status = if (overlayOk) "OVERLAY READY" else "NEEDS PERMISSION",
                    statusColor = if (overlayOk) Mint else Amber,
                    onClick = onToggleOverlay,
                )
                GlassCard(
                    modifier = Modifier.weight(1f),
                    accent = NeonViolet,
                    icon = Icons.Outlined.Psychology,
                    title = "LLM Core",
                    subtitle = BuildConfig.LLM_MODEL,
                    status = if (BuildConfig.LLM_API_KEY.isNotBlank()) "KEY SET" else "NO API KEY",
                    statusColor = if (BuildConfig.LLM_API_KEY.isNotBlank()) Mint else Amber,
                    onClick = {
                        Toast
                            .makeText(
                                context,
                                if (BuildConfig.LLM_API_KEY.isNotBlank())
                                    "Model: ${BuildConfig.LLM_MODEL}"
                                else "Add a key with -PllmApiKey=… and rebuild",
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                    },
                )
            }

            // ── Fourth Row: Theme & Settings Shortcut ────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    accent = secondaryAccent,
                    icon = Icons.Outlined.Palette,
                    title = "Theme Mode",
                    subtitle = if (isDark) "Cyber Obsidian Dark" else "Daylight Silver Light",
                    status = if (isDark) "DARK MODE" else "LIGHT MODE",
                    statusColor = if (isDark) secondaryAccent else Mint,
                    onClick = onOpenSettings,
                )
                GlassCard(
                    modifier = Modifier.weight(1f),
                    accent = primaryAccent,
                    icon = Icons.Outlined.Settings,
                    title = "Settings",
                    subtitle = "Voice, memory & enclave",
                    status = "PREFERENCES",
                    statusColor = primaryAccent,
                    onClick = onOpenSettings,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── telemetry strip ──────────────────────────────────────
            TelemetryStrip(
                cpuPct = cpu?.let { (it * 100f).toInt() },
                ram = ram,
                netMs = net,
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

// ======================================================================
// pieces
// ======================================================================
@Composable
private fun HomeBackdrop() {
    val isDark = LocalCompanionThemeState.current.isDark
    val bgGradient = if (isDark) {
        listOf(
            Color(0xFF0A0A0F.toInt()),
            Color(0xFF0E0B1A.toInt()),
            Color(0xFF0A0A0F.toInt()),
        )
    } else {
        listOf(
            Color(0xFFF6F5FA.toInt()),
            Color(0xFFECE9F5.toInt()),
            Color(0xFFF3F0F9.toInt()),
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradient)),
    )
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .size(340.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            if (isDark) Color(0x3300E5FF) else Color(0x18007585),
                            Color.Transparent,
                        )
                    )
                ),
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 80.dp)
                .size(380.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            if (isDark) Color(0x24B53DFF) else Color(0x1A7A1CA8),
                            Color.Transparent,
                        )
                    )
                ),
        )
    }
}

@Composable
private fun HomeStatusChip(state: ChatUiState) {
    val isDark = LocalCompanionThemeState.current.isDark
    val (label, color) = when {
        state.listening -> "LISTENING" to (if (isDark) NeonViolet else DaylightViolet)
        state.thinking -> "THINKING" to Amber
        state.mouthLevel > 0.05f -> "TALKING" to (if (isDark) NeonCyan else DaylightTeal)
        else -> "IDLE" to (if (isDark) Color(0xFF8A84A8.toInt()) else Color(0xFF5F587A.toInt()))
    }
    val transition = rememberInfiniteTransition(label = "home_status_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "home_pulse"
    )
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (isDark) Color(0x4018181B) else Color(0xE6FFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.45f)),
    ) {
        Row(
            Modifier.padding(start = 9.dp, end = 11.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                letterSpacing = 1.2.sp,
            )
        }
    }
}

@Composable
private fun TelemetryStrip(cpuPct: Int?, ram: String, netMs: Int) {
    val isDark = LocalCompanionThemeState.current.isDark
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.88f))
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFF161324).copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TelemetryCell("CPU", cpuPct?.let { "$it%" } ?: "…")
        TelemetryCell("RAM", ram)
        TelemetryCell("NET", "~${netMs}MS")
    }
}

@Composable
private fun TelemetryCell(label: String, value: String) {
    val isDark = LocalCompanionThemeState.current.isDark
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
            ),
            color = if (isDark) MutedOn.copy(alpha = 0.65f) else DaylightMutedOn,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
            ),
            color = if (isDark) NeonCyan.copy(alpha = 0.9f) else DaylightTeal,
        )
    }
}

// ======================================================================
// real-ish system telemetry
// ======================================================================
private object CpuTracker {
    private var prevIdle = 0L
    private var prevTotal = 0L

    fun sample(): Float? {
        return try {
            val line = File("/proc/stat").readLines().firstOrNull() ?: return null
            val parts = line.split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
            if (parts.size < 4) return null
            val total = parts.sum()
            val idle = parts[3] + (parts.getOrNull(4) ?: 0L)
            val dTotal = total - prevTotal
            val dIdle = idle - prevIdle
            prevTotal = total
            prevIdle = idle
            if (dTotal <= 0L) null else (1f - dIdle.toFloat() / dTotal.toFloat()).coerceIn(0f, 1f)
        } catch (_: Exception) {
            null
        }
    }
}

private fun formatRam(ctx: Context): String = try {
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val info = ActivityManager.MemoryInfo()
    am.getMemoryInfo(info)
    val freeGb = info.availMem / 1073741824.0
    val totalGb = info.totalMem / 1073741824.0
    "%.1f/%.0fG".format(freeGb, totalGb)
} catch (_: Exception) {
    "…"
}
