package io.github.halilozel1903.wearkit.sample

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ScreenScaffold
import io.github.halilozel1903.wearkit.StopwatchFace
import io.github.halilozel1903.wearkit.core.Stopwatch
import io.github.halilozel1903.wearkit.rememberStopwatchState

/** The stopwatch sample. [preset] fills it with fixed laps for the screenshot. */
@Composable
fun StopwatchScreen(preset: Stopwatch? = null) {
    val stopwatch = rememberStopwatchState(initial = preset ?: Stopwatch())
    // The seconds ring runs along the edge, so the clock at the top is hidden on this screen.
    ScreenScaffold(timeText = {}) {
        StopwatchFace(stopwatch)
    }
}
