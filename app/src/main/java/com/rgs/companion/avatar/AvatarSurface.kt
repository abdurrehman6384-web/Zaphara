package com.rgs.companion.avatar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.rgs.companion.companion.Emotion

/**
 * Compose wrapper for [AvatarView] — the cute animated companion girl.
 *
 * The `update` block only writes cheap volatile fields; the view animates
 * itself at the display's refresh rate without ever triggering recomposition
 * of the chat screen.
 *
 * ```
 * AvatarSurface(
 *     modifier = Modifier.size(266.dp),
 *     emotion = state.emotion,
 *     mouthLevel = state.mouthLevel,
 *     thinking = state.thinking,
 *     listening = state.listening,
 *     onTap = { viewModel.pokeAvatar() },
 * )
 * ```
 */
@Composable
fun AvatarSurface(
    modifier: Modifier = Modifier,
    emotion: Emotion = Emotion.NEUTRAL,
    mouthLevel: Float = 0f,
    thinking: Boolean = false,
    listening: Boolean = false,
    onTap: () -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx -> AvatarView(ctx) },
        update = { view ->
            view.emotion = emotion
            view.mouthLevel = mouthLevel
            view.thinking = thinking
            view.listening = listening
            view.onTap = onTap
        },
    )
}
