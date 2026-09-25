package com.mystic.grammio.presentation.settings

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.repository.ApiKeyRepository
import com.mystic.grammio.testing.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class InMemoryApiKeyRepository : ApiKeyRepository {
        var saved: String? = null
        override val hasApiKey = MutableStateFlow(false)
        override suspend fun save(apiKey: String) {
            saved = apiKey
            hasApiKey.value = true
        }
        override suspend fun clear() {
            saved = null
            hasApiKey.value = false
        }
    }

    private val repository = InMemoryApiKeyRepository()

    // Lazy: must be created after MainDispatcherRule has installed the test Main dispatcher.
    private val viewModel by lazy { SettingsViewModel(repository) }

    @Test
    fun `saving stores the trimmed key and clears the field`() = runTest {
        backgroundScope.launch { viewModel.state.collect {} }

        viewModel.onAction(SettingsAction.KeyInputChanged("  AIza-key  "))
        advanceUntilIdle()
        assertThat(viewModel.state.value.canSave).isTrue()

        viewModel.onAction(SettingsAction.Save)
        advanceUntilIdle()

        assertThat(repository.saved).isEqualTo("AIza-key")
        assertThat(viewModel.state.value).isEqualTo(SettingsUiState(hasApiKey = true, keyInput = ""))
    }

    @Test
    fun `blank input is not saved and clear removes the key`() = runTest {
        backgroundScope.launch { viewModel.state.collect {} }

        viewModel.onAction(SettingsAction.KeyInputChanged("   "))
        viewModel.onAction(SettingsAction.Save)
        advanceUntilIdle()
        assertThat(repository.saved).isNull()
        assertThat(viewModel.state.value.canSave).isFalse()

        repository.save("k")
        advanceUntilIdle()
        assertThat(viewModel.state.value.hasApiKey).isTrue()

        viewModel.onAction(SettingsAction.Clear)
        advanceUntilIdle()
        assertThat(viewModel.state.value.hasApiKey).isFalse()
    }
}
