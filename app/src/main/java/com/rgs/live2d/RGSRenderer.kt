package com.rgs.live2d

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class RGSRenderer : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) =
        RGSJniBridge.onSurfaceCreated()

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) =
        RGSJniBridge.onSurfaceChanged(width, height)

    override fun onDrawFrame(gl: GL10?) = RGSJniBridge.onDrawFrame()
}