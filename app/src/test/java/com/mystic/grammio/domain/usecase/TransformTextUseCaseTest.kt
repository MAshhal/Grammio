package com.mystic.grammio.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.model.Outcome
import com.mystic.grammio.domain.model.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.testing.RecordingTextTransformRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TransformTextUseCaseTest {

    private val repository = RecordingTextTransformRepository()
    private val useCase = TransformTextUseCase(repository)

    @Test
    fun `blank input fails without calling the repository`() = runTest {
        val result = useCase("   \n\t ", Transformation.FixGrammar)

        assertThat(result).isEqualTo(Outcome.Failure(TransformError.EmptyInput))
        assertThat(repository.calls).isEmpty()
    }

    @Test
    fun `input over the limit fails without calling the repository`() = runTest {
        val text = "a".repeat(TransformTextUseCase.MAX_INPUT_CHARS + 1)

        val result = useCase(text, Transformation.Shorten)

        assertThat(result).isEqualTo(
            Outcome.Failure(TransformError.InputTooLong(TransformTextUseCase.MAX_INPUT_CHARS)),
        )
        assertThat(repository.calls).isEmpty()
    }

    @Test
    fun `input at the limit is accepted`() = runTest {
        val text = "a".repeat(TransformTextUseCase.MAX_INPUT_CHARS)

        val result = useCase(text, Transformation.Shorten)

        assertThat(result).isInstanceOf(Outcome.Success::class.java)
    }

    @Test
    fun `input is trimmed before delegating`() = runTest {
        useCase("  hello world \n", Transformation.Rephrase)

        assertThat(repository.calls).containsExactly("hello world" to Transformation.Rephrase)
    }

    @Test
    fun `repository failure is propagated unchanged`() = runTest {
        repository.nextResult = Outcome.Failure(TransformError.RateLimited)

        val result = useCase("hello", Transformation.Translate("es"))

        assertThat(result).isEqualTo(Outcome.Failure(TransformError.RateLimited))
    }
}
