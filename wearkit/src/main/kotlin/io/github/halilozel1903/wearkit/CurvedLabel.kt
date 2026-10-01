package io.github.halilozel1903.wearkit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.foundation.background
import androidx.wear.compose.foundation.basicCurvedText
import androidx.wear.compose.foundation.padding
import androidx.wear.compose.foundation.sizeIn
import androidx.wear.compose.material3.MaterialTheme
import io.github.halilozel1903.wearkit.core.BezelPosition

/** Defaults for [CurvedLabel]. */
public object CurvedLabelDefaults {
    /** Distance from the screen edge to the outer side of the text. */
    public val EdgeInset: Dp = 4.dp

    /** Text longer than this many degrees is cut with an ellipsis. */
    public const val MaxSweepDegrees: Float = 120f

    public val FontSize: TextUnit = 13.sp
    public val LetterSpacing: TextUnit = 0.6.sp

    @Composable
    public fun color(): Color = MaterialTheme.colorScheme.onBackground
}

/**
 * Text that follows the bezel of a round screen. On [BezelPosition.Bottom] it runs counter
 * clockwise so it still reads upright, which plain `CurvedLayout` text does not do on its own.
 *
 * Give it the full screen (the default `Modifier.fillMaxSize()` is applied for you) and stack it
 * over other content in a `Box`.
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     CurvedLabel("WEAR KIT")                                         // 12 o'clock
 *     CurvedLabel("LAP 3 · +0:01.20", position = BezelPosition.Bottom) // 6 o'clock, upright
 * }
 * ```
 *
 * @param inset distance from the edge, for example to clear a [ProgressRing] around the screen.
 * @param background when not [Color.Transparent], draws a rounded pill behind the text.
 * @param anchorDegrees overrides the center angle (`drawArc` degrees); `null` uses [position].
 */
@Composable
public fun CurvedLabel(
    text: String,
    modifier: Modifier = Modifier,
    position: BezelPosition = BezelPosition.Top,
    color: Color = CurvedLabelDefaults.color(),
    fontSize: TextUnit = CurvedLabelDefaults.FontSize,
    fontWeight: FontWeight? = FontWeight.Medium,
    letterSpacing: TextUnit = CurvedLabelDefaults.LetterSpacing,
    background: Color = Color.Transparent,
    inset: Dp = CurvedLabelDefaults.EdgeInset,
    maxSweepDegrees: Float = CurvedLabelDefaults.MaxSweepDegrees,
    anchorDegrees: Float? = null,
) {
    val style = CurvedTextStyle(
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
    )
    val hasBackground = background != Color.Transparent
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(inset)
            .semantics { contentDescription = text },
    ) {
        CurvedLayout(
            modifier = Modifier.fillMaxSize(),
            anchor = anchorDegrees ?: position.anchorDegrees,
            angularDirection = if (position.readsClockwise) {
                CurvedDirection.Angular.Normal
            } else {
                CurvedDirection.Angular.Reversed
            },
        ) {
            basicCurvedText(
                text,
                style,
                modifier = CurvedModifier
                    .sizeIn(maxSweepDegrees = maxSweepDegrees)
                    .let { if (hasBackground) it.background(background, StrokeCap.Round) else it }
                    .padding(radial = if (hasBackground) 3.dp else 0.dp, angular = if (hasBackground) 6.dp else 0.dp),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
