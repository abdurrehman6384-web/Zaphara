package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.AccessibilityHelper
import com.example.automation.InGameHudService
import com.example.automation.ZaiphraAccessibilityService
import com.example.skills.FreeFireStateBus
import com.example.skills.FreeFireMasterAgent
import com.example.automation.ScreenAnalyzer
import com.example.speech.TacticalVoiceEngine
import kotlinx.coroutines.launch

@Composable
fun TacticalDashboardScreen(
    tacticalVoice: TacticalVoiceEngine,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionState by FreeFireStateBus.state.collectAsState()
    
    var isAccessibilityActive by remember {
        mutableStateOf(AccessibilityHelper.isAccessibilityServiceEnabled(context, ZaiphraAccessibilityService::class.java))
    }
    var isOverlayActive by remember {
        mutableStateOf(AccessibilityHelper.canDrawOverlays(context))
    }
    var isHudRunning by remember {
        mutableStateOf(InGameHudService.isRunning)
    }

    val modes = listOf(
        "Ranked Push" to "Compound dominance, safe rotation, team trade priority",
        "Clutch" to "1vN isolation trees, slide cancel drop-shots, anti-tilt",
        "Safe Ranked" to "Stealth navigation, edge-of-zone positioning, passive macro",
        "Aggressive Hotdrop" to "Rapid loot acquisition, gloo wall rush, fast aim reset",
        "Adaptive" to "Real-time playstyle shift (Passive vs Aggressive based on zone)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "FREE FIRE TACTICAL SUITE",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Zaiphra Elite Master Automation Engine",
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Permission & Engine Status
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF18181B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SYSTEM & PERMISSION STATE",
                            color = Color(0xFF818CF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Accessibility Switch row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Accessibility Automation", color = Color.White, fontSize = 13.sp)
                                Text(
                                    if (isAccessibilityActive) "Active & Bound" else "Permission Required",
                                    color = if (isAccessibilityActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = {
                                    AccessibilityHelper.openAccessibilitySettings(context)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAccessibilityActive) Color(0xFF27272A) else Color(0xFF6366F1)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isAccessibilityActive) "Check" else "Enable", fontSize = 11.sp)
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))

                        // In-Game Floating HUD Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("In-Game Floating HUD", color = Color.White, fontSize = 13.sp)
                                Text(
                                    if (isOverlayActive) (if (isHudRunning) "HUD Live on Screen" else "Permission Ready") else "Overlay Perm Required",
                                    color = if (isOverlayActive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = {
                                    if (!isOverlayActive) {
                                        AccessibilityHelper.openOverlaySettings(context)
                                    } else {
                                        if (isHudRunning) {
                                            InGameHudService.stop(context)
                                            isHudRunning = false
                                        } else {
                                            InGameHudService.start(context)
                                            isHudRunning = true
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHudRunning) Color(0xFFDC2626) else Color(0xFF00E5FF)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (!isOverlayActive) "Grant" else if (isHudRunning) "Stop HUD" else "Start HUD",
                                    fontSize = 11.sp,
                                    color = if (isHudRunning) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Live Telemetry & Anti-Ban Monitor
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF18181B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE TELEMETRY & HUMANIZATION",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ANTI-BAN ACTIVE", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("STATUS", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    if (sessionState.isRunning) "RUNNING" else "STANDBY",
                                    color = if (sessionState.isRunning) Color(0xFF10B981) else Color(0xFFA1A1AA),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("THREAT LVL", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("${sessionState.threatLevel}%", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("APM", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("${sessionState.actionsPerMinute}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("HUMAN BEZIER", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("ENABLED", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Last Action: ${sessionState.lastAction}",
                            color = Color(0xFFA5B4FC),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = sessionState.statusSummary,
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Tactical Modes Selection
            item {
                Text(
                    text = "SELECT OPERATIONAL MODE",
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            items(modes.size) { index ->
                val (modeName, modeDesc) = modes[index]
                val isSelected = sessionState.mode == modeName

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF27272A) else Color(0xFF18181B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                FreeFireStateBus.update {
                                    it.copy(mode = modeName, statusSummary = "Switched to $modeName")
                                }
                                tacticalVoice.speak("$modeName mode select ho gaya hai.")
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF00E5FF) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = modeName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = modeDesc,
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.sp
                            )
                        }
                        if (isSelected && sessionState.isRunning) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Start / Stop Main Control Buttons
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val service = ZaiphraAccessibilityService.instance
                                if (service != null) {
                                    val agent = FreeFireMasterAgent(
                                        accessibilityService = service,
                                        screenAnalyzer = ScreenAnalyzer(),
                                        tacticalVoice = tacticalVoice
                                    )
                                    agent.startGame(sessionState.mode)
                                } else {
                                    tacticalVoice.speak("Accessibility permission enable nahi hai. Pehle permission do.")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("START BOT", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            FreeFireStateBus.update {
                                it.copy(isRunning = false, lastAction = "STOPPED", statusSummary = "Manually stopped")
                            }
                            tacticalVoice.speakTacticalCallout("stop")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("STOP BOT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
