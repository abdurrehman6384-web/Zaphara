package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.LLMProvider
import com.example.ui.theme.*

/**
 * High-fidelity Model Selection UI Component.
 * Allows switching between Gemini, Groq, Mistral, and GLM-4.6,
 * displaying real-time Retrofit HTTP request headers, active endpoint URLs,
 * latency indicators, and quick ping diagnostic testing.
 */
@Composable
fun ModelSelectorCard(
    selectedProvider: LLMProvider,
    onProviderSelected: (LLMProvider) -> Unit,
    activeHeaders: Map<String, String> = emptyMap(),
    onTestConnection: (LLMProvider) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isHeadersExpanded by remember { mutableStateOf(false) }

    val activeAccentColor = when (selectedProvider) {
        LLMProvider.GEMINI -> RgsCyan
        LLMProvider.GROQ -> RgsAmber
        LLMProvider.MISTRAL -> RgsPurple
        LLMProvider.GLM_4_6 -> RgsCyanGlow
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("model_selector_card"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF080E1E),
        border = BorderStroke(1.dp, activeAccentColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title, Active Badge & Live Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(activeAccentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedProvider) {
                                LLMProvider.GEMINI -> Icons.Default.AutoAwesome
                                LLMProvider.GROQ -> Icons.Default.ElectricBolt
                                LLMProvider.MISTRAL -> Icons.Default.Code
                                LLMProvider.GLM_4_6 -> Icons.Default.Memory
                            },
                            contentDescription = selectedProvider.displayName,
                            tint = activeAccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "AI MODEL ENGINE",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = selectedProvider.displayName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = RgsTerminalGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, RgsTerminalGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(RgsTerminalGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RETROFIT SYNCED",
                            color = RgsTerminalGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Model Selection Grid / Chips (Gemini, Groq, Mistral, GLM-4.6)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    LLMProvider.GEMINI,
                    LLMProvider.GROQ,
                    LLMProvider.MISTRAL,
                    LLMProvider.GLM_4_6
                ).forEach { provider ->
                    val isSelected = provider == selectedProvider
                    val chipAccent = when (provider) {
                        LLMProvider.GEMINI -> RgsCyan
                        LLMProvider.GROQ -> RgsAmber
                        LLMProvider.MISTRAL -> RgsPurple
                        LLMProvider.GLM_4_6 -> RgsCyanGlow
                    }

                    val animBorder by animateColorAsState(
                        targetValue = if (isSelected) chipAccent else GlassBorder,
                        animationSpec = spring(),
                        label = "border_anim"
                    )

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onProviderSelected(provider) }
                            .testTag("model_chip_${provider.id}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) chipAccent.copy(alpha = 0.18f) else Color(0xFF0C152B),
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, animBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (provider) {
                                    LLMProvider.GEMINI -> Icons.Default.AutoAwesome
                                    LLMProvider.GROQ -> Icons.Default.ElectricBolt
                                    LLMProvider.MISTRAL -> Icons.Default.Code
                                    LLMProvider.GLM_4_6 -> Icons.Default.Memory
                                },
                                contentDescription = provider.shortName,
                                tint = if (isSelected) chipAccent else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = provider.shortName,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "~${provider.latencyMs}ms",
                                color = if (isSelected) chipAccent else Color.DarkGray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Model Details & Specs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF040711),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE ENDPOINT:",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedProvider.defaultModel,
                            color = activeAccentColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = selectedProvider.endpointUrl,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Retrofit Headers Toggle Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isHeadersExpanded = !isHeadersExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Http,
                        contentDescription = "HTTP Headers",
                        tint = activeAccentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Retrofit Service Headers",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isHeadersExpanded) "HIDE" else "INSPECT",
                        color = activeAccentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (isHeadersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = activeAccentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expanded Retrofit HTTP Headers Viewer
            AnimatedVisibility(
                visible = isHeadersExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF040711))
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "// Outgoing Retrofit Interceptor Headers",
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (activeHeaders.isNotEmpty()) {
                        activeHeaders.forEach { (k, v) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$k:",
                                    color = IrisCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = v,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        // Default representation for selected provider
                        val defaultHeaders = when (selectedProvider) {
                            LLMProvider.GEMINI -> listOf(
                                "x-goog-api-key" to "•••••••• (Gemini API Key)",
                                "Content-Type" to "application/json",
                                "Accept" to "application/json",
                                "User-Agent" to "RGS-AI-Retrofit/3.1"
                            )
                            LLMProvider.GROQ -> listOf(
                                "Authorization" to "Bearer gsk_•••••••• (Groq Auth)",
                                "Content-Type" to "application/json",
                                "Accept" to "application/json",
                                "User-Agent" to "RGS-AI-Retrofit/3.1"
                            )
                            LLMProvider.MISTRAL -> listOf(
                                "Authorization" to "Bearer mistral_•••••••• (Mistral Auth)",
                                "Content-Type" to "application/json",
                                "Accept" to "application/json",
                                "User-Agent" to "RGS-AI-Retrofit/3.1"
                            )
                            LLMProvider.GLM_4_6 -> listOf(
                                "Authorization" to "Bearer zai_••••••••",
                                "Content-Type" to "application/json",
                                "Accept" to "application/json"
                            )
                        }

                        defaultHeaders.forEach { (k, v) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$k:",
                                    color = activeAccentColor,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = v,
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { onTestConnection(selectedProvider) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RgsTerminalGreen),
                        border = BorderStroke(1.dp, RgsTerminalGreen.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = "Test", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ping Active Endpoint Header", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

/**
 * Compact Segmented Selector for quick model toggling in headers, chats, and toolbars.
 */
@Composable
fun ModelSelectorSegmentedBar(
    selectedProvider: LLMProvider,
    onProviderSelected: (LLMProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("model_selector_segmented_bar"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0C152B),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                LLMProvider.GEMINI,
                LLMProvider.GROQ,
                LLMProvider.MISTRAL
            ).forEach { provider ->
                val isSelected = provider == selectedProvider
                val accent = when (provider) {
                    LLMProvider.GEMINI -> RgsCyan
                    LLMProvider.GROQ -> RgsAmber
                    LLMProvider.MISTRAL -> RgsPurple
                    LLMProvider.GLM_4_6 -> RgsCyanGlow
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onProviderSelected(provider) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) accent.copy(alpha = 0.2f) else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, accent) else null
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) accent else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = provider.shortName,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
