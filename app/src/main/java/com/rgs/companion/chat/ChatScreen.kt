package com.rgs.companion.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.rgs.companion.avatar.AvatarSurface
import com.rgs.companion.ui.theme.MutedOn
import com.rgs.companion.ui.theme.NeonCyan
import com.rgs.companion.ui.theme.NeonViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    companionName: String = viewModel.personaName,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var draft by remember { mutableStateOf("") }

    // STT Intent Fallback Launcher
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()?.trim()
            if (!spoken.isNullOrEmpty()) {
                viewModel.onExternalVoiceResult(spoken)
            }
        }
    }

    // Microphone Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            if (viewModel.speech.isRecognitionAvailable) {
                viewModel.startListening()
            } else {
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to $companionName...")
                    }
                    speechIntentLauncher.launch(intent)
                } catch (_: Exception) {}
            }
        }
    }

    val onMicClick: () -> Unit = {
        if (state.listening) {
            viewModel.stopListening()
        } else {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPerm) {
                if (viewModel.speech.isRecognitionAvailable) {
                    viewModel.startListening()
                } else {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to $companionName...")
                        }
                        speechIntentLauncher.launch(intent)
                    } catch (_: Exception) {}
                }
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    LaunchedEffect(state.messages.size, state.thinking) {
        val target = state.messages.size + (if (state.thinking) 1 else 0)
        if (target > 0) listState.animateScrollToItem(target - 1)
    }

    Box(modifier = modifier.fillMaxSize()) {
        BackdropGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
        ) {
            HeroPanel(companionName, state, onAvatarTap = { viewModel.pokeAvatar() })
            SystemLine()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.messages.isEmpty() && state.greeting.isNotEmpty()) {
                    item {
                        GlassBubble(text = state.greeting, fromUser = false, italic = true)
                    }
                }
                items(state.messages, key = { it.id }) { message ->
                    GlassBubble(
                        text = message.text,
                        fromUser = message.role == "user",
                        isError = message.role == "error",
                        proactive = message.proactive,
                        animateTypewriter = message.animateTypewriter,
                        onTypewriterProgress = {
                            coroutineScope.launch {
                                val idx = state.messages.size - 1 + (if (state.thinking) 1 else 0)
                                if (idx >= 0) listState.scrollToItem(idx)
                            }
                        },
                    )
                }
                if (state.thinking) {
                    item { ThinkingBubble() }
                }
            }

            // Speech Error Notice
            AnimatedVisibility(
                visible = state.speechError != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                state.speechError?.let { err ->
                    SpeechErrorBanner(
                        message = err,
                        onDismiss = { viewModel.dismissSpeechError() },
                    )
                }
            }

            // Live Voice-to-Text Listening Visualizer & Waveform
            AnimatedVisibility(
                visible = state.listening,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                VoiceListeningPanel(
                    state = state,
                    onStop = { viewModel.stopListening() },
                    onCancel = { viewModel.cancelListening() },
                )
            }

            InputBar(
                draft = draft,
                onDraft = { draft = it },
                placeholder = if (state.listening) "Listening to you speak..." else "Say something to ${companionName}...",
                state = state,
                onMic = onMicClick,
                onSend = {
                    if (draft.isNotBlank()) {
                        viewModel.send(draft)
                        draft = ""
                    }
                },
            )
        }

        onBack?.let { back ->
            IconButton(
                onClick = back,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 8.dp, top = 8.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MutedOn)
            }
        }
    }
}

// ======================================================================
// backdrop
// ======================================================================
@Composable
private fun BackdropGlow() {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0A0F.toInt()),
                        Color(0xFF0E0B1A.toInt()),
                        Color(0xFF0A0A0F.toInt()),
                    ),
                ),
            ),
    )
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-90).dp)
                .size(360.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0x2E00E5FF), Color.Transparent))),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-160).dp, y = 140.dp)
                .size(440.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0x26B53DFF), Color.Transparent))),
        )
    }
}

// ======================================================================
// hero: the girl + nameplate
// ======================================================================
@Composable
private fun HeroPanel(
    name: String,
    state: ChatUiState,
    onAvatarTap: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(292.dp)
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x3300E5FF), Color(0x14B53DFF), Color.Transparent),
                    ),
                ),
        )
        AvatarSurface(
            modifier = Modifier.size(266.dp),
            emotion = state.emotion,
            mouthLevel = state.mouthLevel,
            thinking = state.thinking,
            listening = state.listening,
            onTap = onAvatarTap,
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(state)
            Spacer(Modifier.width(8.dp))
            Text(
                text = name.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFE8E3F5.toInt()),
                letterSpacing = 3.sp,
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0x4018181B),
                border = BorderStroke(1.dp, Color(0x5900E5FF)),
            ) {
                Text(
                    state.emotion.tag.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9FEFFF.toInt()),
                    letterSpacing = 1.5.sp,
                )
            }
        }
    }
}

