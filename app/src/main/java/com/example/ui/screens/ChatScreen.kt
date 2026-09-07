package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.models.AgentType
import com.example.data.models.WorkflowItem
import com.example.data.models.WorkflowLibrary
import com.example.ui.components.AgentBadge
import com.example.ui.components.CodeBlockView
import com.example.ui.components.GlassCard
import com.example.ui.components.ModelSelectorBottomSheet
import com.example.ui.theme.*
import com.example.ui.viewmodel.RgsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: RgsViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.currentMessages.collectAsState()
    val activeAgent by viewModel.selectedAgent.collectAsState()
    val isGenerating by viewModel.isGeneratingResponse.collectAsState()
    val isTtsEnabled by viewModel.isTtsSpeakingEnabled.collectAsState()
    val selectedModelEndpoint by viewModel.selectedModelEndpoint.collectAsState()
    val isModelSheetOpen by viewModel.isModelSelectorSheetOpen.collectAsState()

    var showWorkflowSheet by remember { mutableStateOf(false) }
    var attachedDocumentName by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (isModelSheetOpen) {
        ModelSelectorBottomSheet(
            selectedProvider = selectedModelEndpoint,
            onProviderSelected = {
                viewModel.selectModelEndpoint(it)
                viewModel.setModelSelectorSheetVisible(false)
            },
            onDismissRequest = { viewModel.setModelSelectorSheetVisible(false) }
        )
    }

    val suggestionPrompts = remember(activeAgent) {
        when (activeAgent) {
            AgentType.RGS_CORE, AgentType.IRIS_CORE, AgentType.AGENT_MANAGER -> listOf(
                "Break down an autonomous multi-step execution plan",
                "Synthesize voice context and coordinate all RGS agents",
                "Inspect device status and trigger security audit"
            )
            AgentType.RGS_VISION, AgentType.IRIS_VISION -> listOf(
                "Trigger ScreenPeeler camera scan on current frame",
                "Extract code coordinates and OCR typography from screen",
                "Analyze UI layout components and generate Compose code"
            )
            AgentType.RGS_TERMINAL, AgentType.IRIS_TERMINAL, AgentType.AI_CODER -> listOf(
                "Execute sandboxed Python script on RGS-Zero CLI",
                "Write Kotlin Compose component with neon cyber styling",
                "Generate shell script to scaffold autonomous AI server"
            )
            AgentType.RGS_TELEKINESIS, AgentType.IRIS_TELEKINESIS, AgentType.ACTION_MANAGER -> listOf(
                "Toggle hardware flashlight and run battery diagnostic",
                "Optimize device RAM cache and tune CPU performance",
                "Measure network ping latency across all LLM endpoints"
            )
            AgentType.RGS_CYBERSEC, AgentType.IRIS_CYBERSEC, AgentType.API_MANAGER -> listOf(
                "Verify HWID cryptographic signature against Supabase vault",
                "Test Gemini 3.1 Live / GLM-4.6 / Groq fallback rotation",
                "Inspect anti-tamper security policies and client logs"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
    ) {
        // Drop-in Avatar Header
        com.example.avatar.CompanionAvatarHeader(
            moodTag = activeAgent?.name
        )

        // Top Header
        Surface(
            color = Color(0xFF080F22),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RGS AI",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TERMINAL",
                                color = IrisCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "Model: ${selectedModelEndpoint.displayName}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Switch Model Button
                        Surface(
                            onClick = { viewModel.setModelSelectorSheetVisible(true) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0C152B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(IrisCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedModelEndpoint.shortName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // TTS Voice Toggle
                        IconButton(
                            onClick = { viewModel.toggleTtsSpeaking() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "TTS",
                                tint = if (isTtsEnabled) IrisCyan else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Workflows Modal
                        IconButton(
                            onClick = { showWorkflowSheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Workflows",
                                tint = IrisMagenta,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // New Chat
                        IconButton(
                            onClick = { viewModel.createNewChatSession() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Specialist Switcher
                Text(
                    text = "RGS SPECIALIST MATRIX:",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(
                        AgentType.RGS_CORE,
                        AgentType.RGS_VISION,
                        AgentType.RGS_TERMINAL,
                        AgentType.RGS_TELEKINESIS,
                        AgentType.RGS_CYBERSEC
                    )) { agent ->
                        AgentBadge(
                            agentType = agent,
                            isSelected = agent == activeAgent,
                            onClick = { viewModel.selectAgent(agent) }
                        )
                    }
                }
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(message = msg, onSpeak = { viewModel.speakText(msg.content) })
            }

            if (isGenerating) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0C152B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IrisCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = IrisCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${activeAgent.title} is executing intent via ${selectedModelEndpoint.shortName}...",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Attached Vision File Indicator
        AnimatedVisibility(visible = attachedDocumentName != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF08182D),
                border = androidx.compose.foundation.BorderStroke(1.dp, IrisVisionSky),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CropFree,
                            contentDescription = "ScreenPeeler",
                            tint = IrisVisionSky,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ScreenPeeler Vision Attached: ${attachedDocumentName ?: ""}",
                            color = IrisVisionSky,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = { attachedDocumentName = null },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Clear", color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
        }

        // Quick Suggestion Prompts
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(suggestionPrompts) { suggestion ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF080F22),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.clickable { inputText = suggestion }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Prompt",
                            tint = IrisCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = suggestion,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Bottom Input Floating Bar
        Surface(
            color = Color(0xFF070B18),
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach ScreenPeeler Viewfinder Frame
                IconButton(
                    onClick = {
                        attachedDocumentName = "screenpeeler_frame_${(100..999).random()}.png"
                        viewModel.selectAgent(AgentType.IRIS_VISION)
                        inputText = "ScreenPeeler: Analyze text and code from camera viewfinder frame."
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CropFree,
                        contentDescription = "ScreenPeeler",
                        tint = IrisVisionSky,
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Command ${activeAgent.title}...",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF040711),
                        unfocusedContainerColor = Color(0xFF040711),
                        focusedBorderColor = IrisCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(6.dp))

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val textToSend = inputText
                            inputText = ""
                            attachedDocumentName = null
                            focusManager.clearFocus()
                            viewModel.sendMessage(textToSend)
                        }
                    },
                    containerColor = IrisCyan,
                    contentColor = Color(0xFF040711),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Workflows Library Sheet
    if (showWorkflowSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWorkflowSheet = false },
            containerColor = Color(0xFF040711)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "RGS AI WORKFLOW CHAINS",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Tap to trigger autonomous multi-agent pipeline execution.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 380.dp)
                ) {
                    items(WorkflowLibrary.workflows) { wf ->
                        Surface(
                            onClick = {
                                showWorkflowSheet = false
                                viewModel.executeWorkflow(wf)
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF080F22),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = wf.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = wf.badgeColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = wf.category,
                                            color = wf.badgeColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = wf.description,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    onSpeak: () -> Unit = {}
) {
    val isUser = message.sender == "USER"
    val agentType = AgentType.fromId(message.agentType)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (isUser) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = IrisCyan.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, IrisCyan.copy(alpha = 0.5f)),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "You",
                        color = IrisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF080F22),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AgentBadge(agentType = agentType)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.modelUsed,
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak Text",
                                    tint = IrisCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message.content,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    // Render code block if present
                    if (!message.codeSnippet.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        CodeBlockView(
                            code = message.codeSnippet,
                            language = message.codeLanguage ?: "kotlin"
                        )
                    }
                }
            }
        }
    }
}
