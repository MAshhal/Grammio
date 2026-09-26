package com.mystic.grammio.presentation.common

import androidx.annotation.StringRes
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.TransformationIcon

/** What a screen reader says for an icon in the icon picker. */
@get:StringRes
val TransformationIcon.labelRes: Int
    get() = when (this) {
        TransformationIcon.Sparkle -> R.string.icon_sparkle
        TransformationIcon.Spellcheck -> R.string.icon_spellcheck
        TransformationIcon.Autorenew -> R.string.icon_autorenew
        TransformationIcon.Briefcase -> R.string.icon_briefcase
        TransformationIcon.Smile -> R.string.icon_smile
        TransformationIcon.ShortText -> R.string.icon_short_text
        TransformationIcon.Notes -> R.string.icon_notes
        TransformationIcon.Summarize -> R.string.icon_summarize
        TransformationIcon.Translate -> R.string.icon_translate
        TransformationIcon.Edit -> R.string.icon_edit
        TransformationIcon.Mail -> R.string.icon_mail
        TransformationIcon.Lightbulb -> R.string.icon_lightbulb
    }
