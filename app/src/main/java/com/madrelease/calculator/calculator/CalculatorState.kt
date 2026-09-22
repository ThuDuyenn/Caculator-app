package com.madrelease.calculator.calculator

data class CalculationHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CalculatorState(
    val displayValue: String = "0",
    val expression: String = "",
    val isError: Boolean = false,
    val history: List<CalculationHistoryItem> = emptyList(),
)
