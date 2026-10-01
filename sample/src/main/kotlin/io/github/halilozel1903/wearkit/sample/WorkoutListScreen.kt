package io.github.halilozel1903.wearkit.sample

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import io.github.halilozel1903.wearkit.ProgressRing
import io.github.halilozel1903.wearkit.RotaryList
import io.github.halilozel1903.wearkit.RotaryMode
import io.github.halilozel1903.wearkit.core.DurationFormat

/** A [RotaryList] in snap mode: each crown detent moves one workout. */
@Composable
fun WorkoutListScreen() {
    val listState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    val accents = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
    )
    val longest = SampleWorkouts.maxOf { it.durationMillis }
    ScreenScaffold(scrollState = listState) { padding ->
        RotaryList(state = listState, contentPadding = padding, rotaryMode = RotaryMode.Snap) {
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text("Workouts")
                }
            }
            items(SampleWorkouts) { workout ->
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    icon = {
                        ProgressRing(
                            progress = workout.durationMillis / longest.toFloat(),
                            color = accents[workout.color],
                            strokeWidth = 4.dp,
                            gap = 2.dp,
                            animate = false,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    secondaryLabel = { Text("${workout.detail} · ${DurationFormat.compact(workout.durationMillis)}") },
                    label = { Text(workout.name) },
                )
            }
            item {
                TitleCard(
                    onClick = {},
                    title = { Text("This week") },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text(DurationFormat.compact(SampleWorkouts.sumOf { it.durationMillis }) + " in ${SampleWorkouts.size} workouts")
                }
            }
        }
    }
}
