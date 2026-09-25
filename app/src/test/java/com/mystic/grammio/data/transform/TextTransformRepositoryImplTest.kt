package com.mystic.grammio.data.transform

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.transform.prompt.PromptBuilder
import com.mystic.grammio.data.transform.sanitize.ModelOutputSanitizer
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.result.Outcome
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TextTransformRepositoryImplTest {

    private class StubLlmDataSource(var reply: Outcome<String, TransformError>) : LlmDataSource {
        var lastPrompt: LlmPrompt? = null
        var lastApiKey: String? = null

        override suspend fun generate(
            prompt: LlmPrompt,
            apiKey: String,
        ) = reply.also {
            lastPrompt = prompt
            lastApiKey = apiKey
        }
    }

    private var apiKey: String? = "test-key"
    private val llmDataSource = StubLlmDataSource(Outcome.Success("Hello."))
    private val repository = TextTransformRepositoryImpl(
        apiKeyProvider = { apiKey },
        promptBuilder = PromptBuilder(),
        llmDataSource = llmDataSource,
        sanitizer = ModelOutputSanitizer(),
    )

    @Test
    fun `builds the prompt from the input and wraps the reply`() = runTest {
        val result = repository.transform("helo", Transformation.FixGrammar)

        assertThat(result).isEqualTo(Outcome.Success(TransformedText("Hello.", Transformation.FixGrammar)))
        assertThat(llmDataSource.lastPrompt?.userText).contains("helo")
        assertThat(llmDataSource.lastApiKey).isEqualTo("test-key")
    }

    @Test
    fun `missing key fails without calling the LLM`() = runTest {
        apiKey = null

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
}
