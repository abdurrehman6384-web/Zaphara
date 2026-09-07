package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AgentType
import com.example.ui.components.AgentBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.RgsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    viewModel: RgsViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToVision: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAgent by viewModel.selectedAgent.collectAsState()
    val telemetry by viewModel.deviceTelemetry.collectAsState()
    val actionOutputMessage by viewModel.actionOutputMessage.collectAsState()
    val flashlightOn by viewModel.flashlightEnabled.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()

    var terminalInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RGS AI TOOLS & AUTOMATION",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Zero-CLI Shell & Hardware Control Matrix",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(IrisAmber.copy(alpha = 0.2f))
                    .border(1.dp, IrisAmber, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "AUTOMATION",
                    color = IrisAmber,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Status Toast
        AnimatedVisibility(visible = actionOutputMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = Color(0xFF0C152B),
                border = androidx.compose.foundation.BorderStroke(1.dp, IrisTerminalGreen)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "OK", tint = IrisTerminalGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = actionOutputMessage ?: "",
                            color = IrisTerminalGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    TextButton(onClick = { viewModel.clearActionMessage() }, contentPadding = PaddingValues(0.dp)) {
                        Text("Dismiss", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Mobile Telekinesis Hardware Matrix
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF080F22),
            border = androidx.compose.foundation.BorderStroke(1.dp, IrisAmber.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PhonelinkSetup, contentDescription = "Hardware", tint = IrisAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MOBILE TELEKINESIS CONTROLS",
                            color = IrisAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(text = "LIVE", color = IrisTerminalGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Flashlight Toggle
                    TelekinesisActionButton(
                        icon = if (flashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        label = if (flashlightOn) "Flash ON" else "Flashlight",
                        isActive = flashlightOn,
                        accentColor = IrisAmber,
                        onClick = { viewModel.toggleFlashlight() },
                        modifier = Modifier.weight(1f)
                    )

                    // RAM Optimizer
                    TelekinesisActionButton(
                        icon = Icons.Default.Memory,
                        label = "RAM Boost",
                        isActive = false,
                        accentColor = IrisCyan,
                        onClick = { viewModel.runRamBooster() },
                        modifier = Modifier.weight(1f)
                    )

                    // Battery Health
                    TelekinesisActionButton(
                        icon = Icons.Default.BatteryChargingFull,
                        label = "Battery",
                        isActive = false,
                        accentColor = IrisTerminalGreen,
                        onClick = { viewModel.runBatteryDiagnostic() },
                        modifier = Modifier.weight(1f)
                    )

                    // Network Latency Ping
                    TelekinesisActionButton(
                        icon = Icons.Default.NetworkCheck,
                        label = "Ping Test",
                        isActive = false,
                        accentColor = IrisPurple,
                        onClick = { viewModel.runPingTest() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. IRIS-Zero Terminal Interactive Sandbox
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF02040A),
            border = androidx.compose.foundation.BorderStroke(1.dp, IrisTerminalGreen.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IrisTerminalGreen))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "IRIS-ZERO CLI SANDBOX",
                            color = IrisTerminalGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "bash 5.2",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Terminal Log Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF040814))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    val logsScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(logsScroll)
                    ) {
                        terminalLogs.forEach { logItem ->
                            Text(
                                text = "$ ${logItem.command}",
                                color = IrisCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = logItem.output,
                                color = if (logItem.output.contains("SUCCESS") || logItem.output.contains("GRANTED") || logItem.output.contains("ONLINE")) IrisTerminalGreen else Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Terminal Command Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "rgs@zero:~$ ",
                        color = IrisTerminalGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = terminalInput,
                        onValueChange = { terminalInput = it },
                        placeholder = { Text("help, ping, mem, hwid...", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (terminalInput.isNotBlank()) {
                                val cmd = terminalInput
                                terminalInput = ""
                                viewModel.executeTerminal(cmd)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = IrisTerminalGreen, modifier = Modifier.size(18.dp))
                    }
                }

                // Quick Command Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("help", "status", "hwid", "mem").forEach { cmd ->
                        Surface(
                            onClick = { viewModel.executeTerminal(cmd) },
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0C152B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Text(
                                text = cmd,
                                color = IrisCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. RGS Autonomous Specialist Agents Deck
        Text(
            text = "RGS SPECIALIST MATRIX",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        listOf(
            AgentType.RGS_CORE,
            AgentType.RGS_VISION,
            AgentType.RGS_TERMINAL,
            AgentType.RGS_TELEKINESIS,
            AgentType.RGS_CYBERSEC
        ).forEach { agent ->
            IrisAgentDeckCard(
                agent = agent,
                isActive = agent == activeAgent,
                onSelect = {
                    viewModel.selectAgent(agent)
                    if (agent == AgentType.RGS_VISION || agent == AgentType.IRIS_VISION) {
                        onNavigateToVision()
                    } else {
                        onNavigateToChat()
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun TelekinesisActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) accentColor.copy(alpha = 0.25f) else Color(0xFF0C152B),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) accentColor else GlassBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) accentColor else Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (isActive) accentColor else Color.LightGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun IrisAgentDeckCard(
    agent: AgentType,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    val accent = when (agent) {
        AgentType.IRIS_CORE -> IrisCyan
        AgentType.IRIS_VISION -> IrisVisionSky
        AgentType.IRIS_TERMINAL -> IrisTerminalGreen
        AgentType.IRIS_TELEKINESIS -> IrisAmber
        AgentType.IRIS_CYBERSEC -> IrisMagenta
        else -> IrisCyan
    }

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) accent.copy(alpha = 0.12f) else Color(0xFF080F22),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) accent else GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (agent) {
                            AgentType.IRIS_CORE -> Icons.Default.RadioButtonChecked
                            AgentType.IRIS_VISION -> Icons.Default.CropFree
                            AgentType.IRIS_TERMINAL -> Icons.Default.Terminal
                            AgentType.IRIS_TELEKINESIS -> Icons.Default.PhonelinkSetup
                            AgentType.IRIS_CYBERSEC -> Icons.Default.Security
                            else -> Icons.Default.SmartToy
                        },
                        contentDescription = agent.title,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = agent.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = agent.primaryRole, color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accent
                ) {
                    Text(
                        text = "ENGAGED",
                        color = Color(0xFF040711),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}
