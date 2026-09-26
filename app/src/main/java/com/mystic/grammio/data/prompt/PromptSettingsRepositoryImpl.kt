package com.mystic.grammio.data.prompt

import com.mystic.grammio.data.prompt.local.PromptPreferencesLocalDataSource
import com.mystic.grammio.data.transform.prompt.DefaultSystemPrompt
import com.mystic.grammio.domain.repository.PromptSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Only a prompt that differs from the default is stored, so a later change to the default still applies. */
class PromptSettingsRepositoryImpl(private val localDataSource: PromptPreferencesLocalDataSource) :
    PromptSettingsRepository {

    override val defaultSystemPrompt: String = DefaultSystemPrompt.TEXT

    override val systemPrompt: Flow<String> = localDataSource.customSystemPrompt.map { it ?: defaultSystemPrompt }

    override suspend fun setSystemPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || trimmed == defaultSystemPrompt) {
            localDataSource.clearCustomSystemPrompt()
        } else {
            localDataSource.setCustomSystemPrompt(trimmed)
        }
    }

    override suspend fun resetSystemPrompt() = localDataSource.clearCustomSystemPrompt()
}
