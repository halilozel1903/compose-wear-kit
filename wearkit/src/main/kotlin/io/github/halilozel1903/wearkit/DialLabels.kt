package io.github.halilozel1903.wearkit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import io.github.halilozel1903.wearkit.core.CurvedTextLayout
import kotlin.math.roundToInt

/**
 * Upright labels spread evenly around a round screen, like the numbers on a stopwatch bezel
 * (`listOf("60", "5", "10", ...)`). The first label sits at 12 o'clock. Each label's center lies on
 * a circle [inset] in from the edge.
 */
@Composable
public fun DialLabels(
    labels: List<String>,
    modifier: Modifier = Modifier,
    inset: Dp = 18.dp,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    highlighted: Set<Int> = emptySet(),
    highlightColor: Color = MaterialTheme.colorScheme.primary,
) {
    if (labels.isEmpty()) return
    val angles = CurvedTextLayout.evenAngles(labels.size)
    Layout(
        modifier = modifier.fillMaxSize(),
        content = {
            labels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    style = style,
                    color = if (index in highlighted) highlightColor else color,
                )
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val radius = minOf(width, height) / 2f - inset.toPx()
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val point = CurvedTextLayout.pointAt(angles[index], radius)
                placeable.place(
                    x = (width / 2f + point.x - placeable.width / 2f).roundToInt(),
                    y = (height / 2f + point.y - placeable.height / 2f).roundToInt(),
                )
            }
        }
    }
}

/**
 * Tick marks around the edge of a round screen: [count] ticks, every [majorEvery]th one longer and
 * brighter. Combine with [DialLabels] for a stopwatch bezel.
 */
@Composable
public fun DialTicks(
    modifier: Modifier = Modifier,
    count: Int = 60,
    majorEvery: Int = 5,
    inset: Dp = 3.dp,
    minorLength: Dp = 4.dp,
    majorLength: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.outline,
    majorColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    require(count >= 1) { "count must be at least 1" }
    val angles = CurvedTextLayout.evenAngles(count)
    Canvas(modifier.fillMaxSize()) {
        val outer = size.minDimension / 2f - inset.toPx()
        angles.forEachIndexed { index, angle ->
            val major = majorEvery > 0 && index % majorEvery == 0
            val length = (if (major) majorLength else minorLength).toPx()
            val from = CurvedTextLayout.pointAt(angle, outer)
            val to = CurvedTextLayout.pointAt(angle, outer - length)
            drawLine(
                color = if (major) majorColor else color,
                start = center + Offset(from.x, from.y),
                end = center + Offset(to.x, to.y),
                strokeWidth = (if (major) 2.dp else 1.dp).toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}
