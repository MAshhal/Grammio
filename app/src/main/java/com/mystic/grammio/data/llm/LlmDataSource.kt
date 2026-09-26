package com.mystic.grammio.data.llm

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome

/**
 * Vendor-neutral "prompt in, text out" contract. Everything specific to one LLM vendor (endpoint
 * paths, auth, DTOs, error codes) stays inside its implementation. Implementations are stateless:
 * the caller supplies the key, model and base URL through [LlmConnection].
 */
interface LlmDataSource {
    suspend fun generate(
        prompt: LlmPrompt,
        connection: LlmConnection,
    ): Outcome<String, TransformError>
}
