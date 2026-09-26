package com.mystic.grammio.data.transform

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.llm.LlmDataSourceRegistry
import com.mystic.grammio.data.provider.ProviderConnectionResolver
import com.mystic.grammio.data.transform.prompt.PromptBuilder
import com.mystic.grammio.data.transform.sanitize.ModelOutputSanitizer
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.domain.result.map
import com.mystic.grammio.testing.FakeApiKeyLocalDataSource
import com.mystic.grammio.testing.FakeProviderPreferencesLocalDataSource
import com.mystic.grammio.testing.FakeTransformationLogPreferencesLocalDataSource
import com.mystic.grammio.testing.FixedClock
import com.mystic.grammio.testing.InMemoryPromptSettingsRepository
import com.mystic.grammio.testing.RecordingTransformationLogLocalDataSource
import com.mystic.grammio.testing.StubLlmDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TextTransformRepositoryImplTest {

    private val apiKeyDataSource = FakeApiKeyLocalDataSource(
        mapOf(AiProvider.Gemini to "test-key", AiProvider.Anthropic to "ant-key"),
    )
    private val preferences = FakeProviderPreferencesLocalDataSource()
    private val llmDataSource = StubLlmDataSource(Outcome.Success("Hello."))
    private val anthropicDataSource = StubLlmDataSource(Outcome.Success("From Claude."))
    private val transformationLog = RecordingTransformationLogLocalDataSource()
    private val logPreferences = FakeTransformationLogPreferencesLocalDataSource(enabled = true)
    private val clock = FixedClock()
    private val promptSettings = InMemoryPromptSettingsRepository()
    private val repository = TextTransformRepositoryImpl(
        connectionResolver = ProviderConnectionResolver(apiKeyDataSource, preferences),
        promptBuilder = PromptBuilder(),
        promptSettings = promptSettings,
        llmDataSources = LlmDataSourceRegistry(
            gemini = llmDataSource,
            openAi = StubLlmDataSource(Outcome.Failure(TransformError.Unknown)),
            anthropic = anthropicDataSource,
        ),
        sanitizer = ModelOutputSanitizer(),
        transformationLog = transformationLog,
        logPreferences = logPreferences,
        clock = clock,
    )

    @Test
    fun `builds the prompt from the input and wraps the reply`() = runTest {
        val result = repository.transform("helo", Transformation.FixGrammar)

        assertThat(result).isEqualTo(Outcome.Success(TransformedText("Hello.", Transformation.FixGrammar)))
        assertThat(llmDataSource.lastPrompt?.userText).contains("helo")
        assertThat(llmDataSource.lastConnection?.apiKey).isEqualTo("test-key")
    }

    @Test
    fun `sends the user's system prompt`() = runTest {
        promptSettings.setSystemPrompt("Always answer in lowercase.")

        repository.transform("helo", Transformation.FixGrammar)

        assertThat(llmDataSource.lastPrompt?.systemInstruction).startsWith("Always answer in lowercase.")
    }

    @Test
    fun `calls the active provider's data source with its connection`() = runTest {
        preferences.setActiveProvider(AiProvider.Anthropic)

        val result = repository.transform("hi", Transformation.Casual)

        assertThat(result).isEqualTo(Outcome.Success(TransformedText("From Claude.", Transformation.Casual)))
        assertThat(anthropicDataSource.lastConnection?.apiKey).isEqualTo("ant-key")
        assertThat(llmDataSource.lastPrompt).isNull()
    }

    @Test
    fun `unconfigured provider fails without calling the LLM`() = runTest {
        apiKeyDataSource.write(AiProvider.OpenAiCompatible, "key")
        preferences.setActiveProvider(AiProvider.OpenAiCompatible)

        assertThat(repository.transform("x", Transformation.Shorten))
            .isEqualTo(Outcome.Failure(TransformError.ProviderNotConfigured))
    }

    @Test
    fun `missing key fails without calling the LLM`() = runTest {
        apiKeyDataSource.clear(AiProvider.Gemini)

        assertThat(repository.transform("x", Transformation.Shorten))
            .isEqualTo(Outcome.Failure(TransformError.MissingApiKey))
        assertThat(llmDataSource.lastPrompt).isNull()
    }

    @Test
    fun `LLM failures pass through`() = runTest {
        llmDataSource.reply = Outcome.Failure(TransformError.RateLimited)

        assertThat(repository.transform("x", Transformation.Shorten))
            .isEqualTo(Outcome.Failure(TransformError.RateLimited))
    }

    @Test
    fun `reply that is empty after cleanup is a failure`() = runTest {
        llmDataSource.reply = Outcome.Success("  \"\"  ")

        assertThat(repository.transform("x", Transformation.Shorten))
            .isEqualTo(Outcome.Failure(TransformError.Unknown))
    }

    @Test
    fun `logs a success with the model that produced it`() = runTest {
        preferences.setActiveProvider(AiProvider.Anthropic)
        preferences.setSelectedModel(AiProvider.Anthropic, "claude-haiku-4-5")

        repository.transform("hi", Transformation.Translate("es"))

        val entry = transformationLog.entries.single()
        assertThat(entry.startedAt).isEqualTo(clock.now)
        assertThat(entry.transformation).isEqualTo(Transformation.Translate("es"))
        assertThat(entry.provider).isEqualTo(AiProvider.Anthropic)
        assertThat(entry.modelId).isEqualTo("claude-haiku-4-5")
        assertThat(entry.inputText).isEqualTo("hi")
        assertThat(entry.result).isEqualTo(Outcome.Success("From Claude."))
    }

    @Test
    fun `logs nothing unless history is on`() = runTest {
        logPreferences.setEnabled(false)

        val result = repository.transform("helo", Transformation.FixGrammar)

        assertThat(result).isEqualTo(Outcome.Success(TransformedText("Hello.", Transformation.FixGrammar)))
        assertThat(transformationLog.entries).isEmpty()
    }

    @Test
    fun `logs the cleaned-up text, not the raw reply`() = runTest {
        llmDataSource.reply = Outcome.Success("  \"Hello.\"  ")

        val result = repository.transform("helo", Transformation.FixGrammar)

        assertThat(transformationLog.entries.single().result).isEqualTo(result.map { it.text })
    }

    @Test
    fun `logs an LLM failure with the model it happened on`() = runTest {
        llmDataSource.reply = Outcome.Failure(TransformError.RateLimited)

        repository.transform("x", Transformation.Shorten)

        val entry = transformationLog.entries.single()
        assertThat(entry.modelId).isNotNull()
        assertThat(entry.result).isEqualTo(Outcome.Failure(TransformError.RateLimited))
    }

    @Test
    fun `logs a provider that could not be resolved, without a model`() = runTest {
        apiKeyDataSource.clear(AiProvider.Gemini)

        repository.transform("x", Transformation.Shorten)

        val entry = transformationLog.entries.single()
        assertThat(entry.provider).isEqualTo(AiProvider.Gemini)
        assertThat(entry.modelId).isNull()
        assertThat(entry.result).isEqualTo(Outcome.Failure(TransformError.MissingApiKey))
    }
}
