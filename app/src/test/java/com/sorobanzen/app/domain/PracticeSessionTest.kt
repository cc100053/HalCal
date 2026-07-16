package com.sorobanzen.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PracticeSessionTest {

    @Test
    fun `a problem can only be submitted once`() {
        val session = PracticeSession()
        session.start(problem = "2 + 3", answer = 5)

        val first = session.submit("5")
        val duplicate = session.submit("5")

        assertEquals(true, first?.isCorrect)
        assertNull(duplicate)
        assertEquals(1, session.progress.score)
        assertEquals(1, session.progress.total)
    }

    @Test
    fun `a blank answer is ignored`() {
        val session = PracticeSession()
        session.start(problem = "7 - 2", answer = 5)

        val submission = session.submit("")

        assertNull(submission)
        assertEquals(0, session.progress.total)
    }
}
