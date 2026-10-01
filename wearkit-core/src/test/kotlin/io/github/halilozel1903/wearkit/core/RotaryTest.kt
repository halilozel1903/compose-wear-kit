package io.github.halilozel1903.wearkit.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RotaryTest {

    @Test
    fun `small deltas add up to whole steps and keep the remainder`() {
        val accumulator = RotaryStepAccumulator(stepPixels = 48f)
        assertEquals(0, accumulator.accumulate(30f, 0L))
        assertEquals(1, accumulator.accumulate(30f, 16L))
        assertEquals(12f, accumulator.pendingPixels)
        assertEquals(2, accumulator.accumulate(90f, 32L))
        assertEquals(6f, accumulator.pendingPixels)
    }

    @Test
    fun `counter clockwise gives negative steps`() {
        val accumulator = RotaryStepAccumulator(stepPixels = 10f)
        assertEquals(-3, accumulator.accumulate(-35f, 0L))
        assertEquals(-5f, accumulator.pendingPixels)
    }

    @Test
    fun `reversing direction drops the pending rotation`() {
        val accumulator = RotaryStepAccumulator(stepPixels = 50f)
        accumulator.accumulate(40f, 0L)
        assertEquals(0, accumulator.accumulate(-20f, 10L))
        assertEquals(-20f, accumulator.pendingPixels)
        val keeping = RotaryStepAccumulator(stepPixels = 50f, resetOnReverse = false)
        keeping.accumulate(40f, 0L)
        keeping.accumulate(-20f, 10L)
        assertEquals(20f, keeping.pendingPixels)
    }

    @Test
    fun `a pause drops the pending rotation`() {
        val accumulator = RotaryStepAccumulator(stepPixels = 50f, idleResetMillis = 300L)
        accumulator.accumulate(40f, 0L)
        assertEquals(0, accumulator.accumulate(20f, 1_000L))
        assertEquals(20f, accumulator.pendingPixels)
        accumulator.reset()
        assertEquals(0f, accumulator.pendingPixels)
    }

    @Test
    fun `zero and NaN deltas are ignored`() {
        val accumulator = RotaryStepAccumulator(stepPixels = 10f)
        assertEquals(0, accumulator.accumulate(0f, 0L))
        assertEquals(0, accumulator.accumulate(Float.NaN, 1L))
        assertEquals(0f, accumulator.pendingPixels)
        assertFailsWith<IllegalArgumentException> { RotaryStepAccumulator(stepPixels = 0f) }
    }

    @Test
    fun `step index clamps or wraps`() {
        assertEquals(4, RotarySnap.stepIndex(2, 5, itemCount = 5))
        assertEquals(0, RotarySnap.stepIndex(2, -9, itemCount = 5))
        assertEquals(2, RotarySnap.stepIndex(4, 3, itemCount = 5, wrap = true))
        assertEquals(4, RotarySnap.stepIndex(0, -1, itemCount = 5, wrap = true))
        assertEquals(-1, RotarySnap.stepIndex(0, 1, itemCount = 0))
    }

    @Test
    fun `nearest index rounds to the closest item`() {
        assertEquals(2, RotarySnap.nearestIndex(130f, 60f, 10))
        assertEquals(3, RotarySnap.nearestIndex(150f, 60f, 10))
        assertEquals(9, RotarySnap.nearestIndex(5_000f, 60f, 10))
        assertEquals(0, RotarySnap.nearestIndex(-40f, 60f, 10))
    }

    @Test
    fun `step value for pickers`() {
        assertEquals(15, RotarySnap.stepValue(5, 2, min = 0, max = 60, step = 5))
        assertEquals(60, RotarySnap.stepValue(55, 4, min = 0, max = 60, step = 5))
        assertEquals(1, RotarySnap.stepValue(59, 2, min = 0, max = 59, wrap = true))
        assertEquals(58, RotarySnap.stepValue(0, -2, min = 0, max = 59, wrap = true))
        assertFailsWith<IllegalArgumentException> { RotarySnap.stepValue(0, 1, min = 5, max = 1) }
    }

    @Test
    fun `fast flings skip snapping`() {
        assertTrue(RotarySnap.isFastFling(-3_000f))
        assertFalse(RotarySnap.isFastFling(500f))
    }
}
