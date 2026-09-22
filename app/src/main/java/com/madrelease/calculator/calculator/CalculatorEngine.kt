package com.madrelease.calculator.calculator

import java.math.BigDecimal
import java.math.RoundingMode

class CalculatorEngine {
    var state: CalculatorState = CalculatorState()
        private set

    private var firstOperand: BigDecimal? = null
    private var pendingOperator: CalculatorOperator? = null
    private var expressionPrefix: String = ""
    private var currentInput: String = "0"
    private var isEnteringNewOperand: Boolean = true
    private var isResultDisplayed: Boolean = false
    private var isPercentApplied: Boolean = false

    private val maxAllowedMagnitude = BigDecimal("1e100")

    fun onAction(action: CalculatorAction): CalculatorState {
        when (action) {
            is CalculatorAction.Clear -> handleClear()
            is CalculatorAction.ClearHistory -> handleClearHistory()
            is CalculatorAction.RestoreHistory -> handleRestoreHistory(action.item)
            else -> {
                if (state.isError) {
                    // Once in error, all calculation actions except Clear are ignored
                    return state
                }
                when (action) {
                    is CalculatorAction.Digit -> handleDigit(action.value)
                    is CalculatorAction.Decimal -> handleDecimal()
                    is CalculatorAction.Operation -> handleOperation(action.operator)
                    is CalculatorAction.Equals -> handleEquals()
                    is CalculatorAction.ToggleSign -> handleToggleSign()
                    is CalculatorAction.Percent -> handlePercent()
                    is CalculatorAction.Backspace -> handleBackspace()
                    is CalculatorAction.Clear,
                    is CalculatorAction.ClearHistory,
                    is CalculatorAction.RestoreHistory -> Unit
                }
            }
        }
        return state
    }

    private fun handleClear() {
        firstOperand = null
        pendingOperator = null
        expressionPrefix = ""
        currentInput = "0"
        isEnteringNewOperand = true
        isResultDisplayed = false
        isPercentApplied = false
        state = state.copy(displayValue = "0", expression = "", isError = false)
    }

    private fun handleClearHistory() {
        state = state.copy(history = emptyList())
    }

    private fun handleRestoreHistory(item: CalculationHistoryItem) {
        val parsed = try {
            parseBigDecimal(item.result)
        } catch (_: Exception) {
            null
        }
        firstOperand = parsed
        pendingOperator = null
        expressionPrefix = ""
        currentInput = item.result
        isEnteringNewOperand = true
        isResultDisplayed = true
        isPercentApplied = false
        state = state.copy(
            displayValue = item.result,
            expression = item.expression,
            isError = false
        )
    }

    private fun handleDigit(digit: Int) {
        if (digit !in 0..9) {
            setError()
            return
        }

        if (isResultDisplayed) {
            firstOperand = null
            pendingOperator = null
            expressionPrefix = ""
            isResultDisplayed = false
            currentInput = digit.toString()
            isEnteringNewOperand = false
            isPercentApplied = false
            state = state.copy(displayValue = currentInput, expression = "")
            return
        }

        if (isPercentApplied) {
            currentInput = digit.toString()
            isPercentApplied = false
            isEnteringNewOperand = false
            state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
            return
        }

        if (isEnteringNewOperand) {
            currentInput = if (currentInput == "-0") {
                "-$digit"
            } else {
                digit.toString()
            }
            isEnteringNewOperand = false
            state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
            return
        }

        val digitCount = currentInput.count { it.isDigit() }
        if (digitCount >= 16) {
            // Limited to 16 digits
            return
        }

        currentInput = when (currentInput) {
            "0" -> digit.toString()
            "-0" -> "-$digit"
            else -> currentInput + digit
        }
        state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
    }

    private fun handleDecimal() {
        if (isResultDisplayed) {
            firstOperand = null
            pendingOperator = null
            expressionPrefix = ""
            isResultDisplayed = false
            currentInput = "0."
            isEnteringNewOperand = false
            isPercentApplied = false
            state = state.copy(displayValue = currentInput, expression = "")
            return
        }

        if (isPercentApplied) {
            currentInput = "0."
            isPercentApplied = false
            isEnteringNewOperand = false
            state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
            return
        }

        if (isEnteringNewOperand) {
            currentInput = if (currentInput == "-0") "-0." else "0."
            isEnteringNewOperand = false
            state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
            return
        }

        if (!currentInput.contains('.')) {
            currentInput += "."
        }
        state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
    }

    private fun handleToggleSign() {
        if (isResultDisplayed) {
            isResultDisplayed = false
            val toggled = if (state.displayValue.startsWith("-")) {
                state.displayValue.removePrefix("-")
            } else if (state.displayValue == "0") {
                "-0"
            } else {
                "-${state.displayValue}"
            }
            currentInput = toggled
            firstOperand = parseBigDecimal(toggled)
            expressionPrefix = ""
            isEnteringNewOperand = false
            state = state.copy(displayValue = currentInput, expression = "")
            return
        }

        if (isEnteringNewOperand) {
            currentInput = if (currentInput == "-0") "0" else "-0"
            state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
            return
        }

        currentInput = if (currentInput.startsWith("-")) {
            currentInput.removePrefix("-")
        } else if (currentInput == "0") {
            "-0"
        } else {
            "-$currentInput"
        }
        state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
    }

