package com.sorobanzen.app.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class TaxCalculatorTest {

    @Test(expected = IllegalArgumentException::class)
    fun `tax calculation rejects negative amounts`() {
        TaxCalculator.addTax(-1.0, TaxCalculator.TaxRate.STANDARD)
    }

    @Test
    fun `standard tax is added and rounded down to whole yen`() {
        val result = TaxCalculator.addTax(1_009.9, TaxCalculator.TaxRate.STANDARD)

        assertEquals(BigDecimal("100"), result.taxAmount)
        assertEquals(BigDecimal("1109.9"), result.totalAmount)
    }

    @Test
    fun `tax included amount is converted back to its pre-tax amount`() {
        val result = TaxCalculator.removeTax(1_100.0, TaxCalculator.TaxRate.STANDARD)

        assertEquals(BigDecimal("1000"), result.originalAmount)
        assertEquals(BigDecimal("100.0"), result.taxAmount)
    }
}