/** LISTENING / THINKING / TALKING / IDLE with a pulsing dot. */
@Composable
private fun StatusPill(state: ChatUiState) {
    val (label, color) = when {
        state.listening -> "LISTENING" to NeonViolet
        state.thinking -> "THINKING" to Color(0xFFFFB84D.toInt())
        state.mouthLevel > 0.05f -> "TALKING" to NeonCyan
        else -> "IDLE" to Color(0xFF8A84A8.toInt())
    }
    val transition = rememberInfiniteTransition(label = "status_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_anim"
    )
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0x4018181B),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(start = 9.dp, end = 11.dp, top = 5.dp, bottom = 5.dp),
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

// ======================================================================
// system line
// ======================================================================
@Composable
private fun SystemLine() {
    val mono = MaterialTheme.typography.labelSmall.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        letterSpacing = 1.2.sp,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text("RGS-COMPANION", style = mono, color = NeonCyan.copy(alpha = 0.7f))
        Spacer(Modifier.weight(1f))
        Text("TTS · MEMORY · CANVAS AVATAR", style = mono, color = MutedOn.copy(alpha = 0.6f))
    }
}

// ======================================================================
// bubbles
// ======================================================================
@Composable
private fun GlassBubble(
    text: String,
    fromUser: Boolean,
    isError: Boolean = false,
    proactive: Boolean = false,
    italic: Boolean = false,
    animateTypewriter: Boolean = false,
    onTypewriterProgress: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (fromUser) 18.dp else 5.dp,
        bottomEnd = if (fromUser) 5.dp else 18.dp,
    )
    val bg: Brush = when {
        isError -> Brush.horizontalGradient(
            listOf(Color(0xE63A1520.toInt()), Color(0xE640202A.toInt())),
        )
        fromUser -> Brush.horizontalGradient(
            listOf(Color(0xE6241A47.toInt()), Color(0xE633206B.toInt())),
        )
        else -> Brush.horizontalGradient(
            listOf(Color(0xE6141420.toInt()), Color(0xE6191726.toInt())),
        )
    }
    val border = when {
        isError -> Color(0x88CB4B5B.toInt())
        fromUser -> Color(0x59B53DFF)
        else -> Color(0x3D00E5FF)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!fromUser) {
            Box(
                Modifier
                    .padding(bottom = 10.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonCyan, NeonCyan.copy(alpha = 0.5f))
                        )
                    ),
            )
            Spacer(Modifier.width(8.dp))
        }
        Box(
            Modifier
                .widthIn(max = 292.dp)
                .clip(shape)
                .background(bg)
                .border(1.dp, border, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Column {
                if (proactive) {
                    Text(
                        "she started this",
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = FontStyle.Italic,
                        color = MutedOn,
                    )
                }
                TypewriterText(
                    text = text,
                    enabled = animateTypewriter && !fromUser,
                    fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                    color = Color(0xFFE8E3F5.toInt()),
                    onProgress = onTypewriterProgress,
                )
            }
        }
    }
}

@Composable
private fun TypewriterText(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle = FontStyle.Normal,
    color: Color = Color(0xFFE8E3F5.toInt()),
    onProgress: (() -> Unit)? = null,
) {
    if (!enabled || text.isEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = fontStyle,
            color = color,
            modifier = modifier,
        )
        return
    }

    var charCount by remember(text) { mutableIntStateOf(0) }
    var isTyping by remember(text) { mutableStateOf(true) }

    LaunchedEffect(text) {
        charCount = 0
        isTyping = true
        val len = text.length
        while (charCount < len) {
            val step = if (len - charCount > 80) 3 else if (len - charCount > 25) 2 else 1
            charCount = (charCount + step).coerceAtMost(len)
            onProgress?.invoke()
            delay(18L)
        }
        isTyping = false
    }

    val transition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursor_alpha",
    )

    val displayedText = text.take(charCount)

    Text(
        text = buildAnnotatedString {
            append(displayedText)
            if (isTyping) {
                withStyle(style = SpanStyle(color = NeonCyan.copy(alpha = cursorAlpha))) {
                    append(" ▌")
                }
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        fontStyle = fontStyle,
        color = color,
        modifier = modifier.clickable {
            if (isTyping) {
                charCount = text.length
                isTyping = false
            }
        },
    )
}

@Composable
private fun ThinkingBubble() {
    val transition = rememberInfiniteTransition(label = "thinking_glow")

    val dotAlpha1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, delayMillis = 0, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot1",
    )
    val dotAlpha2 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot2",
    )
    val dotAlpha3 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot3",
    )

    val scanOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scanline",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            Modifier
                .padding(bottom = 12.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(NeonCyan, NeonCyan.copy(alpha = 0.4f))
                    )
                ),
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xE6141420.toInt()),
            border = BorderStroke(1.dp, Color(0x3D00E5FF)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = dotAlpha1)),
                    )
                    Spacer(Modifier.width(5.dp))
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NeonViolet.copy(alpha = dotAlpha2)),
                    )
                    Spacer(Modifier.width(5.dp))
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = dotAlpha3)),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "synthesizing neural response...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.4.sp,
                        ),
                        color = NeonCyan.copy(alpha = 0.85f),
                    )
                }
                Spacer(Modifier.height(6.dp))
                // Subtle high-tech energy scan line
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    NeonCyan.copy(alpha = 0.85f),
                                    NeonViolet.copy(alpha = 0.85f),
                                    Color.Transparent,
                                ),
                                startX = scanOffset * 300f - 100f,
                                endX = scanOffset * 300f + 100f,
                            )
                        )
                )
            }
        }
    }
}

