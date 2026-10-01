package io.github.halilozel1903.wearkit.core

/** Where a [Stopwatch] or [CountdownTimer] is in its life cycle. */
public enum class RunStatus {
    /** Never started, or reset. Shows zero (or the full duration for a countdown). */
    Idle,

    /** Counting. */
    Running,

    /** Stopped, keeping the time counted so far. */
    Paused,
}

/**
 * One lap of a [Stopwatch].
 *
 * @property number 1 based lap number.
 * @property lapMillis how long this lap took.
 * @property totalMillis the stopwatch time when the lap was recorded.
 */
public data class Lap(
    val number: Int,
    val lapMillis: Long,
    val totalMillis: Long,
)

/**
 * An immutable stopwatch. Every transition takes the current time and returns a new value, so the
 * stopwatch can be stored in Compose state, saved and tested with fake times.
 *
 * Transitions that make no sense in the current state (pausing an idle stopwatch, a lap while
 * paused) return the same value instead of throwing, so buttons can call them blindly.
 *
 * ```kotlin
 * var watch = Stopwatch()
 * watch = watch.start(nowMillis = 0)
 * watch = watch.lap(nowMillis = 41_200)    // Lap(1, lapMillis = 41_200, totalMillis = 41_200)
 * watch = watch.pause(nowMillis = 60_000)
 * watch.elapsedAt(nowMillis = 99_999)      // 60_000, a paused stopwatch doesn't move
 * ```
 */
public data class Stopwatch(
    val status: RunStatus = RunStatus.Idle,
    /** Time counted before the current run started. */
    val accumulatedMillis: Long = 0L,
    /** When the current run started, `null` unless [status] is [RunStatus.Running]. */
    val startedAtMillis: Long? = null,
    /** Recorded laps, oldest first. */
    val laps: List<Lap> = emptyList(),
) {
    init {
        require(accumulatedMillis >= 0) { "accumulatedMillis must not be negative" }
        require((status == RunStatus.Running) == (startedAtMillis != null)) {
            "startedAtMillis must be set exactly when the stopwatch is running"
        }
    }

    public val isRunning: Boolean get() = status == RunStatus.Running

    /** Total counted time at [nowMillis]. */
    public fun elapsedAt(nowMillis: Long): Long {
        val start = startedAtMillis ?: return accumulatedMillis
        return accumulatedMillis + (nowMillis - start).coerceAtLeast(0L)
    }

    /** Time since the last lap (or since the start) at [nowMillis]. */
    public fun currentLapAt(nowMillis: Long): Long =
        (elapsedAt(nowMillis) - (laps.lastOrNull()?.totalMillis ?: 0L)).coerceAtLeast(0L)

    /** Starts an idle stopwatch or resumes a paused one. */
    public fun start(nowMillis: Long): Stopwatch =
        if (isRunning) this else copy(status = RunStatus.Running, startedAtMillis = nowMillis)

    /** Pauses a running stopwatch, keeping the elapsed time. */
    public fun pause(nowMillis: Long): Stopwatch =
        if (!isRunning) this else copy(
            status = RunStatus.Paused,
            accumulatedMillis = elapsedAt(nowMillis),
            startedAtMillis = null,
        )

    /** Starts or resumes when not running, pauses when running. */
    public fun toggle(nowMillis: Long): Stopwatch = if (isRunning) pause(nowMillis) else start(nowMillis)

    /** Records a lap. Only a running stopwatch records laps. */
    public fun lap(nowMillis: Long): Stopwatch {
        if (!isRunning) return this
        val total = elapsedAt(nowMillis)
        val lap = Lap(number = laps.size + 1, lapMillis = currentLapAt(nowMillis), totalMillis = total)
        return copy(laps = laps + lap)
    }

    /** Back to zero with no laps. */
    public fun reset(): Stopwatch = Stopwatch()

    /** The fastest lap, or `null` with fewer than two laps (one lap can't be compared). */
    public val fastestLap: Lap?
        get() = if (laps.size < 2) null else laps.minBy { it.lapMillis }

    /** The slowest lap, or `null` with fewer than two laps. */
    public val slowestLap: Lap?
        get() = if (laps.size < 2) null else laps.maxBy { it.lapMillis }

    public companion object {
        /**
         * A paused stopwatch showing [elapsedMillis], with laps rebuilt from their durations.
         * Useful for previews, screenshots and restoring saved state.
         */
        public fun paused(elapsedMillis: Long, lapDurations: List<Long> = emptyList()): Stopwatch {
            require(elapsedMillis >= lapDurations.sum()) { "Laps can't add up to more than the elapsed time" }
            var total = 0L
            val laps = lapDurations.mapIndexed { index, duration ->
                require(duration >= 0) { "Lap durations must not be negative" }
                total += duration
                Lap(number = index + 1, lapMillis = duration, totalMillis = total)
            }
            val status = if (elapsedMillis == 0L && laps.isEmpty()) RunStatus.Idle else RunStatus.Paused
            return Stopwatch(status = status, accumulatedMillis = elapsedMillis, laps = laps)
        }
    }
}
