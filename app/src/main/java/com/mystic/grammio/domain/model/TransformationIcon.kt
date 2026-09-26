package com.mystic.grammio.domain.model

/**
 * The symbol shown next to a transformation's name. A closed set so it can be stored by key and
 * drawn by the UI however it likes; [Sparkle] is the one new transformations start with.
 */
enum class TransformationIcon {
    Sparkle,
    Spellcheck,
    Autorenew,
    Briefcase,
    Smile,
    ShortText,
    Notes,
    Summarize,
    Translate,
    Edit,
    Mail,
    Lightbulb,
}
