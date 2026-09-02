package com.sorobanzen.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitConverterTest {

    private fun convert(category: String, from: String, to: String, value: Double): Double =
        UnitConverter.convertFrom(category, from, value).first { it.first.key == to }.second

    @Test
    fun `conversion round trips through the base unit without losing precision`() {
        val meters = 12.345

        assertEquals(meters, convert("length", "shaku", "m", convert("length", "m", "shaku", meters)), 1e-10)
        assertEquals(meters, convert("length", "ken", "m", convert("length", "m", "ken", meters)), 1e-10)
    }

    @Test
    fun `converting between two traditional units skips the metric round trip`() {
        // 1 ken is 6 shaku by definition, and 1 shaku is 10 sun.
        assertEquals(6.0, convert("length", "ken", "shaku", 1.0), 1e-10)
        assertEquals(10.0, convert("length", "shaku", "sun", 1.0), 1e-10)
        assertEquals(2.0, convert("area", "tsubo", "jo", 1.0), 1e-10)
        assertEquals(1000.0, convert("weight", "kan", "momme", 1.0), 1e-10)
    }

    @Test
    fun `results exclude the input unit and keep the declared order`() {
        val results = UnitConverter.convertFrom("length", "shaku", 1.0)

        assertEquals(listOf("m", "sun", "ken"), results.map { it.first.key })
        assertTrue(UnitConverter.unitsByCategory.keys.all { UnitConverter.baseUnit(it)?.inBase == 1.0 })
    }

    @Test
    fun `an unknown category or unit converts to nothing`() {
        assertTrue(UnitConverter.convertFrom("time", "m", 1.0).isEmpty())
        assertTrue(UnitConverter.convertFrom("length", "mile", 1.0).isEmpty())
    }
}
