package com.mystic.grammio.data.llm.gemini.mapper

import com.mystic.grammio.data.llm.gemini.dto.ListModelsResponseDto
import com.mystic.grammio.domain.model.AiModel

/** Gemini model list → the models that can serve `generateContent`. */
internal object GeminiModelListMapper {

    fun map(response: ListModelsResponseDto): List<AiModel> = response.models
        .filter { GENERATE_CONTENT in it.supportedGenerationMethods }
        .map { model ->
            val id = model.name.removePrefix(RESOURCE_PREFIX)
            AiModel(id = id, displayName = model.displayName ?: id)
        }

    private const val GENERATE_CONTENT = "generateContent"
    private const val RESOURCE_PREFIX = "models/"
}
