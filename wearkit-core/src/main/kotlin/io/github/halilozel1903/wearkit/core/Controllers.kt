package io.github.halilozel1903.wearkit.core

/**
 * A mutable [Stopwatch] driven by a [WearClock]. Every call reads the clock once, so tests can
 * drive it with a [ManualClock]:
 *
 * ```kotlin
 * val clock = ManualClock()
 * val controller = StopwatchController(clock)
 * controller.start()
 * clock.advanceBy(41_200)
 * controller.lap()
 * controller.elapsedMillis() // 41_200
 * ```
 */
public class StopwatchController(
    private val clock: WearClock,
    initial: Stopwatch = Stopwatch(),
) {
    /** The current value. Assign it to restore a saved stopwatch. */
    public var stopwatch: Stopwatch = initial

    public val status: RunStatus get() = stopwatch.status
    public val laps: List<Lap> get() = stopwatch.laps

    public fun elapsedMillis(): Long = stopwatch.elapsedAt(clock.nowMillis())
    public fun currentLapMillis(): Long = stopwatch.currentLapAt(clock.nowMillis())

    public fun start(): Unit = update { start(it) }
    public fun pause(): Unit = update { pause(it) }
    public fun toggle(): Unit = update { toggle(it) }
    public fun lap(): Unit = update { lap(it) }
    public fun reset(): Unit = update { reset() }

    private inline fun update(block: Stopwatch.(Long) -> Stopwatch) {
        stopwatch = stopwatch.block(clock.nowMillis())
    }
}

/** A mutable [CountdownTimer] driven by a [WearClock]. */
public class CountdownController(
    private val clock: WearClock,
    initial: CountdownTimer,
) {
    public var timer: CountdownTimer = initial

    public val status: RunStatus get() = timer.status

    public fun remainingMillis(): Long = timer.remainingAt(clock.nowMillis())
    public fun fractionRemaining(): Float = timer.fractionRemainingAt(clock.nowMillis())
    public fun isFinished(): Boolean = timer.isFinishedAt(clock.nowMillis())

    public fun start(): Unit = update { start(it) }
    public fun pause(): Unit = update { pause(it) }
    public fun toggle(): Unit = update { toggle(it) }
    public fun reset(): Unit = update { reset() }
    public fun plus(millis: Long): Unit = update { plus(millis, it) }

    private inline fun update(block: CountdownTimer.(Long) -> CountdownTimer) {
        timer = timer.block(clock.nowMillis())
    }
}
