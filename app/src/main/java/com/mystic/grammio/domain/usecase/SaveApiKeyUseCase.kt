package com.mystic.grammio.domain.usecase

import com.mystic.grammio.domain.repository.ApiKeyRepository

/** Saves the user's API key. Whitespace picked up when pasting is dropped, and a blank key is refused. */
class SaveApiKeyUseCase(private val repository: ApiKeyRepository) {

    /** @return false when [apiKey] is blank and nothing was saved. */
    suspend operator fun invoke(apiKey: String): Boolean {
        val key = apiKey.trim()
        if (key.isEmpty()) return false
        repository.save(key)
        return true
    }
}
