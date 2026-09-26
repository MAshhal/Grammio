package com.mystic.grammio.presentation.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mystic.grammio.presentation.theme.GrammioTheme

/** Launcher screen: hosts the Settings pages, starting with how to use Grammio. */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrammioTheme {
                SettingsNavigation()
            }
        }
    }
}
