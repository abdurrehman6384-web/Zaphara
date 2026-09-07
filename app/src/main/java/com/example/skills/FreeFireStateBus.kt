package com.example.skills

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FreeFireSessionState(
    val isRunning: Boolean = false,
    val mode: String = "Full Power",
    val playstyle: String = "Balanced",
    val antiTiltActive: Boolean = false,
    val threatLevel: Int = 18,
    val actionsPerMinute: Int = 0,
    val activeStrategy: String? = null,
    val lastAction: String = "STANDBY",
    val statusSummary: String = "Ready for deployment"
)

object FreeFireStateBus {
    private val _state = MutableStateFlow(FreeFireSessionState())
    val state: StateFlow<FreeFireSessionState> = _state.asStateFlow()

    fun update(transform: (FreeFireSessionState) -> FreeFireSessionState) {
        _state.value = transform(_state.value)
    }
}
