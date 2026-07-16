package com.sorobanzen.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun `entering a second operator replaces the pending operator`() {
        val calculator = CalculatorEngine()

        calculator.press("1")
        calculator.press("+")
        calculator.press("×")
        val result = calculator.press("2")
        val completed = calculator.press("=")

        assertEquals("1*", result.expression)
        assertEquals("2", completed.display)
    }

    @Test
    fun `loading a negative history result preserves its sign`() {
        val calculator = CalculatorEngine()

        val loaded = calculator.loadResult("-42")

        assertEquals("-42", loaded.display)
        assertEquals("", loaded.expression)
    }

    @Test
    fun `calculation errors use the Japanese display label`() {
        val calculator = CalculatorEngine()

        calculator.press("1")
        calculator.press("÷")
        calculator.press("0")
        val result = calculator.press("=")

        assertEquals("エラー", result.display)
        assertEquals(true, result.hasError)
    }

    @Test
    fun `repeated equals keeps the completed result stable`() {
        val calculator = CalculatorEngine()

        calculator.press("2")
        calculator.press("+")
        calculator.press("3")
        val completed = calculator.press("=")
        val repeated = calculator.press("=")

        assertEquals("5", completed.display)
        assertEquals("5", repeated.display)
        assertNull(repeated.completedExpression)
    }

    @Test
    fun `equals without an operation does not create a completed calculation`() {
        val calculator = CalculatorEngine()

        calculator.press("7")
        val result = calculator.press("=")

        assertEquals("7", result.display)
        assertNull(result.completedExpression)
    }

    @Test
    fun `non finite history results are rejected`() {
        val calculator = CalculatorEngine()

        assertEquals("0", calculator.loadResult("NaN").display)
        assertEquals("0", calculator.loadResult("Infinity").display)
    }

    @Test
    fun `manual input is capped to a readable length`() {
        val calculator = CalculatorEngine()

        repeat(20) { calculator.press("9") }

        assertEquals("9999999999999999", calculator.press("9").display)
    }
}
