package com.example.automation

import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class ScreenAnalyzer {

    /**
     * Extracts a structural representation of the current screen.
     * Parses through the AccessibilityNodeInfo tree to pull all visible text,
     * content descriptions, and interactive elements.
     */
    fun extractScreenContext(rootNode: AccessibilityNodeInfo?): String {
        if (rootNode == null) {
            Log.w("ZaiphraVision", "Root node is null. Screen is empty or inaccessible.")
            return "SCREEN_EMPTY"
        }

        val screenData = java.lang.StringBuilder()
        traverseNode(rootNode, screenData, 0)
        return screenData.toString().trim()
    }

    private fun traverseNode(node: AccessibilityNodeInfo?, builder: java.lang.StringBuilder, depth: Int) {
        if (node == null) return

        val text = node.text?.toString()
        val contentDesc = node.contentDescription?.toString()
        val isClickable = node.isClickable

        val nodeContent = when {
            !text.isNullOrBlank() -> text
            !contentDesc.isNullOrBlank() -> contentDesc
            else -> null
        }

        if (nodeContent != null) {
            val indent = "  ".repeat(depth)
            val actionTag = if (isClickable) "[CLICKABLE] " else ""
            builder.append("$indent- $actionTag$nodeContent\n")
        }

        for (i in 0 until node.childCount) {
            traverseNode(node.getChild(i), builder, depth + 1)
        }
    }
}
