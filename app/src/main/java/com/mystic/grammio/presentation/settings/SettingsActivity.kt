package com.mystic.grammio.presentation.settings

import android.content.Context
import android.content.Intent
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
        val initialBackStack = if (intent.getBooleanExtra(EXTRA_OPEN_PROVIDER, false)) {
            listOf(SettingsRoute.Home, SettingsRoute.Provider)
        } else {
            listOf(SettingsRoute.Home)
        }
        setContent {
            GrammioTheme {
                SettingsNavigation(initialBackStack)
            }
        }
    }

    companion object {
        private const val EXTRA_OPEN_PROVIDER = "com.mystic.grammio.extra.OPEN_PROVIDER"

        /** Opens Settings on the provider page, with Home underneath so back still leads there. */
        fun providerIntent(context: Context): Intent =
            Intent(context, SettingsActivity::class.java).putExtra(EXTRA_OPEN_PROVIDER, true)
    }
}
