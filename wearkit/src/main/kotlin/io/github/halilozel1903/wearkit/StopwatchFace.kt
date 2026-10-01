package io.github.halilozel1903.wearkit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import io.github.halilozel1903.wearkit.core.BezelPosition
import io.github.halilozel1903.wearkit.core.DurationFormat
import io.github.halilozel1903.wearkit.core.RunStatus

/** Colors of a [StopwatchFace]. */
@Immutable
public data class StopwatchFaceColors(
    val ring: Color,
    val track: Color,
    val time: Color,
    val label: Color,
    val secondary: Color,
)

/** Texts of a [StopwatchFace], so apps can translate them. */
@Immutable
public data class StopwatchFaceLabels(
    val title: String = "STOPWATCH",
    val start: String = "Start",
    val pause: String = "Pause",
    val lap: String = "Lap",
    val reset: String = "Reset",
    val lapPrefix: String = "LAP",
    val best: String = "BEST",
)

/** Defaults for [StopwatchFace]. */
public object StopwatchFaceDefaults {
    public val RingStrokeWidth: Dp = 6.dp

    @Composable
    public fun colors(
        ring: Color = MaterialTheme.colorScheme.primary,
        track: Color = MaterialTheme.colorScheme.surfaceContainer,
        time: Color = MaterialTheme.colorScheme.onBackground,
        label: Color = MaterialTheme.colorScheme.primary,
        secondary: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): StopwatchFaceColors = StopwatchFaceColors(ring, track, time, label, secondary)
}

/**
 * A complete stopwatch screen for a round watch: a seconds ring around the edge, minute ticks,
 * the time with hundredths, the current lap, start/pause and lap/reset buttons, and the best lap
 * on the bottom bezel.
 *
 * ```kotlin
 * val stopwatch = rememberStopwatchState()
 * ScreenScaffold(timeText = {}) { StopwatchFace(stopwatch) }
 * ```
 */
@Composable
public fun StopwatchFace(
    state: StopwatchState,
    modifier: Modifier = Modifier,
    colors: StopwatchFaceColors = StopwatchFaceDefaults.colors(),
    labels: StopwatchFaceLabels = StopwatchFaceLabels(),
    showTicks: Boolean = true,
) {
    val elapsed = state.elapsedMillis
    val laps = state.laps
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ProgressRing(
            progress = (elapsed % 60_000L) / 60_000f,
            modifier = Modifier.fillMaxSize().padding(RingDefaults.EdgeInset),
            color = colors.ring,
            trackColor = colors.track,
            strokeWidth = StopwatchFaceDefaults.RingStrokeWidth,
            gap = 4.dp,
            animate = false,
        )
        if (showTicks) {
            DialTicks(
                modifier = Modifier.padding(RingDefaults.EdgeInset + StopwatchFaceDefaults.RingStrokeWidth + 3.dp),
                minorLength = 3.dp,
                majorLength = 6.dp,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (laps.isEmpty()) labels.title else "${labels.lapPrefix} ${laps.size + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.label,
            )
            Row(
                modifier = Modifier.semantics {
                    contentDescription = DurationFormat.stopwatch(elapsed)
                },
            ) {
                Text(
                    text = DurationFormat.clock(elapsed),
                    style = MaterialTheme.typography.numeralMedium,
                    color = colors.time,
                    modifier = Modifier.alignByBaseline(),
                )
                Text(
                    text = "." + DurationFormat.centis(elapsed),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.secondary,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            Text(
                text = if (laps.isEmpty()) " " else DurationFormat.stopwatch(state.currentLapMillis),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            StopwatchButtons(state, labels = labels)
        }
        val best = state.stopwatch.fastestLap
        if (best != null) {
            CurvedLabel(
                text = "${labels.best} ${labels.lapPrefix} ${best.number} · ${DurationFormat.stopwatch(best.lapMillis)}",
                position = BezelPosition.Bottom,
                color = colors.label,
                fontSize = 12.sp,
                inset = 22.dp,
            )
        }
    }
}

/** The two round buttons of a [StopwatchFace]: lap or reset on the left, start or pause on the right. */
@Composable
public fun StopwatchButtons(
    state: StopwatchState,
    modifier: Modifier = Modifier,
    labels: StopwatchFaceLabels = StopwatchFaceLabels(),
    buttonSize: Dp = 44.dp,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val running = state.isRunning
        FilledTonalIconButton(
            onClick = { if (running) state.lap() else state.reset() },
            enabled = state.status != RunStatus.Idle,
            modifier = Modifier.size(buttonSize),
        ) {
            WearKitIconImage(
                icon = if (running) WearKitIcon.Lap else WearKitIcon.Reset,
                contentDescription = if (running) labels.lap else labels.reset,
                size = 20.dp,
            )
        }
        FilledIconButton(
            onClick = { state.toggle() },
            modifier = Modifier.size(buttonSize),
            colors = IconButtonDefaults.filledIconButtonColors(),
        ) {
            WearKitIconImage(
                icon = if (running) WearKitIcon.Pause else WearKitIcon.Play,
                contentDescription = if (running) labels.pause else labels.start,
                size = 20.dp,
            )
        }
    }
}
