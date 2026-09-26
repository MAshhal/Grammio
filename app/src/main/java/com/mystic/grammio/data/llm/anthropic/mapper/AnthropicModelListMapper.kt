package com.mystic.grammio.data.llm.anthropic.mapper

import com.mystic.grammio.data.llm.anthropic.dto.AnthropicModelListResponseDto
import com.mystic.grammio.domain.model.AiModel

/** Claude model list → [AiModel]s, keeping the API's newest-first order. */
internal object AnthropicModelListMapper {

    fun map(response: AnthropicModelListResponseDto): List<AiModel> =
        response.data.map { AiModel(id = it.id, displayName = it.displayName ?: it.id) }
}
