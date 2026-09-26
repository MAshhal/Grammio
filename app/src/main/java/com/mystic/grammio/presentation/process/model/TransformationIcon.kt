package com.mystic.grammio.presentation.process.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.mystic.grammio.domain.model.Transformation

/** Chip icon for a transformation, shown next to its label. */
val Transformation.icon: ImageVector
    get() = when (this) {
        Transformation.FixGrammar -> Icons.Outlined.Spellcheck
        Transformation.Rephrase -> Icons.Outlined.Autorenew
        Transformation.Professional -> Icons.Outlined.WorkOutline
        Transformation.Casual -> Icons.Outlined.SentimentSatisfied
        Transformation.Shorten -> Icons.AutoMirrored.Outlined.ShortText
        Transformation.Expand -> Icons.AutoMirrored.Outlined.Notes
        Transformation.Summarize -> Icons.Outlined.Summarize
        is Transformation.Translate -> Icons.Outlined.Translate
    }
