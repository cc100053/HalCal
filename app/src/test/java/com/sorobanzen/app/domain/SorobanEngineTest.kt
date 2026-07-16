package com.sorobanzen.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SorobanEngineTest {

    @Test
    fun `large Japanese numbers use four digit units`() {
        assertEquals("一億二千三百四十五万六千七百八十九", SorobanEngine.convertToKanji(123_456_789))
    }
}
