package com.mystic.grammio.feature.process

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.model.Outcome
import com.mystic.grammio.domain.model.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.usecase.TransformTextUseCase
import com.mystic.grammio.testing.MainDispatcherRule
import com.mystic.grammio.testing.RecordingTextTransformRepository
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProcessTextViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = RecordingTextTransformRepository(latencyMs = 100)

    private fun viewModel(
        text: String = "helo wrld",
        canReplace: Boolean = true,
    ) = ProcessTextViewModel(
        input = ProcessTextInput(text, canReplace),
        transformText = TransformTextUseCase(repository),
        defaultTargetLanguageTag = "en",
    )

    @Test
    fun `initial state reflects the input`() {
        val state = viewModel(text = "hi", canReplace = false).state.value

        assertThat(state.originalText).isEqualTo("hi")
        assertThat(state.canReplace).isFalse()
        assertThat(state.selected).isNull()
        assertThat(state.result).isEqualTo(ResultState.Idle)
        assertThat(state.transformations).contains(Transformation.Translate("en"))
    }

    @Test
    fun `select goes loading then success`() = runTest {
        val vm = viewModel()

        vm.state.test {
            assertThat(awaitItem().result).isEqualTo(ResultState.Idle)

            vm.onAction(ProcessTextAction.Select(Transformation.FixGrammar))

            val loading = awaitItem()
            assertThat(loading.selected).isEqualTo(Transformation.FixGrammar)
            assertThat(loading.result).isEqualTo(ResultState.Loading)
            assertThat(awaitItem().result).isEqualTo(ResultState.Success("FixGrammar:helo wrld"))
        }
    }

    @Test
    fun `failure is exposed and retry re-runs the selected transformation`() = runTest {
        val vm = viewModel()
        repository.nextResult = Outcome.Failure(TransformError.Network)

        vm.onAction(ProcessTextAction.Select(Transformation.Shorten))
        advanceUntilIdle()
        assertThat(vm.state.value.result).isEqualTo(ResultState.Failure(TransformError.Network))

        repository.nextResult = null
        vm.onAction(ProcessTextAction.Retry)
        advanceUntilIdle()

        assertThat(vm.state.value.result).isEqualTo(ResultState.Success("Shorten:helo wrld"))
        assertThat(repository.calls.map { it.second })
            .containsExactly(Transformation.Shorten, Transformation.Shorten)
    }

    @Test
    fun `a new selection cancels the in-flight one`() = runTest {
        val vm = viewModel()

        vm.onAction(ProcessTextAction.Select(Transformation.Casual))
        vm.onAction(ProcessTextAction.Select(Transformation.Professional))
        advanceUntilIdle()

        assertThat(vm.state.value.selected).isEqualTo(Transformation.Professional)
        assertThat(vm.state.value.result).isEqualTo(ResultState.Success("Professional:helo wrld"))
    }

    @Test
    fun `changing language re-runs only when translate is selected`() = runTest {
        val vm = viewModel()

        vm.onAction(ProcessTextAction.ChangeTargetLanguage("de"))
        advanceUntilIdle()
        assertThat(repository.calls).isEmpty()

        vm.onAction(ProcessTextAction.Select(Transformation.Translate("de")))
        vm.onAction(ProcessTextAction.ChangeTargetLanguage("fr"))
        advanceUntilIdle()

        assertThat(vm.state.value.selected).isEqualTo(Transformation.Translate("fr"))
        assertThat(vm.state.value.result).isEqualTo(ResultState.Success("${Transformation.Translate("fr")}:helo wrld"))
    }

    @Test
    fun `replace returns the result when the caller accepts it`() = runTest {
        val vm = viewModel(canReplace = true)
        vm.onAction(ProcessTextAction.Select(Transformation.FixGrammar))
        advanceUntilIdle()

        vm.effects.test {
            vm.onAction(ProcessTextAction.Replace)
            assertThat(awaitItem()).isEqualTo(ProcessTextEffect.ReturnResult("FixGrammar:helo wrld"))
        }
    }

    @Test
    fun `replace does nothing when the selection is read-only`() = runTest {
        val vm = viewModel(canReplace = false)
        vm.onAction(ProcessTextAction.Select(Transformation.FixGrammar))
        advanceUntilIdle()

        vm.effects.test {
            vm.onAction(ProcessTextAction.Replace)
            vm.onAction(ProcessTextAction.Copy)
            assertThat(awaitItem()).isEqualTo(ProcessTextEffect.CopyToClipboard("FixGrammar:helo wrld"))
        }
    }

    @Test
    fun `copy and replace do nothing before there is a result`() = runTest {
        val vm = viewModel()

        vm.effects.test {
            vm.onAction(ProcessTextAction.Copy)
            vm.onAction(ProcessTextAction.Replace)
            vm.onAction(ProcessTextAction.Dismiss)
            assertThat(awaitItem()).isEqualTo(ProcessTextEffect.Close)
        }
    }
}
