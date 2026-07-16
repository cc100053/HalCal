package com.sorobanzen.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun `traditional unit conversions round trip without losing precision`() {
        val meters = 12.345

        assertEquals(meters, UnitConverter.shakuToMeters(UnitConverter.metersToShaku(meters)), 1e-10)
        assertEquals(meters, UnitConverter.kenToMeters(UnitConverter.metersToKen(meters)), 1e-10)
    }
}
