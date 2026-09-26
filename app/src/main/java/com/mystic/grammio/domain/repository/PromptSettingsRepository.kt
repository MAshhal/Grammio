package com.mystic.grammio.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * The system prompt: the instructions sent with every transformation, before its task. The user
 * can replace the built-in one and go back to it at any time.
 */
interface PromptSettingsRepository {
    /** The user's own system prompt, or [defaultSystemPrompt] when they have none. */
    val systemPrompt: Flow<String>

    val defaultSystemPrompt: String

    /** Saving blank text, or the default itself, goes back to the default. */
    suspend fun setSystemPrompt(prompt: String)

    suspend fun resetSystemPrompt()
}
