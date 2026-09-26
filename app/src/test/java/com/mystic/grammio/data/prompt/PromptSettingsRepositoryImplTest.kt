package com.mystic.grammio.data.prompt

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.transform.prompt.DefaultSystemPrompt
import com.mystic.grammio.testing.FakePromptPreferencesLocalDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PromptSettingsRepositoryImplTest {

    private val localDataSource = FakePromptPreferencesLocalDataSource()
    private val repository = PromptSettingsRepositoryImpl(localDataSource)

    @Test
    fun `the default applies until the user saves their own`() = runTest {
        assertThat(repository.systemPrompt.first()).isEqualTo(DefaultSystemPrompt.TEXT)

        repository.setSystemPrompt("  Be terse.  ")

        assertThat(repository.systemPrompt.first()).isEqualTo("Be terse.")
    }

    @Test
    fun `blank text or the default itself is not stored`() = runTest {
        repository.setSystemPrompt("Be terse.")
        repository.setSystemPrompt("   ")
        assertThat(localDataSource.customSystemPrompt.value).isNull()

        repository.setSystemPrompt("Be terse.")
        repository.setSystemPrompt(DefaultSystemPrompt.TEXT + "\n")
        assertThat(localDataSource.customSystemPrompt.value).isNull()
    }

    @Test
    fun `reset goes back to the default`() = runTest {
        repository.setSystemPrompt("Be terse.")

        repository.resetSystemPrompt()

        assertThat(repository.systemPrompt.first()).isEqualTo(DefaultSystemPrompt.TEXT)
    }
}
