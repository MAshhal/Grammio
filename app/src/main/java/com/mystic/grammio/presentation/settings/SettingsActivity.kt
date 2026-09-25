package com.mystic.grammio.presentation.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mystic.grammio.presentation.theme.GrammioTheme
import org.koin.androidx.compose.koinViewModel

/** Launcher screen: how to use Grammio and where to enter the API key. */
class SettingsActivity : ComponentActivity() {
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
