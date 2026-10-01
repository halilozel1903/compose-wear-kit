package io.github.halilozel1903.wearkit

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import io.github.halilozel1903.wearkit.core.ArcSegment
import io.github.halilozel1903.wearkit.core.RingMath

/** Defaults for [ProgressRing] and [SegmentedRing]. */
public object RingDefaults {
    /** Stroke width that reads well on a 40 to 45 mm watch. */
    public val StrokeWidth: Dp = 8.dp

    /** Gap between the indicator and the track, and between segments. */
    public val Gap: Dp = 6.dp

    /** Inset from the screen edge when a ring hugs the bezel. */
    public val EdgeInset: Dp = 3.dp

    /** 12 o'clock. */
    public const val StartAngle: Float = RingMath.TOP

    /** Animation used when the progress changes. */
    public val ProgressAnimation: AnimationSpec<Float> = tween(durationMillis = 450)

    @Composable
    public fun indicatorColor(): Color = MaterialTheme.colorScheme.primary

    @Composable
    public fun trackColor(): Color = MaterialTheme.colorScheme.surfaceContainerHigh
}

/**
 * A circular progress ring with a gap between the filled indicator and the remaining track, drawn
 * from [startAngle] (12 o'clock) clockwise over [sweepAngle] degrees. Place it with
 * `Modifier.fillMaxSize()` to follow the bezel of a round screen, and put text in [content].
 *
 * @param progress `0..1`; values outside are clamped.
 * @param sweepAngle less than 360 for an open arc (for example 270 with `startAngle = 135f`).
 * @param animate animates progress changes with [RingDefaults.ProgressAnimation].
 */
@Composable
public fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = RingDefaults.indicatorColor(),
    trackColor: Color = RingDefaults.trackColor(),
    strokeWidth: Dp = RingDefaults.StrokeWidth,
    gap: Dp = RingDefaults.Gap,
    startAngle: Float = RingDefaults.StartAngle,
    sweepAngle: Float = 360f,
    animate: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val target = RingMath.clampProgress(progress)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = RingDefaults.ProgressAnimation,
        label = "ProgressRing",
    )
    val shown = if (animate) animated else target
    Box(
        modifier = modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = RingMath.ringRadius(size.minDimension, stroke)
            val arcs = RingMath.progressArcs(
                progress = shown,
                startAngle = startAngle,
                totalSweep = sweepAngle,
                gapDegrees = RingMath.degreesForLength(gap.toPx(), radius),
                capDegrees = RingMath.capDegrees(stroke, radius),
            )
            arcs.track?.let { drawRingArc(it, trackColor, stroke, radius) }
            arcs.indicator?.let { drawRingArc(it, color, stroke, radius) }
        }
        content()
    }
}

/**
 * A ring split into [segmentCount] equal segments, filled up to [progress]: steps toward a goal,
 * glasses of water, intervals of a workout. The segment holding the progress is filled partly.
 *
 * @param segmentColor color of a filled segment by index, for multi colored rings.
 */
@Composable
public fun SegmentedRing(
    segmentCount: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    segmentColor: @Composable (index: Int) -> Color = { RingDefaults.indicatorColor() },
    trackColor: Color = RingDefaults.trackColor(),
    strokeWidth: Dp = RingDefaults.StrokeWidth,
    gap: Dp = RingDefaults.Gap,
    startAngle: Float = RingDefaults.StartAngle,
    sweepAngle: Float = 360f,
    animate: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {},
) {
    require(segmentCount >= 1) { "segmentCount must be at least 1" }
    val target = RingMath.clampProgress(progress)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = RingDefaults.ProgressAnimation,
        label = "SegmentedRing",
    )
    val fills = RingMath.segmentFills(segmentCount, if (animate) animated else target)
    val colors = List(segmentCount) { index -> segmentColor(index) }
    SegmentedRingLayout(
        fills = fills,
        colors = colors,
        trackColor = trackColor,
        strokeWidth = strokeWidth,
        gap = gap,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        modifier = modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f, segmentCount) },
        content = content,
    )
}

/**
 * A segmented ring where each segment has its own fill in `0..1`, for example one segment per day
 * of the week.
 */
@Composable
public fun SegmentedRing(
    segmentFills: List<Float>,
    modifier: Modifier = Modifier,
    segmentColor: @Composable (index: Int) -> Color = { RingDefaults.indicatorColor() },
    trackColor: Color = RingDefaults.trackColor(),
    strokeWidth: Dp = RingDefaults.StrokeWidth,
    gap: Dp = RingDefaults.Gap,
    startAngle: Float = RingDefaults.StartAngle,
    sweepAngle: Float = 360f,
    content: @Composable BoxScope.() -> Unit = {},
) {
    require(segmentFills.isNotEmpty()) { "segmentFills must not be empty" }
    val colors = List(segmentFills.size) { index -> segmentColor(index) }
    SegmentedRingLayout(
        fills = segmentFills.map { RingMath.clampProgress(it) },
        colors = colors,
        trackColor = trackColor,
        strokeWidth = strokeWidth,
        gap = gap,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        modifier = modifier,
        content = content,
    )
}

@Composable
private fun SegmentedRingLayout(
    fills: List<Float>,
    colors: List<Color>,
    trackColor: Color,
    strokeWidth: Dp,
    gap: Dp,
    startAngle: Float,
    sweepAngle: Float,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = RingMath.ringRadius(size.minDimension, stroke)
            val segments = RingMath.segments(
                count = fills.size,
                gapDegrees = if (fills.size == 1) 0f else RingMath.degreesForLength(gap.toPx(), radius),
                startAngle = startAngle,
                totalSweep = sweepAngle,
                capDegrees = if (fills.size == 1) 0f else RingMath.capDegrees(stroke, radius),
            )
            segments.forEachIndexed { index, segment ->
                drawRingArc(segment, trackColor, stroke, radius)
                val fill = fills[index]
                if (fill > 0f) drawRingArc(segment.trimmed(fill), colors[index], stroke, radius)
            }
        }
        content()
    }
}

private fun DrawScope.drawRingArc(arc: ArcSegment, color: Color, stroke: Float, radius: Float) {
    val diameter = radius * 2f
    drawArc(
        color = color,
        startAngle = arc.startAngle,
        sweepAngle = arc.sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(diameter, diameter),
        style = Stroke(width = stroke, cap = if (arc.sweepAngle >= 360f) StrokeCap.Butt else StrokeCap.Round),
    )
}
