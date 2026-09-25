package com.mystic.grammio.data.llm

import com.mystic.grammio.domain.model.Outcome
import com.mystic.grammio.domain.model.TransformError

/**
 * Vendor-neutral "prompt in, text out" contract. Everything specific to one LLM vendor (endpoint,
 * auth, DTOs, error codes) stays inside its implementation.
 */
interface LlmProvider {
    suspend fun generate(prompt: LlmPrompt): Outcome<String, TransformError>
}

data class LlmPrompt(
    val systemInstruction: String,
    val userText: String,
    val temperature: Double,
)
