package io.github.halilozel1903.wearkit.sample

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme

/**
 * Taps and crown turns can't be timed reliably through adb on a fresh emulator, so
 * `scripts/screenshots.sh` starts the app with `--es scene <scene>` to open a demo with fixed data:
 *
 * - `stopwatch`: a paused stopwatch at 12:34.56 with three laps
 * - `rings`: a progress ring and a segmented ring
 * - `list`: the rotary workout list
 * - `curved`: curved text, ticks and dial numbers
 *
 * Without the extra the app opens on its menu.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        if (scene != null) {
            // Keeps the emulator from dimming into ambient mode while screenshots are taken.
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        setContent {
            MaterialTheme(colorScheme = SampleColors) {
                // The scaffold paints the theme background, so every screen and its text follow the theme.
                AppScaffold {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        SampleApp(scene)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}

/** Screenshot scenes, see [MainActivity]. */
enum class Scene(val id: String, val demo: Demo) {
    Stopwatch("stopwatch", Demo.Stopwatch),
    Rings("rings", Demo.Rings),
    Workouts("list", Demo.Workouts),
    Curved("curved", Demo.Curved),
    ;

    companion object {
        fun from(id: String?): Scene? = entries.firstOrNull { it.id == id }
    }
}
