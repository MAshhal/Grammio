package com.mystic.grammio.presentation.settings.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.repository.ApiKeyRepository
import com.mystic.grammio.domain.repository.ModelCatalogRepository
import com.mystic.grammio.domain.repository.ProviderSettingsRepository
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.domain.usecase.SaveApiKeyUseCase
import com.mystic.grammio.domain.usecase.SaveCustomEndpointUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Settings for the active provider: its key, its endpoint when it is a custom one, and its model.
 * Saving a key or endpoint goes through use cases because they have rules; the rest has none, so it
 * uses the repositories directly rather than through pass-through use cases.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProviderSettingsViewModel(
    private val apiKeyRepository: ApiKeyRepository,
    private val providerSettings: ProviderSettingsRepository,
    private val modelCatalog: ModelCatalogRepository,
    private val saveApiKey: SaveApiKeyUseCase,
    private val saveCustomEndpoint: SaveCustomEndpointUseCase,
) : ViewModel() {

    /** What is stored for the active provider. */
    private data class Stored(
        val provider: AiProvider,
        val hasApiKey: Boolean,
        val selectedModelId: String?,
        val customBaseUrl: String?,
    )

    /** What the user is typing. A null [baseUrl] means untouched, so the saved URL shows. */
    private data class Inputs(
        val key: String = "",
        val baseUrl: String? = null,
        val isBaseUrlInvalid: Boolean = false,
    )

    private val stored: Flow<Stored> = providerSettings.activeProvider.flatMapLatest { provider ->
        combine(
            apiKeyRepository.hasApiKey(provider),
            providerSettings.selectedModel(provider),
            providerSettings.customBaseUrl,
        ) { hasKey, modelId, baseUrl -> Stored(provider, hasKey, modelId, baseUrl) }
    }
    private val inputs = MutableStateFlow(Inputs())
    private val models = MutableStateFlow<ModelListUiState>(ModelListUiState.Idle)
    private var modelsJob: Job? = null

    /** The provider, key presence and endpoint the model list was last synced for. */
    private var modelsSyncedFor: Triple<AiProvider, Boolean, String?>? = null

    val state: StateFlow<ProviderSettingsUiState> =
        combine(
            stored.onEach(::syncModels),
            inputs,
            models,
        ) { stored, inputs, models ->
            ProviderSettingsUiState(
                provider = stored.provider,
                hasApiKey = stored.hasApiKey,
                keyInput = inputs.key,
                savedBaseUrl = stored.customBaseUrl,
                baseUrlInput = inputs.baseUrl ?: stored.customBaseUrl.orEmpty(),
                isBaseUrlInvalid = inputs.isBaseUrlInvalid,
                defaultModelId = providerSettings.defaultModel(stored.provider),
                selectedModelId = stored.selectedModelId,
                models = models,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProviderSettingsUiState())

    fun onAction(action: ProviderSettingsAction) {
        when (action) {
            is ProviderSettingsAction.ProviderSelected -> {
                inputs.value = Inputs()
                viewModelScope.launch { providerSettings.setActiveProvider(action.provider) }
            }

            is ProviderSettingsAction.KeyInputChanged -> inputs.update { it.copy(key = action.value) }

            ProviderSettingsAction.Save -> viewModelScope.launch {
                if (saveApiKey(activeProvider(), inputs.value.key)) inputs.update { it.copy(key = "") }
            }

            ProviderSettingsAction.Clear -> viewModelScope.launch { apiKeyRepository.clear(activeProvider()) }

            is ProviderSettingsAction.BaseUrlInputChanged ->
                inputs.update { it.copy(baseUrl = action.value, isBaseUrlInvalid = false) }

            ProviderSettingsAction.SaveBaseUrl -> viewModelScope.launch {
                val saved = saveCustomEndpoint(inputs.value.baseUrl.orEmpty())
                inputs.update { if (saved) it.copy(baseUrl = null) else it.copy(isBaseUrlInvalid = true) }
            }

            is ProviderSettingsAction.ModelSelected -> viewModelScope.launch {
                providerSettings.setSelectedModel(activeProvider(), action.modelId)
            }

            ProviderSettingsAction.RefreshModels -> viewModelScope.launch { loadModels(activeProvider()) }
        }
    }

    /**
     * Fetches the model list whenever the provider changes or becomes reachable (key or endpoint
     * saved). Runs from the state flow, so nothing is fetched while no screen is watching.
     */
    private fun syncModels(stored: Stored) {
        val syncKey = Triple(stored.provider, stored.hasApiKey, stored.customBaseUrl)
        if (syncKey == modelsSyncedFor) return
        modelsSyncedFor = syncKey

        val reachable = stored.hasApiKey &&
            (stored.provider != AiProvider.OpenAiCompatible || stored.customBaseUrl != null)
        if (reachable) loadModels(stored.provider) else clearModels()
    }

    private suspend fun activeProvider(): AiProvider = providerSettings.activeProvider.first()

    private fun loadModels(provider: AiProvider) {
        modelsJob?.cancel()
        modelsJob = viewModelScope.launch {
            models.value = ModelListUiState.Loading
            models.value = when (val result = modelCatalog.listModels(provider)) {
                is Outcome.Success -> ModelListUiState.Loaded(result.value)
                is Outcome.Failure -> ModelListUiState.Failed(result.error)
            }
        }
    }

    private fun clearModels() {
        modelsJob?.cancel()
        models.value = ModelListUiState.Idle
    }
}
