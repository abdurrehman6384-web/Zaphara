package com.rgs.companion.avatar

import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Typeface
import com.rgs.companion.companion.Emotion
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Paints the companion girl — a fully procedural, dependency-free
 * "Live2D-like" avatar: big glossy eyes that blink and follow your finger,
 * swaying lavender hair, a lip-synced mouth, emotion-driven expressions,
 * blush, a cyber top, and a pulsing neon aura with orbiting particles.
 *
 * ## Coordinate system
 * Everything is expressed in "u" units where 100u == min(width, height), and
 * the origin is placed at (width/2, height * 0.53) — slightly below centre so
 * the aura has more room above her head. The bust occupies roughly
 * -38u..+48u vertically.
 *
 * ## Threading
 * Called only from [AvatarView.onDraw] (the UI thread). The shared
 * [Paint]/[Path] instances below are single-threaded by construction.
 *
 * ## Cost
 * A few dozen small paths and ~10 gradient shaders per frame — cheap enough
 * for 60-120fps on any recent phone.
 */

private fun LinearGradientShader(
    x0: Float, y0: Float, x1: Float, y1: Float,
    colors: IntArray, stops: FloatArray? = null,
    tileMode: Shader.TileMode = Shader.TileMode.CLAMP,
): Shader = LinearGradient(x0, y0, x1, y1, colors, stops, tileMode)

private fun RadialGradientShader(
    centerX: Float, centerY: Float, radius: Float,
    colors: IntArray, stops: FloatArray? = null,
    tileMode: Shader.TileMode = Shader.TileMode.CLAMP,
): Shader = RadialGradient(centerX, centerY, radius.coerceAtLeast(0.01f), colors, stops, tileMode)

object GirlPainter {

    // ── palette ──────────────────────────────────────────────────────────
    // Fair skin ("goriya"), pale lavender hair, cyber indigo top, neon accents.
    private const val SKIN_HI = 0xFFFFF3E8.toInt()
    private const val SKIN = 0xFFFFE9D6.toInt()
    private const val SKIN_LO = 0xFFFFD9BE.toInt()
    private const val HAIR_LT = 0xFFEADCFB.toInt()
    private const val HAIR_MD = 0xFFCDB5F2.toInt()
    private const val HAIR_DK = 0xFFA585D6.toInt()
    private const val TOP_1 = 0xFF1B1233.toInt()
    private const val TOP_2 = 0xFF2A1E52.toInt()
    private const val INK = 0xFF241439.toInt()
    private const val BROW = 0xFF5A4470.toInt()
    private const val MOUTH_C = 0xFFA84465.toInt()
    private const val MOUTH_IN = 0xFF46233B.toInt()
    private const val TONGUE = 0xFFE88A9A.toInt()
    private const val IRIS_T = 0xFF9FF6FF.toInt()
    private const val IRIS_M = 0xFF46C8FF.toInt()
    private const val IRIS_B = 0xFF6D4DFF.toInt()
    private const val PUPIL = 0xFF170D33.toInt()

