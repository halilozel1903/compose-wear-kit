package io.github.halilozel1903.wearkit.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.halilozel1903.wearkit.CurvedLabel
import io.github.halilozel1903.wearkit.DialLabels
import io.github.halilozel1903.wearkit.DialTicks
import io.github.halilozel1903.wearkit.core.BezelPosition

/** Curved labels on the top and bottom bezel (both upright), ticks and dial numbers. */
@Composable
fun CurvedScreen() {
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CurvedLabel(
                text = "COMPOSE WEAR KIT",
                position = BezelPosition.Top,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                background = MaterialTheme.colorScheme.primaryContainer,
                inset = 5.dp,
            )
            CurvedLabel(
                text = "TEXT FOLLOWS THE BEZEL",
                position = BezelPosition.Bottom,
                color = MaterialTheme.colorScheme.secondary,
                inset = 6.dp,
            )
            DialTicks(modifier = Modifier.padding(28.dp))
            DialLabels(
                labels = (0 until 12).map { if (it == 0) "60" else (it * 5).toString() },
                modifier = Modifier.padding(28.dp),
                inset = 20.dp,
                highlighted = setOf(0, 3, 6, 9),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Curved", style = MaterialTheme.typography.titleMedium)
                Text(
                    "labels & dials",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
