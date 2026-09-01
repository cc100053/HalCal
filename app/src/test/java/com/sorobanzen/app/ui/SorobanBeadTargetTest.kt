package com.sorobanzen.app.ui

import com.sorobanzen.app.ui.components.earthBeadTarget
import com.sorobanzen.app.ui.components.earthDragTarget
import com.sorobanzen.app.ui.components.nearestEarthBead
import org.junit.Assert.assertEquals
import org.junit.Test

class SorobanBeadTargetTest {

    // Geometry sampled from a portrait canvas: raised stack sits under the beam,
    // lowered stack rests on the bottom frame, with dead space in between.
    private val firstActiveY = 532f
    private val lastInactiveY = 1121f
    private val pitch = 36.8f

    private fun target(touchY: Float, activeCount: Int) =
        earthBeadTarget(touchY, activeCount, firstActiveY, lastInactiveY, pitch)

    private fun activeY(index: Int) = firstActiveY + index * pitch
    private fun inactiveY(index: Int) = lastInactiveY - (3 - index) * pitch

    @Test
    fun `tapping a lowered bead raises exactly that bead and the ones below it`() {
        assertEquals(1, target(inactiveY(0), activeCount = 0))
        assertEquals(2, target(inactiveY(1), activeCount = 0))
        assertEquals(3, target(inactiveY(2), activeCount = 0))
        assertEquals(4, target(inactiveY(3), activeCount = 0))
    }

    @Test
    fun `tapping a raised bead lowers it together with the beads above it`() {
        assertEquals(2, target(activeY(2), activeCount = 3))
        assertEquals(1, target(activeY(1), activeCount = 3))
        assertEquals(0, target(activeY(0), activeCount = 3))
    }

    @Test
    fun `dead space between the stacks resolves to the closer bead`() {
        val gapTop = activeY(1) + (inactiveY(2) - activeY(1)) * 0.25f
        val gapBottom = activeY(1) + (inactiveY(2) - activeY(1)) * 0.75f

        assertEquals(1, target(gapTop, activeCount = 2))
        assertEquals(3, target(gapBottom, activeCount = 2))
    }

    private fun drag(touchY: Float, startCount: Int, grabbedIndex: Int) =
        earthDragTarget(touchY, startCount, grabbedIndex, firstActiveY, lastInactiveY, pitch)

    @Test
    fun `dragging a lowered bead up settles it past the midpoint of its travel`() {
        val grabbed = nearestEarthBead(inactiveY(1), 0, firstActiveY, lastInactiveY, pitch)
        assertEquals(1, grabbed)

        val travelMid = (activeY(1) + inactiveY(1)) / 2f
        assertEquals(0, drag(inactiveY(1) - 10f, startCount = 0, grabbedIndex = grabbed))
        assertEquals(0, drag(travelMid + 10f, startCount = 0, grabbedIndex = grabbed))
        assertEquals(2, drag(travelMid - 10f, startCount = 0, grabbedIndex = grabbed))
        assertEquals(2, drag(activeY(1), startCount = 0, grabbedIndex = grabbed))
    }

    @Test
    fun `dragging a raised bead down settles it past the midpoint of its travel`() {
        val grabbed = nearestEarthBead(activeY(2), 3, firstActiveY, lastInactiveY, pitch)
        assertEquals(2, grabbed)

        val travelMid = (activeY(2) + inactiveY(2)) / 2f
        assertEquals(3, drag(activeY(2) + 10f, startCount = 3, grabbedIndex = grabbed))
        assertEquals(3, drag(travelMid - 10f, startCount = 3, grabbedIndex = grabbed))
        assertEquals(2, drag(travelMid + 10f, startCount = 3, grabbedIndex = grabbed))
    }

    @Test
    fun `dragging back before release restores the starting count`() {
        val travelMid = (activeY(0) + inactiveY(0)) / 2f

        assertEquals(1, drag(travelMid - 40f, startCount = 0, grabbedIndex = 0))
        assertEquals(0, drag(travelMid + 40f, startCount = 0, grabbedIndex = 0))
    }
}
