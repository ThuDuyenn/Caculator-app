package com.madrelease.calculator.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {
    private val engine = CalculatorEngine()

    @Test fun addition() = assertDisplay("5", "2+3=")
    @Test fun subtraction() = assertDisplay("6", "10-4=")
    @Test fun multiplication() = assertDisplay("12", "3*4=")
    @Test fun division() = assertDisplay("4", "20/5=")
    @Test fun decimalAddition() = assertDisplay("15", "12.5+2.5=")
    @Test fun decimalArithmeticIsNotBinaryFloatingPoint() = assertDisplay("0.3", "0.1+0.2=")
    @Test fun nonTerminatingDivisionIsRounded() = assertDisplay("0.3333333333333333", "1/3=")
    @Test fun leftToRightChaining() = assertDisplay("20", "2+3*4=")
    @Test fun percentDividesByOneHundred() = assertDisplay("0.5", "50%=")
    @Test fun percentAppliesToCurrentOperand() = assertDisplay("200.1", "200+10%=")
    @Test fun percentCanBeAppliedTwice() = assertDisplay("0.005", "50%%")
    @Test fun negativeNumber() = assertDisplay("-5", "5s")
    @Test fun signBeforeNumber() = assertDisplay("-5", "s5")
    @Test fun negativeSecondOperand() = assertDisplay("7", "10+s3=")
    @Test fun toggleSignTwice() = assertDisplay("5", "5ss")
    @Test fun signOfResultCanBeUsedInNextOperation() = assertDisplay("-10", "2+3=s*2=")
    @Test fun clearRemovesPendingOperation() = assertDisplay("4", "12+3c4=")
    @Test fun backspaceRemovesLastDigit() = assertDisplay("12", "123b")
    @Test fun backspaceRemovesDecimalPoint() = assertDisplay("12", "12.b")
    @Test fun backspaceAtZeroIsSafe() = assertDisplay("0", "bbb")
    @Test fun backspaceOfNegativeSingleDigitResetsToZero() = assertDisplay("0", "5sb")
    @Test fun backspaceBeforeSecondOperandPreservesOperation() = assertDisplay("5", "2+b3=")
    @Test fun backspaceAfterResultClearsResult() = assertDisplay("0", "2+3=b")
    @Test fun repeatedDecimalIsIgnored() = assertDisplay("1.23", "1.2..3")
    @Test fun decimalStartsWithZero() = assertDisplay("0.25", ".25")
    @Test fun decimalAfterOperatorStartsNewOperand() = assertDisplay("2.5", "2+.5=")
    @Test fun consecutiveOperatorsReplacePendingOperator() = assertDisplay("6", "8+*/-2=")
    @Test fun equalsOnEmptyCalculatorIsSafe() = assertDisplay("0", "=")
    @Test fun equalsWithoutSecondOperandIsNoOp() = assertDisplay("5", "2+=3=")
    @Test fun repeatedEqualsDoesNotRepeatOperation() = assertDisplay("5", "2+3===")
    @Test fun digitAfterResultStartsNewCalculation() = assertDisplay("7", "2+3=7")
    @Test fun decimalAfterResultStartsNewCalculation() = assertDisplay("0.7", "2+3=.7")
    @Test fun operatorAfterResultContinuesCalculation() = assertDisplay("20", "2+3=*4=")
    @Test fun leadingZeroesAreNotAccumulated() = assertDisplay("5", "0005")
    @Test fun inputIsLimitedToSixteenDigits() = assertDisplay("1234567890123456", "12345678901234567")
    @Test fun typingAfterPercentReplacesTransformedOperand() = assertDisplay("203", "200+10%3=")
    @Test fun deletingPercentResultClearsOnlyCurrentOperand() = assertDisplay("202", "200+10%b2=")

    @Test fun divisionByZeroShowsError() {
        press("10/0=")
        assertEquals("Error", engine.state.displayValue)
        assertTrue(engine.state.isError)
    }

    @Test fun divisionByZeroDuringChainShowsError() {
        press("10/0+")
        assertTrue(engine.state.isError)
    }

    @Test fun negativeZeroCannotBeADivisor() {
        press("10/0s=")
        assertTrue(engine.state.isError)
    }

    @Test fun errorRemainsUntilClear() {
        press("10/0=12+.s%b=")
        assertEquals("Error", engine.state.displayValue)
    }

    @Test fun clearRecoversFromError() {
        assertDisplay("5", "10/0=c2+3=")
        assertFalse(engine.state.isError)
        assertEquals("2 + 3 =", engine.state.expression)
    }

    @Test fun invalidDigitShowsRecoverableError() {
        for (invalid in listOf(-1, 10, Int.MAX_VALUE)) {
            engine.onAction(CalculatorAction.Digit(invalid))
            assertTrue(engine.state.isError)
            engine.onAction(CalculatorAction.Clear)
            assertEquals(CalculatorState(), engine.state)
        }
    }

    @Test fun veryLargeResultIsBoundedAndRecoverable() {
        press("9999999999999999")
        repeat(8) { press("*9999999999999999=") }
        assertTrue(engine.state.isError)
        assertDisplay("2", "c1+1=")
    }

    @Test fun expressionTracksOperatorReplacement() {
        press("8+-")
        assertEquals("8 −", engine.state.expression)
    }

    @Test fun clearResetsExpression() {
        press("8+2=c")
        assertEquals("0", engine.state.displayValue)
        assertEquals("", engine.state.expression)
        assertFalse(engine.state.isError)
        assertEquals(1, engine.state.history.size)
    }

    @Test fun equalsAddsItemToHistory() {
        press("5*6=")
        assertEquals(1, engine.state.history.size)
        assertEquals("5 × 6 =", engine.state.history.first().expression)
        assertEquals("30", engine.state.history.first().result)
    }

    @Test fun clearHistoryEmptiesList() {
        press("5*6=")
        assertEquals(1, engine.state.history.size)
        engine.onAction(CalculatorAction.ClearHistory)
        assertTrue(engine.state.history.isEmpty())
    }

    @Test fun restoreHistoryLoadsPreviousResult() {
        press("5*6=")
        val item = engine.state.history.first()
        press("c10+")
        engine.onAction(CalculatorAction.RestoreHistory(item))
        assertEquals("30", engine.state.displayValue)
        assertEquals("5 × 6 =", engine.state.expression)
        assertFalse(engine.state.isError)
    }

    @Test fun expressionShowsFullCalculationBeforeEquals() {
        press("21s*2")
        assertEquals("-21 × 2", engine.state.expression)
        assertEquals("2", engine.state.displayValue)
    }

    @Test fun expressionAppendsEqualsAndCalculatesResultOnEquals() {
        press("21s*2=")
        assertEquals("-21 × 2 =", engine.state.expression)
        assertEquals("-42", engine.state.displayValue)
    }

    @Test fun expressionUpdatesOnBackspaceDuringSecondOperand() {
        press("10+25b")
        assertEquals("10 + 2", engine.state.expression)
        assertEquals("2", engine.state.displayValue)
    }

    @Test fun expressionUpdatesOnDecimalDuringSecondOperand() {
        press("5+3.")
        assertEquals("5 + 3.", engine.state.expression)
        assertEquals("3.", engine.state.displayValue)
    }

    @Test fun expressionUpdatesDuringChainedOperations() {
        press("2+3*4")
        assertEquals("2 + 3 × 4", engine.state.expression)
        assertEquals("4", engine.state.displayValue)

        press("=")
        assertEquals("2 + 3 × 4 =", engine.state.expression)
        assertEquals("20", engine.state.displayValue)
    }

    @Test fun expressionContinuesChainedOperationsUntilEquals() {
        press("2-4-7-2-4-252-2-2-2-")
        assertEquals("2 − 4 − 7 − 2 − 4 − 252 − 2 − 2 − 2 −", engine.state.expression)
        assertEquals("-273", engine.state.displayValue)

        press("1=")
        assertEquals("2 − 4 − 7 − 2 − 4 − 252 − 2 − 2 − 2 − 1 =", engine.state.expression)
        assertEquals("-274", engine.state.displayValue)
    }

    private fun assertDisplay(expected: String, keys: String) {
        press(keys)
        assertEquals(keys, expected, engine.state.displayValue)
    }

    private fun press(keys: String) {
        keys.forEach { key ->
            val action = when (key) {
                in '0'..'9' -> CalculatorAction.Digit(key.digitToInt())
                '+' -> CalculatorAction.Operation(CalculatorOperator.ADD)
                '-' -> CalculatorAction.Operation(CalculatorOperator.SUBTRACT)
                '*' -> CalculatorAction.Operation(CalculatorOperator.MULTIPLY)
                '/' -> CalculatorAction.Operation(CalculatorOperator.DIVIDE)
                '.' -> CalculatorAction.Decimal
                '=' -> CalculatorAction.Equals
                'c' -> CalculatorAction.Clear
                'b' -> CalculatorAction.Backspace
                's' -> CalculatorAction.ToggleSign
                '%' -> CalculatorAction.Percent
                else -> error("Unknown test key: $key")
            }
            engine.onAction(action)
        }
    }
}
