package com.example.os

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * On-device control for RGS AI Mobile -- non-root, mediated by Android's
 * AccessibilityService.
 *
 * The user enables it once in Settings > Accessibility; every grant is explicit
 * and revocable, which is what keeps this an assistant rather than spyware (see
 * the capability table in rgs-mobile-os-integration/INTEGRATION_PROMPT.md).
 *
 * A static [instance] is how the app reaches the service, because Android owns
 * its lifecycle. It is null until the user enables it -- callers must check
 * [DeviceControl.isAvailable].
 */
interface DeviceControlApi {
    val isAvailable: Boolean
    suspend fun readScreen(maxChars: Int): String
    suspend fun tapByText(label: String): Boolean
    suspend fun tapAt(x: Float, y: Float): Boolean
    suspend fun typeText(text: String): Boolean
    suspend fun scroll(direction: String): Boolean
    suspend fun pressBack(): Boolean
    suspend fun pressHome(): Boolean
    suspend fun pressRecents(): Boolean
    suspend fun openApp(packageName: String): Boolean
}

/** Used while the accessibility service is not enabled. */
object NoOpDeviceControl : DeviceControlApi {
    override val isAvailable = false
    override suspend fun readScreen(maxChars: Int) = "Device control is not enabled."
    override suspend fun tapByText(label: String) = false
    override suspend fun tapAt(x: Float, y: Float) = false
    override suspend fun typeText(text: String) = false
    override suspend fun scroll(direction: String) = false
    override suspend fun pressBack() = false
    override suspend fun pressHome() = false
    override suspend fun pressRecents() = false
    override suspend fun openApp(packageName: String) = false
}

/** Static handle so the app can call the service without Hilt/DI. */
object DeviceControl {
    @Volatile
    var instance: RgsAccessibilityService? = null
        internal set

    val api: DeviceControlApi get() = instance ?: NoOpDeviceControl
    val isAvailable: Boolean get() = instance != null
}

class RgsAccessibilityService : AccessibilityService(), DeviceControlApi {

    override val isAvailable: Boolean get() = true

    override fun onServiceConnected() {
        super.onServiceConnected()
        DeviceControl.instance = this
        Log.i(TAG, "connected")
    }

    override fun onDestroy() {
        if (DeviceControl.instance === this) DeviceControl.instance = null
        super.onDestroy()
    }

    override fun onInterrupt() = Unit
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    // ------------------------------------------------------------------
    // PhoneControl
    // ------------------------------------------------------------------
    override suspend fun readScreen(maxChars: Int): String {
        val root = rootInActiveWindow ?: return "(no window)"
        val sb = StringBuilder()
        try {
            dump(root, 0, sb)
        } finally {
            root.recycle()
        }
        val text = if (sb.isBlank()) "(no readable text on screen)" else sb.toString()
        return if (text.length > maxChars) text.take(maxChars) + "\n…(truncated)" else text
    }

    /**
     * Compact tree dump: only visible, enabled nodes that have text, a content
     * description, or are actionable. A full dump is thousands of lines of
     * layout noise; this is what an LLM can actually reason over.
     */
    private fun dump(node: AccessibilityNodeInfo, depth: Int, out: StringBuilder) {
        if (depth > MAX_DEPTH || out.length > MAX_DUMP_CHARS) return
        val text = node.text?.toString()?.take(80).orEmpty()
        val desc = node.contentDescription?.toString()?.take(80).orEmpty()
        val cls = node.className?.toString()?.substringAfterLast('.').orEmpty()
        val rect = Rect().also { node.getBoundsInScreen(it) }
        val visible = rect.width() > 0 && rect.height() > 0

        if (visible && node.isEnabled &&
            (text.isNotBlank() || desc.isNotBlank() || node.isClickable || node.isScrollable)
        ) {
            out.append("  ".repeat(minOf(depth, 6))).append('[').append(cls).append(']')
            val label = text.ifBlank { desc }
            if (label.isNotBlank()) out.append(" \"").append(label).append('"')
            if (node.isClickable) out.append(" clickable")
            if (node.isScrollable) out.append(" scrollable")
            out.append(" center=(").append(rect.centerX()).append(',').append(rect.centerY())
                .append(")\n")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            dump(child, depth + 1, out)
            child.recycle()
        }
    }

    override suspend fun tapByText(label: String): Boolean {
        if (label.isBlank()) return false
        val root = rootInActiveWindow ?: return false
        return try {
            val matches = root.findAccessibilityNodeInfosByText(label)
            if (matches.isNullOrEmpty()) false
            else clickNodeOrAncestor(matches.firstOrNull { it.isEnabled } ?: matches.first())
        } finally {
            root.recycle()
        }
    }

    /** TextViews are usually not clickable -- their parent is. Walk up. */
    private fun clickNodeOrAncestor(node: AccessibilityNodeInfo?): Boolean {
        var current = node
        var hops = 0
        while (current != null && hops < MAX_ANCESTOR_HOPS) {
            if (current.isClickable && current.isEnabled) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            val parent = current.parent
            current.recycle()
            current = parent
            hops++
        }
        current?.recycle()
        return false
    }

    override suspend fun tapAt(x: Float, y: Float): Boolean =
        gesture(Path().apply { moveTo(x, y) }, TAP_MS)

    override suspend fun typeText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        return try {
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
            val args = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text,
                )
            }
            focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                .also { focused.recycle() }
        } finally {
            root.recycle()
        }
    }

    override suspend fun scroll(direction: String): Boolean {
        val root = rootInActiveWindow ?: return false
        return try {
            val scrollable = findScrollable(root) ?: return false
            val action = if (direction.equals("up", true)) {
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            } else {
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            }
            scrollable.performAction(action).also { scrollable.recycle() }
        } finally {
            root.recycle()
        }
    }

    private fun findScrollable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return AccessibilityNodeInfo.obtain(node)
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollable(child)
            if (found != null) {
                child.recycle()
                return found
            }
            child.recycle()
        }
        return null
    }

    override suspend fun pressBack() = performGlobalAction(GLOBAL_ACTION_BACK)
    override suspend fun pressHome() = performGlobalAction(GLOBAL_ACTION_HOME)
    override suspend fun pressRecents() = performGlobalAction(GLOBAL_ACTION_RECENTS)

    override suspend fun openApp(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(TAG, "openApp failed: $packageName", e)
            false
        }
    }

    /**
     * Inject a gesture at coordinates (API 24+). `dispatchGesture` is
     * fire-and-forget, so this bridges its callback into a suspend function;
     * otherwise every action would report success before the OS started it.
     */
    private suspend fun gesture(path: Path, durationMs: Long): Boolean =
        withTimeoutOrNull(2_000) {
            suspendCancellableCoroutine { cont ->
                val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
                val request = GestureDescription.Builder().addStroke(stroke).build()
                val callback = object : GestureResultCallback() {
                    override fun onCompleted(d: GestureDescription?) {
                        if (cont.isActive) cont.resume(true)
                    }
                    override fun onCancelled(d: GestureDescription?) {
                        if (cont.isActive) cont.resume(false)
                    }
                }
                val dispatched = try {
                    dispatchGesture(request, callback, null)
                } catch (e: Exception) {
                    false
                }
                if (!dispatched && cont.isActive) cont.resume(false)
            }
        } ?: false

    companion object {
        private const val TAG = "RgsA11y"
        private const val MAX_ANCESTOR_HOPS = 6
        private const val MAX_DEPTH = 18
        private const val MAX_DUMP_CHARS = 8_000
        private const val TAP_MS = 40L
    }
}
