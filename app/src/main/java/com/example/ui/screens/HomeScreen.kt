package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.rgs.live2d.Live2DView
import com.rgs.live2d.Live2DController
import com.example.data.models.AgentType
import com.example.data.models.IrisVoiceState
import com.example.data.models.LLMProvider
import com.example.ui.components.IrisOrbReactor
import com.example.ui.components.ModelSelectorBottomSheet
import com.example.ui.theme.*
import com.example.ui.viewmodel.RgsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RgsViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToVision: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.irisVoiceState.collectAsState()
    val audioWaves by viewModel.audioWaveLevels.collectAsState()
    val isWakeWordActive by viewModel.isWakeWordEnabled.collectAsState()
    val selectedModel by viewModel.selectedModelEndpoint.collectAsState()
    val isModelSheetOpen by viewModel.isModelSelectorSheetOpen.collectAsState()
    val isTtsEnabled by viewModel.isTtsSpeakingEnabled.collectAsState()
    val telemetry by viewModel.deviceTelemetry.collectAsState()
    val isHwidGranted by viewModel.isHwidAccessGranted.collectAsState()
    val actionMessage by viewModel.actionOutputMessage.collectAsState()

    var promptInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val currentTime = remember {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date())
    }

    if (isModelSheetOpen) {
        ModelSelectorBottomSheet(
            selectedProvider = selectedModel,
            onProviderSelected = {
                viewModel.selectModelEndpoint(it)
                viewModel.setModelSelectorSheetVisible(false)
            },
            onDismissRequest = { viewModel.setModelSelectorSheetVisible(false) }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 90.dp)
        ) {
            // Status Bar & Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(IrisCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RGS AI · v1.2.0 (VOICE ENGINE)",
                        color = IrisCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // HWID Status Lock Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isHwidGranted) IrisTerminalGreen.copy(alpha = 0.15f) else IrisRedAlert.copy(alpha = 0.2f))
                            .border(1.dp, if (isHwidGranted) IrisTerminalGreen.copy(alpha = 0.5f) else IrisRedAlert, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isHwidGranted) "HWID: SECURE" else "HWID: LOCKED",
                            color = if (isHwidGranted) IrisTerminalGreen else IrisRedAlert,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // TTS Audio Toggle
                    IconButton(
                        onClick = { viewModel.toggleTtsSpeaking() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "TTS Voice",
                            tint = if (isTtsEnabled) IrisVisionSky else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Brand Title & Tagline
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RGS",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "·AI",
                        color = IrisCyan,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IrisPurple.copy(alpha = 0.3f))
                            .border(1.dp, IrisPurple, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VOICE FIRST",
                            color = IrisMagenta,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = "Autonomous Desktop & Mobile AI Assistant & System Operator",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Output Notification Toast
            AnimatedVisibility(visible = actionMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0C152B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IrisCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Action",
                                tint = IrisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = actionMessage ?: "",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearActionMessage() },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hero IRIS Orb Reactor & Live2D
            var live2DEnabled by remember { mutableStateOf(false) }

            if (live2DEnabled) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { context ->
                            Live2DView(context).also { view ->
                                view.loadModel("live2d/aria", "aria.model3.json")
                                val controller = Live2DController(view)
                                // Switch back to Orb on tap
                                view.onBodyTap = { live2DEnabled = false }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                IrisOrbReactor(
                    voiceState = voiceState,
                    audioWaveLevels = audioWaves,
                    isListening = voiceState == IrisVoiceState.LISTENING,
                    onOrbClick = {
                        live2DEnabled = true // Toggle Live2D on tap
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wake Word "Hey RGS" Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF080F22),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Wake Word",
                            tint = if (isWakeWordActive) IrisTerminalGreen else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Wake-Word: 'Hey RGS'",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isWakeWordActive) "Listening for hands-free command" else "Wake-word paused",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.triggerWakeWord() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IrisCyan.copy(alpha = 0.2f),
                            contentColor = IrisCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(text = "Trigger", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-Model Routing Selector Strip
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUTONOMOUS MODEL ENDPOINT",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Change",
                        color = IrisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.setModelSelectorSheetVisible(true) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LLMProvider.entries.forEach { provider ->
                        val isSelected = selectedModel == provider
                        val accent = when (provider) {
                            LLMProvider.GLM_4_6 -> IrisPurple
                            LLMProvider.GEMINI -> IrisCyan
                            LLMProvider.GROQ -> IrisAmber
                            LLMProvider.MISTRAL -> IrisMagenta
                        }

                        Surface(
                            onClick = { viewModel.selectModelEndpoint(provider) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) accent.copy(alpha = 0.2f) else Color(0xFF090F20),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) accent else GlassBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = provider.shortName,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Tool Launchpad Matrix
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "RGS AI TOOLS SUITE",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Vision Tool Card
                    Surface(
                        onClick = onNavigateToVision,
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF090F20),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IrisVisionSky.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CropFree,
                                    contentDescription = "ScreenPeeler",
                                    tint = IrisVisionSky,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "ScreenPeeler", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "AR Vision & OCR Scanner", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    // Terminal Tool Card
                    Surface(
                        onClick = {
                            viewModel.selectAgent(AgentType.RGS_TERMINAL)
                            onNavigateToChat()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF090F20),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IrisTerminalGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "Terminal",
                                    tint = IrisTerminalGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "RGS-Zero CLI", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Shell & Code Sandbox", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mobile Telekinesis Card
                    Surface(
                        onClick = onNavigateToTools,
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF090F20),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IrisAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhonelinkSetup,
                                    contentDescription = "Telekinesis",
                                    tint = IrisAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Telekinesis", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Device & ADB Operator", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    // Live Web Intelligence Card
                    Surface(
                        onClick = {
                            viewModel.sendMessage("Perform deep live web search on: latest autonomous AI systems")
                            onNavigateToChat()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF090F20),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IrisPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TravelExplore,
                                    contentDescription = "Web",
                                    tint = IrisPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Web Grounding", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Live Search & Scraper", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // System Telemetry Strip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF080F22),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DEVICE TELEMETRY & HARDWARE BIND",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "CPU", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "${telemetry.cpuUsagePercent}%", color = IrisCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "RAM CACHE", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "${telemetry.ramUsedMb} MB", color = IrisTerminalGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "LATENCY", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "${telemetry.networkLatencyMs} ms", color = IrisAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "HWID BIND", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(text = telemetry.hwid.take(8) + "…", color = IrisMagenta, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Floating Bottom Command Bar
        Surface(
            color = Color(0xFF070B18).copy(alpha = 0.95f),
            tonalElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = {
                        Text(
                            text = "Command RGS AI or speak…",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                IconButton(
                    onClick = onNavigateToVision,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Vision",
                        tint = IrisVisionSky,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            viewModel.sendMessage(promptInput)
                            promptInput = ""
                            onNavigateToChat()
                        } else {
                            val nextState = voiceState != IrisVoiceState.LISTENING
                            viewModel.toggleVoiceListening(nextState)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IrisCyan, IrisPurple)
                            )
                        )
                ) {
                    Icon(
                        imageVector = if (promptInput.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                        contentDescription = "Send",
                        tint = Color(0xFF040711),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
