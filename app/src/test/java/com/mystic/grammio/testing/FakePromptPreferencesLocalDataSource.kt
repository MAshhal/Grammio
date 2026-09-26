package com.mystic.grammio.testing

import com.mystic.grammio.data.prompt.local.PromptPreferencesLocalDataSource
import kotlinx.coroutines.flow.MutableStateFlow

/** The custom system prompt held in memory. */
class FakePromptPreferencesLocalDataSource : PromptPreferencesLocalDataSource {
    override val customSystemPrompt = MutableStateFlow<String?>(null)

    override suspend fun setCustomSystemPrompt(prompt: String) {
        customSystemPrompt.value = prompt
    }

    override suspend fun clearCustomSystemPrompt() {
        customSystemPrompt.value = null
    }
}
