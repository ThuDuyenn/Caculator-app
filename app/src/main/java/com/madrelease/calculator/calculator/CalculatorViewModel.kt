package com.madrelease.calculator.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel(
    private val engine: CalculatorEngine = CalculatorEngine()
) : ViewModel() {

    private val _state = MutableStateFlow(engine.state)
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onAction(action: CalculatorAction) {
        _state.value = engine.onAction(action)
    }
}
