package io.github.halilozel1903.wearkit.core

import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

/**
 * An arc in degrees, using the same convention as Compose `drawArc`: 0 is 3 o'clock, angles grow
 * clockwise, so -90 is 12 o'clock.
 */
public data class ArcSegment(
    val startAngle: Float,
    val sweepAngle: Float,
) {
    init {
        require(sweepAngle >= 0f) { "sweepAngle must not be negative ($sweepAngle)" }
    }

    public val endAngle: Float get() = startAngle + sweepAngle

    /** The first [fraction] (clamped to `0..1`) of this arc. */
    public fun trimmed(fraction: Float): ArcSegment =
        copy(sweepAngle = sweepAngle * fraction.coerceIn(0f, 1f))
}

/** The arcs a progress ring draws: the filled indicator and the remaining track. Either can be missing. */
public data class ProgressArcs(
    val indicator: ArcSegment?,
    val track: ArcSegment?,
)

/** Arc math for progress rings and segmented rings on round screens. */
public object RingMath {

    /** 12 o'clock in `drawArc` degrees. */
    public const val TOP: Float = -90f

    /** Normalizes [progress] to `0..1`; NaN counts as 0. */
    public fun clampProgress(progress: Float): Float = if (progress.isNaN()) 0f else progress.coerceIn(0f, 1f)

    /** Sweep of the filled part: [progress] of [totalSweep]. */
    public fun sweepFor(progress: Float, totalSweep: Float = 360f): Float = clampProgress(progress) * totalSweep

    /**
     * Degrees a straight [length] covers on a circle of [radius] (both in the same unit, usually
     * pixels). Converts a gap or a stroke cap measured in dp into an angle.
     */
    public fun degreesForLength(length: Float, radius: Float): Float {
        if (radius <= 0f || length <= 0f) return 0f
        return (length / radius * 180f / PI.toFloat())
    }

    /**
     * How many degrees a round stroke cap adds at each end of an arc with [strokeWidth] on
     * [radius]. Subtract it from both ends so the visible gap matches the requested one.
     */
    public fun capDegrees(strokeWidth: Float, radius: Float): Float = degreesForLength(strokeWidth / 2f, radius)

    /**
     * The radius of a ring with [strokeWidth] that fits inside a [diameter] wide box, measured to
     * the middle of the stroke.
     */
    public fun ringRadius(diameter: Float, strokeWidth: Float): Float = max(0f, (diameter - strokeWidth) / 2f)

    /**
     * Splits a ring into the indicator for [progress] and the remaining track, with [gapDegrees]
     * of empty space between them (like Material 3 progress indicators). On a full circle the gap
     * is also left where the indicator starts, so its round cap doesn't touch the track.
     *
     * @param capDegrees cap overhang from [capDegrees]; both arcs are shortened by it at each end.
     */
    public fun progressArcs(
        progress: Float,
        startAngle: Float = TOP,
        totalSweep: Float = 360f,
        gapDegrees: Float = 0f,
        capDegrees: Float = 0f,
    ): ProgressArcs {
        val p = clampProgress(progress)
        val total = totalSweep.coerceIn(0f, 360f)
        val fullCircle = total >= 360f
        val indicatorSweep = total * p
        val indicator = if (p == 0f) null else {
            val sweep = max(0f, indicatorSweep - 2 * capDegrees)
            if (p == 1f && fullCircle) {
                ArcSegment(startAngle, total) // a closed ring has no ends, so no caps or gaps
            } else {
                ArcSegment(startAngle + capDegrees, sweep)
            }
        }
        if (p == 1f) return ProgressArcs(indicator, null)
        val leadingGap = if (p == 0f) 0f else gapDegrees
        val trailingGap = if (fullCircle && p > 0f) gapDegrees else 0f
        val trackStart = startAngle + indicatorSweep + leadingGap
        val trackSweep = total - indicatorSweep - leadingGap - trailingGap
        val track = if (trackSweep <= 0f) null else if (p == 0f && fullCircle) {
            ArcSegment(startAngle, total)
        } else {
            val trimmed = trackSweep - 2 * capDegrees
            if (trimmed <= 0f) null else ArcSegment(trackStart + capDegrees, trimmed)
        }
        return ProgressArcs(indicator, track)
    }

    /**
     * Lays out [count] equal segments with [gapDegrees] between them. A full circle gets [count]
     * gaps (the last segment is followed by a gap before the first); a partial arc gets `count - 1`.
     * Each segment is shortened by [capDegrees] at both ends to make room for round caps.
     */
    public fun segments(
        count: Int,
        gapDegrees: Float,
        startAngle: Float = TOP,
        totalSweep: Float = 360f,
        capDegrees: Float = 0f,
    ): List<ArcSegment> {
        require(count >= 1) { "A segmented ring needs at least one segment" }
        require(gapDegrees >= 0f) { "gapDegrees must not be negative" }
        val total = totalSweep.coerceIn(0f, 360f)
        val gaps = if (count == 1) 0 else if (total >= 360f) count else count - 1
        val segmentSweep = max(0f, (total - gaps * gapDegrees) / count)
        val visibleSweep = max(0f, segmentSweep - 2 * capDegrees)
        // On a full circle the gaps sit around the start angle: half a gap before segment 0.
        val offset = if (total >= 360f && count > 1) gapDegrees / 2f else 0f
        return buildList<ArcSegment>(count) {
            for (index in 0 until count) {
                val start = startAngle + offset + index * (segmentSweep + gapDegrees)
                add(ArcSegment(startAngle = start + capDegrees, sweepAngle = visibleSweep))
            }
        }
    }

    /**
     * How full each of [count] segments is for an overall [progress]: segments before the progress
     * are 1, the one containing it is partial, the rest are 0.
     */
    public fun segmentFills(count: Int, progress: Float): List<Float> {
        require(count >= 1) { "A segmented ring needs at least one segment" }
        val scaled = clampProgress(progress) * count
        return List(count) { index -> min(1f, max(0f, scaled - index)) }
    }

    /** How many whole segments [progress] fills, for "3 of 8" labels. */
    public fun filledSegmentCount(count: Int, progress: Float): Int =
        segmentFills(count, progress).count { it >= 1f }
}
