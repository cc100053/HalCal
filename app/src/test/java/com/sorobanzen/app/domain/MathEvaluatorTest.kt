package com.sorobanzen.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathEvaluatorTest {

    @Test
    fun `calculator respects operator precedence`() {
        assertEquals(14.0, MathEvaluator.evaluate("2+3*4"), 0.0)
    }

    @Test
    fun `calculator rejects an unclosed parenthesis`() {
        assertTrue(MathEvaluator.evaluate("(2+3").isNaN())
    }
}
