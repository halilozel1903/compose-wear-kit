package io.github.halilozel1903.wearkit.core

/**
 * An immutable countdown timer. Like [Stopwatch], every transition takes the current time.
 * A running timer whose time is up stays [RunStatus.Running]; ask [isFinishedAt].
 *
 * ```kotlin
 * var timer = CountdownTimer(durationMillis = 5 * 60_000)
 * timer = timer.start(nowMillis = 0)
 * timer.remainingAt(nowMillis = 90_000)          // 210_000
 * timer.fractionRemainingAt(nowMillis = 90_000)  // 0.7f
 * ```
 */
public data class CountdownTimer(
    val durationMillis: Long,
    val status: RunStatus = RunStatus.Idle,
    /** Time counted down before the current run started. */
    val accumulatedMillis: Long = 0L,
    /** When the current run started, `null` unless [status] is [RunStatus.Running]. */
    val startedAtMillis: Long? = null,
) {
    init {
        require(durationMillis >= 0) { "durationMillis must not be negative" }
        require(accumulatedMillis >= 0) { "accumulatedMillis must not be negative" }
        require((status == RunStatus.Running) == (startedAtMillis != null)) {
            "startedAtMillis must be set exactly when the timer is running"
        }
    }

    public val isRunning: Boolean get() = status == RunStatus.Running

    /** Time counted down at [nowMillis], never more than [durationMillis]. */
    public fun elapsedAt(nowMillis: Long): Long {
        val start = startedAtMillis ?: return accumulatedMillis.coerceAtMost(durationMillis)
        return (accumulatedMillis + (nowMillis - start).coerceAtLeast(0L)).coerceAtMost(durationMillis)
    }

    /** Time left at [nowMillis]. */
    public fun remainingAt(nowMillis: Long): Long = durationMillis - elapsedAt(nowMillis)

    /** `true` once the full duration has been counted down. A zero length timer is finished as soon as it starts. */
    public fun isFinishedAt(nowMillis: Long): Boolean = status != RunStatus.Idle && remainingAt(nowMillis) == 0L

    /** Elapsed part of the duration in `0..1`. */
    public fun fractionElapsedAt(nowMillis: Long): Float =
        if (durationMillis == 0L) (if (status == RunStatus.Idle) 0f else 1f)
        else elapsedAt(nowMillis).toFloat() / durationMillis

    /** Remaining part of the duration in `0..1`, what a countdown ring usually shows. */
    public fun fractionRemainingAt(nowMillis: Long): Float = 1f - fractionElapsedAt(nowMillis)

    /** Starts an idle timer or resumes a paused one. A finished timer stays finished. */
    public fun start(nowMillis: Long): CountdownTimer =
        if (isRunning) this else copy(status = RunStatus.Running, startedAtMillis = nowMillis)

    /** Pauses a running timer. */
    public fun pause(nowMillis: Long): CountdownTimer =
        if (!isRunning) this else copy(
            status = RunStatus.Paused,
            accumulatedMillis = elapsedAt(nowMillis),
            startedAtMillis = null,
        )

    public fun toggle(nowMillis: Long): CountdownTimer = if (isRunning) pause(nowMillis) else start(nowMillis)

    /** Back to the full duration, not running. */
    public fun reset(): CountdownTimer = CountdownTimer(durationMillis)

    /**
     * Adds (or with a negative value removes) time, keeping what was already counted.
     * The duration never drops below the time already counted down, or below zero.
     */
    public fun plus(millis: Long, nowMillis: Long): CountdownTimer {
        val counted = elapsedAt(nowMillis)
        val duration = (durationMillis + millis).coerceAtLeast(counted).coerceAtLeast(0L)
        return copy(durationMillis = duration)
    }
}
