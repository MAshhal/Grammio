package com.mystic.grammio.data.repository

import com.mystic.grammio.data.llm.LlmProvider
import com.mystic.grammio.data.prompt.PromptBuilder
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.repository.TextTransformRepository
import com.mystic.grammio.domain.result.Outcome

class TextTransformRepositoryImpl(
    private val promptBuilder: PromptBuilder,
    private val llmProvider: LlmProvider,
) : TextTransformRepository {

    override suspend fun transform(
        text: String,
        transformation: Transformation,
    ): Outcome<TransformedText, TransformError> =
        when (val outcome = llmProvider.generate(promptBuilder.build(text, transformation))) {
            is Outcome.Failure -> outcome

            is Outcome.Success -> {
                val cleaned = cleanModelOutput(outcome.value)
                if (cleaned.isEmpty()) {
                    Outcome.Failure(TransformError.Unknown)
                } else {
                    Outcome.Success(TransformedText(cleaned, transformation))
                }
            }
        }
}

/**
 * Removes wrapping that models sometimes add despite instructions: code fences, echoed
 * `<text>` tags, and quotes around the whole reply.
 */
internal fun cleanModelOutput(raw: String): String {
    var text = raw.trim()
    CODE_FENCE.matchEntire(text)?.let { text = it.groupValues[1].trim() }
    if (text.startsWith("<text>") && text.endsWith("</text>")) {
        text = text.removePrefix("<text>").removeSuffix("</text>").trim()
    }
    QUOTE_PAIRS.firstOrNull { (open, close) ->
        text.length >= 2 && text.startsWith(open) && text.endsWith(close)
    }?.let { text = text.substring(1, text.length - 1).trim() }
    return text
}

private val CODE_FENCE = Regex("^```[\\w-]*\\s*\\n(.*?)\\n?```$", RegexOption.DOT_MATCHES_ALL)
private val QUOTE_PAIRS = listOf('"' to '"', '“' to '”', '«' to '»')
