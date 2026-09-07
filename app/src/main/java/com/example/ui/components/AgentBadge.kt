package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AgentType
import com.example.ui.theme.*

@Composable
fun AgentBadge(
    agentType: AgentType,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val color = when (agentType) {
        AgentType.RGS_CORE, AgentType.IRIS_CORE -> IrisCyan
        AgentType.RGS_VISION, AgentType.IRIS_VISION -> IrisVisionSky
        AgentType.RGS_TERMINAL, AgentType.IRIS_TERMINAL -> IrisTerminalGreen
        AgentType.RGS_TELEKINESIS, AgentType.IRIS_TELEKINESIS -> IrisAmber
        AgentType.RGS_CYBERSEC, AgentType.IRIS_CYBERSEC -> IrisMagenta
        else -> IrisCyan
    }

    val icon = when (agentType) {
        AgentType.RGS_CORE, AgentType.IRIS_CORE -> Icons.Default.RadioButtonChecked
        AgentType.RGS_VISION, AgentType.IRIS_VISION -> Icons.Default.CropFree
        AgentType.RGS_TERMINAL, AgentType.IRIS_TERMINAL -> Icons.Default.Terminal
        AgentType.RGS_TELEKINESIS, AgentType.IRIS_TELEKINESIS -> Icons.Default.PhonelinkSetup
        AgentType.RGS_CYBERSEC, AgentType.IRIS_CYBERSEC -> Icons.Default.Security
        else -> Icons.Default.SmartToy
    }

    val modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(color.copy(alpha = if (isSelected) 0.22f else 0.08f))
        .border(
            width = if (isSelected) 1.5.dp else 0.8.dp,
            color = if (isSelected) color else color.copy(alpha = 0.3f),
            shape = RoundedCornerShape(20.dp)
        )
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(horizontal = 12.dp, vertical = 6.dp)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = icon,
            contentDescription = agentType.title,
            tint = color,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = agentType.title,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
