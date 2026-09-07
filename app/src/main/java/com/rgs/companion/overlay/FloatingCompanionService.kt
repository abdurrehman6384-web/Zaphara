package com.rgs.companion.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.rgs.companion.avatar.AvatarView
import com.rgs.companion.companion.Emotion

/**
 * Foreground service managing the desktop-pet floating avatar overlay.
 */
class FloatingCompanionService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubble: FrameLayout? = null
    private var avatar: AvatarView? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, buildNotification())
        if (canDrawOverlays(this)) {
            addOverlay()
        }
    }

    private fun addOverlay() {
        val lp = WindowManager.LayoutParams(
            EXPANDED_SIZE_PX,
            EXPANDED_SIZE_PX,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }
        params = lp

        val root = FrameLayout(this)
        val avatarView = AvatarView(this).apply {
            onTap = {
                onAvatarTap?.invoke()
            }
        }
        avatar = avatarView
        root.addView(
            avatarView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )

        // Drag handling
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        root.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = lp.x
                    initialY = lp.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    lp.x = initialX + (event.rawX - initialTouchX).toInt()
                    lp.y = initialY + (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager.updateViewLayout(root, lp)
                    } catch (_: Exception) {}
                    true
                }
                else -> false
            }
        }

        bubble = root
        try {
            windowManager.addView(root, lp)
            Log.i(TAG, "Overlay shown (${EXPANDED_SIZE_PX}px)")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to add window overlay", e)
        }
    }

    private fun removeOverlay() {
        val root = bubble ?: return
        avatar?.onPause()
        avatar = null
        try {
            windowManager.removeView(root)
        } catch (e: Exception) {
            Log.w(TAG, "removeView failed", e)
        }
        bubble = null
    }

    fun setEmotion(expressionName: String) {
        avatar?.emotion = when (expressionName.lowercase()) {
            "smile", "happy" -> Emotion.HAPPY
            "sad" -> Emotion.SAD
            "angry" -> Emotion.ANGRY
            "surprised" -> Emotion.SURPRISED
            else -> Emotion.NEUTRAL
        }
    }

    fun setMouthOpen(v: Float) {
        avatar?.mouthLevel = v
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    private fun overlayWindowType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private fun buildNotification(): Notification {
        val channelId = CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, "Companion Overlay", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(chan)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Companion Overlay Active")
            .setContentText("Avatar is floating above apps")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    companion object {
        private const val TAG = "FloatingCompanion"
        private const val CHANNEL_ID = "companion_overlay"
        private const val NOTIFICATION_ID = 4201
        private const val EXPANDED_SIZE_PX = 620

        var onAvatarTap: (() -> Unit)? = null

        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        }
    }
}
