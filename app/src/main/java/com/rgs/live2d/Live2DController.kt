package com.rgs.live2d

import kotlin.math.abs

/**
 * UI/LLM/TTS-facing controller. It keeps policy out of the renderer.
 */
class Live2DController(private val view: Live2DView) {
    fun showIdle() = view.playMotion("Idle", 0, priority = 1)
    fun showTalking() = view.playMotion("Talk", 0, priority = 2)
    fun showHappy() {
        view.setExpression("expressions/happy.exp3.json")
        view.playMotion("Happy", 0, priority = 3)
    }
    fun showSad() {
        view.setExpression("expressions/sad.exp3.json")
        view.playMotion("Sad", 0, priority = 3)
    }

    fun onLlmResponse(text: String) {
        val lower = text.lowercase()
        when {
            listOf("sorry", "sad", "udaas", "dukhi").any(lower::contains) -> showSad()
            listOf("yay", "great", "happy", "khushi", "congrat").any(lower::contains) -> showHappy()
            else -> showTalking()
        }
    }

    /** Call from TTS PCM amplitude callbacks at ~20–60 Hz. */
    fun onTtsAmplitude(rms: Float) {
        val mouth = (rms.coerceIn(0f, 1f) * 1.8f).coerceIn(0f, 1f)
        view.setMouthOpenY(mouth)
    }

    fun onTtsFinished() {
        view.setMouthOpenY(0f)
        showIdle()
    }

    fun onTapReaction() = view.playMotion("TapBody", 0, priority = 3)
}