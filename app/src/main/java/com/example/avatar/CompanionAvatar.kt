package com.example.avatar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.sin

/**
 * Companion avatar for RGS AI Mobile -- drawn entirely with Canvas.
 *
 * Self-contained on purpose: no Live2D, no image assets, no external dependency,
 * so the merged app builds with nothing extra to download. The Live2D native
 * module from `rgs-live2d-integration` remains available as an optional upgrade
 * (it needs the Cubism Core native library + NDK), documented in MERGE.md.
 *
 * A single [ValueAnimator] drives bob + blink + mouth so the parts never drift
 * out of phase and there is one thing to cancel in [onDetachedFromWindow].
 */
class CompanionAvatarView(context: Context) : View(context) {

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bellyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val cheekPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val earPath = Path()

    private var mood: AvatarMood = AvatarMood.NEUTRAL
    private var phase = 0f
    private var blinkT = 0f
    private var nextBlinkAt = 2_400L
    private var lastBlinkCheck = 0L
    private var tapBounce = 0f

    /** 0..1 mouth openness (lip-sync). */
    private var mouthOpen: Float = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 3_000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener {
            phase = it.animatedValue as Float
            tickBlink()
            tapBounce = (tapBounce * 0.90f).coerceAtLeast(0f)
            invalidate()
        }
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        applyMood(mood)
    }

    fun setMood(m: AvatarMood) {
        if (m == mood) return
        mood = m
        applyMood(m)
        animator.duration = m.cycleMs
        invalidate()
    }

    /** Accept a loose tag (e.g. an agent name) so callers need no enum import. */
    fun setMoodTag(tag: String?) {
        setMood(AvatarMood.fromTag(tag))
    }

    fun setMouthOpen(v: Float) {
        mouthOpen = v.coerceIn(0f, 1f)
    }

    fun react() {
        tapBounce = 1f
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val bob = sin(phase * Math.PI * 2).toFloat() * (h * 0.022f) * mood.bobAmount
        val bounce = tapBounce * h * 0.06f
        val cx = w / 2f
        val cy = h / 2f + bob - bounce
        val r = minOf(w, h) * 0.34f

        drawGlow(canvas, cx, cy, r)
        drawEars(canvas, cx, cy, r)
        drawBody(canvas, cx, cy, r)
        drawFace(canvas, cx, cy, r)
    }

    private fun drawGlow(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val pulse = (sin(phase * Math.PI * 2).toFloat() + 1f) / 2f
        glowPaint.color = withAlpha(mood.accent, (30 + pulse * 45).toInt())
        canvas.drawCircle(cx, cy, r * (1.35f + pulse * 0.18f), glowPaint)
    }

    private fun drawEars(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val earH = r * (0.55f + 0.25f * mood.earPerk)
        val spread = r * 0.62f
        for (side in intArrayOf(-1, 1)) {
            earPath.reset()
            val baseX = cx + side * spread * 0.55f
            val baseY = cy - r * 0.62f
            val tipX = cx + side * spread * 1.05f
            val tipY = cy - earH
            earPath.moveTo(baseX - side * r * 0.20f, baseY + r * 0.10f)
            earPath.quadTo(tipX, tipY - r * 0.1f, baseX + side * r * 0.20f, baseY - r * 0.05f)
            earPath.close()
            canvas.drawPath(earPath, bodyPaint)
        }
    }

    private fun drawBody(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val squash = 1f + tapBounce * 0.10f
        canvas.drawOval(RectF(cx - r, cy - r * squash, cx + r, cy + r / squash), bodyPaint)
        canvas.drawOval(
            RectF(cx - r * 0.58f, cy - r * 0.18f, cx + r * 0.58f, cy + r * 0.80f), bellyPaint,
        )
    }

    private fun drawFace(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val eyeY = cy - r * (0.18f - mood.lookUp * 0.12f)
        val eyeSpread = r * 0.40f
        val eyeR = r * 0.115f * mood.eyeOpen
        val openness = (1f - blinkT).coerceIn(0f, 1f) * mood.eyeOpen

        for (side in intArrayOf(-1, 1)) {
            val ex = cx + side * eyeSpread
            if (openness < 0.15f) {
                linePaint.strokeWidth = r * 0.09f
                linePaint.color = mood.line
                canvas.drawLine(ex - eyeR, eyeY, ex + eyeR, eyeY, linePaint)
            } else {
                canvas.drawOval(
                    RectF(ex - eyeR, eyeY - eyeR * openness, ex + eyeR, eyeY + eyeR * openness),
                    eyePaint,
                )
                canvas.drawCircle(
                    ex - eyeR * 0.35f, eyeY - eyeR * 0.35f * openness, eyeR * 0.34f, bellyPaint,
                )
            }
        }
        drawCheeks(canvas, cx, cy, r)
        drawMouth(canvas, cx, cy, r)
    }

    private fun drawCheeks(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        if (mood.blush <= 0f) return
        cheekPaint.color = withAlpha(Color.parseColor("#FF7A9C"), (mood.blush * 110).toInt())
        for (side in intArrayOf(-1, 1)) {
            canvas.drawOval(
                RectF(
                    cx + side * r * 0.72f - r * 0.18f, cy + r * 0.05f,
                    cx + side * r * 0.72f + r * 0.18f, cy + r * 0.28f,
                ),
                cheekPaint,
            )
        }
    }

    private fun drawMouth(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        linePaint.color = mood.line
        linePaint.strokeWidth = r * 0.085f
        linePaint.style = Paint.Style.STROKE
        val my = cy + r * 0.34f
        val open = (mouthOpen * mood.talkAmount).coerceIn(0f, 1f)

        if (open > 0.08f) {
            linePaint.style = Paint.Style.FILL
            canvas.drawOval(
                RectF(cx - r * 0.22f, my - r * 0.10f * open, cx + r * 0.22f, my + r * 0.34f * open),
                linePaint,
            )
            linePaint.style = Paint.Style.STROKE
        } else {
            val curve = r * 0.22f * mood.smile
            val path = Path().apply {
                moveTo(cx - r * 0.24f, my)
                quadTo(cx, my + curve, cx + r * 0.24f, my)
            }
            canvas.drawPath(path, linePaint)
        }
    }

    private fun tickBlink() {
        val now = System.currentTimeMillis()
        if (blinkT > 0f) {
            blinkT = (blinkT - 0.16f).coerceAtLeast(0f)
            return
        }
        if (now - lastBlinkCheck > nextBlinkAt) {
            lastBlinkCheck = now
            blinkT = 1f
            nextBlinkAt = 1_800L + (Math.random() * 3_200L).toLong()
        }
    }

    private fun applyMood(m: AvatarMood) {
        bodyPaint.color = m.body
        bellyPaint.color = m.belly
        eyePaint.color = m.line
        cheekPaint.color = m.accent
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))
}

