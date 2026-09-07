package com.rgs.live2d

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import kotlin.math.abs

/**
 * Drop-in Android View for a native Live2D Cubism model.
 *
 * Coordinates passed to native code are normalized to [-1, 1]. A tap is
 * reported as a touch with identical start/end coordinates; the native model
 * decides which drawable/body region was hit.
 */
class Live2DView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {
    var onBodyTap: (() -> Unit)? = null

    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f

    init {
        setEGLContextClientVersion(2)
        setRenderer(RGSRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
        RGSJniBridge.setAssetRoot(context.applicationContext)
    }

    fun loadModel(assetDirectory: String, modelFile: String) {
        queueEvent { RGSJniBridge.loadModel(assetDirectory, modelFile) }
    }

    fun playMotion(group: String, index: Int = 0, priority: Int = 2) {
        queueEvent { RGSJniBridge.setMotion(group, index, priority) }
    }

    fun setExpression(expressionFile: String) {
        queueEvent { RGSJniBridge.setExpression(expressionFile) }
    }

    /** Drive ParamMouthOpenY from a 0..1 TTS amplitude envelope. */
    fun setMouthOpenY(value: Float) {
        queueEvent { RGSJniBridge.setMouthOpenY(value.coerceIn(0f, 1f)) }
    }

    fun setModelTransform(scale: Float, offsetX: Float = 0f, offsetY: Float = 0f) {
        queueEvent {
            RGSJniBridge.setModelScale(scale.coerceIn(0.1f, 4f))
            RGSJniBridge.setModelOffset(offsetX, offsetY)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = ((event.x / width.coerceAtLeast(1)) * 2f - 1f)
        val y = -((event.y / height.coerceAtLeast(1)) * 2f - 1f)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                lastX = x
                lastY = y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(x - lastX) > 0.002f || abs(y - lastY) > 0.002f) {
                    queueEvent { RGSJniBridge.setTouch(x, y) }
                    lastX = x
                    lastY = y
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                queueEvent { RGSJniBridge.setTouch(x, y) }
                // A body tap can be mapped to a reaction motion in native code.
                if (abs(x - downX) < 0.04f && abs(y - downY) < 0.04f) {
                    onBodyTap?.invoke()
                }
                performClick()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}