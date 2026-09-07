package com.rgs.companion.avatar

import com.rgs.companion.companion.Emotion

/**
 * All the knobs an emotion turns for the avatar's face.
 *
 * The [Emotion] enum (owned by the companion/agent layer) maps to exactly one
 * [FaceState] via [forEmotion]; [GirlPainter] only ever sees numbers, so the
 * painter never needs to know anything about the LLM's tags.
 *
 * Ranges:
 *  - [browLift]  0..1   how high the brows sit
 *  - [browTilt]  -1..1  negative = inner ends down (angry), positive = up (worried/sad)
 *  - [eyeOpen]   0.4..1.2  base openness (multiplied by the blink envelope)
 *  - [squint]    0..1   happy-narrow: lower lid curves up, eye content shrinks
 *  - [iris]      0.8..1.15  iris size scale (< 1 reads as wide-eyed/surprised)
 *  - [smile]     -1..1  -1 deep frown .. +1 full grin (also drives the "U" curve)
 *  - [mouthBase] 0..1  resting mouth open amount (before lip-sync is added)
 *  - [blush]     0..1  cheek colour intensity
 *  - [tiltRad]   head tilt in radians (positive = leaning right)
 */
data class FaceState(
    val browLift: Float,
    val browTilt: Float,
    val eyeOpen: Float,
    val squint: Float,
    val iris: Float,
    val smile: Float,
    val mouthBase: Float,
    val blush: Float,
    val tiltRad: Float,
) {
    companion object {
        val NEUTRAL = FaceState(0.5f, 0f, 1f, 0f, 1f, 0.35f, 0f, 0.3f, 0f)

        fun forEmotion(e: Emotion): FaceState = when (e) {
            Emotion.NEUTRAL -> NEUTRAL
            Emotion.HAPPY -> FaceState(0.7f, 0.15f, 0.95f, 0.45f, 1.05f, 1f, 0.22f, 0.6f, -0.02f)
            Emotion.AFFECTIONATE -> FaceState(0.6f, 0.1f, 0.9f, 0.35f, 1.12f, 0.8f, 0.08f, 1f, 0.03f)
            Emotion.SHY -> FaceState(0.55f, 0.35f, 0.85f, 0.2f, 0.95f, 0.3f, 0f, 1f, 0.05f)
            Emotion.PLAYFUL -> FaceState(0.8f, -0.1f, 1f, 0.3f, 1.1f, 0.9f, 0.18f, 0.5f, -0.04f)
            Emotion.SAD -> FaceState(0.35f, 0.8f, 0.8f, 0.1f, 0.85f, -0.6f, 0f, 0.1f, 0f)
            Emotion.WORRIED -> FaceState(0.75f, 0.8f, 0.95f, 0.15f, 1f, -0.35f, 0.04f, 0.2f, 0.02f)
            Emotion.ANGRY -> FaceState(0.25f, -1f, 0.8f, 0.35f, 0.9f, -0.8f, 0.04f, 0.15f, 0f)
            Emotion.SURPRISED -> FaceState(1f, 0f, 1.15f, 0f, 0.8f, 0f, 0.3f, 0.3f, -0.03f)
            Emotion.SLEEPY -> FaceState(0.3f, 0.1f, 0.5f, 0.1f, 0.9f, 0.15f, 0.08f, 0.4f, 0.06f)
        }
    }
}

/**
 * One frame's worth of inputs for [GirlPainter].
 *
 * A mutable class on purpose: [AvatarView] reuses the same instance every
 * frame, which keeps the 60-120fps loop allocation-free.
 */
class RenderState {
    var tMs: Long = 0L
    var emotion: Emotion = Emotion.NEUTRAL
    var face: FaceState = FaceState.NEUTRAL
    /** Blink envelope, 0 = eyes open, 1 = lids closed. */
    var lidClose: Float = 0f
    /** Gaze direction in u-units (already eased by [AvatarView]). */
    var gazeX: Float = 0f
    var gazeY: Float = 0f
    /** 0..1, from the TTS lip-sync envelope. */
    var mouthOpen: Float = 0f
    var thinking: Boolean = false
    var listening: Boolean = false
    /** Tap bounce offset in u-units (negative = up). */
    var bounceY: Float = 0f
    /** Pop hearts, e.g. after a tap or during affectionate moments. */
    val hearts = ArrayList<HeartFx>()
}

/** A small heart that rises and fades. [p] advances 0..1 over ~950ms. */
class HeartFx(
    var x: Float,
    var y: Float,
    var p: Float,
    val hue: Int,
)
