package com.mystic.grammio.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.mystic.grammio.domain.model.TransformationIcon

/** How a transformation's icon is drawn, in the sheet's chips and in Settings. */
val TransformationIcon.imageVector: ImageVector
    get() = when (this) {
        TransformationIcon.Sparkle -> Icons.Outlined.AutoAwesome
        TransformationIcon.Spellcheck -> Icons.Outlined.Spellcheck
        TransformationIcon.Autorenew -> Icons.Outlined.Autorenew
        TransformationIcon.Briefcase -> Icons.Outlined.WorkOutline
        TransformationIcon.Smile -> Icons.Outlined.SentimentSatisfied
        TransformationIcon.ShortText -> Icons.AutoMirrored.Outlined.ShortText
        TransformationIcon.Notes -> Icons.AutoMirrored.Outlined.Notes
        TransformationIcon.Summarize -> Icons.Outlined.Summarize
        TransformationIcon.Translate -> Icons.Outlined.Translate
        TransformationIcon.Edit -> Icons.Outlined.Edit
        TransformationIcon.Mail -> Icons.Outlined.MailOutline
        TransformationIcon.Lightbulb -> Icons.Outlined.Lightbulb
    }
