package com.sorobanzen.app

import android.content.res.Configuration
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.sorobanzen.app.data.HistoryDatabase
import com.sorobanzen.app.ui.screens.CalculatorScreen
import com.sorobanzen.app.ui.screens.SettingsScreen
import com.sorobanzen.app.ui.screens.SorobanScreen
import com.sorobanzen.app.ui.theme.SorobanZenTheme
import com.sorobanzen.app.viewmodel.ZenViewModel
import com.sorobanzen.app.viewmodel.ZenViewModelFactory
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isTtsInitialized = false

    private val database by lazy { HistoryDatabase.getDatabase(this) }
    private val viewModel: ZenViewModel by viewModels {
        ZenViewModelFactory(database.historyDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize TextToSpeech in Japanese
        tts = TextToSpeech(this, this)

        // Observe TTS event flow
        lifecycle.addObserver(object : androidx.lifecycle.LifecycleEventObserver {
            override fun onStateChanged(
                source: androidx.lifecycle.LifecycleOwner,
                event: androidx.lifecycle.Lifecycle.Event
            ) {
                if (event == androidx.lifecycle.Lifecycle.Event.ON_CREATE) {
                    androidx.lifecycle.lifecycleScope.launchWhenStarted {
                        viewModel.ttsEvent.collect { reading ->
                            if (isTtsInitialized) {
                                tts.speak(reading, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                    }
                }
            }
        })

        setContent {
            // Observe settings configurations
            val currentLang by viewModel.language.collectAsState()
            
            // Apply language localization override dynamically
            val config = LocalConfiguration.current
            val locale = Locale(currentLang)
            Locale.setDefault(locale)
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)

            SorobanZenTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var isSettingsActive by remember { mutableStateOf(false) }
                    val orientation = LocalConfiguration.current.orientation
                    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

                    if (isSettingsActive) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { isSettingsActive = false }
                        )
                    } else {
                        // Smooth transition between Calculator and Soroban views
                        AnimatedContent(
                            targetState = isLandscape,
                            transitionSpec = {
                                if (targetState) {
                                    // Transitioning from Portrait -> Landscape
                                    (slideInHorizontally { width -> width } + fadeIn() + scaleIn(initialScale = 0.9f))
                                        .togetherWith(slideOutHorizontally { width -> -width } + fadeOut() + scaleOut(targetScale = 0.9f))
                                } else {
                                    // Transitioning from Landscape -> Portrait
                                    (slideInHorizontally { width -> -width } + fadeIn() + scaleIn(initialScale = 0.9f))
                                        .togetherWith(slideOutHorizontally { width -> width } + fadeOut() + scaleOut(targetScale = 0.9f))
                                }
                            },
                            label = "OrientationTransition"
                        ) { landscape ->
                            if (landscape) {
                                SorobanScreen(viewModel = viewModel)
                            } else {
                                CalculatorScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = { isSettingsActive = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.JAPANESE)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = true
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
