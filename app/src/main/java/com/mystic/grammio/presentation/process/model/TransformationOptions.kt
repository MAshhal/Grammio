package com.mystic.grammio.presentation.process.model

import com.mystic.grammio.domain.model.Transformation

/** The chips offered in the sheet, in display order. */
object TransformationOptions {

    /** Translate always carries the currently chosen target language. */
    fun all(targetLanguageTag: String): List<Transformation> = listOf(
        Transformation.FixGrammar,
        Transformation.Rephrase,
        Transformation.Professional,
        Transformation.Casual,
        Transformation.Shorten,
        Transformation.Expand,
        Transformation.Summarize,
        Transformation.Translate(targetLanguageTag),
    )
}
