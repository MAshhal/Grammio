package com.mystic.grammio.presentation.settings

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The pages of Settings. Serializable so the back stack survives process death. */
@Serializable
sealed interface SettingsRoute : NavKey {
    @Serializable
    data object Home : SettingsRoute

    @Serializable
    data object Provider : SettingsRoute

    @Serializable
    data object SystemPrompt : SettingsRoute

    @Serializable
    data object History : SettingsRoute
}
