package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TacticalVoiceEngine(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    var isVoiceEnabled: Boolean = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Attempt Hindi / English (India)
            val result = tts?.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.ENGLISH)
            }
            tts?.setPitch(0.95f)
            tts?.setSpeechRate(1.05f)
            isReady = true
            Log.d("TacticalVoiceEngine", "Zaiphra Tactical Voice Engine initialized.")
        } else {
            Log.e("TacticalVoiceEngine", "TTS initialization failed.")
        }
    }

    fun speak(text: String, priorityQueue: Boolean = false) {
        if (!isVoiceEnabled || !isReady) return
        val queueMode = if (priorityQueue) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, "zaiphra_tactical_${System.currentTimeMillis()}")
    }

    fun speakTacticalCallout(event: String) {
        val callout = when (event.lowercase()) {
            "clutch" -> "Clutch mode active hai. Single target isolate karo, sab set hai."
            "ranked_push" -> "Ranked push shuru. Safe rotations aur compound dominance priority hai."
            "safe_ranked" -> "Safe ranked mode on. Fights avoid karo, zone center pakdo."
            "hotdrop" -> "Aggressive hotdrop mode. Fast loot aur instant push ke liye ready raho."
            "adaptive" -> "Adaptive intelligence on. Situation ke hisaab se playstyle shift hoga."
            "stop" -> "Execution rok di gayi hai. Safe stand-by."
            "error_recover" -> "Tactical recovery mode. Threat neutralize hone ka intezaar hai."
            else -> event
        }
        speak(callout, priorityQueue = true)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
