package com.mystic.grammio.data.provider

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.testing.FakeApiKeyLocalDataSource
import com.mystic.grammio.testing.FakeProviderPreferencesLocalDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProviderConnectionResolverTest {

    private val apiKeys = FakeApiKeyLocalDataSource(
        mapOf(AiProvider.Anthropic to "ant-key", AiProvider.OpenAiCompatible to "custom-key"),
    )
    private val preferences = FakeProviderPreferencesLocalDataSource()
    private val resolver = ProviderConnectionResolver(apiKeys, preferences)

    @Test
    fun `first-party provider uses its default base URL and model`() = runTest {
        assertThat(resolver.resolve(AiProvider.Anthropic)).isEqualTo(
            Outcome.Success(
                LlmConnection(
                    apiKey = "ant-key",
                    modelId = ProviderDefaults.model(AiProvider.Anthropic)!!,
                    baseUrl = ProviderDefaults.baseUrl(AiProvider.Anthropic)!!,
                ),
            ),
        )
    }

    @Test
    fun `selected model overrides the default`() = runTest {
        preferences.setSelectedModel(AiProvider.Anthropic, "claude-sonnet-5")

        val connection = (resolver.resolve(AiProvider.Anthropic) as Outcome.Success).value

        assertThat(connection.modelId).isEqualTo("claude-sonnet-5")
    }

    @Test
    fun `missing key comes first`() = runTest {
        assertThat(resolver.resolve(AiProvider.Gemini)).isEqualTo(Outcome.Failure(TransformError.MissingApiKey))
    }

    @Test
    fun `custom endpoint needs a base URL and a model`() = runTest {
        val notConfigured = Outcome.Failure(TransformError.ProviderNotConfigured)
        assertThat(resolver.resolve(AiProvider.OpenAiCompatible)).isEqualTo(notConfigured)

        preferences.setCustomBaseUrl("https://openrouter.ai/api/v1")
        assertThat(resolver.resolve(AiProvider.OpenAiCompatible)).isEqualTo(notConfigured)

        preferences.setSelectedModel(AiProvider.OpenAiCompatible, "some/model")
        assertThat(resolver.resolve(AiProvider.OpenAiCompatible)).isEqualTo(
            Outcome.Success(LlmConnection("custom-key", "some/model", "https://openrouter.ai/api/v1")),
        )
    }

    @Test
    fun `connection never prints its key`() {
        assertThat(LlmConnection("secret-key", "m", "https://x.test").toString()).doesNotContain("secret-key")
    }
}
