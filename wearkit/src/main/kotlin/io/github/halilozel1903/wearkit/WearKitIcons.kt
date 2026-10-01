package io.github.halilozel1903.wearkit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.LocalContentColor

/** Small drawn icons used by [StopwatchFace], so the library needs no icon dependency. */
public enum class WearKitIcon { Play, Pause, Lap, Reset }

/**
 * Draws a [WearKitIcon] in [tint] (the current content color by default, so it follows the
 * button it sits in).
 */
@Composable
public fun WearKitIconImage(
    icon: WearKitIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = LocalContentColor.current,
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Canvas(modifier.size(size).then(semanticsModifier)) {
        val w = this.size.width
        val h = this.size.height
        when (icon) {
            WearKitIcon.Play -> {
                val path = Path().apply {
                    moveTo(w * 0.28f, h * 0.18f)
                    lineTo(w * 0.84f, h * 0.5f)
                    lineTo(w * 0.28f, h * 0.82f)
                    close()
                }
                drawPath(path, tint)
                drawPath(path, tint, style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))
            }
            WearKitIcon.Pause -> {
                val barWidth = w * 0.22f
                val radius = CornerRadius(barWidth / 3f)
                drawRoundRect(tint, Offset(w * 0.22f, h * 0.18f), Size(barWidth, h * 0.64f), radius)
                drawRoundRect(tint, Offset(w * 0.56f, h * 0.18f), Size(barWidth, h * 0.64f), radius)
            }
            WearKitIcon.Lap -> {
                val stroke = w * 0.1f
                drawLine(tint, Offset(w * 0.26f, h * 0.12f), Offset(w * 0.26f, h * 0.9f), stroke, StrokeCap.Round)
                val flag = Path().apply {
                    moveTo(w * 0.26f, h * 0.14f)
                    lineTo(w * 0.8f, h * 0.14f)
                    lineTo(w * 0.66f, h * 0.33f)
                    lineTo(w * 0.8f, h * 0.52f)
                    lineTo(w * 0.26f, h * 0.52f)
                    close()
                }
                drawPath(flag, tint)
            }
            WearKitIcon.Reset -> {
                val stroke = w * 0.1f
                val inset = w * 0.18f
                drawArc(
                    color = tint,
                    startAngle = -60f,
                    sweepAngle = 300f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(w - 2 * inset, h - 2 * inset),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                // Arrow head at the end of the arc (the -60 degree point, upper right).
                val head = Path().apply {
                    moveTo(w * 0.60f, h * 0.06f)
                    lineTo(w * 0.86f, h * 0.20f)
                    lineTo(w * 0.62f, h * 0.36f)
                    close()
                }
                drawPath(head, tint)
            }
        }
    }
}
