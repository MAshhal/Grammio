package com.mystic.grammio.data.transformation

import com.mystic.grammio.domain.model.TransformationIcon

/**
 * Stable name for [TransformationIcon] in anything persisted. Spelled out rather than derived from
 * the enum name so renaming an entry can never orphan a stored transformation's icon.
 */
val TransformationIcon.storageKey: String
    get() = when (this) {
        TransformationIcon.Sparkle -> "sparkle"
        TransformationIcon.Spellcheck -> "spellcheck"
        TransformationIcon.Autorenew -> "autorenew"
        TransformationIcon.Briefcase -> "briefcase"
        TransformationIcon.Smile -> "smile"
        TransformationIcon.ShortText -> "short_text"
        TransformationIcon.Notes -> "notes"
        TransformationIcon.Summarize -> "summarize"
        TransformationIcon.Translate -> "translate"
        TransformationIcon.Edit -> "edit"
        TransformationIcon.Mail -> "mail"
        TransformationIcon.Lightbulb -> "lightbulb"
    }

/** The icon stored under [key], or null for a key this version doesn't know. */
fun transformationIconForStorageKey(key: String): TransformationIcon? =
    TransformationIcon.entries.firstOrNull { it.storageKey == key }
