package com.example.automation.advanced

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Zaiphra's Humanic Gesture Engine.
 * Replaces linear, robotic swipes with randomized, Bezier-curved paths and variable durations
 * to bypass anti-cheat heuristic detection (similar to FGA and advanced automation tools).
 */
class HumanicGestureEngine(private val service: AccessibilityService) {

    suspend fun performHumanClick(x: Float, y: Float) {
        // Randomize touch point slightly to avoid pixel-perfect robotic clicks
        val offsetX = x + Random.nextFloat() * 10 - 5
        val offsetY = y + Random.nextFloat() * 10 - 5
        val duration = Random.nextLong(40, 120) // Human tap duration

        val path = Path().apply {
            moveTo(offsetX, offsetY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        service.dispatchGesture(gesture, null, null)
        Log.d("ZaiphraExecution", "Humanic Click at ($offsetX, $offsetY) for ${duration}ms")
        delay(duration + Random.nextLong(20, 50))
    }

    suspend fun performHumanSwipe(startX: Float, startY: Float, endX: Float, endY: Float) {
        val duration = Random.nextLong(300, 600)
        
        // Add control points for a slight Bezier curve instead of a straight line
        val controlX = startX + (endX - startX) / 2 + Random.nextInt(-50, 50)
        val controlY = startY + (endY - startY) / 2 + Random.nextInt(-50, 50)

        val path = Path().apply {
            moveTo(startX, startY)
            quadTo(controlX.toFloat(), controlY.toFloat(), endX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        service.dispatchGesture(gesture, null, null)
        Log.d("ZaiphraExecution", "Humanic Swipe from ($startX, $startY) to ($endX, $endY) over ${duration}ms")
        delay(duration + Random.nextLong(50, 150))
    }
}
