package com.sorobanzen.app.domain

import org.junit.Assert.assertEquals
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
}
