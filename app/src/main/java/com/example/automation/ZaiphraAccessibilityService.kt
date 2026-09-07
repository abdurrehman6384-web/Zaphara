package com.example.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import com.example.automation.advanced.HumanicGestureEngine

class ZaiphraAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ZaiphraAccessibilityService? = null
    }

    lateinit var humanicGestureEngine: HumanicGestureEngine
        private set

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        humanicGestureEngine = HumanicGestureEngine(this)
        Log.d("ZaiphraVision", "Accessibility Service Connected and Bound.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Passive monitoring for screen state changes
    }

    override fun onInterrupt() {
        Log.d("ZaiphraVision", "Accessibility interrupted.")
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    // --- Global Actions ---
    fun performBack() = performGlobalAction(GLOBAL_ACTION_BACK)
    fun performHome() = performGlobalAction(GLOBAL_ACTION_HOME)
    fun performRecents() = performGlobalAction(GLOBAL_ACTION_RECENTS)

    // --- Screen Interaction ---
    fun clickNodeByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text)
        
        for (node in nodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            // If the text node itself isn't clickable, traverse up to find a clickable parent
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
                parent = parent.parent
            }
        }
        return false
    }

    fun swipeUp() {
        val displayMetrics = resources.displayMetrics
        val middleX = displayMetrics.widthPixels / 2f
        val startY = displayMetrics.heightPixels * 0.8f
        val endY = displayMetrics.heightPixels * 0.2f
        
        val path = Path().apply {
            moveTo(middleX, startY)
            lineTo(middleX, endY)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()
            
        dispatchGesture(gesture, null, null)
    }
}
