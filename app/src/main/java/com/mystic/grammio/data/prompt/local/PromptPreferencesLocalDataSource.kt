package com.mystic.grammio.data.prompt.local

import kotlinx.coroutines.flow.Flow

/** The user's own system prompt, if they wrote one. */
interface PromptPreferencesLocalDataSource {
    /** Null when the user has not replaced the default. */
    val customSystemPrompt: Flow<String?>

    suspend fun setCustomSystemPrompt(prompt: String)

    suspend fun clearCustomSystemPrompt()
}
