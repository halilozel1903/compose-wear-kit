package io.github.halilozel1903.wearkit.core

/** A duration split into clock parts. */
public data class DurationParts(
    val hours: Long,
    val minutes: Int,
    val seconds: Int,
    val centis: Int,
) {
    public companion object {
        /** Splits [millis] (negative counts as zero), rounding down to hundredths. */
        public fun of(millis: Long): DurationParts {
            val ms = millis.coerceAtLeast(0L)
            return DurationParts(
                hours = ms / 3_600_000L,
                minutes = ((ms / 60_000L) % 60L).toInt(),
                seconds = ((ms / 1_000L) % 60L).toInt(),
                centis = ((ms / 10L) % 100L).toInt(),
            )
        }
    }
}

/** Short, watch friendly duration texts. Negative durations are shown as zero unless noted. */
public object DurationFormat {

    /** `"4:05"`, `"12:34"`, `"1:02:03"`: minutes and seconds, hours only when needed. Rounds down. */
    public fun clock(millis: Long): String {
        val p = DurationParts.of(millis)
        return if (p.hours > 0) "${p.hours}:${two(p.minutes)}:${two(p.seconds)}" else "${p.minutes}:${two(p.seconds)}"
    }

    /** `"12:34.56"`, `"1:02:03.45"`: [clock] plus hundredths, the classic stopwatch display. */
    public fun stopwatch(millis: Long): String = "${clock(millis)}.${centis(millis)}"

    /** Only the hundredths, `"56"`, for a smaller second line under [clock]. */
    public fun centis(millis: Long): String = two(DurationParts.of(millis).centis)

    /**
     * A countdown display: like [clock] but rounds up to the next second, so a timer shows `"0:01"`
     * until it really reaches zero and starts at its full length.
     */
    public fun countdown(remainingMillis: Long): String {
        val ms = remainingMillis.coerceAtLeast(0L)
        return clock((ms + 999L) / 1_000L * 1_000L)
    }

    /** `"1h 5m"`, `"5m 3s"`, `"42s"`, `"0s"`: two units at most, rounded down. */
    public fun compact(millis: Long): String {
        val p = DurationParts.of(millis)
        return when {
            p.hours > 0 -> if (p.minutes > 0) "${p.hours}h ${p.minutes}m" else "${p.hours}h"
            p.minutes > 0 -> if (p.seconds > 0) "${p.minutes}m ${p.seconds}s" else "${p.minutes}m"
            else -> "${p.seconds}s"
        }
    }

    /** A signed difference such as `"+0:01.20"` or `"-0:00.80"`, for comparing laps. */
    public fun delta(millis: Long): String {
        val sign = if (millis < 0) "-" else "+"
        val abs = if (millis == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(millis)
        return sign + stopwatch(abs)
    }

    /** `"Lap 3"`. */
    public fun lapLabel(number: Int): String = "Lap $number"

    private fun two(value: Int): String = if (value < 10) "0$value" else value.toString()
}
