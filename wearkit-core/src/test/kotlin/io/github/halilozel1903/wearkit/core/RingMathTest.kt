package io.github.halilozel1903.wearkit.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class RingMathTest {

    private val eps = 0.001f

    @Test
    fun `sweep is clamped progress of the total`() {
        assertEquals(90f, RingMath.sweepFor(0.25f))
        assertEquals(0f, RingMath.sweepFor(-1f))
        assertEquals(360f, RingMath.sweepFor(3f))
        assertEquals(0f, RingMath.sweepFor(Float.NaN))
        assertEquals(135f, RingMath.sweepFor(0.5f, totalSweep = 270f))
    }

    @Test
    fun `lengths convert to degrees on a circle`() {
        // A quarter of the circumference of r = 100 is 90 degrees.
        assertEquals(90f, RingMath.degreesForLength((Math.PI * 50).toFloat(), 100f), eps)
        assertEquals(0f, RingMath.degreesForLength(10f, 0f))
        assertEquals(RingMath.degreesForLength(4f, 100f), RingMath.capDegrees(8f, 100f), eps)
        assertEquals(96f, RingMath.ringRadius(200f, 8f))
    }

    @Test
    fun `progress arcs leave a gap on both sides on a full circle`() {
        val arcs = RingMath.progressArcs(progress = 0.25f, gapDegrees = 4f)
        assertEquals(ArcSegment(-90f, 90f), arcs.indicator)
        assertEquals(4f, arcs.track!!.startAngle, eps)
        assertEquals(262f, arcs.track!!.sweepAngle, eps)
        assertEquals(266f, arcs.track!!.endAngle, eps) // 4 degrees before 270 (the start, -90)
    }

    @Test
    fun `progress arcs at the ends`() {
        val empty = RingMath.progressArcs(0f, gapDegrees = 4f)
        assertNull(empty.indicator)
        assertEquals(ArcSegment(-90f, 360f), empty.track)
        val full = RingMath.progressArcs(1f, gapDegrees = 4f, capDegrees = 2f)
        assertEquals(ArcSegment(-90f, 360f), full.indicator)
        assertNull(full.track)
    }

    @Test
    fun `progress arcs on a partial arc have one gap and cap room`() {
        val arcs = RingMath.progressArcs(0.5f, startAngle = 135f, totalSweep = 270f, gapDegrees = 6f, capDegrees = 1f)
        assertEquals(ArcSegment(136f, 133f), arcs.indicator)
        assertEquals(ArcSegment(277f, 127f), arcs.track)
    }

    @Test
    fun `a full circle of segments has as many gaps as segments`() {
        val segments = RingMath.segments(count = 4, gapDegrees = 10f)
        assertEquals(4, segments.size)
        segments.forEach { assertEquals(80f, it.sweepAngle, eps) }
        assertEquals(-85f, segments[0].startAngle, eps)
        assertEquals(5f, segments[1].startAngle, eps)
        // The gap after the last segment ends where the first begins.
        assertEquals(segments[0].startAngle + 360f - 10f, segments[3].endAngle, eps)
    }

    @Test
    fun `a partial arc of segments has count minus one gaps and caps shorten each segment`() {
        val segments = RingMath.segments(count = 3, gapDegrees = 15f, startAngle = 0f, totalSweep = 180f, capDegrees = 2f)
        segments.forEach { assertEquals(46f, it.sweepAngle, eps) }
        assertEquals(2f, segments[0].startAngle, eps)
        assertEquals(67f, segments[1].startAngle, eps)
        assertEquals(178f, segments[2].endAngle, eps)
    }

    @Test
    fun `one segment is the whole ring`() {
        assertEquals(listOf(ArcSegment(-90f, 360f)), RingMath.segments(1, gapDegrees = 20f))
        assertFailsWith<IllegalArgumentException> { RingMath.segments(0, 2f) }
    }

    @Test
    fun `segment fills spread progress over segments`() {
        val fills = RingMath.segmentFills(count = 4, progress = 0.6f)
        assertEquals(listOf(1f, 1f, 0.4f, 0f), fills.map { (it * 1000).toInt() / 1000f })
        assertEquals(2, RingMath.filledSegmentCount(4, 0.6f))
        assertEquals(List(4) { 0f }, RingMath.segmentFills(4, 0f))
        assertEquals(List(4) { 1f }, RingMath.segmentFills(4, 1f))
    }

    @Test
    fun `trimming an arc`() {
        assertEquals(ArcSegment(10f, 25f), ArcSegment(10f, 50f).trimmed(0.5f))
        assertEquals(ArcSegment(10f, 50f), ArcSegment(10f, 50f).trimmed(2f))
        assertFailsWith<IllegalArgumentException> { ArcSegment(0f, -1f) }
    }
}
