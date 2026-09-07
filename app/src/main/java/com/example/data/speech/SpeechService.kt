package com.example.data.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class SpeechService(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    var isTtsEnabled = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("SpeechService", "Language US is not supported.")
            } else {
                isInitialized = true
            }
        } else {
            Log.e("SpeechService", "TextToSpeech initialization failed.")
        }
    }

    fun speak(text: String) {
        if (!isTtsEnabled || !isInitialized) return
        val cleanText = text.replace(Regex("```[a-zA-Z]*\\n[\\s\\S]*?```"), "Code snippet outputted.")
            .take(300)
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "RGS_SPEECH_ID")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
