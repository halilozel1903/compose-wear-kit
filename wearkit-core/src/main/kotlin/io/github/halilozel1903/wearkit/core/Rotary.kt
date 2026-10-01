package io.github.halilozel1903.wearkit.core

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * Turns the stream of small rotary (crown or bezel) deltas into whole steps.
 *
 * A crown sends many events of a few pixels each. Every [stepPixels] of rotation becomes one step;
 * the remainder is kept for the next event. The remainder is dropped when the rotation changes
 * direction ([resetOnReverse]) or after [idleResetMillis] without events, so a slow nudge after a
 * pause never triggers a stray step.
 *
 * ```kotlin
 * val accumulator = RotaryStepAccumulator(stepPixels = 48f)
 * accumulator.accumulate(30f, timeMillis = 0)   // 0, 30 px pending
 * accumulator.accumulate(30f, timeMillis = 16)  // 1, 12 px pending
 * ```
 */
public class RotaryStepAccumulator(
    public val stepPixels: Float,
    public val idleResetMillis: Long = DEFAULT_IDLE_RESET_MILLIS,
    public val resetOnReverse: Boolean = true,
) {
    init {
        require(stepPixels > 0f) { "stepPixels must be positive" }
        require(idleResetMillis > 0) { "idleResetMillis must be positive" }
    }

    private var lastEventMillis: Long? = null

    /** Rotation collected toward the next step, signed. */
    public var pendingPixels: Float = 0f
        private set

    /** Adds [deltaPixels] (positive is clockwise / down) at [timeMillis] and returns the whole steps, signed. */
    public fun accumulate(deltaPixels: Float, timeMillis: Long): Int {
        val last = lastEventMillis
        if (last != null && timeMillis - last > idleResetMillis) pendingPixels = 0f
        lastEventMillis = timeMillis
        if (deltaPixels == 0f || deltaPixels.isNaN()) return 0
        if (resetOnReverse && pendingPixels != 0f && sign(deltaPixels) != sign(pendingPixels)) pendingPixels = 0f
        pendingPixels += deltaPixels
        val steps = (pendingPixels / stepPixels).toInt()
        pendingPixels -= steps * stepPixels
        return steps
    }

    /** Forgets the pending rotation. */
    public fun reset() {
        pendingPixels = 0f
        lastEventMillis = null
    }

    public companion object {
        public const val DEFAULT_IDLE_RESET_MILLIS: Long = 300L
    }
}

/** Snapping helpers for rotary driven lists and pickers. */
public object RotarySnap {

    /**
     * Moves [current] by [steps] inside `0 until itemCount`. With [wrap] it cycles around,
     * otherwise it stops at the ends. Returns -1 for an empty list.
     */
    public fun stepIndex(current: Int, steps: Int, itemCount: Int, wrap: Boolean = false): Int {
        if (itemCount <= 0) return -1
        val target = current + steps
        return if (wrap) Math.floorMod(target, itemCount) else target.coerceIn(0, itemCount - 1)
    }

    /**
     * The item to snap to after free scrolling: [scrollOffset] pixels from the first item with
     * items [itemSizePx] tall. Rounds to the nearest item.
     */
    public fun nearestIndex(scrollOffset: Float, itemSizePx: Float, itemCount: Int): Int {
        if (itemCount <= 0) return -1
        require(itemSizePx > 0f) { "itemSizePx must be positive" }
        return (scrollOffset / itemSizePx).roundToInt().coerceIn(0, itemCount - 1)
    }

    /**
     * Steps a value in `min..max` by [steps] times [step], for crown driven pickers (minutes,
     * volume). With [wrap] going past [max] starts again at [min].
     */
    public fun stepValue(value: Int, steps: Int, min: Int, max: Int, step: Int = 1, wrap: Boolean = false): Int {
        require(min <= max) { "min must not be greater than max" }
        require(step > 0) { "step must be positive" }
        val target = value + steps * step
        if (!wrap) return target.coerceIn(min, max)
        val span = max - min + 1
        return min + Math.floorMod(target - min, span)
    }

    /**
     * Whether a fling of [velocityPixelsPerSecond] is fast enough to skip snapping one item at a
     * time; above [threshold] lists should scroll freely.
     */
    public fun isFastFling(velocityPixelsPerSecond: Float, threshold: Float = 2_000f): Boolean =
        abs(velocityPixelsPerSecond) > threshold
}
