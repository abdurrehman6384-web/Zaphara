package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.RgsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionScreen(
    viewModel: RgsViewModel,
    onNavigateToChat: () -> Unit
) {
    val isScanning by viewModel.isVisionScanning.collectAsState()
    val ocrResult by viewModel.visionOcrResult.collectAsState()
    val flashlightOn by viewModel.flashlightEnabled.collectAsState()
    val telemetry by viewModel.deviceTelemetry.collectAsState()

    var activeScanMode by remember { mutableStateOf("CAMERA") }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(IrisVisionSky)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RGS SCREENPEELER HUD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFlashlight() }) {
                        Icon(
                            imageVector = if (flashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (flashlightOn) IrisAmber else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF040711))
            )
        },
        containerColor = Color(0xFF040711)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Viewfinder Container with AR Scanning Reticle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF060D1F))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(IrisVisionSky, IrisCyan.copy(alpha = 0.3f), IrisPurple)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                // Viewfinder HUD Canvas (Brackets & Crosshair)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val bracketLen = 30.dp.toPx()
                    val strokeW = 3.dp.toPx()
                    val bracketColor = if (isScanning) Color(0xFF00FF87) else Color(0xFF00F2FE)

                    // Top Left Bracket
                    drawLine(bracketColor, Offset(24f, 24f), Offset(24f + bracketLen, 24f), strokeW)
                    drawLine(bracketColor, Offset(24f, 24f), Offset(24f, 24f + bracketLen), strokeW)

                    // Top Right Bracket
                    drawLine(bracketColor, Offset(w - 24f, 24f), Offset(w - 24f - bracketLen, 24f), strokeW)
                    drawLine(bracketColor, Offset(w - 24f, 24f), Offset(w - 24f, 24f + bracketLen), strokeW)

                    // Bottom Left Bracket
                    drawLine(bracketColor, Offset(24f, h - 24f), Offset(24f + bracketLen, h - 24f), strokeW)
                    drawLine(bracketColor, Offset(24f, h - 24f), Offset(24f, h - 24f - bracketLen), strokeW)

                    // Bottom Right Bracket
                    drawLine(bracketColor, Offset(w - 24f, h - 24f), Offset(w - 24f - bracketLen, h - 24f), strokeW)
                    drawLine(bracketColor, Offset(w - 24f, h - 24f), Offset(w - 24f, h - 24f - bracketLen), strokeW)

                    // Center Crosshair Reticle
                    val cx = w / 2
                    val cy = h / 2
                    drawLine(bracketColor.copy(alpha = 0.4f), Offset(cx - 20f, cy), Offset(cx + 20f, cy), 2.dp.toPx())
                    drawLine(bracketColor.copy(alpha = 0.4f), Offset(cx, cy - 20f), Offset(cx, cy + 20f), 2.dp.toPx())

                    // Target Bounding Box
                    drawRect(
                        color = bracketColor.copy(alpha = if (isScanning) 0.35f else 0.15f),
                        topLeft = Offset(w * 0.2f, h * 0.25f),
                        size = Size(w * 0.6f, h * 0.5f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // Top Left Telemetry Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "FPS: 60 · ISO 400 · EXP 1/120s",
                        color = IrisVisionSky,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "RES: 1080P FHD (60FPS)",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Top Right Mode Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C152B))
                        .border(1.dp, IrisVisionSky.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isScanning) "SCANNING..." else "READY",
                        color = if (isScanning) IrisTerminalGreen else IrisVisionSky,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Bottom Zoom Controls
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF070C1B).copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    listOf(0.5f, 1.0f, 2.0f, 5.0f).forEach { z ->
                        Text(
                            text = "${z}x",
                            color = if (zoomLevel == z) IrisCyan else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (zoomLevel == z) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable { zoomLevel = z }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector Segmented Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("CAMERA", "Camera AR", Icons.Default.CameraAlt),
                    Triple("CODE", "Code OCR", Icons.Default.Code),
                    Triple("SCREEN", "ScreenPeeler", Icons.Default.CropFree)
                ).forEach { (modeKey, title, icon) ->
                    val isSelected = activeScanMode == modeKey
                    Surface(
                        onClick = { activeScanMode = modeKey },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) IrisVisionSky.copy(alpha = 0.2f) else Color(0xFF0C152B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) IrisVisionSky else GlassBorder
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) IrisVisionSky else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trigger Scan Button
            Button(
                onClick = { viewModel.triggerScreenPeelerScan(activeScanMode) },
                enabled = !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IrisVisionSky,
                    contentColor = Color(0xFF040711)
                )
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF040711),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ANALYZING MULTIMODAL CONTEXT...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Icon(imageVector = Icons.Default.Scanner, contentDescription = "Scan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXECUTE SCREENPEELER SCAN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // OCR & Multimodal Analysis Output Card
            if (ocrResult != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0C152B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IrisVisionSky.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = IrisTerminalGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MULTIMODAL RECOGNITION (99.6%)",
                                    color = IrisTerminalGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { viewModel.speakText(ocrResult ?: "") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = IrisVisionSky,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = ocrResult ?: "",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.sendMessage("Analyze this ScreenPeeler vision context: ${ocrResult}")
                                onNavigateToChat()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IrisCyan.copy(alpha = 0.15f),
                                contentColor = IrisCyan
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Send Context to RGS Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
