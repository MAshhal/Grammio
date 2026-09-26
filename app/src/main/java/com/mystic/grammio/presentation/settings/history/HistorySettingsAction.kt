package com.mystic.grammio.presentation.settings.history

sealed interface HistorySettingsAction {
    data class Toggled(val enabled: Boolean) : HistorySettingsAction

    data object Cleared : HistorySettingsAction
}
