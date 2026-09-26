package com.mystic.grammio.testing

import com.mystic.grammio.domain.repository.PromptSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** The system prompt held in memory; blank text goes back to the default, like the real one. */
class InMemoryPromptSettingsRepository(override val defaultSystemPrompt: String = "Default rules.") :
    PromptSettingsRepository {
    override val systemPrompt = MutableStateFlow(defaultSystemPrompt)

    override suspend fun setSystemPrompt(prompt: String) {
        systemPrompt.value = prompt.trim().ifEmpty { defaultSystemPrompt }
    }

    override suspend fun resetSystemPrompt() {
        systemPrompt.value = defaultSystemPrompt
    }
}
