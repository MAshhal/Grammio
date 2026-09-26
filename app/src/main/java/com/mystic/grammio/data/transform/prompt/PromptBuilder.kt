package com.mystic.grammio.data.transform.prompt

import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.domain.model.Transformation
import java.util.Locale

/**
 * Turns a [Transformation] into an LLM prompt: the system prompt, then the task and the output
 * language. The task and temperature come from the transformation itself.
 */
class PromptBuilder {

    /** @param targetLanguageTag BCP-47 tag; only used when [Transformation.usesTargetLanguage]. */
    fun build(
        text: String,
        transformation: Transformation,
        targetLanguageTag: String,
        systemPrompt: String,
    ): LlmPrompt = LlmPrompt(
        systemInstruction = systemInstruction(transformation, targetLanguageTag, systemPrompt),
        userText = "<text>\n$text\n</text>",
        temperature = transformation.temperature,
    )

    private fun systemInstruction(
        transformation: Transformation,
        targetLanguageTag: String,
        systemPrompt: String,
    ): String {
        val task: String
        val languageRule: String
        if (transformation.usesTargetLanguage) {
            val language = languageName(targetLanguageTag)
            task = transformation.taskPrompt.replace(Transformation.LANGUAGE_PLACEHOLDER, language)
            languageRule = "Write the output in $language."
        } else {
            task = transformation.taskPrompt
            languageRule = "Reply in the same language as the input text."
        }
        return "$systemPrompt\n\nTask: $task\n$languageRule"
    }

    private fun languageName(tag: String): String =
        Locale.forLanguageTag(tag).getDisplayLanguage(Locale.ENGLISH).ifBlank { tag }
}
