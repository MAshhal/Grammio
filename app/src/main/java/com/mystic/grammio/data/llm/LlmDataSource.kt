package com.mystic.grammio.data.llm

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.result.Outcome

/**
 * Vendor-neutral "prompt in, text out" contract. Everything specific to one LLM vendor (endpoint
 * paths, auth, DTOs, error codes) stays inside its implementation. Implementations hold no
 * configuration: the caller supplies the key, base URL and model on every call.
 */
interface LlmDataSource {
    suspend fun generate(
        prompt: LlmPrompt,
        connection: LlmConnection,
    ): Outcome<String, TransformError>

    /** Models this endpoint offers that can generate text. */
    suspend fun listModels(endpoint: LlmEndpoint): Outcome<List<AiModel>, TransformError>
}
