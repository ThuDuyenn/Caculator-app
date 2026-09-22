package com.madrelease.calculator.calculator

sealed interface CalculatorAction {
    data class Digit(val value: Int) : CalculatorAction
    data class Operation(val operator: CalculatorOperator) : CalculatorAction
    data object Decimal : CalculatorAction
    data object Equals : CalculatorAction
    data object Clear : CalculatorAction
    data object Backspace : CalculatorAction
    data object ToggleSign : CalculatorAction
    data object Percent : CalculatorAction
    data object ClearHistory : CalculatorAction
    data class RestoreHistory(val item: CalculationHistoryItem) : CalculatorAction
}

enum class CalculatorOperator(val symbol: String) {
    ADD("+"), SUBTRACT("−"), MULTIPLY("×"), DIVIDE("÷")
}
