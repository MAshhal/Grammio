package com.mystic.grammio.presentation.settings.prompt

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.testing.InMemoryPromptSettingsRepository
import com.mystic.grammio.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SystemPromptViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val promptSettings = InMemoryPromptSettingsRepository(defaultSystemPrompt = "Default rules.")

    // Lazy: must be created after MainDispatcherRule has installed the test Main dispatcher.
    private val viewModel by lazy { SystemPromptViewModel(promptSettings) }

    private fun TestScope.collectState() {
        // Create the ViewModel here, not inside the background coroutine: its sharing then starts as
        // foreground work that advanceUntilIdle waits for.
        val state = viewModel.state
        backgroundScope.launch { state.collect {} }
    }

    @Test
    fun `shows the default until edited, and saves the edit`() = runTest {
        collectState()
        advanceUntilIdle()
        with(viewModel.state.value) {
            assertThat(input).isEqualTo("Default rules.")
            assertThat(isDefault).isTrue()
            assertThat(canSave).isFalse()
            assertThat(canReset).isFalse()
        }

        viewModel.onAction(SystemPromptAction.InputChanged("My rules."))
        advanceUntilIdle()
        assertThat(viewModel.state.value.canSave).isTrue()

        viewModel.onAction(SystemPromptAction.Save)
        advanceUntilIdle()

        assertThat(promptSettings.systemPrompt.value).isEqualTo("My rules.")
        with(viewModel.state.value) {
            assertThat(input).isEqualTo("My rules.")
            assertThat(isDefault).isFalse()
            assertThat(canSave).isFalse()
            assertThat(canReset).isTrue()
        }
    }

    @Test
    fun `reset discards both the saved prompt and the typing`() = runTest {
        collectState()
        promptSettings.setSystemPrompt("My rules.")
        viewModel.onAction(SystemPromptAction.InputChanged("Half-typed"))
        advanceUntilIdle()

        viewModel.onAction(SystemPromptAction.ResetToDefault)
        advanceUntilIdle()

        assertThat(promptSettings.systemPrompt.value).isEqualTo("Default rules.")
        assertThat(viewModel.state.value.input).isEqualTo("Default rules.")
    }
}
