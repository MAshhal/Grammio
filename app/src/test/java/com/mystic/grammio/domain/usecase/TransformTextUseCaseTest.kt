package com.mystic.grammio.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.testing.RecordingTextTransformRepository
import com.mystic.grammio.testing.TestTransformations.fixGrammar
import com.mystic.grammio.testing.TestTransformations.shorten
import com.mystic.grammio.testing.TestTransformations.translate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TransformTextUseCaseTest {

    private val repository = RecordingTextTransformRepository()
    private val useCase = TransformTextUseCase(repository)

    @Test
    fun `blank input fails without calling the repository`() = runTest {
        val result = useCase("   \n\t ", fixGrammar, "en")

        assertThat(result).isEqualTo(Outcome.Failure(TransformError.EmptyInput))
        assertThat(repository.calls).isEmpty()
    }

    @Test
    fun `input over the limit fails without calling the repository`() = runTest {
        val text = "a".repeat(TransformTextUseCase.MAX_INPUT_CHARS + 1)

        val result = useCase(text, shorten, "en")

        assertThat(result).isEqualTo(
            Outcome.Failure(TransformError.InputTooLong(TransformTextUseCase.MAX_INPUT_CHARS)),
        )
        assertThat(repository.calls).isEmpty()
    }

    @Test
    fun `input at the limit is accepted`() = runTest {
        val text = "a".repeat(TransformTextUseCase.MAX_INPUT_CHARS)

        val result = useCase(text, shorten, "en")

        assertThat(result).isInstanceOf(Outcome.Success::class.java)
    }

    @Test
    fun `input is trimmed before delegating`() = runTest {
        useCase("  hello world \n", shorten, "en")

        assertThat(repository.calls).containsExactly("hello world" to shorten)
    }

    @Test
    fun `repository failure is propagated unchanged`() = runTest {
        repository.nextResult = Outcome.Failure(TransformError.RateLimited)

        val result = useCase("hello", translate, "es")

        assertThat(result).isEqualTo(Outcome.Failure(TransformError.RateLimited))
    }
}
