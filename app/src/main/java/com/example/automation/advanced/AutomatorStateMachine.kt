package com.example.automation.advanced

import android.util.Log
import kotlinx.coroutines.delay

/**
 * Zaiphra's Automator State Machine.
 * Handles robust execution, error recovery, and state fallbacks.
 */
enum class AgentState {
    IDLE, PERCEIVING, DECIDING, ACTING, ERROR_RECOVERY
}

abstract class AutomatorStateMachine(val name: String) {
    protected var currentState = AgentState.IDLE
    protected var isRunning = false
    private var errorCount = 0
    private val MAX_ERRORS = 3

    suspend fun startMachine() {
        isRunning = true
        currentState = AgentState.PERCEIVING
        Log.d("ZaiphraCognitive", "[$name] State Machine Initialized.")

        while (isRunning) {
            try {
                when (currentState) {
                    AgentState.PERCEIVING -> {
                        perceive()
                        currentState = AgentState.DECIDING
                    }
                    AgentState.DECIDING -> {
                        decide()
                        currentState = AgentState.ACTING
                    }
                    AgentState.ACTING -> {
                        act()
                        currentState = AgentState.PERCEIVING
                        errorCount = 0 // Reset on successful action
                    }
                    AgentState.ERROR_RECOVERY -> {
                        recover()
                    }
                    AgentState.IDLE -> {
                        delay(1000)
                    }
                }
            } catch (e: Exception) {
                Log.e("ZaiphraCognitive", "[$name] Exception in state $currentState: ${e.message}")
                handleError()
            }
        }
    }

    open fun stopMachine() {
        isRunning = false
        currentState = AgentState.IDLE
        Log.d("ZaiphraCognitive", "[$name] State Machine Halted.")
    }

    private fun handleError() {
        errorCount++
        if (errorCount >= MAX_ERRORS) {
            Log.e("ZaiphraCognitive", "[$name] Max errors reached. Halting macro to prevent detection or infinite loops.")
            stopMachine()
        } else {
            currentState = AgentState.ERROR_RECOVERY
        }
    }

    protected abstract suspend fun perceive()
    protected abstract suspend fun decide()
    protected abstract suspend fun act()
    protected abstract suspend fun recover()
}
