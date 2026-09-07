package com.rgs.companion.control

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Service to support companion screen awareness and accessibility interaction.
 */
class CompanionAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "CompanionAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) instance = null
        Log.i(TAG, "CompanionAccessibilityService destroyed")
    }

    companion object {
        const val TAG = "CompanionA11y"
        var instance: CompanionAccessibilityService? = null
    }
}
