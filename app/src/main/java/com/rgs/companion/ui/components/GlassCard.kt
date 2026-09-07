package com.rgs.companion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rgs.companion.ui.theme.DaylightInkOn
import com.rgs.companion.ui.theme.DaylightMutedOn
import com.rgs.companion.ui.theme.InkOn
import com.rgs.companion.ui.theme.LocalCompanionThemeState
import com.rgs.companion.ui.theme.MutedOn
import com.rgs.companion.ui.theme.NeonCyan

/**
 * The app's glassmorphism building block.
 *
 * Design constraints:
 *  - dark: translucent white on obsidian base
 *  - light: elevated translucent white glass on soft daylight canvas
 *  - border: 1dp — translucent at rest, glowing accent when active
 *  - 16dp rounded corners, 16dp content padding
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    active: Boolean = false,
    accent: Color = NeonCyan,
    icon: ImageVector,
    title: String,
    subtitle: String,
    status: String? = null,
    statusColor: Color = MutedOn,
    onClick: () -> Unit,
) {
    val isDark = LocalCompanionThemeState.current.isDark
    val shape = RoundedCornerShape(16.dp)

    val border = if (active) {
        accent.copy(alpha = 0.55f)
    } else if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color(0xFF161324).copy(alpha = 0.12f)
    }

    val background = if (active) {
        accent.copy(alpha = if (isDark) 0.08f else 0.14f)
    } else if (isDark) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.90f)
    }

    val titleColor = if (isDark) InkOn else DaylightInkOn
    val subtitleColor = if (isDark) MutedOn else DaylightMutedOn
    val iconTint = if (active) accent else if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF161324).copy(alpha = 0.75f)

    Column(
        modifier
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = subtitleColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (status != null) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BoxDot(color = statusColor)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                    ),
                    color = statusColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Tiny status dot used by cards and chips. */
@Composable
fun BoxDot(
    color: Color,
    sizeDp: Int = 6,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(color),
    )
}
