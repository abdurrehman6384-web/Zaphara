package com.rgs.companion.avatar

import android.content.Context
import android.graphics.Canvas
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.rgs.companion.companion.Emotion
import kotlin.random.Random

/**
 * The companion girl, as a self-animating [View].
 *
 * This is the dependency-free alternative to `live2d.Live2DView`: the whole
 * character is painted procedurally by [GirlPainter], so it works without the
 * licence-gated Cubism SDK, on any device, with any model.
 *
 * ## Why a plain View instead of a Compose Canvas
 * She animates continuously (breathing, blinking, hair sway, aura) at the
 * display's refresh rate. Driving that through Compose state would recompose
 * the whole chat screen 60-120 times per second. A `View` that simply
 * `postInvalidateOnAnimation()`s itself every frame costs nothing else, and it
 * mirrors the `Live2DView` (GLSurfaceView) architecture this codebase already
 * uses, so swapping between the two renderers is a one-line change.
 *
 * ## Public API
 * All setters are plain `@Volatile` fields read on the next frame — safe to
 * call from the UI thread or a coroutine, exactly like `Live2DView`:
 *
 * ```
 * avatar.emotion = Emotion.HAPPY
 * avatar.mouthLevel = 0.7f          // TTS lip-sync
 * avatar.thinking = true
 * avatar.onTap = { viewModel.pokeAvatar() }
 * ```
 *
 * Touch: dragging makes her eyes (and a little of her head) follow your
 * finger; a tap triggers a bounce, pop hearts, and [onTap].
 */
