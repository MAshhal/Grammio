package com.mystic.grammio.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.testing.InMemoryApiKeyRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveApiKeyUseCaseTest {

    private val repository = InMemoryApiKeyRepository()
    private val saveApiKey = SaveApiKeyUseCase(repository)

    @Test
    fun `key is trimmed before saving`() = runTest {
        assertThat(saveApiKey("  AIza-key \n")).isTrue()

        assertThat(repository.saved).isEqualTo("AIza-key")
    }

    @Test
    fun `blank key is refused`() = runTest {
        assertThat(saveApiKey(" \t ")).isFalse()

        assertThat(repository.saved).isNull()
    }
}
