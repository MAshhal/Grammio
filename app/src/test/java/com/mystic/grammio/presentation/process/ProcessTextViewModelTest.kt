package com.mystic.grammio.presentation.process

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.domain.usecase.TransformTextUseCase
import com.mystic.grammio.testing.InMemoryTransformationRepository
import com.mystic.grammio.testing.MainDispatcherRule
import com.mystic.grammio.testing.RecordingTextTransformRepository
import com.mystic.grammio.testing.TestTransformations.casual
import com.mystic.grammio.testing.TestTransformations.fixGrammar
import com.mystic.grammio.testing.TestTransformations.shorten
import com.mystic.grammio.testing.TestTransformations.translate
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProcessTextViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = RecordingTextTransformRepository(latencyMs = 100)
    private val transformations = InMemoryTransformationRepository(listOf(fixGrammar, shorten, casual, translate))

    private fun viewModel(
        text: String = "helo wrld",
        canReplace: Boolean = true,
    ) = ProcessTextViewModel(
        input = ProcessTextInput(text, canReplace),
        transformText = TransformTextUseCase(repository),
        transformationRepository = transformations,
        defaultTargetLanguageTag = "en",
    )

    @Test
    fun `initial state reflects the input`() {
        val state = viewModel(text = "hi", canReplace = false).state.value

        assertThat(state.originalText).isEqualTo("hi")
        assertThat(state.canReplace).isFalse()
        assertThat(state.selected).isNull()
        assertThat(state.result).isEqualTo(ResultUiState.Idle)
        assertThat(state.targetLanguageTag).isEqualTo("en")
    }

    @Test
    fun `offers only the enabled transformations, in order, and follows changes`() = runTest {
        transformations.setEnabled(shorten.id, false)
        val vm = viewModel()
        advanceUntilIdle()
        assertThat(vm.state.value.transformations).containsExactly(fixGrammar, casual, translate).inOrder()

        transformations.moveUp(translate.id)
        advanceUntilIdle()

        assertThat(vm.state.value.transformations).containsExactly(fixGrammar, translate, casual).inOrder()
    }

    @Test
    fun `says so when no transformation is enabled`() = runTest {
        listOf(fixGrammar, shorten, casual, translate).forEach { transformations.setEnabled(it.id, false) }
        val vm = viewModel()
        assertThat(vm.state.value.hasNoTransformations).isFalse()

        advanceUntilIdle()

        assertThat(vm.state.value.hasNoTransformations).isTrue()
    }

    @Test
    fun `select goes loading then success`() = runTest {
        val vm = viewModel()
        advanceUntilIdle() // Let the transformations load first.

        vm.state.test {
            assertThat(awaitItem().result).isEqualTo(ResultUiState.Idle)

            vm.onAction(ProcessTextAction.Select(fixGrammar))

            val loading = awaitItem()
            assertThat(loading.result).isEqualTo(ResultUiState.Loading)
            assertThat(loading.selected).isEqualTo(fixGrammar)
            assertThat(awaitItem().result).isEqualTo(ResultUiState.Success("fix_grammar:helo wrld"))
        }
    }

    @Test
    fun `failure is exposed and retry re-runs the selected transformation`() = runTest {
        val vm = viewModel()
        repository.nextResult = Outcome.Failure(TransformError.Network)

        vm.onAction(ProcessTextAction.Select(shorten))
        advanceUntilIdle()
        assertThat(vm.state.value.result).isEqualTo(ResultUiState.Failure(TransformError.Network))

        repository.nextResult = null
        vm.onAction(ProcessTextAction.Retry)
        advanceUntilIdle()

        assertThat(vm.state.value.result).isEqualTo(ResultUiState.Success("shorten:helo wrld"))
        assertThat(repository.calls.map { it.second }).containsExactly(shorten, shorten)
    }

    @Test
    fun `a new selection cancels the in-flight one`() = runTest {
        val vm = viewModel()

        vm.onAction(ProcessTextAction.Select(casual))
        vm.onAction(ProcessTextAction.Select(fixGrammar))
        advanceUntilIdle()

        assertThat(vm.state.value.selected).isEqualTo(fixGrammar)
        assertThat(vm.state.value.result).isEqualTo(ResultUiState.Success("fix_grammar:helo wrld"))
    }

    @Test
    fun `the language picker shows, and a change re-runs, only for transformations that use it`() = runTest {
        val vm = viewModel()

        vm.onAction(ProcessTextAction.Select(fixGrammar))
        vm.onAction(ProcessTextAction.ChangeTargetLanguage("de"))
        advanceUntilIdle()
        assertThat(vm.state.value.showsLanguagePicker).isFalse()
        assertThat(repository.calls).hasSize(1)

        vm.onAction(ProcessTextAction.Select(translate))
        vm.onAction(ProcessTextAction.ChangeTargetLanguage("fr"))
        advanceUntilIdle()

        assertThat(vm.state.value.showsLanguagePicker).isTrue()
        assertThat(vm.state.value.selected).isEqualTo(translate)
        assertThat(vm.state.value.result).isEqualTo(ResultUiState.Success("translate[fr]:helo wrld"))
    }

    @Test
    fun `replace returns the result when the caller accepts it`() = runTest {
        val vm = viewModel(canReplace = true)
        vm.onAction(ProcessTextAction.Select(fixGrammar))
        advanceUntilIdle()

        vm.effects.test {
            vm.onAction(ProcessTextAction.Replace)
            assertThat(awaitItem()).isEqualTo(ProcessTextEffect.ReturnResult("fix_grammar:helo wrld"))
        }
    }

    @Test
    fun `replace does nothing when the selection is read-only`() = runTest {
        val vm = viewModel(canReplace = false)
        vm.onAction(ProcessTextAction.Select(fixGrammar))
        advanceUntilIdle()

        vm.effects.test {
            vm.onAction(ProcessTextAction.Replace)
            vm.onAction(ProcessTextAction.Copy)
            assertThat(awaitItem()).isEqualTo(ProcessTextEffect.CopyToClipboard("fix_grammar:helo wrld"))
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

    @Test
    fun `settings open on the page that fixes the problem`() = runTest {
        val vm = viewModel()

        vm.effects.test {
            vm.onAction(ProcessTextAction.OpenSettings)
            assertThat(awaitItem()).isEqualTo(ProcessTextEffect.OpenSettings)
            vm.onAction(ProcessTextAction.ManageTransformations)
            assertThat(awaitItem()).isEqualTo(ProcessTextEffect.OpenTransformationSettings)
        }
    }
}
