package com.mystic.grammio.presentation.settings.history

/** @property entries newest first; null until loaded, so the page doesn't flash "nothing saved". */
data class HistorySettingsUiState(
    val isHistoryEnabled: Boolean = false,
    val entries: List<HistoryItem>? = null,
)
