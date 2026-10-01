package io.github.halilozel1903.wearkit.sample

import io.github.halilozel1903.wearkit.core.Stopwatch

/** The demos on the menu. */
enum class Demo(val title: String, val subtitle: String) {
    Stopwatch("Stopwatch", "Laps and a seconds ring"),
    Rings("Rings", "Progress and segments"),
    Workouts("Rotary list", "Turn the crown"),
    Curved("Curved text", "Labels on the bezel"),
    Pages("Pages", "Swipe right to close"),
}

data class Workout(val name: String, val detail: String, val durationMillis: Long, val color: Int)

val SampleWorkouts = listOf(
    Workout("Morning run", "5.2 km", 32 * 60_000L + 10_000L, 0),
    Workout("Intervals", "8 x 400 m", 24 * 60_000L + 45_000L, 1),
    Workout("Cycling", "18.4 km", 58 * 60_000L, 2),
    Workout("Yoga", "Flow", 45 * 60_000L, 0),
    Workout("Swim", "1.5 km", 38 * 60_000L + 20_000L, 1),
    Workout("Hike", "9.1 km", 2 * 3_600_000L + 12 * 60_000L, 2),
    Workout("Rowing", "6 km", 27 * 60_000L + 5_000L, 0),
)

/** The stopwatch of the `stopwatch` screenshot: 12:34.56 with three laps, paused. */
val PresetStopwatch: Stopwatch = Stopwatch.paused(
    elapsedMillis = 754_560L,
    lapDurations = listOf(241_200L, 236_900L, 239_300L),
)

const val StepGoal = 10_000
