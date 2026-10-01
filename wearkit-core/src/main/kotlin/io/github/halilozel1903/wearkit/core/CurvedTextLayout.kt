package io.github.halilozel1903.wearkit.core

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Where curved text sits on a round screen. */
public enum class BezelPosition(
    /** Center of the text in `drawArc` degrees (0 is 3 o'clock, clockwise). */
    public val anchorDegrees: Float,
) {
    Top(270f),
    Right(0f),
    Bottom(90f),
    Left(180f),
    ;

    /**
     * Text on the bottom half reads upright only when it runs counter clockwise; on the top half
     * it runs clockwise.
     */
    public val readsClockwise: Boolean get() = this != Bottom
}

/** A point relative to the center of the screen, in the same unit as the radius. */
public data class PolarPoint(val x: Float, val y: Float)

/** Angle math for laying out curved text and labels around a round screen. */
public object CurvedTextLayout {

    /** Converts a clock position (12 at the top, may be fractional) into `drawArc` degrees. */
    public fun clockPositionDegrees(hour: Float): Float = normalize(hour * 30f - 90f)

    /** Converts minutes or seconds on a dial (0 at the top, 60 per turn) into `drawArc` degrees. */
    public fun dialDegrees(value: Float, perTurn: Float = 60f): Float {
        require(perTurn > 0f) { "perTurn must be positive" }
        return normalize(value / perTurn * 360f - 90f)
    }

    /** Normalizes [degrees] into `0 until 360`. */
    public fun normalize(degrees: Float): Float {
        val r = degrees % 360f
        return if (r < 0f) r + 360f else r
    }

    /** The angle a run of text [textWidth] wide covers on a circle of [radius]. */
    public fun sweepDegrees(textWidth: Float, radius: Float): Float = RingMath.degreesForLength(textWidth, radius)

    /** Whether text [textWidth] wide fits in [maxSweepDegrees] at [radius]. */
    public fun fits(textWidth: Float, radius: Float, maxSweepDegrees: Float): Boolean =
        sweepDegrees(textWidth, radius) <= maxSweepDegrees

    /**
     * The center angle of each glyph when text is laid out on a circle and centered on
     * [anchorDegrees]. [glyphWidths] are the advances of each character. Clockwise text grows to
     * increasing angles; counter clockwise text (the bottom of a watch) grows to decreasing angles.
     */
    public fun glyphAngles(
        glyphWidths: List<Float>,
        radius: Float,
        anchorDegrees: Float,
        clockwise: Boolean = true,
    ): List<Float> {
        if (glyphWidths.isEmpty()) return emptyList()
        val total = sweepDegrees(glyphWidths.sum(), radius)
        val direction = if (clockwise) 1f else -1f
        var cursor = -total / 2f
        return buildList<Float>(glyphWidths.size) {
            for (width in glyphWidths) {
                val sweep = sweepDegrees(width, radius)
                add(normalize(anchorDegrees + direction * (cursor + sweep / 2f)))
                cursor += sweep
            }
        }
    }

    /**
     * Angles for [count] labels spread evenly around the dial, the first at [startDegrees]
     * (12 o'clock by default). Used for bezel numbers like 0, 5, 10 ... 55.
     */
    public fun evenAngles(count: Int, startDegrees: Float = RingMath.TOP): List<Float> {
        require(count >= 1) { "count must be at least 1" }
        return List(count) { index -> normalize(startDegrees + index * 360f / count) }
    }

    /** The point at [degrees] on a circle of [radius] around the center. */
    public fun pointAt(degrees: Float, radius: Float): PolarPoint {
        val radians = degrees * PI.toFloat() / 180f
        return PolarPoint(x = radius * cos(radians), y = radius * sin(radians))
    }

    /**
     * Shortens [text] with an ellipsis until it fits in [maxSweepDegrees], measuring with
     * [measure] (the width of a string in the radius unit). Returns the text unchanged when it fits.
     */
    public fun ellipsize(
        text: String,
        radius: Float,
        maxSweepDegrees: Float,
        ellipsis: String = "…",
        measure: (String) -> Float,
    ): String {
        if (fits(measure(text), radius, maxSweepDegrees)) return text
        var end = text.length
        while (end > 0) {
            val candidate = text.substring(0, end).trimEnd() + ellipsis
            if (fits(measure(candidate), radius, maxSweepDegrees)) return candidate
            end--
        }
        return if (fits(measure(ellipsis), radius, maxSweepDegrees)) ellipsis else ""
    }
}
