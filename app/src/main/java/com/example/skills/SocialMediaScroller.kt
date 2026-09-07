package com.example.skills

import android.util.Log
import com.example.automation.ZaiphraAccessibilityService
import kotlinx.coroutines.*
import kotlin.random.Random

class SocialMediaScroller(private val accessibilityService: ZaiphraAccessibilityService) {
    private var scrollJob: Job? = null

    fun startScrolling(platform: String) {
        if (scrollJob?.isActive == true) return
        Log.d("ZaiphraSkill", "Starting Social Media Scroller for $platform")
        
        scrollJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                // Human-like viewing duration (varies between 5 to 20 seconds)
                val delayMillis = Random.nextLong(5000, 20000)
                delay(delayMillis)

                // 10% chance to like the video via double tap
                if (Random.nextFloat() < 0.1f) {
                    performDoubleTapLike()
                    delay(500)
                }

                // Swipe to next video
                accessibilityService.swipeUp()
                Log.d("ZaiphraSkill", "Swiped to next content on $platform")
            }
        }
    }

    fun stopScrolling() {
        Log.d("ZaiphraSkill", "Stopping Social Media Scroller")
        scrollJob?.cancel()
        scrollJob = null
    }

    private fun performDoubleTapLike() {
        Log.d("ZaiphraSkill", "Performing Double Tap to Like")
        // Logic will interface with ZaiphraAccessibilityService path dispatching
    }
}
