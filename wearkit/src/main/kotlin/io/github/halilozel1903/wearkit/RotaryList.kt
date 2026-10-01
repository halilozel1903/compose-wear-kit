package io.github.halilozel1903.wearkit

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableBehavior
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import io.github.halilozel1903.wearkit.core.RotaryStepAccumulator

/** How a [RotaryList] reacts to the crown or rotating bezel. */
public enum class RotaryMode {
    /** Scrolls smoothly with the crown, with fling, like the system lists. */
    Scroll,

    /** Moves one item per detent and settles with the item centered; good for short lists and pickers. */
    Snap,
}

/** Defaults for [RotaryList], [RotaryScalingList] and [rotarySteps]. */
public object RotaryListDefaults {
    public val ItemSpacing: Dp = 4.dp

    /** Rotation in pixels for one step of [rotarySteps]; about one detent on a Pixel Watch crown. */
    public const val StepPixels: Float = 48f
}

/**
 * A [TransformingLazyColumn] (the Material 3 list for Wear OS, items morph as they reach the
 * edges) that scrolls with the crown out of the box, smoothly or one item at a time with
 * [RotaryMode.Snap].
 *
 * Put it in a `ScreenScaffold(scrollState = state)` so it gets the scroll indicator and the
 * padding that keeps the first and last item clear of the round edge:
 *
 * ```kotlin
 * val listState = rememberTransformingLazyColumnState()
 * ScreenScaffold(scrollState = listState) { padding ->
 *     RotaryList(state = listState, contentPadding = padding, rotaryMode = RotaryMode.Snap) {
 *         items(workouts) { workout -> Button(onClick = {}, label = { Text(workout.name) }) }
 *     }
 * }
 * ```
 */
@Composable
public fun RotaryList(
    modifier: Modifier = Modifier,
    state: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    rotaryMode: RotaryMode = RotaryMode.Scroll,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(RotaryListDefaults.ItemSpacing, Alignment.Top),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    hapticFeedbackEnabled: Boolean = true,
    content: TransformingLazyColumnScope.() -> Unit,
) {
    val behavior: RotaryScrollableBehavior = when (rotaryMode) {
        RotaryMode.Scroll -> RotaryScrollableDefaults.behavior(
            scrollableState = state,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
        )
        RotaryMode.Snap -> RotaryScrollableDefaults.snapBehavior(
            scrollableState = state,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
        )
    }
    TransformingLazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        rotaryScrollableBehavior = behavior,
        content = content,
    )
}

/**
 * The same as [RotaryList] on a [ScalingLazyColumn], the classic Wear OS list where items shrink
 * and fade toward the edges. Use it with `ScreenScaffold(scrollState = state)`.
 */
@Composable
public fun RotaryScalingList(
    modifier: Modifier = Modifier,
    state: ScalingLazyListState = rememberScalingLazyListState(),
    rotaryMode: RotaryMode = RotaryMode.Scroll,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(RotaryListDefaults.ItemSpacing, Alignment.Top),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    hapticFeedbackEnabled: Boolean = true,
    content: ScalingLazyListScope.() -> Unit,
) {
    val behavior: RotaryScrollableBehavior = when (rotaryMode) {
        RotaryMode.Scroll -> RotaryScrollableDefaults.behavior(
            scrollableState = state,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
        )
        RotaryMode.Snap -> RotaryScrollableDefaults.snapBehavior(
            scrollableState = state,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
        )
    }
    ScalingLazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        rotaryScrollableBehavior = behavior,
        content = content,
    )
}

/** Remembers a [RotaryStepAccumulator] for [rotarySteps]. */
@Composable
public fun rememberRotaryStepAccumulator(
    stepPixels: Float = RotaryListDefaults.StepPixels,
    idleResetMillis: Long = RotaryStepAccumulator.DEFAULT_IDLE_RESET_MILLIS,
): RotaryStepAccumulator = remember(stepPixels, idleResetMillis) {
    RotaryStepAccumulator(stepPixels = stepPixels, idleResetMillis = idleResetMillis)
}

/**
 * Turns crown rotation into whole steps for things that are not lists: a minute picker, a volume
 * ring, a page. Clockwise gives positive steps. The element must have focus to get rotary events,
 * so request it once it is shown:
 *
 * ```kotlin
 * val focusRequester = remember { FocusRequester() }
 * val accumulator = rememberRotaryStepAccumulator()
 * Box(Modifier.rotarySteps(accumulator, focusRequester) { steps -> minutes = (minutes + steps).coerceIn(1, 60) })
 * LaunchedEffect(Unit) { focusRequester.requestFocus() }
 * ```
 */
public fun Modifier.rotarySteps(
    accumulator: RotaryStepAccumulator,
    focusRequester: FocusRequester,
    onSteps: (steps: Int) -> Unit,
): Modifier = this
    .onRotaryScrollEvent { event ->
        val steps = accumulator.accumulate(event.verticalScrollPixels, event.uptimeMillis)
        if (steps != 0) onSteps(steps)
        true
    }
    .focusRequester(focusRequester)
    .focusable()
