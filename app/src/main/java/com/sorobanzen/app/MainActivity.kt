package com.sorobanzen.app

import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.min
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.sorobanzen.app.data.AppPreferences
import com.sorobanzen.app.data.HistoryDatabase
import com.sorobanzen.app.ui.screens.CalculatorScreen
import com.sorobanzen.app.ui.screens.SettingsScreen
import com.sorobanzen.app.ui.screens.SorobanScreen
import com.sorobanzen.app.ui.theme.SorobanZenTheme
import com.sorobanzen.app.viewmodel.ZenViewModel
import com.sorobanzen.app.viewmodel.ZenViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { HistoryDatabase.getDatabase(this) }
    private val preferences by lazy { AppPreferences(this) }
    private val viewModel: ZenViewModel by viewModels {
        ZenViewModelFactory(database.historyDao(), preferences)
    }

    /**
     * Where the phone is actually being held, which the configuration cannot tell us while the
     * そろばん button is holding the window in landscape.
     */
    private var physicallyLandscape by mutableStateOf(false)

    private val orientationWatcher by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(degrees: Int) {
                if (degrees == ORIENTATION_UNKNOWN) return
                val offLandscape = min(abs(degrees - 90), abs(degrees - 270))
                physicallyLandscape = offLandscape < 35
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
                    // null = follow the phone. true/false = hold a mode the phone disagrees
                    // with, for as long as it disagrees.
                    var heldSoroban by rememberSaveable { mutableStateOf<Boolean?>(null) }

                    val isLandscape = LocalConfiguration.current.orientation ==
                        Configuration.ORIENTATION_LANDSCAPE

                    // Sideways is the soroban and upright the calculator, as it always was. Each
                    // button is the second way in for a phone being kept the other way up: it
                    // turns the window and lets the rotation itself swap the screen, so the
                    // crossfade rides that rotation instead of racing it.

                    // A hold exists only while the phone disagrees with it. The moment the phone
                    // is turned to match, it has nothing left to do — and letting go there is what
                    // lets the next turn of the phone work normally again.
                    LaunchedEffect(heldSoroban, physicallyLandscape) {
                        if (heldSoroban == physicallyLandscape) heldSoroban = null
                    }

                    LaunchedEffect(heldSoroban) {
                        requestedOrientation = when (heldSoroban) {
                            true -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            false -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            null -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }

                    if (isSettingsActive) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { isSettingsActive = false }
                        )
                    } else {
                        AnimatedContent(
                            targetState = isLandscape,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(durationMillis = 240))
                                    .togetherWith(fadeOut(animationSpec = tween(durationMillis = 160)))
                            },
                            label = "ModeTransition"
                        ) { soroban ->
                            if (soroban) {
                                SorobanScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = { isSettingsActive = true },
                                    onExit = { heldSoroban = false }
                                )
                            } else {
                                CalculatorScreen(
                                    viewModel = viewModel,
                                    onEnterSoroban = { heldSoroban = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
