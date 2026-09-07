package com.rgs.live2d

import android.content.Context

/**
 * Small JNI surface for the native Cubism renderer.
 *
 * The native library owns the Cubism framework and model instances. This
 * class deliberately has no LLM or TTS dependency so it can be used from
 * Compose, a ViewModel, or a foreground service.
 */
internal object RGSJniBridge {
    init {
        System.loadLibrary("rgs-live2d")
    }

    external fun onSurfaceCreated()
    external fun onSurfaceChanged(width: Int, height: Int)
    external fun onDrawFrame()
    external fun onSurfaceDestroyed()
    external fun setAssetRoot(context: Context)
    external fun loadModel(modelDirectory: String, modelFile: String): Boolean
    external fun setMotion(group: String, index: Int, priority: Int)
    external fun setExpression(expressionFile: String): Boolean
    external fun setMouthOpenY(value: Float)
    external fun setTouch(x: Float, y: Float)
    external fun setModelScale(value: Float)
    external fun setModelOffset(x: Float, y: Float)
}