package com.mystic.grammio.data.transform.prompt

import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.domain.model.Transformation
import java.util.Locale

/**
 * Turns a [Transformation] into an LLM prompt: the system prompt, then the task and the output
 * language. One place to tune wording and temperature per task.
 */
class PromptBuilder {

    fun build(
        text: String,
        transformation: Transformation,
        systemPrompt: String,
    ): LlmPrompt = LlmPrompt(
        systemInstruction = systemInstruction(transformation, systemPrompt),
        userText = "<text>\n$text\n</text>",
        temperature = temperature(transformation),
    )

    private fun systemInstruction(
        transformation: Transformation,
        systemPrompt: String,
    ): String {
        val languageRule = if (transformation is Transformation.Translate) {
            "Write the output in ${languageName(transformation.targetLanguageTag)}."
        } else {
            "Reply in the same language as the input text."
        }
        return "$systemPrompt\n\nTask: ${task(transformation)}\n$languageRule"
    }

    private fun task(transformation: Transformation): String = when (transformation) {
        Transformation.FixGrammar ->
            "Correct spelling, grammar and punctuation. Keep the original wording, tone and meaning; change as little as possible. If it is already correct, return it unchanged."

        Transformation.Rephrase ->
            "Rephrase the text with different wording while keeping the meaning, tone and approximate length."

        Transformation.Professional ->
            "Rewrite the text in a clear, polite, professional tone suitable for work communication."

        Transformation.Casual ->
            "Rewrite the text in a relaxed, friendly, conversational tone."

        Transformation.Shorten ->
            "Make the text noticeably shorter and more concise while keeping its key meaning."

        Transformation.Expand ->
            "Expand the text with more detail and fuller sentences while keeping its meaning and tone. Do not invent specific facts."

        Transformation.Summarize ->
            "Summarize the text, keeping only the essential points. Use a short paragraph, or brief bullet points if the input is long."

        is Transformation.Translate ->
            "Translate the text into ${languageName(transformation.targetLanguageTag)}, preserving meaning and tone."
    }

    private fun temperature(transformation: Transformation): Double = when (transformation) {
        Transformation.FixGrammar -> 0.1
        is Transformation.Translate -> 0.2
        Transformation.Summarize, Transformation.Shorten -> 0.3
        Transformation.Rephrase, Transformation.Professional, Transformation.Casual, Transformation.Expand -> 0.7
    }

    private fun languageName(tag: String): String =
        Locale.forLanguageTag(tag).getDisplayLanguage(Locale.ENGLISH).ifBlank { tag }
}
