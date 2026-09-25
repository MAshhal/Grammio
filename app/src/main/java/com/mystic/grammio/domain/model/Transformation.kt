package com.mystic.grammio.domain.model

/**
 * What the user wants done to the selected text. A sealed type (not an enum) because [Translate]
 * carries a parameter; exhaustive `when`s make every layer handle each transformation.
 */
sealed interface Transformation {
    data object Rephrase : Transformation
    data object FixGrammar : Transformation
    data object Professional : Transformation
    data object Casual : Transformation
    data object Shorten : Transformation
    data object Expand : Transformation
    data object Summarize : Transformation

    /** @property targetLanguageTag BCP-47 tag, e.g. "en", "es", "ur". */
    data class Translate(val targetLanguageTag: String) : Transformation
}
