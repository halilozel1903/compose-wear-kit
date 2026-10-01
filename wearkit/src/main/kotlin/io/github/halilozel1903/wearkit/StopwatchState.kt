package io.github.halilozel1903.wearkit

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.halilozel1903.wearkit.core.Lap
import io.github.halilozel1903.wearkit.core.RunStatus
import io.github.halilozel1903.wearkit.core.Stopwatch
import io.github.halilozel1903.wearkit.core.WearClock
import kotlinx.coroutines.delay

/**
 * Compose state for a stopwatch: a core [Stopwatch] value plus a ticking [elapsedMillis] while it
 * runs. Create it with [rememberStopwatchState]; it survives configuration changes and process
 * death (it keeps `SystemClock.elapsedRealtime` based start times, which keep counting while the
 * process is gone).
 */
@Stable
public class StopwatchState(
    initial: Stopwatch = Stopwatch(),
    private val clock: WearClock = ElapsedRealtimeClock,
) {
    /** The current value. Set it to restore or preset a stopwatch. */
    public var stopwatch: Stopwatch by mutableStateOf(initial)

    private var tickMillis by mutableLongStateOf(clock.nowMillis())

    /** Elapsed time, updated every tick while running. */
    public val elapsedMillis: Long get() = stopwatch.elapsedAt(tickMillis)

    /** Time in the current lap, updated every tick while running. */
    public val currentLapMillis: Long get() = stopwatch.currentLapAt(tickMillis)

    public val status: RunStatus get() = stopwatch.status
    public val isRunning: Boolean get() = stopwatch.isRunning
    public val laps: List<Lap> get() = stopwatch.laps

    public fun start(): Unit = update { start(it) }
    public fun pause(): Unit = update { pause(it) }
    public fun toggle(): Unit = update { toggle(it) }
    public fun lap(): Unit = update { lap(it) }
    public fun reset(): Unit = update { reset() }

    /** Reads the clock again. [rememberStopwatchState] calls it while running. */
    public fun tick() {
        tickMillis = clock.nowMillis()
    }

    private inline fun update(block: Stopwatch.(Long) -> Stopwatch) {
        val now = clock.nowMillis()
        tickMillis = now
        stopwatch = stopwatch.block(now)
    }

    public companion object {
        /** Saves the status, the times and the lap durations. */
        public fun saver(clock: WearClock = ElapsedRealtimeClock): Saver<StopwatchState, Any> =
            listSaver<StopwatchState, Long>(
                save = { state ->
                    val watch = state.stopwatch
                    buildList<Long> {
                        add(watch.status.ordinal.toLong())
                        add(watch.accumulatedMillis)
                        add(watch.startedAtMillis ?: -1L)
                        watch.laps.forEach { add(it.lapMillis) }
                    }
                },
                restore = { saved ->
                    val status = RunStatus.entries[saved[0].toInt()]
                    var total = 0L
                    val laps = saved.drop(3).mapIndexed { index, duration ->
                        total += duration
                        Lap(number = index + 1, lapMillis = duration, totalMillis = total)
                    }
                    StopwatchState(
                        initial = Stopwatch(
                            status = status,
                            accumulatedMillis = saved[1],
                            startedAtMillis = saved[2].takeIf { status == RunStatus.Running },
                            laps = laps,
                        ),
                        clock = clock,
                    )
                },
            )
    }
}

/** `SystemClock.elapsedRealtime`: monotonic, and it keeps counting in deep sleep. */
public val ElapsedRealtimeClock: WearClock = WearClock { SystemClock.elapsedRealtime() }

/**
 * Remembers a [StopwatchState] and ticks it every [tickIntervalMillis] while it runs.
 *
 * @param initial the starting value, for example `Stopwatch.paused(...)` in previews.
 */
@Composable
public fun rememberStopwatchState(
    initial: Stopwatch = Stopwatch(),
    clock: WearClock = ElapsedRealtimeClock,
    tickIntervalMillis: Long = 33L,
): StopwatchState {
    val state = rememberSaveable(saver = StopwatchState.saver(clock)) {
        StopwatchState(initial, clock)
    }
    LaunchedEffect(state, state.isRunning, tickIntervalMillis) {
        state.tick()
        while (state.isRunning) {
            delay(tickIntervalMillis)
            state.tick()
        }
    }
    return state
}
