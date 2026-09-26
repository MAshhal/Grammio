package com.mystic.grammio.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.testing.InMemoryProviderSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveCustomEndpointUseCaseTest {

    private val repository = InMemoryProviderSettingsRepository()
    private val saveCustomEndpoint = SaveCustomEndpointUseCase(repository)

    @Test
    fun `https URL is trimmed and loses its trailing slash`() = runTest {
        assertThat(saveCustomEndpoint("  https://openrouter.ai/api/v1/ \n")).isTrue()

        assertThat(repository.customBaseUrl.value).isEqualTo("https://openrouter.ai/api/v1")
    }

    @Test
    fun `unusable URLs are refused`() = runTest {
        listOf(
            "",
            "   ",
            "http://openrouter.ai/api/v1",
            "openrouter.ai/api/v1",
            "https://",
            "https://host/v1?key=x",
            "not a url",
        ).forEach { url ->
            assertThat(saveCustomEndpoint(url)).isFalse()
        }

        assertThat(repository.customBaseUrl.value).isNull()
    }
}