class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    @Volatile var emotion: Emotion = Emotion.NEUTRAL
    @Volatile var mouthLevel: Float = 0f
    @Volatile var thinking: Boolean = false
    @Volatile var listening: Boolean = false

    /** Fired on a tap that was not a drag. */
    var onTap: (() -> Unit)? = null

    private val state = RenderState()

    private var running = false
    private var lastFrameMs = -1L

    // blink state machine
    private var blinkStartMs = -1L
    private var blinkDouble = false
    private var nextBlinkMs = 2200L

    // gaze (eased) + wander
    private var gazeX = 0f
    private var gazeY = 0f
    private var gazeTX = 0f
    private var gazeTY = 0f
    private var nextWanderMs = 2500L
    private var fingerDown = false
    private var fingerNX = 0f
    private var fingerNY = 0f

    // tap bounce
    private var tapMs = -1L
    private var nextAutoHeartMs = 4000L

    private var downX = 0f
    private var downY = 0f

    // ------------------------------------------------------------------
    // lifecycle — the frame loop
    // ------------------------------------------------------------------
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        running = true
        lastFrameMs = -1L
        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        running = false
        super.onDetachedFromWindow()
    }

    /** Stop drawing (e.g. when the overlay window is removed). */
    fun onPause() {
        running = false
    }

    /** Resume drawing if the view is attached. */
    fun onResume() {
        if (isAttachedToWindow) {
            running = true
            lastFrameMs = -1L
            postInvalidateOnAnimation()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val t = SystemClock.elapsedRealtime()
        update(t)

        state.tMs = t
        state.emotion = emotion
        state.face = FaceState.forEmotion(emotion)
        state.mouthOpen = mouthLevel.coerceIn(0f, 1f)
        state.thinking = thinking
        state.listening = listening
        state.gazeX = gazeX
        state.gazeY = gazeY

        GirlPainter.paint(canvas, width, height, state)

        if (running) postInvalidateOnAnimation()
    }

    // ------------------------------------------------------------------
    // per-frame animation state
    // ------------------------------------------------------------------
    private fun update(t: Long) {
        val dtMs = if (lastFrameMs < 0) 16L else (t - lastFrameMs).coerceIn(1L, 100L)
        lastFrameMs = t
        val dt = dtMs.toFloat()

        // ── blink ──────────────────────────────────────────────────────
        // Random 2.2-5.4s between blinks, ~18% chance of a double blink,
        // slower lids when sleepy.
        val blinkHalf = if (emotion == Emotion.SLEEPY) 150L else 70L
        if (blinkStartMs < 0 && t >= nextBlinkMs) {
            blinkStartMs = t
            blinkDouble = Random.nextFloat() < 0.18f
        }
        if (blinkStartMs >= 0) {
            val p = t - blinkStartMs
            state.lidClose = when {
                p < blinkHalf ->
                    (p.toFloat() / blinkHalf.toFloat()).coerceIn(0f, 1f)

                p < blinkHalf * 2 ->
                    (1f - (p - blinkHalf).toFloat() / blinkHalf.toFloat()).coerceIn(0f, 1f)

                blinkDouble && p < blinkHalf * 2 + 140L -> 0f

                blinkDouble && p < blinkHalf * 4 + 140L -> if (p < blinkHalf * 3 + 140L) {
                    ((p - (blinkHalf * 2 + 140L)).toFloat() / blinkHalf.toFloat()).coerceIn(0f, 1f)
                } else {
                    (1f - ((p - (blinkHalf * 3 + 140L)).toFloat() / blinkHalf.toFloat())).coerceIn(0f, 1f)
                }

                else -> {
                    blinkStartMs = -1L
                    nextBlinkMs = t + 2200L + Random.nextLong(0L, 3200L)
                    0f
                }
            }
        } else {
            state.lidClose = 0f
        }

        // ── gaze ──────────────────────────────────────────────────────
        // Finger wins; then mood (sad/shy look down, thinking looks up);
        // otherwise she wanders her eyes every few seconds.
        if (fingerDown) {
            gazeTX = (fingerNX * 4f).coerceIn(-4.5f, 4.5f)
            gazeTY = (fingerNY * 3f).coerceIn(-3.5f, 3.5f)
        } else when (emotion) {
            Emotion.SAD, Emotion.SHY -> {
                gazeTX = 0f
                gazeTY = 2f
            }

            Emotion.SLEEPY -> {
                gazeTX = 0f
                gazeTY = 1.2f
            }

            else -> {
                if (thinking) {
                    gazeTX = 2.6f
                    gazeTY = -3.4f
                } else if (t >= nextWanderMs) {
                    if (Random.nextFloat() < 0.3f) {
                        gazeTX = 0f
                        gazeTY = 0f
                    } else {
                        gazeTX = (Random.nextFloat() * 2f - 1f) * 3.4f
                        gazeTY = (Random.nextFloat() * 2f - 1f) * 2.2f
                    }
                    nextWanderMs = t + 2500L + Random.nextLong(0L, 4000L)
                }
            }
        }
        val k = 1f - Math.exp((-dt / 240f).toDouble()).toFloat()
        gazeX += (gazeTX - gazeX) * k
        gazeY += (gazeTY - gazeY) * k

        // ── tap bounce (damped oscillation, ~650ms) ────────────────────
        if (tapMs >= 0) {
            val p = (t - tapMs).toDouble() / 650.0
            if (p >= 1.0) {
                tapMs = -1L
                state.bounceY = 0f
            } else {
                state.bounceY = (-5.5 * Math.sin(p * 3.6 * Math.PI) * (1.0 - 0.55 * p)).toFloat()
            }
        } else {
            state.bounceY = 0f
        }

        // ── hearts ─────────────────────────────────────────────────────
        var i = 0
        while (i < state.hearts.size) {
            val h = state.hearts[i]
            h.p += dt / 950f
            if (h.p >= 1f) state.hearts.removeAt(i) else i++
        }
        val affectionate = emotion == Emotion.AFFECTIONATE ||
            emotion == Emotion.HAPPY ||
            emotion == Emotion.PLAYFUL
        if (affectionate && t >= nextAutoHeartMs) {
            nextAutoHeartMs = t + 2600L + Random.nextLong(0L, 2400L)
            state.hearts.add(
                HeartFx((Random.nextFloat() * 2f - 1f) * 9f, -33f, 0f, Random.nextInt(3)),
            )
        }
    }

    // ------------------------------------------------------------------
    // touch: drag to make her follow you, tap to make her react
    // ------------------------------------------------------------------
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                fingerDown = true
                setFinger(event)
                parent?.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                if (fingerDown) setFinger(event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                fingerDown = false
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (dx * dx + dy * dy < TAP_SLOP * TAP_SLOP) {
                        tapMs = SystemClock.elapsedRealtime()
                        state.hearts.add(HeartFx(-7f, -34f, 0f, 0))
                        state.hearts.add(HeartFx(0f, -36f, 0.1f, 1))
                        state.hearts.add(HeartFx(7f, -34f, 0.2f, 2))
                        onTap?.invoke()
                    }
                }
            }

            else -> return false
        }
        return true
    }

    private fun setFinger(e: MotionEvent) {
        fingerNX = (((e.x / width.toFloat().coerceAtLeast(1f)) - 0.5f) * 2f).coerceIn(-1f, 1f)
        fingerNY = (((e.y / height.toFloat().coerceAtLeast(1f)) - 0.5f) * 2f).coerceIn(-1f, 1f)
    }

    private companion object {
        const val TAP_SLOP = 24f
    }
}
