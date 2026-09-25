package com.mystic.grammio.data.repository

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.LlmProvider
import com.mystic.grammio.data.prompt.PromptBuilder
import com.mystic.grammio.domain.model.Outcome
import com.mystic.grammio.domain.model.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TextTransformRepositoryImplTest {

    private class StubProvider(var reply: Outcome<String, TransformError>) : LlmProvider {
        var lastPrompt: LlmPrompt? = null
        override suspend fun generate(prompt: LlmPrompt) = reply.also { lastPrompt = prompt }
    }

    private val provider = StubProvider(Outcome.Success("Hello."))
    private val repository = TextTransformRepositoryImpl(PromptBuilder(), provider)

    @Test
    fun `builds the prompt from the input and wraps the reply`() = runTest {
        val result = repository.transform("helo", Transformation.FixGrammar)

        assertThat(result).isEqualTo(Outcome.Success(TransformedText("Hello.", Transformation.FixGrammar)))
        assertThat(provider.lastPrompt?.userText).contains("helo")
    }

    @Test
    fun `provider failures pass through`() = runTest {
        provider.reply = Outcome.Failure(TransformError.RateLimited)

        assertThat(repository.transform("x", Transformation.Shorten))
            .isEqualTo(Outcome.Failure(TransformError.RateLimited))
    }

    @Test
    fun `reply that is empty after cleanup is a failure`() = runTest {
        provider.reply = Outcome.Success("  \"\"  ")

        assertThat(repository.transform("x", Transformation.Shorten))
            .isEqualTo(Outcome.Failure(TransformError.Unknown))
    }

    @Test
    fun `cleanup strips fences, echoed tags and wrapping quotes`() {
        assertThat(cleanModelOutput("```\nHello\nthere\n```")).isEqualTo("Hello\nthere")
        assertThat(cleanModelOutput("```text\nHello\n```")).isEqualTo("Hello")
        assertThat(cleanModelOutput("<text>\nHello\n</text>")).isEqualTo("Hello")
        assertThat(cleanModelOutput("\"Hello\"")).isEqualTo("Hello")
        assertThat(cleanModelOutput("“Hello”")).isEqualTo("Hello")
        assertThat(cleanModelOutput("  Hello  ")).isEqualTo("Hello")
    }

    @Test
    fun `cleanup keeps quotes that are part of the text`() {
        assertThat(cleanModelOutput("He said \"hi\" twice")).isEqualTo("He said \"hi\" twice")
        assertThat(cleanModelOutput("\"")).isEqualTo("\"")
    }
}
