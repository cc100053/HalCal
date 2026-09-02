package com.sorobanzen.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayFormatTest {

    @Test
    fun `numbers are grouped in threes from the right`() {
        assertEquals("0", DisplayFormat.groupDigits("0"))
        assertEquals("999", DisplayFormat.groupDigits("999"))
        assertEquals("1,000", DisplayFormat.groupDigits("1000"))
        assertEquals("1,234,567", DisplayFormat.groupDigits("1234567"))
        assertEquals("-1,234", DisplayFormat.groupDigits("-1234"))
    }

    @Test
    fun `only the integer part is grouped`() {
        assertEquals("1,234.5678", DisplayFormat.groupDigits("1234.5678"))
        assertEquals("0.123456", DisplayFormat.groupDigits("0.123456"))
    }

    @Test
    fun `a half typed decimal keeps its trailing point`() {
        assertEquals("1,234.", DisplayFormat.groupDigits("1234."))
    }

    @Test
    fun `text without digits passes through untouched`() {
        assertEquals("エラー", DisplayFormat.groupDigits("エラー"))
    }

    @Test
    fun `expressions get glyphs, spacing and grouping together`() {
        assertEquals("1,234 + 5,678", DisplayFormat.expression("1234+5678"))
        assertEquals("12,000 × 3", DisplayFormat.expression("12000*3"))
        assertEquals("10,000 ÷ 4", DisplayFormat.expression("10000/4"))
    }

    @Test
    fun `a leading minus is a sign, not an operator`() {
        assertEquals("-500", DisplayFormat.expression("-500"))
        assertEquals("1,000 - 500", DisplayFormat.expression("1000-500"))
    }
}
