package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.IrisVoiceState
import com.example.ui.theme.*

@Composable
fun IrisOrbReactor(
    voiceState: IrisVoiceState,
    audioWaveLevels: List<Float>,
    isListening: Boolean,
    onOrbClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "iris_orb_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = if (voiceState == IrisVoiceState.LISTENING || voiceState == IrisVoiceState.SPEAKING) 1.14f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (voiceState == IrisVoiceState.LISTENING) 600 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    val primaryAccent = when (voiceState) {
        IrisVoiceState.IDLE -> IrisCyan
        IrisVoiceState.LISTENING -> IrisTerminalGreen
        IrisVoiceState.THINKING -> IrisNeonViolet
        IrisVoiceState.SPEAKING -> IrisVisionSky
        IrisVoiceState.EXECUTING -> IrisAmber
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Orb Core
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(220.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOrbClick
                )
        ) {
            // Background Diffuse Glow
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(pulseScale * 1.15f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = 0.5f),
                                primaryAccent.copy(alpha = 0.25f),
                                IrisPurple.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Outer Orbit Arc Rings
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .rotate(rotationAngle)
            ) {
                drawCircle(
                    color = primaryAccent.copy(alpha = 0.35f),
                    radius = size.minDimension / 2 - 8.dp.toPx(),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(30f, 25f, 60f, 30f),
                            0f
                        )
                    )
                )
            }

            Canvas(
                modifier = Modifier
                    .size(170.dp)
                    .rotate(counterRotationAngle)
            ) {
                drawCircle(
                    color = IrisMagenta.copy(alpha = 0.4f),
                    radius = size.minDimension / 2 - 12.dp.toPx(),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(15f, 20f, 40f, 15f),
                            0f
                        )
                    )
                )
            }

            // Core 3D Holographic Sphere
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                primaryAccent,
                                IrisPurple,
                                Color(0xFF040711)
                            ),
                            center = Offset(70f, 70f),
                            radius = 200f
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                primaryAccent,
                                IrisMagenta,
                                IrisTerminalGreen,
                                primaryAccent
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                // Central Icon / Status Symbol
                Icon(
                    imageVector = if (voiceState == IrisVoiceState.LISTENING) Icons.Default.GraphicEq else Icons.Default.Mic,
                    contentDescription = "IRIS Orb",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic Frequency Waveform Visualizer
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(28.dp)
                .fillMaxWidth(0.7f)
        ) {
            audioWaveLevels.forEachIndexed { index, level ->
                val waveHeight = if (voiceState == IrisVoiceState.LISTENING || voiceState == IrisVoiceState.SPEAKING) {
                    (level * 24.dp.value).coerceIn(4f, 26f).dp
                } else {
                    4.dp
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(3.dp)
                        .height(waveHeight)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    primaryAccent,
                                    IrisPurple
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Voice Status Display Pill
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF0C152B))
                .border(1.dp, primaryAccent.copy(alpha = 0.5f), CircleShape)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = voiceState.statusText,
                color = primaryAccent,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
        }
    }
}
