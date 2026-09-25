package com.mystic.grammio.data.llm.gemini.mapper

import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.gemini.dto.ContentDto
import com.mystic.grammio.data.llm.gemini.dto.GenerateContentRequestDto
import com.mystic.grammio.data.llm.gemini.dto.GenerationConfigDto
import com.mystic.grammio.data.llm.gemini.dto.PartDto

/** [LlmPrompt] → Gemini request body. */
internal object GeminiRequestMapper {

    fun map(
        prompt: LlmPrompt,
        maxOutputTokens: Int,
    ) = GenerateContentRequestDto(
        systemInstruction = ContentDto(parts = listOf(PartDto(text = prompt.systemInstruction))),
        contents = listOf(ContentDto(role = USER_ROLE, parts = listOf(PartDto(text = prompt.userText)))),
        generationConfig = GenerationConfigDto(
            temperature = prompt.temperature,
            maxOutputTokens = maxOutputTokens,
        ),
    )

    private const val USER_ROLE = "user"
}
