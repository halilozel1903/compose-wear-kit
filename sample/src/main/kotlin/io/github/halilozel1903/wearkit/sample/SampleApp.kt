package io.github.halilozel1903.wearkit.sample

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwipeToDismissBox
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import io.github.halilozel1903.wearkit.RotaryList

/**
 * A menu of demos. Opening one shows it on top of the menu; swiping right (or the back button)
 * returns. The pages demo handles the swipe itself with `SwipeDismissPages`.
 */
@Composable
fun SampleApp(scene: Scene?) {
    var open by rememberSaveable { mutableStateOf(scene?.demo) }
    val current = open
    BackHandler(enabled = current != null) { open = null }
    SwipeToDismissBox(
        onDismissed = { open = null },
        backgroundKey = "menu",
        contentKey = current ?: "menu",
        userSwipeEnabled = current != null && current != Demo.Pages,
    ) { isBackground ->
        if (isBackground || current == null) {
            MenuScreen(onOpen = { open = it })
        } else {
            DemoScreen(current, fromScene = scene?.demo == current, onClose = { open = null })
        }
    }
}

@Composable
private fun DemoScreen(demo: Demo, fromScene: Boolean, onClose: () -> Unit) {
    when (demo) {
        Demo.Stopwatch -> StopwatchScreen(preset = if (fromScene) PresetStopwatch else null)
        Demo.Rings -> RingsScreen()
        Demo.Workouts -> WorkoutListScreen()
        Demo.Curved -> CurvedScreen()
        Demo.Pages -> PagesScreen(onClose = onClose)
    }
}

@Composable
private fun MenuScreen(onOpen: (Demo) -> Unit) {
    val listState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    ScreenScaffold(scrollState = listState) { padding ->
        RotaryList(state = listState, contentPadding = padding) {
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text("Wear Kit")
                }
            }
            items(Demo.entries) { demo ->
                Button(
                    onClick = { onOpen(demo) },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                    secondaryLabel = { Text(demo.subtitle) },
                    label = { Text(demo.title) },
                )
            }
        }
    }
}