// ======================================================================
// input & voice-to-text
// ======================================================================
@Composable
private fun SpeechErrorBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xF0201018),
        border = BorderStroke(1.dp, Color(0x66FF5252)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFFFFD1D1),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0x99FFD1D1),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun VoiceListeningPanel(
    state: ChatUiState,
    onStop: () -> Unit,
    onCancel: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "listening_pulse")
    val dotPulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_pulse",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xF2141124),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(NeonCyan.copy(alpha = 0.6f), NeonViolet.copy(alpha = 0.6f))
            )
        ),
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Pulsing recording indicator
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .scale(dotPulse)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3366)),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "VOICE INPUT ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = NeonCyan,
                )
                Spacer(Modifier.width(10.dp))
                // Live Soundwave equalizer bars
                AudioWaveformBars(
                    level = state.audioRmsLevel,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Cancel voice input",
                        tint = MutedOn,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Real-time transcribed text preview
            val liveText = state.partialTranscript.trim()
            if (liveText.isNotEmpty()) {
                Text(
                    text = "\"$liveText\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color(0xFFF3EFFF),
                    maxLines = 3,
                )
            } else {
                Text(
                    text = "Speak naturally in English, Hindi, or Hinglish...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = MutedOn.copy(alpha = 0.8f),
                    ),
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Tap mic when finished",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = MutedOn.copy(alpha = 0.65f),
                    ),
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0x3300E5FF),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable(onClick = onStop),
                ) {
                    Text(
                        text = if (liveText.isNotEmpty()) "SEND NOW" else "FINISH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioWaveformBars(
    level: Float,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Row(
        modifier = modifier.height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val barCount = 11
        for (i in 0 until barCount) {
            val sineVal = kotlin.math.abs(kotlin.math.sin(phase + i * 0.55f)).toFloat()
            val dynamicHeight = (4f + (level * 14f) + (sineVal * (3f + level * 6f))).coerceIn(3f, 18f)
            val isCenter = kotlin.math.abs(i - barCount / 2) <= 1
            val barColor = if (isCenter) NeonCyan else NeonViolet.copy(alpha = 0.85f)

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(dynamicHeight.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(barColor),
            )
        }
    }
}

@Composable
private fun InputBar(
    draft: String,
    onDraft: (String) -> Unit,
    placeholder: String,
    state: ChatUiState,
    onMic: () -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp)),
        ) {
            TextField(
                value = draft,
                onValueChange = onDraft,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = Color(0xFF6E6890.toInt())) },
                maxLines = 3,
                shape = RoundedCornerShape(23.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xE615141D.toInt()),
                    unfocusedContainerColor = Color(0xE615141D.toInt()),
                    disabledContainerColor = Color(0xE615141D.toInt()),
                    errorContainerColor = Color(0xE615141D.toInt()),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = NeonCyan,
                ),
            )
        }
        Spacer(Modifier.width(8.dp))
        // Reactive pulsing microphone button
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (state.listening) {
                val micTransition = rememberInfiniteTransition(label = "mic_pulse")
                val haloScale by micTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "halo_scale",
                )
                val haloAlpha by micTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "halo_alpha",
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(haloScale)
                        .clip(CircleShape)
                        .background(NeonViolet.copy(alpha = haloAlpha)),
                )
            }
            Surface(
                onClick = onMic,
                shape = CircleShape,
                color = if (state.listening) Color(0x33B53DFF) else Color(0x22181724),
                border = BorderStroke(
                    1.dp,
                    if (state.listening) NeonViolet else Color(0x33FFFFFF)
                ),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = if (state.listening) "Stop voice listening" else "Voice-to-text input",
                        tint = if (state.listening) NeonViolet else NeonCyan.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(NeonCyan, NeonViolet)))
                .clickable(onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Send,
                contentDescription = "Send",
                tint = Color(0xFF081014.toInt()),
            )
        }
    }
}
