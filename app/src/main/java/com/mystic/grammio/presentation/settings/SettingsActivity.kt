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
        val initialBackStack = when (intent.getStringExtra(EXTRA_START_PAGE)) {
            PAGE_PROVIDER -> listOf(SettingsRoute.Home, SettingsRoute.Provider)
            PAGE_TRANSFORMATIONS -> listOf(SettingsRoute.Home, SettingsRoute.Transformations)
            else -> listOf(SettingsRoute.Home)
        }
        setContent {
            GrammioTheme {
                SettingsNavigation(initialBackStack)
            }
        }
    }

    companion object {
        private const val EXTRA_START_PAGE = "com.mystic.grammio.extra.START_PAGE"
        private const val PAGE_PROVIDER = "provider"
        private const val PAGE_TRANSFORMATIONS = "transformations"

        /** Opens Settings on the provider page, with Home underneath so back still leads there. */
        fun providerIntent(context: Context): Intent = startPageIntent(context, PAGE_PROVIDER)

        /** Opens Settings on the transformations page, with Home underneath. */
        fun transformationsIntent(context: Context): Intent = startPageIntent(context, PAGE_TRANSFORMATIONS)

        private fun startPageIntent(
            context: Context,
            page: String,
        ): Intent = Intent(context, SettingsActivity::class.java).putExtra(EXTRA_START_PAGE, page)
    }
}
