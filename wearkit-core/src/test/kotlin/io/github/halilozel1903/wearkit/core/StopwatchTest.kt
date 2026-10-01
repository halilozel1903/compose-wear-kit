package io.github.halilozel1903.wearkit.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class StopwatchTest {

    @Test
    fun `a new stopwatch is idle at zero`() {
        val watch = Stopwatch()
        assertEquals(RunStatus.Idle, watch.status)
        assertEquals(0L, watch.elapsedAt(123_456L))
        assertTrue(watch.laps.isEmpty())
    }

    @Test
    fun `running time follows the clock and pausing freezes it`() {
        var watch = Stopwatch().start(nowMillis = 1_000L)
        assertEquals(RunStatus.Running, watch.status)
        assertEquals(2_500L, watch.elapsedAt(3_500L))
        watch = watch.pause(nowMillis = 4_000L)
        assertEquals(RunStatus.Paused, watch.status)
        assertEquals(3_000L, watch.elapsedAt(50_000L))
    }

    @Test
    fun `resuming adds to the time counted before`() {
        val watch = Stopwatch().start(0L).pause(10_000L).start(20_000L)
        assertEquals(15_000L, watch.elapsedAt(25_000L))
        assertEquals(15_000L, watch.toggle(25_000L).elapsedAt(99_000L))
    }

    @Test
    fun `a clock reading before the start never goes negative`() {
        assertEquals(0L, Stopwatch().start(5_000L).elapsedAt(4_000L))
    }

    @Test
    fun `invalid transitions return the same value`() {
        val idle = Stopwatch()
        assertSame(idle, idle.pause(10L))
        assertSame(idle, idle.lap(10L))
        val running = idle.start(0L)
        assertSame(running, running.start(50L))
        val paused = running.pause(100L)
        assertSame(paused, paused.lap(200L))
    }

    @Test
    fun `laps record their own length and the running total`() {
        val clock = ManualClock()
        val controller = StopwatchController(clock)
        controller.start()
        clock.advanceBy(41_200L)
        controller.lap()
        clock.advanceBy(39_800L)
        controller.lap()
        clock.advanceBy(5_000L)
        assertEquals(
            listOf(Lap(1, 41_200L, 41_200L), Lap(2, 39_800L, 81_000L)),
            controller.laps,
        )
        assertEquals(5_000L, controller.currentLapMillis())
        assertEquals(86_000L, controller.elapsedMillis())
    }

    @Test
    fun `laps keep counting across a pause`() {
        val clock = ManualClock()
        val controller = StopwatchController(clock)
        controller.start()
        clock.advanceBy(10_000L)
        controller.pause()
        clock.advanceBy(60_000L)
        controller.start()
        clock.advanceBy(2_000L)
        controller.lap()
        assertEquals(Lap(1, 12_000L, 12_000L), controller.laps.single())
    }

    @Test
    fun `fastest and slowest need two laps`() {
        val one = Stopwatch.paused(50_000L, listOf(40_000L))
        assertNull(one.fastestLap)
        assertNull(one.slowestLap)
        val three = Stopwatch.paused(150_000L, listOf(41_000L, 38_500L, 44_000L))
        assertEquals(2, three.fastestLap?.number)
        assertEquals(3, three.slowestLap?.number)
    }

    @Test
    fun `reset clears time and laps`() {
        val clock = ManualClock()
        val controller = StopwatchController(clock)
        controller.toggle()
        clock.advanceBy(3_000L)
        controller.lap()
        controller.reset()
        assertEquals(Stopwatch(), controller.stopwatch)
        assertEquals(0L, controller.elapsedMillis())
    }

    @Test
    fun `paused factory rebuilds laps`() {
        val watch = Stopwatch.paused(754_560L, listOf(241_200L, 238_900L))
        assertEquals(RunStatus.Paused, watch.status)
        assertEquals(480_100L, watch.laps.last().totalMillis)
        assertEquals(274_460L, watch.currentLapAt(0L))
        assertEquals(RunStatus.Idle, Stopwatch.paused(0L).status)
        assertFailsWith<IllegalArgumentException> { Stopwatch.paused(10L, listOf(20L)) }
    }

    @Test
    fun `inconsistent values are rejected`() {
        assertFailsWith<IllegalArgumentException> { Stopwatch(status = RunStatus.Running) }
        assertFailsWith<IllegalArgumentException> { Stopwatch(status = RunStatus.Paused, startedAtMillis = 3L) }
        assertFailsWith<IllegalArgumentException> { Stopwatch(accumulatedMillis = -1L) }
    }

    @Test
    fun `manual clock never goes back`() {
        val clock = ManualClock(100L)
        assertEquals(150L, clock.advanceBy(50L))
        assertFailsWith<IllegalArgumentException> { clock.advanceBy(-1L) }
        assertFailsWith<IllegalArgumentException> { clock.set(10L) }
    }
}
