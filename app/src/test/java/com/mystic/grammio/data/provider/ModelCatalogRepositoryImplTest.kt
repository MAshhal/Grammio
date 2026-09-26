package com.mystic.grammio.data.provider

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.llm.LlmDataSourceRegistry
import com.mystic.grammio.data.llm.LlmEndpoint
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.testing.FakeApiKeyLocalDataSource
import com.mystic.grammio.testing.FakeProviderPreferencesLocalDataSource
import com.mystic.grammio.testing.StubLlmDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ModelCatalogRepositoryImplTest {

    private val models = listOf(AiModel("m1", "Model 1"))
    private val apiKeys = FakeApiKeyLocalDataSource(mapOf(AiProvider.OpenAiCompatible to "custom-key"))
    private val preferences = FakeProviderPreferencesLocalDataSource()
    private val openAi = StubLlmDataSource(models = Outcome.Success(models))
    private val repository = ModelCatalogRepositoryImpl(
        connectionResolver = ProviderConnectionResolver(apiKeys, preferences),
        llmDataSources = LlmDataSourceRegistry(
            gemini = StubLlmDataSource(),
            openAi = openAi,
            anthropic = StubLlmDataSource(),
        ),
    )

    @Test
    fun `lists models from the provider's endpoint`() = runTest {
        preferences.setCustomBaseUrl("https://llm.test/v1")

        assertThat(repository.listModels(AiProvider.OpenAiCompatible)).isEqualTo(Outcome.Success(models))
        assertThat(openAi.lastEndpoint).isEqualTo(LlmEndpoint("custom-key", "https://llm.test/v1"))
    }

    @Test
    fun `listing needs no model, but does need a key and base URL`() = runTest {
        assertThat(repository.listModels(AiProvider.OpenAiCompatible))
            .isEqualTo(Outcome.Failure(TransformError.ProviderNotConfigured))
        assertThat(repository.listModels(AiProvider.OpenAi))
            .isEqualTo(Outcome.Failure(TransformError.MissingApiKey))
        assertThat(openAi.lastEndpoint).isNull()
    }
}
