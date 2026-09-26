package com.mystic.grammio.data.transform.log

import com.mystic.grammio.domain.model.Transformation

/**
 * Stable name for [Transformation] in anything persisted. A translation's target language is not
 * part of the key; it is stored alongside it.
 */
val Transformation.storageKey: String
    get() = when (this) {
        Transformation.Rephrase -> "rephrase"
        Transformation.FixGrammar -> "fix_grammar"
        Transformation.Professional -> "professional"
        Transformation.Casual -> "casual"
        Transformation.Shorten -> "shorten"
        Transformation.Expand -> "expand"
        Transformation.Summarize -> "summarize"
        is Transformation.Translate -> "translate"
    }
