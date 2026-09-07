package com.rgs.companion.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Speech engine for TTS and STT with lip-sync driving and real-time voice recognition.
 */
class SpeechEngine(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var stt: SpeechRecognizer? = null
    private var ready = false

    /** True once the TTS engine has finished initialising. */
    val isReady: Boolean get() = ready

    private val utteranceSeq = AtomicInteger(0)
    var locale: Locale = Locale.US

    /** 0..1 audio envelope for mouth animation. */
    @Volatile
    var mouthLevel: Float = 0f
        private set

    @Volatile
    private var isSpeaking = false

    var onTranscript: ((String) -> Unit)? = null
    var onPartialTranscript: ((String) -> Unit)? = null
    var onListeningChanged: ((Boolean) -> Unit)? = null
    var onRmsChanged: ((Float) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun init() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = locale
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                        mouthLevel = 0f
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                        mouthLevel = 0f
                    }
                })
                ready = true
                Log.i(TAG, "TTS initialized successfully")
            } else {
                Log.w(TAG, "TTS init failed with status $status")
            }
        }
    }

    /**
     * Periodically called to advance lip-sync simulation.
     */
    fun pumpLipSync() {
        if (isSpeaking) {
            // Natural mouth modulation while speaking
            val t = System.currentTimeMillis() * 0.015f
            mouthLevel = (0.25f + 0.65f * kotlin.math.abs(kotlin.math.sin(t))).coerceIn(0f, 1f)
        } else {
            mouthLevel = 0f
        }
    }

    fun speak(text: String) {
        if (!ready) return
        val id = "utt_${utteranceSeq.incrementAndGet()}"
        isSpeaking = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun stop() {
        tts?.stop()
        isSpeaking = false
        mouthLevel = 0f
    }

    fun startListening() {
        mainHandler.post {
            // Silence TTS when user speaks
            stop()

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.w(TAG, "SpeechRecognizer not available on device")
                onError?.invoke("Speech recognition service not available")
                onListeningChanged?.invoke(false)
                return@post
            }

            try {
                // Cancel existing session to avoid RECOGNIZER_BUSY
                stt?.cancel()

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                }

                if (stt == null) {
                    stt = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {
                                Log.d(TAG, "Speech recognition ready")
                                onListeningChanged?.invoke(true)
                            }

                            override fun onBeginningOfSpeech() {
                                Log.d(TAG, "Speech beginning detected")
                            }

                            override fun onRmsChanged(rmsdB: Float) {
                                // rmsdB is typically between -2f and +10f
                                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                                onRmsChanged?.invoke(normalized)
                            }

                            override fun onBufferReceived(buffer: ByteArray?) {}

                            override fun onEndOfSpeech() {
                                Log.d(TAG, "Speech ended")
                            }

                            override fun onError(error: Int) {
                                Log.w(TAG, "SpeechRecognizer error: $error")
                                onListeningChanged?.invoke(false)
                                onRmsChanged?.invoke(0f)
                                val msg = when (error) {
                                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> null // transient
                                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network required for speech"
                                    else -> "Speech recognition error ($error)"
                                }
                                if (msg != null) {
                                    onError?.invoke(msg)
                                }
                            }

                            override fun onResults(results: Bundle?) {
                                onListeningChanged?.invoke(false)
                                onRmsChanged?.invoke(0f)
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = matches?.firstOrNull()?.trim()
                                if (!text.isNullOrEmpty()) {
                                    Log.d(TAG, "Speech recognized: $text")
                                    onTranscript?.invoke(text)
                                }
                            }

                            override fun onPartialResults(partialResults: Bundle?) {
                                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = matches?.firstOrNull()?.trim()
                                if (!text.isNullOrEmpty()) {
                                    onPartialTranscript?.invoke(text)
                                }
                            }

                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }
                }

                onListeningChanged?.invoke(true)
                stt?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting voice recognition", e)
                onListeningChanged?.invoke(false)
                onRmsChanged?.invoke(0f)
                onError?.invoke("Failed to start microphone: ${e.localizedMessage}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                stt?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping STT", e)
            }
            onListeningChanged?.invoke(false)
            onRmsChanged?.invoke(0f)
        }
    }

    fun cancelListening() {
        mainHandler.post {
            try {
                stt?.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error canceling STT", e)
            }
            onListeningChanged?.invoke(false)
            onRmsChanged?.invoke(0f)
        }
    }

    fun shutdown() {
        mainHandler.post {
            tts?.stop()
            tts?.shutdown()
            tts = null
            stt?.cancel()
            stt?.destroy()
            stt = null
            ready = false
        }
    }

    companion object {
        private const val TAG = "SpeechEngine"
    }
}
