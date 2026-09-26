package com.mystic.grammio.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.testing.InMemoryApiKeyRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveApiKeyUseCaseTest {

    private val repository = InMemoryApiKeyRepository()
    private val saveApiKey = SaveApiKeyUseCase(repository)

    @Test
    fun `key is trimmed and saved for its provider`() = runTest {
        assertThat(saveApiKey(AiProvider.Anthropic, "  sk-ant-key \n")).isTrue()

        assertThat(repository.saved).containsExactly(AiProvider.Anthropic, "sk-ant-key")
    }

    @Test
    fun `blank key is refused`() = runTest {
        assertThat(saveApiKey(AiProvider.Gemini, " \t ")).isFalse()

        assertThat(repository.saved).isEmpty()
    }
}
