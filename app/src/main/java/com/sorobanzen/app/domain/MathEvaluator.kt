package com.sorobanzen.app.domain

object MathEvaluator {

    /**
     * Evaluates a basic mathematical expression with +, -, *, / operators and brackets.
     * Returns Double.NaN on error.
     */
    fun evaluate(expression: String): Double {
        val sanitized = expression.replace(" ", "").replace("×", "*").replace("÷", "/")
        if (sanitized.isEmpty()) return 0.0
        
        return try {
            ExpressionParser(sanitized).parse()
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private class ExpressionParser(val str: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else return x
            }
        }

        private fun parseTerm(): Double {
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

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus

            var x: Double
            val startPos = pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                if (!eat(')'.code)) {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            return x
        }
    }
}
