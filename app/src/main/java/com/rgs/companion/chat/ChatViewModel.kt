package com.rgs.companion.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rgs.companion.companion.Emotion
import com.rgs.companion.companion.MemoryStore
import com.rgs.companion.voice.SpeechEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    val speech = SpeechEngine(app).also { it.init() }

    /** Shown on the avatar nameplate. */
    val personaName: String = "Zaiphra"

    private val localFacts = mutableListOf(
        "User prefers direct and efficient communication",
        "Cyber Obsidian interface active",
        "Autonomous mobile assistant configured"
    )

    val memoryStore: MemoryStore = object : MemoryStore {
        override suspend fun allFacts(): List<String> = localFacts
    }

    private val _state = MutableStateFlow(
        ChatUiState(
            greeting = "Online and ready. Speak or type to begin.",
            messages = listOf(
                ChatMessage(
                    text = "Zaiphra system core initialized. Neural procedural companion active.",
                    role = "assistant",
                    proactive = true
                )
            )
        )
    )
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    init {
        // Lip sync and state pumping loop
        viewModelScope.launch {
            while (true) {
                speech.pumpLipSync()
                val m = speech.mouthLevel
                _state.update { if (it.mouthLevel != m) it.copy(mouthLevel = m) else it }
                delay(50L)
            }
        }

        // Voice input straight into conversation
        speech.onTranscript = { text ->
            viewModelScope.launch {
                _state.update { it.copy(partialTranscript = "", listening = false, audioRmsLevel = 0f) }
                send(text)
            }
        }
        speech.onPartialTranscript = { partial ->
            viewModelScope.launch {
                _state.update { it.copy(partialTranscript = partial) }
            }
        }
        speech.onRmsChanged = { rms ->
            viewModelScope.launch {
                _state.update { it.copy(audioRmsLevel = rms) }
            }
        }
        speech.onError = { errorMsg ->
            viewModelScope.launch {
                _state.update { it.copy(speechError = errorMsg, listening = false, audioRmsLevel = 0f) }
                delay(3200L)
                _state.update { if (it.speechError == errorMsg) it.copy(speechError = null) else it }
            }
        }
        speech.onListeningChanged = { on ->
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        listening = on,
                        partialTranscript = if (!on) "" else it.partialTranscript,
                        audioRmsLevel = if (!on) 0f else it.audioRmsLevel,
                    )
                }
            }
        }
    }

    fun pokeAvatar() {
        _state.update {
            val next = when (it.emotion) {
                Emotion.NEUTRAL -> Emotion.PLAYFUL
                Emotion.PLAYFUL -> Emotion.HAPPY
                Emotion.HAPPY -> Emotion.AFFECTIONATE
                else -> Emotion.NEUTRAL
            }
            it.copy(emotion = next)
        }
    }

    fun startListening() {
        _state.update { it.copy(speechError = null, partialTranscript = "") }
        speech.startListening()
    }

    fun stopListening() {
        speech.stopListening()
    }

    fun cancelListening() {
        speech.cancelListening()
        _state.update { it.copy(partialTranscript = "", listening = false, audioRmsLevel = 0f) }
    }

    fun toggleMic() {
        if (_state.value.listening) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun onExternalVoiceResult(spokenText: String) {
        val trimmed = spokenText.trim()
        if (trimmed.isNotEmpty()) {
            send(trimmed)
        }
    }

    fun dismissSpeechError() {
        _state.update { it.copy(speechError = null) }
    }

    fun send(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text = text, role = "user")
        _state.update {
            it.copy(
                messages = it.messages + userMsg,
                thinking = true,
                emotion = Emotion.NEUTRAL,
                partialTranscript = "",
                listening = false,
                audioRmsLevel = 0f,
            )
        }

        viewModelScope.launch {
            delay(600L) // natural thinking latency
            val replyText = generateResponse(text)
            val emotion = detectEmotion(replyText, text)
            val assistantMsg = ChatMessage(text = replyText, role = "assistant", animateTypewriter = true)

            _state.update {
                it.copy(
                    messages = it.messages + assistantMsg,
                    thinking = false,
                    emotion = emotion
                )
            }
            speech.speak(replyText)
        }
    }

    private fun generateResponse(input: String): String {
        val lower = input.lowercase()
        return when {
            "hello" in lower || "hi" in lower || "hey" in lower ->
                "Hello. I'm right here with you. What's on your mind?"
            "who are you" in lower || "what are you" in lower ->
                "I am Zaiphra — your autonomous personal companion and intelligence multiplier."
            "status" in lower || "system" in lower ->
                "Systems running nominal. Display refresh synced, neural lip-sync active, memory nominal."
            "happy" in lower || "great" in lower || "love" in lower ->
                "That brings genuine warmth to hear. Let's keep this momentum going."
            "help" in lower ->
                "You have full command over chat, voice recognition, automation, and long-term memory."
            else ->
                "Understood. Analyzing: \"$input\". Ready for the next directive."
        }
    }

    private fun detectEmotion(reply: String, input: String): Emotion {
        val combined = "$reply $input".lowercase()
        return when {
            "warmth" in combined || "love" in combined || "care" in combined -> Emotion.AFFECTIONATE
            "momentum" in combined || "great" in combined || "happy" in combined -> Emotion.HAPPY
            "wink" in combined || "play" in combined || "fun" in combined -> Emotion.PLAYFUL
            "analyzing" in combined || "nominal" in combined || "status" in combined -> Emotion.NEUTRAL
            "worry" in combined || "problem" in combined || "error" in combined -> Emotion.WORRIED
            else -> Emotion.NEUTRAL
        }
    }

    override fun onCleared() {
        speech.shutdown()
        super.onCleared()
    }
}
