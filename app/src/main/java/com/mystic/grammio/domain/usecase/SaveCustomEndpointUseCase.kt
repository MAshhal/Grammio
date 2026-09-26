package com.mystic.grammio.domain.usecase

import com.mystic.grammio.domain.repository.ProviderSettingsRepository
import java.net.URI

/**
 * Saves the base URL of the user's OpenAI-compatible endpoint. Only `https://` URLs with a host are
 * accepted, since the API key travels with every request, and a trailing slash is dropped so paths
 * can be appended as they are.
 */
class SaveCustomEndpointUseCase(private val repository: ProviderSettingsRepository) {

    /** @return false when [url] is not a usable https URL and nothing was saved. */
    suspend operator fun invoke(url: String): Boolean {
        val normalized = url.trim().trimEnd('/')
        val uri = runCatching { URI(normalized) }.getOrNull() ?: return false
        if (!uri.scheme.equals(HTTPS, ignoreCase = true) || uri.host.isNullOrBlank()) return false
        if (uri.query != null || uri.fragment != null) return false
        repository.setCustomBaseUrl(normalized)
        return true
    }

    private companion object {
        const val HTTPS = "https"
    }
}
