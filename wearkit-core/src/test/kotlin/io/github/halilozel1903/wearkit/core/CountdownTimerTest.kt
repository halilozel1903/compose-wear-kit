package io.github.halilozel1903.wearkit.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CountdownTimerTest {

    private val fiveMinutes = 5 * 60_000L

    @Test
    fun `an idle timer shows its full length`() {
        val timer = CountdownTimer(fiveMinutes)
        assertEquals(fiveMinutes, timer.remainingAt(999_999L))
        assertEquals(1f, timer.fractionRemainingAt(0L))
        assertFalse(timer.isFinishedAt(999_999L))
    }

    @Test
    fun `counts down while running and stops at zero`() {
        val timer = CountdownTimer(fiveMinutes).start(nowMillis = 0L)
        assertEquals(210_000L, timer.remainingAt(90_000L))
        assertEquals(0.7f, timer.fractionRemainingAt(90_000L), 0.0001f)
        assertEquals(0L, timer.remainingAt(fiveMinutes + 10_000L))
        assertTrue(timer.isFinishedAt(fiveMinutes))
        assertEquals(1f, timer.fractionElapsedAt(fiveMinutes * 2))
    }

    @Test
    fun `pause and resume with a controller`() {
        val clock = ManualClock()
        val controller = CountdownController(clock, CountdownTimer(60_000L))
        controller.start()
        clock.advanceBy(20_000L)
        controller.pause()
        clock.advanceBy(100_000L)
        assertEquals(40_000L, controller.remainingMillis())
        controller.toggle()
        clock.advanceBy(40_000L)
        assertTrue(controller.isFinished())
        controller.reset()
        assertEquals(RunStatus.Idle, controller.status)
        assertEquals(60_000L, controller.remainingMillis())
    }

    @Test
    fun `adding time keeps what was counted`() {
        val clock = ManualClock()
        val controller = CountdownController(clock, CountdownTimer(60_000L))
        controller.start()
        clock.advanceBy(50_000L)
        controller.plus(60_000L)
        assertEquals(70_000L, controller.remainingMillis())
        controller.plus(-500_000L)
        assertEquals(0L, controller.remainingMillis())
        assertEquals(50_000L, controller.timer.durationMillis)
    }

    @Test
    fun `a zero length timer finishes as soon as it starts`() {
        val timer = CountdownTimer(0L)
        assertEquals(0f, timer.fractionElapsedAt(0L))
        assertFalse(timer.isFinishedAt(0L))
        val started = timer.start(0L)
        assertTrue(started.isFinishedAt(0L))
        assertEquals(1f, started.fractionElapsedAt(0L))
    }
}