enum class AvatarMood(
    val body: Int, val belly: Int, val line: Int, val accent: Int,
    val smile: Float, val eyeOpen: Float, val earPerk: Float, val lookUp: Float,
    val bobAmount: Float, val talkAmount: Float, val blush: Float, val cycleMs: Long,
) {
    NEUTRAL(0xFF7C5CFF.toInt(), 0xFFEDE7FF.toInt(), 0xFF2B1B5E.toInt(), 0xFFFF7A9C.toInt(),
        0.55f, 1.0f, 0.25f, 0.0f, 1.0f, 1.0f, 0.25f, 3_000),
    THINKING(0xFF6A7BFF.toInt(), 0xFFE6EAFF.toInt(), 0xFF1F2A5E.toInt(), 0xFF9BB0FF.toInt(),
        0.10f, 0.65f, 0.55f, 0.9f, 1.6f, 0.4f, 0.1f, 1_100),
    HAPPY(0xFFFF6FA5.toInt(), 0xFFFFE3EE.toInt(), 0xFF5E1030.toInt(), 0xFFFFC2D6.toInt(),
        0.9f, 1.1f, 0.8f, 0.1f, 1.8f, 1.0f, 0.7f, 1_200),
    FOCUSED(0xFF4E8CFF.toInt(), 0xFFDCE9FF.toInt(), 0xFF12294F.toInt(), 0xFF8FB6FF.toInt(),
        0.20f, 0.85f, 0.7f, 0.35f, 0.7f, 0.6f, 0.05f, 2_000),
    ALERT(0xFF31C6A8.toInt(), 0xFFDFFBF4.toInt(), 0xFF0E4A3E.toInt(), 0xFF7CE8CF.toInt(),
        0.35f, 1.35f, 1.0f, 0.15f, 1.3f, 0.8f, 0.35f, 1_600),
    ;

    companion object {
        /** Map a loose tag (agent name / emotion word) onto a mood. */
        fun fromTag(tag: String?): AvatarMood {
            val t = tag?.lowercase().orEmpty()
            return when {
                "vision" in t -> ALERT
                "terminal" in t || "coder" in t -> FOCUSED
                "telekinesis" in t || "action" in t -> FOCUSED
                "cybersec" in t || "api" in t -> THINKING
                "think" in t -> THINKING
                "happy" in t || "love" in t -> HAPPY
                else -> NEUTRAL
            }
        }
    }
}

/**
 * Drop-in header for the chat screen. Kept dependency-free (no ViewModel import)
 * so it compiles regardless of the surrounding app; the caller passes a loose
 * mood tag such as the active agent's name.
 */
@Composable
fun CompanionAvatarHeader(moodTag: String? = null, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        factory = { ctx ->
            CompanionAvatarView(ctx).apply {
                setOnClickListener { react() }
            }
        },
        update = { view ->
            view.setMoodTag(moodTag)
        },
    )
}
