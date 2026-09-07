package com.rgs.companion.companion

/**
 * The companion avatar's emotion states.
 *
 * Each emotion drives facial features, gaze tendencies, and procedural
 * animations in the canvas renderer.
 */
enum class Emotion(val tag: String, val expressionName: String = tag) {
    NEUTRAL("neutral", "Neutral"),
    HAPPY("happy", "Smile"),
    AFFECTIONATE("affectionate", "Smile"),
    SHY("shy", "Blush"),
    PLAYFUL("playful", "Wink"),
    SAD("sad", "Sad"),
    WORRIED("worried", "Worried"),
    ANGRY("angry", "Angry"),
    SURPRISED("surprised", "Surprised"),
    SLEEPY("sleepy", "Sleepy");

    companion object {
        fun fromTag(tag: String?): Emotion {
            val t = tag?.lowercase()?.trim().orEmpty()
            return entries.firstOrNull { it.tag == t || it.name.lowercase() == t } ?: NEUTRAL
        }
    }
}
