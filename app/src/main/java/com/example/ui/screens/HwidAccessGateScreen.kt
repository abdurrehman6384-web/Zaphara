package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.RgsViewModel

@Composable
fun HwidAccessGateScreen(
    viewModel: RgsViewModel,
    onAccessGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isGranted by viewModel.isHwidAccessGranted.collectAsState()
    val verificationMsg by viewModel.hwidVerificationMessage.collectAsState()
    val auditLogs by viewModel.hwidAuditLogs.collectAsState()
    val isGenerating by viewModel.isGeneratingResponse.collectAsState()
    val telemetry by viewModel.deviceTelemetry.collectAsState()

    var inputKey by remember { mutableStateOf("") }

    LaunchedEffect(verificationMsg) {
        verificationMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearHwidVerificationMessage()
        }
    }

    LaunchedEffect(isGranted) {
        if (isGranted) {
            onAccessGranted()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Security Terminal Badge
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(IrisMagenta.copy(alpha = 0.15f))
                        .border(1.5.dp, IrisMagenta, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "HWID Gate",
                        tint = IrisMagenta,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "RGS AI CYBERSEC GATEWAY",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = "Cryptographic HWID License Verification & Supabase Vault",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Gated Status Banner
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isGranted) IrisTerminalGreen.copy(alpha = 0.15f) else IrisRedAlert.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isGranted) IrisTerminalGreen.copy(alpha = 0.5f) else IrisRedAlert.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.Shield else Icons.Default.Lock,
                            contentDescription = "Status",
                            tint = if (isGranted) IrisTerminalGreen else IrisRedAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGranted) "ENCLAVE UNLOCKED · HWID VALIDATED" else "ENCLAVE LOCKED · ACCESS GATED",
                            color = if (isGranted) IrisTerminalGreen else IrisRedAlert,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Generated Unique HWID Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF080F22),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IrisMagenta.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DEVICE HARDWARE IDENTIFIER (HWID)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("HWID", viewModel.deviceHwid)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "HWID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy HWID",
                                    tint = IrisMagenta,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF040814),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Text(
                                text = viewModel.deviceHwid,
                                color = IrisCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "SHA-256 Cryptographic Fingerprint:",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                        Text(
                            text = viewModel.deviceFingerprintSha256,
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Device Model:", color = Color.Gray, fontSize = 11.sp)
                            Text(text = telemetry.deviceModel, color = Color.White, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "System OS:", color = Color.Gray, fontSize = 11.sp)
                            Text(text = telemetry.androidVersion, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Key Verification Form
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF080F22),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IrisCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ENTER RGS AI LICENSE KEY",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bind device HWID to activate FREE, PRO, or VIP GOD MODE tier.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            placeholder = { Text("e.g. RGS-VIP-GOD-MODE-9999", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IrisCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "1-Tap Quick Unlock Keys:",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { inputKey = "RGS-FREE-DEV-2026" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text("FREE", fontSize = 10.sp, color = StarterTierColor, fontFamily = FontFamily.Monospace)
                            }
                            OutlinedButton(
                                onClick = { inputKey = "RGS-PRO-VIP-2026" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text("PRO", fontSize = 10.sp, color = ProTierColor, fontFamily = FontFamily.Monospace)
                            }
                            OutlinedButton(
                                onClick = { inputKey = "RGS-VIP-GOD-MODE-9999" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text("GOD MODE", fontSize = 10.sp, color = GodModeTierColor, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.verifyHwidSecurityGate(inputKey) },
                            colors = ButtonDefaults.buttonColors(containerColor = IrisCyan, contentColor = Color(0xFF040711)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color(0xFF040711), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying HWID Enclave...", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            } else {
                                Icon(imageVector = Icons.Default.Key, contentDescription = "Verify", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verify HWID & Activate RGS AI", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Master Administrator Bypass Button
                        TextButton(
                            onClick = { viewModel.unlockHwidGateDirectly() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Bypass", tint = IrisMagenta, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RGS Administrator Master Bypass (GOD MODE)", color = IrisMagenta, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Security Audit Trail
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENCLAVE AUDIT TRAIL",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "SHA-256 VERIFIED",
                        color = IrisTerminalGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            items(auditLogs) { log ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF080F22),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (log.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = log.event,
                            tint = if (log.isSuccess) IrisTerminalGreen else IrisRedAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.event,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.timestamp,
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = log.details,
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
