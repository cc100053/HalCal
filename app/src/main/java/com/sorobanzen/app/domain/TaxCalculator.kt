package com.sorobanzen.app.domain

import java.math.BigDecimal
import java.math.RoundingMode

object TaxCalculator {

    enum class TaxRate(val rate: BigDecimal) {
        STANDARD(BigDecimal("0.10")), // 10% standard rate
        REDUCED(BigDecimal("0.08"))   // 8% reduced rate (food, etc.)
    }

    data class TaxBreakdown(
        val originalAmount: BigDecimal,
        val taxRate: BigDecimal,
        val taxAmount: BigDecimal,
        val totalAmount: BigDecimal,
        val isAdded: Boolean
    )

    /**
     * Calculates tax details when adding tax (excluding to including).
     */
    fun addTax(amount: Double, rate: TaxRate): TaxBreakdown {
        require(amount.isFinite() && amount >= 0.0) { "Amount must be a finite, non-negative number" }
        val original = BigDecimal(amount.toString())
        val taxRate = rate.rate
        
        // Tax = original * rate (rounded down to nearest Yen, which is typical in Japan)
        val tax = original.multiply(taxRate).setScale(0, RoundingMode.DOWN)
        val total = original.add(tax)
        
        return TaxBreakdown(
            originalAmount = original,
            taxRate = taxRate,
            taxAmount = tax,
            totalAmount = total,
            isAdded = true
        )
    }

    /**
     * Calculates tax details when removing tax (including to excluding).
     */
    fun removeTax(amount: Double, rate: TaxRate): TaxBreakdown {
        require(amount.isFinite() && amount >= 0.0) { "Amount must be a finite, non-negative number" }
        val total = BigDecimal(amount.toString())
        val taxRate = rate.rate
        
        // Excl = total / (1 + rate) (rounded down to nearest Yen)
        val divider = BigDecimal("1.0").add(taxRate)
        val original = total.divide(divider, 0, RoundingMode.DOWN)
        val tax = total.subtract(original)
        
        return TaxBreakdown(
            originalAmount = original,
            taxRate = taxRate,
            taxAmount = tax,
            totalAmount = total,
            isAdded = false
        )
    }
}
