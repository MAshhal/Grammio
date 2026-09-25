package com.mystic.grammio.presentation.process.model

import androidx.annotation.StringRes
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.Transformation

/** Chip label for a transformation. Labels are presentation, so they live here and not in the domain. */
@StringRes
fun Transformation.labelRes(): Int = when (this) {
    Transformation.FixGrammar -> R.string.transformation_fix_grammar
    Transformation.Rephrase -> R.string.transformation_rephrase
    Transformation.Professional -> R.string.transformation_professional
    Transformation.Casual -> R.string.transformation_casual
    Transformation.Shorten -> R.string.transformation_shorten
    Transformation.Expand -> R.string.transformation_expand
    Transformation.Summarize -> R.string.transformation_summarize
    is Transformation.Translate -> R.string.transformation_translate
}