    private fun handlePercent() {
        val current = parseBigDecimal(state.displayValue)
        val result = current.divide(BigDecimal("100"))
        currentInput = formatBigDecimal(result)
        isPercentApplied = true
        state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
    }

    private fun handleBackspace() {
        if (isResultDisplayed) {
            currentInput = "0"
            isResultDisplayed = false
            isEnteringNewOperand = true
            expressionPrefix = ""
            state = state.copy(displayValue = "0", expression = "")
            return
        }

        if (isPercentApplied) {
            currentInput = "0"
            isPercentApplied = false
            isEnteringNewOperand = true
            state = state.copy(displayValue = "0", expression = buildOngoingExpression())
            return
        }

        if (isEnteringNewOperand) {
            return
        }

        currentInput = if (currentInput.length <= 1) {
            "0"
        } else if (currentInput.startsWith("-") && (currentInput.length == 2 || (currentInput.length == 3 && currentInput.endsWith(".")))) {
            "0"
        } else {
            val truncated = currentInput.dropLast(1)
            if (truncated == "-" || truncated.isEmpty()) "0" else truncated
        }
        state = state.copy(displayValue = currentInput, expression = buildOngoingExpression())
    }

    private fun buildOngoingExpression(): String {
        if (expressionPrefix.isEmpty()) {
            return ""
        }
        return if (isEnteringNewOperand) {
            if (currentInput == "-0") {
                "$expressionPrefix-0"
            } else {
                expressionPrefix.trimEnd()
            }
        } else {
            "$expressionPrefix$currentInput"
        }
    }

    private fun replaceTrailingOperatorInPrefix(newOp: CalculatorOperator) {
        val trimmed = expressionPrefix.trimEnd()
        val lastSpaceIdx = trimmed.lastIndexOf(' ')
        expressionPrefix = if (lastSpaceIdx != -1) {
            trimmed.substring(0, lastSpaceIdx + 1) + "${newOp.symbol} "
        } else {
            "${newOp.symbol} "
        }
    }

    private fun handleOperation(op: CalculatorOperator) {
        if (firstOperand == null || pendingOperator == null || isResultDisplayed) {
            val operand = parseBigDecimal(state.displayValue)
            firstOperand = operand
            pendingOperator = op
            isEnteringNewOperand = true
            isResultDisplayed = false
            isPercentApplied = false
            val formatted = formatBigDecimal(operand)
            expressionPrefix = "$formatted ${op.symbol} "
            state = state.copy(expression = "$formatted ${op.symbol}")
        } else if (isEnteringNewOperand) {
            // Consecutive operator replaces previous pending operator
            pendingOperator = op
            replaceTrailingOperatorInPrefix(op)
            state = state.copy(expression = expressionPrefix.trimEnd())
        } else {
            // Chaining evaluation: left-to-right immediate execution
            val second = parseBigDecimal(state.displayValue)
            val result = calculate(firstOperand!!, pendingOperator!!, second)
            if (result == null) {
                setError()
                return
            }
            firstOperand = result
            pendingOperator = op
            isEnteringNewOperand = true
            isResultDisplayed = false
            isPercentApplied = false
            expressionPrefix = "$expressionPrefix$currentInput ${op.symbol} "
            val formatted = formatBigDecimal(result)
            state = state.copy(
                displayValue = formatted,
                expression = expressionPrefix.trimEnd()
            )
        }
    }

    private fun handleEquals() {
        if (firstOperand == null || isResultDisplayed || isEnteringNewOperand) {
            return
        }

        val second = parseBigDecimal(state.displayValue)
        val result = calculate(firstOperand!!, pendingOperator!!, second)
        if (result == null) {
            setError()
            return
        }

        val formattedResult = formatBigDecimal(result)
        val fullExpression = "$expressionPrefix$currentInput ="
        val historyItem = CalculationHistoryItem(
            expression = fullExpression,
            result = formattedResult
        )

        firstOperand = result
        pendingOperator = null
        expressionPrefix = ""
        isResultDisplayed = true
        isEnteringNewOperand = true
        isPercentApplied = false

        state = state.copy(
            displayValue = formattedResult,
            expression = fullExpression,
            history = listOf(historyItem) + state.history
        )
    }

    private fun calculate(a: BigDecimal, op: CalculatorOperator, b: BigDecimal): BigDecimal? {
        return try {
            val result = when (op) {
                CalculatorOperator.ADD -> a.add(b)
                CalculatorOperator.SUBTRACT -> a.subtract(b)
                CalculatorOperator.MULTIPLY -> a.multiply(b)
                CalculatorOperator.DIVIDE -> {
                    if (b.compareTo(BigDecimal.ZERO) == 0) return null
                    try {
                        a.divide(b)
                    } catch (_: ArithmeticException) {
                        a.divide(b, 16, RoundingMode.HALF_UP)
                    }
                }
            }
            if (result.abs() >= maxAllowedMagnitude) null else result
        } catch (_: Exception) {
            null
        }
    }

    private fun parseBigDecimal(value: String): BigDecimal {
        return if (value == "-0") {
            BigDecimal.ZERO
        } else {
            BigDecimal(value)
        }
    }

    private fun formatBigDecimal(value: BigDecimal): String {
        return if (value.compareTo(BigDecimal.ZERO) == 0) {
            "0"
        } else {
            value.stripTrailingZeros().toPlainString()
        }
    }

    private fun setError() {
        expressionPrefix = ""
        state = state.copy(displayValue = "Error", isError = true)
    }
}
