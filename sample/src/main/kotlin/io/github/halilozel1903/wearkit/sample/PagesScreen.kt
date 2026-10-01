package io.github.halilozel1903.wearkit.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.halilozel1903.wearkit.ProgressRing
import io.github.halilozel1903.wearkit.SegmentedRing
import io.github.halilozel1903.wearkit.SwipeDismissPages

/** Three pages; swiping right on the first one closes the demo. */
@Composable
fun PagesScreen(onClose: () -> Unit) {
    SwipeDismissPages(pageCount = 3, onDismiss = onClose) { page ->
        ScreenScaffold(timeText = {}) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (page) {
                    0 -> ProgressRing(
                        progress = 0.62f,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        color = MaterialTheme.colorScheme.error,
                    ) {
                        PageText(title = "142 bpm", body = "Cardio zone")
                    }
                    1 -> SegmentedRing(
                        segmentFills = listOf(1f, 1f, 0.4f, 1f, 1f, 0f, 0f),
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        segmentColor = { MaterialTheme.colorScheme.tertiary },
                    ) {
                        PageText(title = "4 of 7", body = "Active days")
                    }
                    else -> PageText(title = "Swipe right", body = "on the first page\nto close")
                }
            }
        }
    }
}

@Composable
private fun PageText(title: String, body: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
