package com.sorobanzen.app.domain

import java.util.Stack

object MathEvaluator {

    /**
     * Evaluates a basic mathematical expression with +, -, *, / operators and brackets.
     * Returns Double.NaN on error.
     */
    fun evaluate(expression: String): Double {
        val sanitized = expression.replace(" ", "").replace("×", "*").replace("÷", "/")
        if (sanitized.isEmpty()) return 0.0
        
        return try {
            parseAndEvaluate(sanitized)
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private fun parseAndEvaluate(str: String): Double {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor // division
                } else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus

            var x: Double
            val startPos = pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            return x
        }

        nextChar()
        val result = parseExpression()
        if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
        return result
    }
}
