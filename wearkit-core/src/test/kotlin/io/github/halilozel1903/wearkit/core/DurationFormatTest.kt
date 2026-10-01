package io.github.halilozel1903.wearkit.core

import kotlin.test.Test
import kotlin.test.assertEquals

class DurationFormatTest {

    private val minute = 60_000L
    private val hour = 60 * minute

    @Test
    fun `parts split and round down`() {
        assertEquals(DurationParts(1, 2, 3, 45), DurationParts.of(hour + 2 * minute + 3_459L))
        assertEquals(DurationParts(0, 0, 0, 0), DurationParts.of(-500L))
    }

    @Test
    fun `clock shows hours only when needed`() {
        assertEquals("0:00", DurationFormat.clock(0L))
        assertEquals("4:05", DurationFormat.clock(4 * minute + 5_999L))
        assertEquals("12:34", DurationFormat.clock(12 * minute + 34_000L))
        assertEquals("1:02:03", DurationFormat.clock(hour + 2 * minute + 3_000L))
        assertEquals("0:00", DurationFormat.clock(-1L))
    }

    @Test
    fun `stopwatch adds hundredths`() {
        assertEquals("12:34.56", DurationFormat.stopwatch(754_560L))
        assertEquals("0:00.09", DurationFormat.stopwatch(99L))
        assertEquals("1:02:03.45", DurationFormat.stopwatch(hour + 2 * minute + 3_450L))
        assertEquals("56", DurationFormat.centis(754_569L))
    }

    @Test
    fun `countdown rounds up to the next second`() {
        assertEquals("5:00", DurationFormat.countdown(5 * minute))
        assertEquals("0:01", DurationFormat.countdown(1L))
        assertEquals("4:59", DurationFormat.countdown(5 * minute - 1_000L))
        assertEquals("0:00", DurationFormat.countdown(0L))
        assertEquals("0:00", DurationFormat.countdown(-20L))
    }

    @Test
    fun `compact keeps two units`() {
        assertEquals("1h 5m", DurationFormat.compact(hour + 5 * minute + 59_000L))
        assertEquals("2h", DurationFormat.compact(2 * hour))
        assertEquals("5m 3s", DurationFormat.compact(5 * minute + 3_000L))
        assertEquals("48m", DurationFormat.compact(48 * minute))
        assertEquals("42s", DurationFormat.compact(42_000L))
        assertEquals("0s", DurationFormat.compact(0L))
    }

    @Test
    fun `delta is signed`() {
        assertEquals("+0:01.20", DurationFormat.delta(1_200L))
        assertEquals("-0:00.80", DurationFormat.delta(-800L))
        assertEquals("+0:00.00", DurationFormat.delta(0L))
        assertEquals("Lap 3", DurationFormat.lapLabel(3))
    }
}
