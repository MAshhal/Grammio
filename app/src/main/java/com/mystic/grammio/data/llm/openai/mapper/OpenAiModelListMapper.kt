package com.mystic.grammio.data.llm.openai.mapper

import com.mystic.grammio.data.llm.openai.dto.ModelListResponseDto
import com.mystic.grammio.domain.model.AiModel

/**
 * `/models` → the models worth offering for text. The API lists every model the key can use,
 * including embeddings, speech and image models that can't chat, so those are dropped by name.
 */
internal object OpenAiModelListMapper {

    fun map(response: ModelListResponseDto): List<AiModel> = response.data
        .map { it.id }
        .filterNot { id -> NON_CHAT_MARKERS.any { id.contains(it, ignoreCase = true) } }
        .distinct()
        .sorted()
        .map { AiModel(id = it, displayName = it) }

    private val NON_CHAT_MARKERS = listOf(
        "embed",
        "tts",
        "whisper",
        "transcribe",
        "dall-e",
        "image",
        "sora",
        "moderation",
        "audio",
        "realtime",
    )
}
