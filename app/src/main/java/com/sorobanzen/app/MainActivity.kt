package com.sorobanzen.app

import android.graphics.Color
import android.os.Bundle
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sorobanzen.app.data.AppPreferences
import com.sorobanzen.app.data.HistoryDatabase
import com.sorobanzen.app.ui.screens.CalculatorScreen
import com.sorobanzen.app.ui.screens.SettingsScreen
import com.sorobanzen.app.ui.screens.SorobanScreen
import com.sorobanzen.app.ui.theme.SorobanZenTheme
import com.sorobanzen.app.viewmodel.ZenViewModel
import com.sorobanzen.app.viewmodel.ZenViewModelFactory
import kotlin.math.abs
import kotlin.math.min

/**
 * The window stays in portrait for the life of the app (see `screenOrientation` in the manifest)
 * and the *content* turns instead. Nothing here asks the system to rotate, so there is no window
 * rotation, no `RotationLayer` snapshot, and therefore no ghost of the screen we just left: both
 * screens live in one window and simply cross-fade.
 *
 * That also collapses the two ways into a mode into one. Turning the phone and pressing
 * そろばん/電卓 now write the same piece of state, so there is no orientation request to make and
 * no hold to release.
 */
class MainActivity : ComponentActivity() {

    private val database by lazy { HistoryDatabase.getDatabase(this) }
    private val preferences by lazy { AppPreferences(this) }
    private val viewModel: ZenViewModel by viewModels {
        ZenViewModelFactory(database.historyDao(), preferences)
    }

    /**
     * How far the content must turn to face the reader: 0 upright, -90 or 90 sideways. Null
     * until the sensor has actually said, so a phone lying at an angle through a restart keeps
     * the mode it was left in rather than being told it is upright.
     */
    private var physicalAngle by mutableStateOf<Float?>(null)

    /** The last turn the phone was actually held at, for the buttons to borrow. */
    private var lastSidewaysAngle by mutableFloatStateOf(-90f)

    /**
     * Fixed angle between the pinned window and the phone's natural orientation, which is what
     * the sensor reading below is measured against. Zero on a phone, a quarter turn on a device
     * whose natural orientation is landscape.
     */
    private val windowOffset: Int
        get() = when (ContextCompat.getDisplayOrDefault(this).rotation) {
            android.view.Surface.ROTATION_90 -> 90
            android.view.Surface.ROTATION_180 -> 180
            android.view.Surface.ROTATION_270 -> 270
            else -> 0
        }

    private val orientationWatcher by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(degrees: Int) {
                if (degrees == ORIENTATION_UNKNOWN) return
                // The sensor says how far the phone has been turned; the content turns back by
                // the same amount to face whoever is reading it.
                val turn = ((-degrees - windowOffset) % 360 + 360) % 360
                // A phone held between two of these keeps the mode it has. That gap is what
                // stops a hand at 45 degrees from flapping between the two screens.
                physicalAngle = when {
                    offBy(turn, 90) < TILT_THRESHOLD -> 90f.also { lastSidewaysAngle = it }
                    offBy(turn, 270) < TILT_THRESHOLD -> (-90f).also { lastSidewaysAngle = it }
                    // Upside down still counts as upright: the calculator never turns over.
                    min(offBy(turn, 0), offBy(turn, 180)) < TILT_THRESHOLD -> 0f
                    else -> physicalAngle
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (orientationWatcher.canDetectOrientation()) orientationWatcher.enable()
    }

    override fun onStop() {
        orientationWatcher.disable()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContent {
            SorobanZenTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var isSettingsActive by rememberSaveable { mutableStateOf(false) }
                    var sorobanMode by rememberSaveable { mutableStateOf(false) }

                    // Turning the phone is still the primary switch; the buttons are the second
                    // way in for a phone being kept the other way up. A button's choice stands
                    // until the phone is next turned, because only a turn changes this key.
                    LaunchedEffect(physicalAngle) {
                        physicalAngle?.let { sorobanMode = it != 0f }
                    }

                    // The bars would sit along a vertical edge with their text on its side while
                    // the phone is sideways, so the soroban runs without them.
                    LaunchedEffect(sorobanMode) {
                        val controller = WindowCompat.getInsetsController(window, window.decorView)
                        controller.systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        if (sorobanMode) {
                            controller.hide(WindowInsetsCompat.Type.systemBars())
                        } else {
                            controller.show(WindowInsetsCompat.Type.systemBars())
                        }
                    }

                    val sidewaysAngle = physicalAngle?.takeIf { it != 0f } ?: lastSidewaysAngle

                    AnimatedContent(
                        targetState = sorobanMode,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(durationMillis = 260, delayMillis = 80))
                                .togetherWith(fadeOut(animationSpec = tween(durationMillis = 180)))
                        },
                        label = "ModeTransition"
                    ) { soroban ->
                        // Each side of the fade keeps its own frame, so the outgoing screen is
                        // never re-measured into the incoming screen's shape on its way out.
                        TurnedFrame(angle = if (soroban) sidewaysAngle else 0f) {
                            when {
                                isSettingsActive -> SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { isSettingsActive = false }
                                )

                                soroban -> SorobanScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = { isSettingsActive = true },
                                    onExit = { sorobanMode = false }
                                )

                                else -> CalculatorScreen(
                                    viewModel = viewModel,
                                    onEnterSoroban = { sorobanMode = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private companion object {
        /** Degrees off an axis before the phone counts as being held along it. */
        const val TILT_THRESHOLD = 35
    }
}

/** Shortest distance in degrees between an orientation reading and an axis. */
private fun offBy(degrees: Int, target: Int): Int {
    val diff = abs(degrees - target) % 360
    return min(diff, 360 - diff)
}

/**
 * Draws [content] turned by [angle], measured for the turn: at plus or minus 90 the child is laid
 * out with the window's width and height swapped, so a landscape screen gets landscape
 * constraints out of a window that never leaves portrait.
 */
@Composable
private fun TurnedFrame(angle: Float, content: @Composable () -> Unit) {
    if (angle == 0f) {
        content()
        return
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // Padding for the cutout out here, where the insets still line up with the window.
            // Everything else is consumed, because a screen inside is turned and would otherwise
            // pad the wrong physical edge.
            .windowInsetsPadding(WindowInsets.displayCutout)
            .consumeWindowInsets(WindowInsets.safeDrawing)
    ) {
        Box(
            modifier = Modifier
                .requiredSize(width = maxHeight, height = maxWidth)
                .align(Alignment.Center)
                .rotate(angle)
        ) {
            content()
        }
    }
}
