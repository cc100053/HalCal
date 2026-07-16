package com.sorobanzen.app.domain

import java.util.Locale

data class CalculatorState(
    val expression: String,
    val display: String,
    val completedExpression: String? = null,
    val hasError: Boolean = false
)

class CalculatorEngine {
    private companion object {
        const val ERROR_DISPLAY = "エラー"
    }

    private var expression = ""
    private var display = "0"
    private var isResultDisplayed = false
    private var isAwaitingOperand = false

    fun press(key: String): CalculatorState {
        var completedExpression: String? = null

        when (key) {
            "AC" -> reset()
            "C" -> clearLastDigit()
            "+", "-", "×", "÷" -> setOperator(key)
            "=" -> completedExpression = evaluate()
            "." -> appendDecimalPoint()
            in "0".."9" -> appendDigit(key)
        }

        return CalculatorState(
            expression = expression,
            display = display,
            completedExpression = completedExpression,
            hasError = display == ERROR_DISPLAY
        )
    }

    fun loadResult(result: String): CalculatorState {
        val normalized = result.toDoubleOrNull()?.let { result } ?: "0"
        expression = ""
        display = normalized
        isResultDisplayed = true
        isAwaitingOperand = false
        return CalculatorState(expression, display)
    }

    private fun reset() {
        expression = ""
        display = "0"
        isResultDisplayed = false
        isAwaitingOperand = false
    }

    private fun clearLastDigit() {
        if (isResultDisplayed || display == ERROR_DISPLAY) {
            reset()
            return
        }
        display = display.dropLast(1).ifEmpty { "0" }
    }

    private fun setOperator(key: String) {
        val operator = when (key) {
            "×" -> "*"
            "÷" -> "/"
            else -> key
        }

        if (display == ERROR_DISPLAY) reset()

        when {
            isResultDisplayed -> expression = display + operator
            isAwaitingOperand && expression.isNotEmpty() -> {
                expression = expression.dropLast(1) + operator
            }
            else -> expression += display + operator
        }

        display = "0"
        isResultDisplayed = false
        isAwaitingOperand = true
    }

    private fun evaluate(): String? {
        if (isAwaitingOperand) return null
        val fullExpression = expression + display
        if (fullExpression.isEmpty()) return null

        val result = MathEvaluator.evaluate(fullExpression)
        if (!result.isFinite()) {
            display = ERROR_DISPLAY
            isResultDisplayed = true
            return null
        }

        display = formatResult(result)
        expression = fullExpression
        isResultDisplayed = true
        return fullExpression
    }

    private fun appendDecimalPoint() {
        if (isResultDisplayed || display == ERROR_DISPLAY) {
            display = "0."
            expression = ""
            isResultDisplayed = false
            isAwaitingOperand = false
        } else if (!display.contains('.')) {
            display += "."
        }
    }

    private fun appendDigit(digit: String) {
        if (isResultDisplayed || display == ERROR_DISPLAY) {
            expression = ""
            display = digit
            isResultDisplayed = false
        } else if (display == "0" || isAwaitingOperand) {
            display = digit
        } else {
            display += digit
        }
        isAwaitingOperand = false
    }

    private fun formatResult(result: Double): String =
        if (result % 1.0 == 0.0 && result in Long.MIN_VALUE.toDouble()..Long.MAX_VALUE.toDouble()) {
            result.toLong().toString()
        } else {
            String.format(Locale.ROOT, "%.4f", result).trimEnd('0').trimEnd('.')
        }
}
