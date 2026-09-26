package com.mystic.grammio.data.transformation

import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon

/**
 * The transformations Grammio ships with, in their initial order. Their ids are the storage keys the
 * hard-coded transformations used, so history recorded before they became editable still matches.
 */
object DefaultTransformations {
    val all: List<Transformation> = listOf(
        Transformation(
            id = "fix_grammar",
            name = "Fix grammar",
            taskPrompt = "Correct spelling, grammar and punctuation. Keep the original wording, tone and meaning; " +
                "change as little as possible. If it is already correct, return it unchanged.",
            icon = TransformationIcon.Spellcheck,
            temperature = 0.1,
        ),
        Transformation(
            id = "professional",
            name = "Professional",
            taskPrompt = "Rewrite the text in a clear, polite, professional tone suitable for work communication.",
            icon = TransformationIcon.Briefcase,
            temperature = 0.7,
        ),
        Transformation(
            id = "summarize",
            name = "Summarize",
            taskPrompt = "Summarize the text, keeping only the essential points. Use a short paragraph, " +
                "or brief bullet points if the input is long.",
            icon = TransformationIcon.Summarize,
            temperature = 0.3,
        ),
        Transformation(
            id = "rephrase",
            name = "Rephrase",
            taskPrompt = "Rephrase the text with different wording while keeping the meaning, tone " +
                "and approximate length.",
            icon = TransformationIcon.Autorenew,
            temperature = 0.7,
        ),
        Transformation(
            id = "translate",
            name = "Translate",
            taskPrompt = "Translate the text into ${Transformation.LANGUAGE_PLACEHOLDER}, preserving meaning and tone.",
            icon = TransformationIcon.Translate,
            temperature = 0.2,
        ),
    )
}
