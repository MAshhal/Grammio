package com.mystic.grammio.testing

import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon

/** Small transformations for tests, independent of the shipped defaults. */
object TestTransformations {
    val fixGrammar = Transformation("fix_grammar", "Fix grammar", "Fix the grammar.", TransformationIcon.Spellcheck)
    val shorten = Transformation("shorten", "Shorten", "Make it shorter.", TransformationIcon.ShortText)
    val casual = Transformation("casual", "Casual", "Make it casual.", TransformationIcon.Smile)
    val translate = Transformation(
        id = "translate",
        name = "Translate",
        taskPrompt = "Translate the text into ${Transformation.LANGUAGE_PLACEHOLDER}.",
        icon = TransformationIcon.Translate,
    )
}
