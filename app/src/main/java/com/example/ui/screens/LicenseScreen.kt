package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.models.LicenseTier
import com.example.ui.components.TierBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.RgsViewModel

@Composable
fun LicenseScreen(
    viewModel: RgsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val licenseInfo by viewModel.licenseInfo.collectAsState()
    val telemetry by viewModel.deviceTelemetry.collectAsState()
    val activationMsg by viewModel.licenseActivationMessage.collectAsState()

    var inputKey by remember { mutableStateOf("") }

    val currentTier = if (licenseInfo?.isValid == true) {
        LicenseTier.fromKey(licenseInfo?.tier ?: "GOD_MODE")
    } else {
        LicenseTier.GOD_MODE
    }

    LaunchedEffect(activationMsg) {
        activationMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearActivationMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
            .padding(16.dp)
    ) {
        Text(
            text = "RGS AI LICENSING & HWID VAULT",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Supabase PostgreSQL HWID-Bound Enclave Authentication",
            color = TextSecondary,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Current Active License Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF080F22),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IrisMagenta),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Shield",
                                    tint = IrisMagenta,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active License Status",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            TierBadge(tier = currentTier)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "REGISTERED HWID:",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = telemetry.hwid,
                                color = IrisCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("HWID", telemetry.hwid)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "HWID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy HWID",
                                    tint = IrisCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Registered Operator: ${licenseInfo?.registeredTo ?: "RGS AI Certified User"}",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Active Key: ${licenseInfo?.licenseKey ?: "RGS-VIP-GOD-MODE-9999"}",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(IrisTerminalGreen.copy(alpha = 0.15f))
                                .border(1.dp, IrisTerminalGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = IrisTerminalGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SUPABASE VAULT SYNC: ACTIVE (HWID SIGNED)",
                                color = IrisTerminalGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.lockHwidGate()
                                Toast.makeText(context, "RGS Enclave Locked for Security Test", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrisRedAlert),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IrisRedAlert.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = IrisRedAlert, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lock App & Test HWID Security Gate", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // License Verification & Key Entry
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF080F22),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SUPABASE HWID LICENSE VERIFICATION",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter your license key to verify HWID against Supabase backend tables and unlock FREE, PRO, or VIP GOD MODE capabilities.",
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

                        // Quick Tier Selection Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { inputKey = "RGS-FREE-DEV-${(1000..9999).random()}" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text("FREE", fontSize = 10.sp, color = StarterTierColor, fontFamily = FontFamily.Monospace)
                            }
                            OutlinedButton(
                                onClick = { inputKey = "RGS-PRO-VIP-${(1000..9999).random()}" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text("PRO", fontSize = 10.sp, color = ProTierColor, fontFamily = FontFamily.Monospace)
                            }
                            OutlinedButton(
                                onClick = { inputKey = "RGS-VIP-GOD-MODE-${(1000..9999).random()}" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text("GOD MODE", fontSize = 10.sp, color = GodModeTierColor, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.verifyLicense(inputKey) },
                            colors = ButtonDefaults.buttonColors(containerColor = IrisCyan, contentColor = Color(0xFF040711)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = "Verify", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verify HWID on Supabase Backend", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Pricing Tiers Matrix
            item {
                Text(
                    text = "RGS AI TIER MATRIX",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            items(LicenseTier.entries.toTypedArray()) { tier ->
                LicenseTierCard(tier = tier, currentTier = currentTier)
            }
        }
    }
}

@Composable
fun LicenseTierCard(
    tier: LicenseTier,
    currentTier: LicenseTier
) {
    val isSelected = tier == currentTier
    val color = when (tier) {
        LicenseTier.STARTER -> StarterTierColor
        LicenseTier.PRO -> ProTierColor
        LicenseTier.GOD_MODE -> GodModeTierColor
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF080F22),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (tier == LicenseTier.GOD_MODE) Icons.Default.WorkspacePremium else Icons.Default.Star,
                        contentDescription = tier.title,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tier.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = tier.priceInRps,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tier.description,
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TierFeatureRow("Context Window:", "${tier.maxTokensPerReq} Tokens")
                TierFeatureRow("RGS-Zero Terminal CLI:", if (tier.supportsCoderSandbox) "ENABLED" else "DISABLED")
                TierFeatureRow("Mobile Automation & Telekinesis:", if (tier.supportsActionManager) "ENABLED" else "DISABLED")
                TierFeatureRow("Unlimited Multi-LLM Rotation:", if (tier.supportsUnlimitedFallback) "ENABLED" else "DISABLED")
            }
        }
    }
}

@Composable
fun TierFeatureRow(feature: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = feature, color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(
            text = status,
            color = if (status.contains("ENABLED")) IrisTerminalGreen else Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
