package com.mystic.grammio.presentation.settings

sealed interface SettingsAction {
    data class KeyInputChanged(val value: String) : SettingsAction

    data object Save : SettingsAction

    data object Clear : SettingsAction
}
