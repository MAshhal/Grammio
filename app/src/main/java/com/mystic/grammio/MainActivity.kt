package com.mystic.grammio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mystic.grammio.feature.settings.SettingsScreen
import com.mystic.grammio.feature.settings.SettingsViewModel
import com.mystic.grammio.ui.theme.GrammioTheme
import org.koin.androidx.compose.koinViewModel

/** Launcher screen: how to use Grammio and where to enter the API key. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrammioTheme {
                val viewModel: SettingsViewModel = koinViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                SettingsScreen(state = state, onAction = viewModel::onAction)
            }
        }
    }
}
