package com.mystic.grammio.presentation.settings.history

import com.mystic.grammio.domain.model.HistoryEntry
import com.mystic.grammio.domain.model.Transformation

/** A saved entry and the transformation that ran, as it is now; null if it has since been deleted. */
data class HistoryItem(
    val entry: HistoryEntry,
    val transformation: Transformation?,
)
