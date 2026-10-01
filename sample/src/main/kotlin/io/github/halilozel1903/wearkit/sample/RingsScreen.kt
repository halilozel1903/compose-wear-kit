package io.github.halilozel1903.wearkit.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.halilozel1903.wearkit.CurvedLabel
import io.github.halilozel1903.wearkit.ProgressRing
import io.github.halilozel1903.wearkit.SegmentedRing
import io.github.halilozel1903.wearkit.core.BezelPosition
import io.github.halilozel1903.wearkit.core.RotarySnap
import io.github.halilozel1903.wearkit.rememberRotaryStepAccumulator
import io.github.halilozel1903.wearkit.rotarySteps
import java.text.NumberFormat
import java.util.Locale

/**
 * An outer [ProgressRing] for steps and an inner [SegmentedRing] for glasses of water.
 * Turning the crown changes the steps in 250 step increments.
 */
@Composable
fun RingsScreen() {
    var steps by rememberSaveable { mutableIntStateOf(7_250) }
    val glasses = 5.5f
    val focusRequester = remember { FocusRequester() }
    val accumulator = rememberRotaryStepAccumulator()
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ScreenScaffold(timeText = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotarySteps(accumulator, focusRequester) { delta ->
                    steps = RotarySnap.stepValue(steps, delta, min = 0, max = 20_000, step = 250)
                },
            contentAlignment = Alignment.Center,
        ) {
            ProgressRing(
                progress = steps / StepGoal.toFloat(),
                modifier = Modifier.fillMaxSize().padding(4.dp),
                strokeWidth = 10.dp,
            )
            SegmentedRing(
                segmentCount = 8,
                progress = glasses / 8f,
                modifier = Modifier.fillMaxSize().padding(22.dp),
                segmentColor = { MaterialTheme.colorScheme.secondary },
                strokeWidth = 8.dp,
                gap = 5.dp,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = NumberFormat.getIntegerInstance(Locale.US).format(steps),
                    style = MaterialTheme.typography.numeralSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "of ${NumberFormat.getIntegerInstance(Locale.US).format(StepGoal)} steps",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "5½ of 8 glasses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            CurvedLabel(
                text = "TURN THE CROWN",
                position = BezelPosition.Bottom,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                inset = 38.dp,
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
            )
        }
    }
}