    // ── shared draw state (UI thread only) ───────────────────────────────
    private val solid = Paint(Paint.ANTI_ALIAS_FLAG)
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) blendMode = BlendMode.SCREEN
    }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val path = Path()
    private val rect = RectF()

    private fun sinF(v: Float): Float = sin(v.toDouble()).toFloat()
    private fun cosF(v: Float): Float = cos(v.toDouble()).toFloat()
    private fun argb(a: Int, r: Int, g: Int, b: Int): Int =
        ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)

    /** Entry point — called once per frame by [AvatarView]. */
    fun paint(c: Canvas, w: Int, h: Int, rs: RenderState) {
        if (w < 16 || h < 16) return
        val u = min(w, h).toFloat() / 100f
        val t = rs.tMs.toFloat()

        c.save()
        c.translate(w.toFloat() * 0.5f, h.toFloat() * 0.53f)

        drawAura(c, u, t, rs)

        c.save()
        if (rs.bounceY != 0f) c.translate(0f, rs.bounceY * u)

        drawBackHair(c, u, t)
        drawBody(c, u, t)

        // ── head (rotates slightly with mood + slow idle sway) ──────────
        c.save()
        val tilt = rs.face.tiltRad + sinF(t * 0.00044f) * 0.022f
        c.rotate(Math.toDegrees(tilt.toDouble()).toFloat(), 0f, -10f * u)
        drawEars(c, u)
        drawFace(c, u)
        drawBlush(c, u, rs)
        drawEye(c, u, rs, -11f * u, -2.5f * u, -1f)
        drawEye(c, u, rs, 11f * u, -2.5f * u, 1f)
        drawBrow(c, u, rs, -11f * u, -2.5f * u, -1f)
        drawBrow(c, u, rs, 11f * u, -2.5f * u, 1f)
        drawNose(c, u)
        drawMouth(c, u, t, rs)
        drawHairCap(c, u, t)
        drawBangs(c, u, t)
        drawSideLock(c, u, t, -1f)
        drawSideLock(c, u, t, 1f)
        drawHairGems(c, u, t)
        drawEarbud(c, u, t, rs)
        c.restore()

        drawEffects(c, u, t, rs)

        c.restore() // bounce
        c.restore() // root
    }

    // ==================================================================
    // aura: glow, rotating rings, orbiting particles, listening ripples,
    // thinking dots
    // ==================================================================
    private fun drawAura(c: Canvas, u: Float, t: Float, rs: RenderState) {
        val pulse = 0.88f + 0.12f * sinF(t * 0.00082f)

        solid.shader = RadialGradientShader(
            0f, -8f * u, 54f * u * pulse,
            intArrayOf(argb(46, 0, 229, 255), argb(20, 181, 61, 255), 0),
            floatArrayOf(0f, 0.55f, 1f),
        )
        c.drawCircle(0f, -8f * u, 54f * u * pulse, solid)
        solid.shader = null

        // two counter-rotating dashed rings (additive so they glow)
        c.save()
        c.rotate((t * 0.012f) % 360f)
        glow.pathEffect = DashPathEffect(floatArrayOf(9f * u, 13f * u), (t * 0.02f).toFloat())
        glow.color = argb(80, 0, 229, 255)
        glow.strokeWidth = 0.9f * u
        c.drawCircle(0f, -8f * u, 42f * u, glow)
        c.restore()

        c.save()
        c.rotate((-t * 0.009f) % 360f)
        glow.pathEffect = DashPathEffect(floatArrayOf(3.5f * u, 16f * u), (-t * 0.03f).toFloat())
        glow.color = argb(60, 181, 61, 255)
        glow.strokeWidth = 0.7f * u
        c.drawCircle(0f, -8f * u, 45.5f * u, glow)
        c.restore()
        glow.pathEffect = null

        // orbiting particles
        for (i in 0 until 6) {
            val fi = i.toFloat() / 5f
            val r = (34f + 12f * fi) * u
            val ang = t * 0.00035f * (0.5f + fi) + i.toFloat() * 1.9f
            val x = cosF(ang) * r
            val y = -8f * u + sinF(ang) * r * 0.42f
            val a = (24f + 30f * (0.5f + 0.5f * sinF(t * 0.0029f + i.toFloat() * 2.1f))).toInt()
            val col = when (i % 3) {
                0 -> argb(a, 0, 229, 255)
                1 -> argb(a, 181, 61, 255)
                else -> argb(a, 255, 255, 255)
            }
            solid.color = col
            c.drawCircle(x, y, (0.7f + 0.7f * fi) * u, solid)
        }

        // listening: expanding ripples around the head
        if (rs.listening) {
            for (k in 0..2) {
                val p = ((t / 1500f) + k.toFloat() / 3f) % 1f
                line.color = argb(((1f - p) * 80f).toInt(), 0, 229, 255)
                line.strokeWidth = 1.1f * u
                c.drawCircle(0f, -8f * u, (30f + p * 17f) * u, line)
            }
        }

        // thinking: three pulsing dots, top-right
        if (rs.thinking) {
            for (i in 0..2) {
                val a = 0.25f + 0.75f * (0.5f + 0.5f * sinF(t * 0.004f - i.toFloat() * 0.9f))
                solid.color = argb((a * 230f).toInt(), 191, 255, 255)
                c.drawCircle((23f + i.toFloat() * 4.6f) * u, -41f * u, 1.35f * u, solid)
            }
        }
    }

    // ==================================================================
    // hair behind the head (sways gently on its own)
    // ==================================================================
    private fun drawBackHair(c: Canvas, u: Float, t: Float) {
        c.save()
        c.translate(sinF(t * 0.0005f) * 0.7f * u, 0f)

        path.reset()
        path.moveTo(0f, -38f * u)
        path.cubicTo(-16f * u, -37f * u, -27f * u, -30f * u, -30f * u, -18f * u)
        path.cubicTo(-33f * u, -8f * u, -34f * u, 4f * u, -33.5f * u, 16f * u)
        path.cubicTo(-33f * u, 26f * u, -29f * u, 32f * u, -26f * u, 35f * u)
        path.cubicTo(-20f * u, 39f * u, -11f * u, 36.5f * u, -5f * u, 38.5f * u)
        path.cubicTo(0f, 40.5f * u, 5f * u, 38.5f * u, 10f * u, 36.5f * u)
        path.cubicTo(18f * u, 39f * u, 26f * u, 35f * u, 26f * u, 35f * u)
        path.cubicTo(29f * u, 32f * u, 33f * u, 26f * u, 33.5f * u, 16f * u)
        path.cubicTo(34f * u, 4f * u, 33f * u, -8f * u, 30f * u, -18f * u)
        path.cubicTo(27f * u, -30f * u, 16f * u, -37f * u, 0f, -38f * u)
        path.close()
        solid.shader = LinearGradientShader(
            0f, -38f * u, 0f, 38f * u,
            intArrayOf(HAIR_DK, 0xFF8A63C9.toInt()),
            floatArrayOf(0f, 1f),
        )
        c.drawPath(path, solid)
        solid.shader = null

        // faint light strands for depth
        line.color = argb(38, 255, 255, 255)
        line.strokeWidth = 0.8f * u
        path.reset()
        path.moveTo(-21f * u, -18f * u)
        path.cubicTo(-25f * u, 0f, -24f * u, 16f * u, -22f * u, 30f * u)
        c.drawPath(path, line)
        path.reset()
        path.moveTo(21f * u, -18f * u)
        path.cubicTo(25f * u, 0f, 24f * u, 16f * u, 22f * u, 30f * u)
        c.drawPath(path, line)

        c.restore()
    }

    // ==================================================================
    // body: cyber top, glowing collar, pulsing heart emblem (breathes)
    // ==================================================================
    private fun drawBody(c: Canvas, u: Float, t: Float) {
        c.save()
        c.translate(0f, sinF(t * 0.001f) * 0.7f * u)

        path.reset()
        path.moveTo(-37f * u, 48f * u)
        path.cubicTo(-34f * u, 26f * u, -27f * u, 16.5f * u, -13f * u, 12.8f * u)
        path.cubicTo(-7f * u, 11.6f * u, -3.5f * u, 12.6f * u, 0f, 14.6f * u)
        path.cubicTo(3.5f * u, 12.6f * u, 7f * u, 11.6f * u, 13f * u, 12.8f * u)
        path.cubicTo(27f * u, 16.5f * u, 34f * u, 26f * u, 37f * u, 48f * u)
        path.close()
        solid.shader = LinearGradientShader(
            0f, 12f * u, 0f, 46f * u,
            intArrayOf(TOP_1, TOP_2),
            floatArrayOf(0f, 1f),
        )
        c.drawPath(path, solid)
        solid.shader = null

        // soft chin shadow on the chest
        solid.shader = RadialGradientShader(
            0f, 16.5f * u, 11f * u,
            intArrayOf(argb(110, 12, 7, 24), 0),
            floatArrayOf(0f, 1f),
        )
        rect.set(-12f * u, 12f * u, 12f * u, 22f * u)
        c.drawOval(rect, solid)
        solid.shader = null

        // glowing V collar
        path.reset()
        path.moveTo(-9f * u, 12.6f * u)
        path.lineTo(0f, 19.4f * u)
        path.lineTo(9f * u, 12.6f * u)
        line.color = argb(40, 0, 229, 255)
        line.strokeWidth = 3f * u
        c.drawPath(path, line)
        line.color = argb(235, 0, 229, 255)
        line.strokeWidth = 1.1f * u
        c.drawPath(path, line)

        // pulsing heart emblem
        val hp = (1f + 0.07f * sinF(t * 0.0024f)) * 3.1f * u
        solid.shader = RadialGradientShader(
            0f, 26.5f * u, 6f * u,
            intArrayOf(argb(70, 181, 61, 255), 0),
            floatArrayOf(0f, 1f),
        )
        c.drawCircle(0f, 26.5f * u, 6f * u, solid)
        solid.shader = null
        heartPath(path, 0f, 26.8f * u, hp)
        solid.color = 0xFFC56BFF.toInt()
        c.drawPath(path, solid)
        heartPath(path, 0f, 26.2f * u, hp * 0.55f)
        solid.color = argb(120, 255, 255, 255)
        c.drawPath(path, solid)

        c.restore()
    }

    // ==================================================================
    // head
    // ==================================================================
    private fun drawEars(c: Canvas, u: Float) {
        solid.color = 0xFFF5CDB4.toInt()
        c.drawOval(-27.5f * u, -13f * u, -19f * u, -3f * u, solid)
        c.drawOval(19f * u, -13f * u, 27.5f * u, -3f * u, solid)
    }

    private fun drawFace(c: Canvas, u: Float) {
        val r = rect
        r.set(-22.5f * u, -34.5f * u, 22.5f * u, 14.5f * u)

        // fair skin with a warm highlight up top
        solid.shader = RadialGradientShader(
            0f, -18f * u, 36f * u,
            intArrayOf(SKIN_HI, SKIN, SKIN_LO),
            floatArrayOf(0f, 0.6f, 1f),
        )
        c.drawOval(r, solid)
        solid.shader = null

        // soft shading on the right side
        solid.shader = RadialGradientShader(
            9f * u, -8f * u, 30f * u,
            intArrayOf(0, argb(46, 226, 168, 130)),
            floatArrayOf(0f, 1f),
        )
        c.drawOval(r, solid)
        solid.shader = null

        // faint rim light on the left
        line.color = argb(30, 255, 255, 255)
        line.strokeWidth = 1.6f * u
        c.drawArc(r, 118f, 64f, false, line)
    }

    private fun drawBlush(c: Canvas, u: Float, rs: RenderState) {
        val b = rs.face.blush
        if (b <= 0.01f) return
        for (s in floatArrayOf(-1f, 1f)) {
            val cx = s * 15.5f * u
            solid.shader = RadialGradientShader(
                cx, 4.5f * u, 5.6f * u,
                intArrayOf(argb((56f * b).toInt(), 255, 157, 190), 0),
                floatArrayOf(0f, 1f),
            )
            rect.set(cx - 5.6f * u, 1.6f * u, cx + 5.6f * u, 7.4f * u)
            c.drawOval(rect, solid)
        }
        solid.shader = null
    }

    /**
     * One big anime eye. [outer] is -1 for the left eye, +1 for the right —
     * it flips which side gets the lash flick. The whole interior (sclera,
     * iris, pupil, highlights, lids) is scaled vertically around the eye
     * centre, so blinking and happy-squint collapse the eye to a line
     * correctly.
     */
    private fun drawEye(c: Canvas, u: Float, rs: RenderState, cx: Float, cy: Float, outer: Float) {
        val f = rs.face
        val squintK = 1f - 0.22f * f.squint
        val openK = (f.eyeOpen * squintK * (1f - rs.lidClose)).coerceIn(0f, 1.12f)
        val gx = rs.gazeX * u
        val gy = rs.gazeY * u

        // sclera rect (also the clip for the eye interior)
        rect.set(cx - 6.3f * u, cy - 7.3f * u, cx + 6.3f * u, cy + 7.3f * u)
        path.reset()
        path.addOval(rect, Path.Direction.CW)

        c.save()
        c.translate(0f, cy)
        c.scale(1f, openK, 0f, 0f)
        c.translate(0f, -cy)

        c.save()
        c.clipPath(path)
        // sclera
        solid.color = 0xFFFDF6F0.toInt()
        c.drawOval(rect, solid)

        // iris — cyan-to-violet gradient, offset by the gaze
        val ix = cx + gx * 0.62f
        val iy = cy + gy * 0.5f + 0.4f * u
        val irx = 4.7f * u * f.iris
        val iry = 5.9f * u * f.iris
        solid.shader = LinearGradientShader(
            ix, iy - iry, ix, iy + iry,
            intArrayOf(IRIS_T, IRIS_M, IRIS_B),
            floatArrayOf(0f, 0.45f, 1f),
        )
        rect.set(ix - irx, iy - iry, ix + irx, iy + iry)
        c.drawOval(rect, solid)
        solid.shader = null
        // iris bottom rim
        line.color = argb(150, 62, 46, 158)
        line.strokeWidth = 0.9f * u
        c.drawArc(rect, 20f, 140f, false, line)
        // pupil
        solid.color = PUPIL
        rect.set(
            ix - 2.3f * u * f.iris - gx * 0.05f,
            iy - 3.0f * u * f.iris + 0.3f * u,
            ix + 2.3f * u * f.iris - gx * 0.05f,
            iy + 3.0f * u * f.iris + 0.3f * u,
        )
        c.drawOval(rect, solid)
        // big + small highlights
        solid.color = 0xE6FFFFFF.toInt()
        c.drawOval(
            cx - 4.4f * u + gx * 0.25f, cy - 5.6f * u + gy * 0.2f,
            cx - 0.2f * u + gx * 0.25f, cy - 2.6f * u + gy * 0.2f, solid,
        )
        solid.color = 0xB3FFFFFF.toInt()
        c.drawOval(
            cx + 1.6f * u + gx * 0.3f, cy + 1.6f * u + gy * 0.25f,
            cx + 3.6f * u + gx * 0.3f, cy + 3.2f * u + gy * 0.25f, solid,
        )
        // sparkle glint — a little heart when she is affectionate
        if (rs.emotion == Emotion.AFFECTIONATE && f.squint > 0.25f) {
            heartPath(path, cx - 2.2f * u + gx * 0.25f, cy - 1.6f * u + gy * 0.2f, 1.05f * u)
            solid.color = 0xE6FFFFFF.toInt()
            c.drawPath(path, solid)
        } else {
            drawSpark(c, u, cx - 2.2f * u + gx * 0.25f, cy - 1.4f * u + gy * 0.2f, 1.15f * u, 200)
        }
        c.restore() // unclip

        // upper lash with an outward flick (drawn in scaled space so it
        // follows the lid down when she blinks)
        val innerX = cx - outer * 6.4f * u
        val outerX = cx + outer * 6.6f * u
        path.reset()
        path.moveTo(innerX, cy - 4.6f * u)
        path.quadTo(cx - outer * 0.5f * u, cy - 9f * u, outerX, cy - 5.4f * u)
        path.quadTo(outerX + outer * 1.4f * u, cy - 5f * u, outerX + outer * 2.4f * u, cy - 3.8f * u)
        line.color = INK
        line.strokeWidth = 1.9f * u
        c.drawPath(path, line)

        // lower lid — curves up when she is happy
        path.reset()
        path.moveTo(cx - 4.6f * u, cy + 7f * u)
        path.quadTo(cx, cy + (7f + 2.4f * f.squint) * u, cx + 4.6f * u, cy + 7f * u)
        line.color = argb((90f + 60f * f.squint).toInt(), 201, 143, 116)
        line.strokeWidth = 0.75f * u
        c.drawPath(path, line)
        c.restore() // unscale

        // closed-lid line once the lids are mostly down
        if (rs.lidClose > 0.55f) {
            path.reset()
            path.moveTo(cx - 5.2f * u, cy + 0.4f * u)
            path.quadTo(cx, cy + 2.8f * u, cx + 5.2f * u, cy + 0.4f * u)
            line.color = INK
            line.strokeWidth = 1.6f * u
            c.drawPath(path, line)
        }
    }

    private fun drawBrow(c: Canvas, u: Float, rs: RenderState, cx: Float, cy: Float, outer: Float) {
        val f = rs.face
        val base = cy - 10f * u - f.browLift * 1.3f * u
        val innerX = cx - outer * 4.4f * u
        val outerX = cx + outer * 4.8f * u
        val innerY = base - f.browTilt * 1.9f * u
        val outerY = base + 0.3f * u
        path.reset()
        path.moveTo(outerX, outerY)
        path.quadTo(cx - outer * 1f * u, base - 1.2f * u - f.browLift * 0.5f * u, innerX, innerY)
        line.color = BROW
        line.alpha = 215
        line.strokeWidth = 1.25f * u
        c.drawPath(path, line)
        line.alpha = 255
    }

    private fun drawNose(c: Canvas, u: Float) {
        path.reset()
        path.moveTo(0.9f * u, 2.6f * u)
        path.quadTo(1.6f * u, 3.2f * u, 1.1f * u, 3.9f * u)
        line.color = argb(140, 216, 154, 124)
        line.strokeWidth = 0.6f * u
        c.drawPath(path, line)
    }

    /**
     * Mouth: an open lip-synced mouth (with tongue) while speaking, a soft
     * "U" smile at rest, a frown when sad, a little "o" while thinking.
     */
    private fun drawMouth(c: Canvas, u: Float, t: Float, rs: RenderState) {
        val f = rs.face
        val my = 9.3f * u
        val speaking = rs.mouthOpen > 0.02f
        val jitter = if (speaking) 0.88f + 0.12f * sinF(t * 0.011f) else 1f
        val open = ((f.mouthBase + rs.mouthOpen) * jitter).coerceIn(0f, 1f)

        if (open > 0.1f) {
            val wM = (5.4f + 2.2f * f.smile.coerceAtLeast(0f)) * u
            val hM = (1.5f + open * 5.6f) * u
            path.reset()
            path.moveTo(-wM * 0.5f, my)
            path.quadTo(0f, my + 0.8f * u, wM * 0.5f, my)
            path.quadTo(wM * 0.52f, my + hM * 0.55f, wM * 0.34f, my + hM * 0.88f)
            path.quadTo(0f, my + hM * 1.12f, -wM * 0.34f, my + hM * 0.88f)
            path.quadTo(-wM * 0.52f, my + hM * 0.55f, -wM * 0.5f, my)
            path.close()
            solid.color = MOUTH_IN
            c.drawPath(path, solid)
            // tongue
            c.save()
            c.clipPath(path)
            solid.color = TONGUE
            rect.set(-wM * 0.3f, my + hM * 0.55f, wM * 0.3f, my + hM * 1.25f)
            c.drawOval(rect, solid)
            c.restore()
        } else if (rs.thinking) {
            // little "o"
            line.color = MOUTH_C
            line.strokeWidth = 1.05f * u
            rect.set(0.6f * u, my - 1.6f * u, 2.6f * u, my + 0.2f * u)
            c.drawOval(rect, line)
        } else {
            val curve = f.smile * 2.4f * u
            path.reset()
            path.moveTo(-3.3f * u, my)
            path.quadTo(0f, my + curve, 3.3f * u, my)
            line.color = MOUTH_C
            line.strokeWidth = 1.5f * u
            c.drawPath(path, line)
            if (f.smile > 0.4f) {
                path.reset()
                path.moveTo(-1.6f * u, my + curve + 1.1f * u)
                path.quadTo(0f, my + curve + 1.7f * u, 1.6f * u, my + curve + 1.1f * u)
                line.color = argb(40, 255, 255, 255)
                line.strokeWidth = 0.5f * u
                c.drawPath(path, line)
            }
        }
    }

    // ==================================================================
    // hair in front: cap, bangs, side locks, gems, earbud
    // ==================================================================
    private fun drawHairCap(c: Canvas, u: Float, t: Float) {
        path.reset()
        path.moveTo(-25f * u, -10f * u)
        path.cubicTo(-26f * u, -26f * u, -14f * u, -36.5f * u, 0f, -36.5f * u)
        path.cubicTo(14f * u, -36.5f * u, 26f * u, -26f * u, 25f * u, -10f * u)
        path.cubicTo(20f * u, -12.5f * u, 16f * u, -16.5f * u, 10f * u, -16f * u)
        path.cubicTo(5f * u, -15.5f * u, 3f * u, -13.5f * u, 0f, -13.5f * u)
        path.cubicTo(-3f * u, -13.5f * u, -5f * u, -15.5f * u, -10f * u, -16f * u)
        path.cubicTo(-16f * u, -16.5f * u, -20f * u, -12.5f * u, -25f * u, -10f * u)
        path.close()
        solid.shader = LinearGradientShader(
            0f, -36.5f * u, 0f, -10f * u,
            intArrayOf(HAIR_LT, HAIR_MD),
            floatArrayOf(0f, 1f),
        )
        c.drawPath(path, solid)
        solid.shader = null

        // moving shine band
        val sw = sinF(t * 0.0009f) * 1.6f * u
        path.reset()
        path.moveTo(-13f * u + sw, -30.5f * u)
        path.quadTo(0f, -34.6f * u, 11f * u + sw, -31f * u)
        line.color = argb(50, 255, 255, 255)
        line.strokeWidth = 2.2f * u
        c.drawPath(path, line)
        path.reset()
        path.moveTo(-8f * u + sw, -28.6f * u)
        path.quadTo(1f * u, -31.4f * u, 7f * u + sw, -29.4f * u)
        line.color = argb(100, 255, 255, 255)
        line.strokeWidth = 0.7f * u
        c.drawPath(path, line)
    }

    /** Eight rounded bang petals with a centre part; tips sway individually. */
    private fun drawBangs(c: Canvas, u: Float, t: Float) {
        // Tip heights are tuned so the bangs stop at brow level in the middle
        // (brows stay visible) but run long enough at the temples to frame
        // the face, grazing the outer eye corners like in anime art.
        val xs = floatArrayOf(-19.5f, -13f, -6.5f, -2.8f, 2.8f, 6.5f, 13f, 19.5f)
        for (x in xs) {
            val dir = if (x < 0f) -1f else 1f
            val tipY = when {
                abs(x) > 17f -> -9f
                abs(x) > 11f -> -11.5f
                abs(x) > 4.5f -> -12.8f
                else -> -13.5f
            } * u
            val sway = sinF(t * 0.0008f + x * 0.35f) * 0.8f * u
            path.reset()
            path.moveTo(x * u - 3.6f * u, -21f * u)
            path.cubicTo(
                x * u - 4.2f * u, -12f * u,
                x * u - 2.6f * u, tipY + 3f * u,
                x * u + sway + dir * 1.4f * u, tipY,
            )
            path.cubicTo(
                x * u + 2.6f * u, tipY + 3f * u,
                x * u + 4.2f * u, -12f * u,
                x * u + 3.6f * u, -21f * u,
            )
            path.close()
            solid.shader = LinearGradientShader(
                x * u, -21f * u, x * u, tipY,
                intArrayOf(0xFFEDDFFB.toInt(), 0xFFC9ADF0.toInt()),
                floatArrayOf(0f, 1f),
            )
            c.drawPath(path, solid)
            solid.shader = null
            line.color = argb(70, 159, 127, 209)
            line.strokeWidth = 0.45f * u
            c.drawPath(path, line)
        }
    }

    /** Long lock framing one side of the face; the tip sways. */
    private fun drawSideLock(c: Canvas, u: Float, t: Float, s: Float) {
        val sw = sinF(t * 0.0007f + s) * 1.1f * u
        path.reset()
        path.moveTo(s * 21.5f * u, -16f * u)
        path.cubicTo(s * 26.5f * u, -8f * u, s * 27.5f * u, 4f * u, s * 26f * u, 14f * u)
        path.cubicTo(s * 25.2f * u, 21f * u, s * 23f * u, 26f * u, s * 20.5f * u + sw, 30f * u)
        path.cubicTo(s * 19.6f * u + sw * 0.6f, 25f * u, s * 20.6f * u, 20f * u, s * 20.2f * u, 13f * u)
        path.cubicTo(s * 19.8f * u, 4f * u, s * 20f * u, -8f * u, s * 21.5f * u, -16f * u)
        path.close()
        solid.shader = LinearGradientShader(
            0f, -16f * u, 0f, 30f * u,
            intArrayOf(0xFFE0D0FA.toInt(), 0xFFB294E0.toInt()),
            floatArrayOf(0f, 1f),
        )
        c.drawPath(path, solid)
        solid.shader = null
    }

    /** Two tiny glowing diodes in the hair — the "AI companion" cue. */
    private fun drawHairGems(c: Canvas, u: Float, t: Float) {
        val p = 0.7f + 0.3f * sinF(t * 0.004f)

        solid.shader = RadialGradientShader(
            -8.5f * u, -19.5f * u, 4.5f * u,
            intArrayOf(argb((95f * p).toInt(), 0, 229, 255), 0),
            floatArrayOf(0f, 1f),
        )
        c.drawCircle(-8.5f * u, -19.5f * u, 4.5f * u, solid)
        solid.shader = null
        line.color = argb(200, 0, 229, 255)
        line.strokeWidth = 0.5f * u
        c.drawLine(-8.5f * u, -19.5f * u, -8.5f * u, -23.5f * u, line)
        solid.color = 0xFFCFFFFF.toInt()
        c.drawCircle(-8.5f * u, -19.5f * u, 1.35f * u, solid)

        solid.shader = RadialGradientShader(
            8f * u, -20.5f * u, 3.2f * u,
            intArrayOf(argb((80f * p).toInt(), 181, 61, 255), 0),
            floatArrayOf(0f, 1f),
        )
        c.drawCircle(8f * u, -20.5f * u, 3.2f * u, solid)
        solid.shader = null
        solid.color = 0xFFECC9FF.toInt()
        c.drawCircle(8f * u, -20.5f * u, 1f * u, solid)
    }

    /** Glowing earbud on her right ear; pulses brighter while listening. */
    private fun drawEarbud(c: Canvas, u: Float, t: Float, rs: RenderState) {
        val p = 0.6f + 0.4f * sinF(t * 0.005f)
        val glowA = ((if (rs.listening) 150f else 70f) * p).toInt()
        solid.shader = RadialGradientShader(
            24.8f * u, -6f * u, 4.5f * u,
            intArrayOf(argb(glowA, 0, 229, 255), 0),
            floatArrayOf(0f, 1f),
        )
        c.drawCircle(24.8f * u, -6f * u, 4.5f * u, solid)
        solid.shader = null
        solid.color = argb(245, 234, 253, 255)
        rect.set(23.6f * u, -8.2f * u, 26f * u, -3.8f * u)
        c.drawRoundRect(rect, 1.1f * u, 1.1f * u, solid)
    }

    // ==================================================================
    // effects: pop hearts, sweat drop (worried), zzz (sleepy), sparkles
    // ==================================================================
    private fun drawEffects(c: Canvas, u: Float, t: Float, rs: RenderState) {
        // hearts
        for (h in rs.hearts) {
            val x = h.x * u + sinF(h.p * 7f + h.hue.toFloat()) * 1.5f * u
            val y = h.y * u - h.p * 12f * u
            val s = (2.1f + h.p * 1.7f) * u
            val a = ((1f - h.p) * 235f).toInt()
            val col = when (h.hue) {
                0 -> argb(a, 255, 125, 174)
                1 -> argb(a, 197, 107, 255)
                else -> argb(a, 0, 229, 255)
            }
            heartPath(path, x, y, s)
            solid.color = col
            c.drawPath(path, solid)
        }

        // sweat drop, dripping while worried
        if (rs.emotion == Emotion.WORRIED) {
            val p = (t % 2600f) / 2600f
            val x = 19f * u
            val y = -21f * u + p * 7f * u
            val a = ((1f - p) * 190f).toInt()
            path.reset()
            path.moveTo(x, y - 2.8f * u)
            path.quadTo(x + 1.7f * u, y + 0.6f * u, x, y + 1.5f * u)
            path.quadTo(x - 1.7f * u, y + 0.6f * u, x, y - 2.8f * u)
            path.close()
            solid.color = argb(a, 214, 244, 255)
            c.drawPath(path, solid)
        }

        // drifting "z"s while sleepy
        if (rs.emotion == Emotion.SLEEPY) {
            for (i in 0..1) {
                val p = ((t / 2800f) + i.toFloat() * 0.5f) % 1f
                text.color = argb(((1f - p) * 190f).toInt(), 191, 233, 255)
                text.textSize = (3.6f + i.toFloat() * 1.4f + p * 2.4f) * u
                c.drawText("z", (21f + i.toFloat() * 3f + p * 5f) * u, (-30f - i.toFloat() * 4f - p * 10f) * u, text)
            }
        }

        // sparkles while happy-ish
        if (rs.face.squint > 0.25f) {
            val a1 = (110f + 110f * sinF(t * 0.006f)).toInt().coerceIn(30, 220)
            val a2 = (110f + 110f * sinF(t * 0.006f + 2.1f)).toInt().coerceIn(30, 220)
            drawSpark(c, u, 17.5f * u, -28f * u, 1.5f * u, a1)
            drawSpark(c, u, -19.5f * u, -25f * u, 1.1f * u, a2)
        }
    }

    // ==================================================================
    // small path helpers
    // ==================================================================
    private fun heartPath(p: Path, x: Float, y: Float, s: Float) {
        p.reset()
        p.moveTo(x, y + 0.55f * s)
        p.cubicTo(x - 1.05f * s, y - 0.15f * s, x - 0.62f * s, y - 1.08f * s, x, y - 0.45f * s)
        p.cubicTo(x + 0.62f * s, y - 1.08f * s, x + 1.05f * s, y - 0.15f * s, x, y + 0.55f * s)
        p.close()
    }

    /** A four-point sparkle star. */
    private fun drawSpark(c: Canvas, u: Float, x: Float, y: Float, s: Float, alpha: Int) {
        path.reset()
        path.moveTo(x, y - s)
        path.lineTo(x + 0.28f * s, y - 0.28f * s)
        path.lineTo(x + s, y)
        path.lineTo(x + 0.28f * s, y + 0.28f * s)
        path.lineTo(x, y + s)
        path.lineTo(x - 0.28f * s, y + 0.28f * s)
        path.lineTo(x - s, y)
        path.lineTo(x - 0.28f * s, y - 0.28f * s)
        path.close()
        solid.color = argb(alpha, 255, 255, 255)
        c.drawPath(path, solid)
    }
}
