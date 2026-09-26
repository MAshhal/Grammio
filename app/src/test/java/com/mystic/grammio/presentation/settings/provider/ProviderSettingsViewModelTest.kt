package com.mystic.grammio.presentation.settings.provider

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.domain.usecase.SaveApiKeyUseCase
import com.mystic.grammio.domain.usecase.SaveCustomEndpointUseCase
import com.mystic.grammio.testing.FakeModelCatalogRepository
import com.mystic.grammio.testing.InMemoryApiKeyRepository
import com.mystic.grammio.testing.InMemoryProviderSettingsRepository
import com.mystic.grammio.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProviderSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val apiKeys = InMemoryApiKeyRepository()
    private val providerSettings = InMemoryProviderSettingsRepository()
    private val modelCatalog = FakeModelCatalogRepository()

    // Lazy: must be created after MainDispatcherRule has installed the test Main dispatcher.
    private val viewModel by lazy {
        ProviderSettingsViewModel(
            apiKeyRepository = apiKeys,
            providerSettings = providerSettings,
            modelCatalog = modelCatalog,
            saveApiKey = SaveApiKeyUseCase(apiKeys),
            saveCustomEndpoint = SaveCustomEndpointUseCase(providerSettings),
        )
    }

    private fun TestScope.collectState() {
        backgroundScope.launch { viewModel.state.collect {} }
    }

    @Test
    fun `saving stores the trimmed key for the active provider and clears the field`() = runTest {
        collectState()
        viewModel.onAction(ProviderSettingsAction.ProviderSelected(AiProvider.Anthropic))
        viewModel.onAction(ProviderSettingsAction.KeyInputChanged("  sk-ant-key  "))
        advanceUntilIdle()
        assertThat(viewModel.state.value.canSave).isTrue()

        viewModel.onAction(ProviderSettingsAction.Save)
        advanceUntilIdle()

        assertThat(apiKeys.saved).containsExactly(AiProvider.Anthropic, "sk-ant-key")
        with(viewModel.state.value) {
            assertThat(provider).isEqualTo(AiProvider.Anthropic)
            assertThat(hasApiKey).isTrue()
            assertThat(keyInput).isEmpty()
        }
    }

    @Test
    fun `blank input is not saved and clear removes only the active provider's key`() = runTest {
        collectState()
        apiKeys.save(AiProvider.Gemini, "g")
        apiKeys.save(AiProvider.OpenAi, "o")
        viewModel.onAction(ProviderSettingsAction.KeyInputChanged("   "))
        viewModel.onAction(ProviderSettingsAction.Save)
        advanceUntilIdle()
        assertThat(viewModel.state.value.canSave).isFalse()

        viewModel.onAction(ProviderSettingsAction.Clear)
        advanceUntilIdle()

        assertThat(viewModel.state.value.hasApiKey).isFalse()
        assertThat(apiKeys.saved).containsExactly(AiProvider.OpenAi, "o")
    }

    @Test
    fun `switching provider shows that provider's key, default model and models`() = runTest {
        collectState()
        val models = listOf(AiModel("gpt-a", "gpt-a"))
        modelCatalog.result = Outcome.Success(models)
        apiKeys.save(AiProvider.OpenAi, "o")
        viewModel.onAction(ProviderSettingsAction.KeyInputChanged("typed for gemini"))
        advanceUntilIdle()

        viewModel.onAction(ProviderSettingsAction.ProviderSelected(AiProvider.OpenAi))
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertThat(provider).isEqualTo(AiProvider.OpenAi)
            assertThat(hasApiKey).isTrue()
            assertThat(keyInput).isEmpty()
            assertThat(defaultModelId).isEqualTo("openai-default")
            assertThat(this.models).isEqualTo(ModelListUiState.Loaded(models))
        }
        assertThat(modelCatalog.requested).containsExactly(AiProvider.OpenAi)
    }

    @Test
    fun `models are not fetched without a key, and a failure can be retried`() = runTest {
        collectState()
        advanceUntilIdle()
        assertThat(viewModel.state.value.models).isEqualTo(ModelListUiState.Idle)
        assertThat(modelCatalog.requested).isEmpty()

        modelCatalog.result = Outcome.Failure(TransformError.InvalidApiKey)
        viewModel.onAction(ProviderSettingsAction.KeyInputChanged("bad"))
        viewModel.onAction(ProviderSettingsAction.Save)
        advanceUntilIdle()
        assertThat(viewModel.state.value.models).isEqualTo(ModelListUiState.Failed(TransformError.InvalidApiKey))

        modelCatalog.result = Outcome.Success(emptyList())
        viewModel.onAction(ProviderSettingsAction.RefreshModels)
        advanceUntilIdle()
        assertThat(viewModel.state.value.models).isEqualTo(ModelListUiState.Loaded(emptyList()))
    }

    @Test
    fun `custom endpoint lists models only once its base URL is saved`() = runTest {
        collectState()
        apiKeys.save(AiProvider.OpenAiCompatible, "c")
        viewModel.onAction(ProviderSettingsAction.ProviderSelected(AiProvider.OpenAiCompatible))
        advanceUntilIdle()
        assertThat(viewModel.state.value.needsBaseUrl).isTrue()
        assertThat(viewModel.state.value.defaultModelId).isNull()
        assertThat(modelCatalog.requested).isEmpty()

        viewModel.onAction(ProviderSettingsAction.BaseUrlInputChanged("http://insecure.test"))
        viewModel.onAction(ProviderSettingsAction.SaveBaseUrl)
        advanceUntilIdle()
        assertThat(viewModel.state.value.isBaseUrlInvalid).isTrue()

        viewModel.onAction(ProviderSettingsAction.BaseUrlInputChanged("https://llm.test/v1/"))
        viewModel.onAction(ProviderSettingsAction.SaveBaseUrl)
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertThat(isBaseUrlInvalid).isFalse()
            assertThat(savedBaseUrl).isEqualTo("https://llm.test/v1")
            assertThat(baseUrlInput).isEqualTo("https://llm.test/v1")
            assertThat(canSaveBaseUrl).isFalse()
        }
        assertThat(modelCatalog.requested).containsExactly(AiProvider.OpenAiCompatible)
    }

    @Test
    fun `selecting a model stores it for the active provider, and null goes back to the default`() = runTest {
        collectState()

        viewModel.onAction(ProviderSettingsAction.ModelSelected("gemini-pro"))
        advanceUntilIdle()
        assertThat(viewModel.state.value.selectedModelId).isEqualTo("gemini-pro")

        viewModel.onAction(ProviderSettingsAction.ModelSelected(null))
        advanceUntilIdle()
        assertThat(viewModel.state.value.selectedModelId).isNull()
    }
}
