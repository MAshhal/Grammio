package com.mystic.grammio.presentation.settings.transformations.editor

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.domain.usecase.SaveTransformationUseCase
import com.mystic.grammio.testing.InMemoryTransformationRepository
import com.mystic.grammio.testing.MainDispatcherRule
import com.mystic.grammio.testing.TestTransformations.fixGrammar
import com.mystic.grammio.testing.TestTransformations.translate
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TransformationEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = InMemoryTransformationRepository(listOf(fixGrammar, translate))

    private fun viewModel(id: String?) = TransformationEditorViewModel(
        id = id,
        repository = repository,
        saveTransformation = SaveTransformationUseCase(repository),
    )

    @Test
    fun `editing loads the transformation, then saves the changes and closes`() = runTest {
        val vm = viewModel(fixGrammar.id)
        assertThat(vm.state.value.isLoading).isTrue()
        assertThat(vm.state.value.canSave).isFalse()
        advanceUntilIdle()

        with(vm.state.value) {
            assertThat(isNew).isFalse()
            assertThat(isLoading).isFalse()
            assertThat(name).isEqualTo(fixGrammar.name)
            assertThat(taskPrompt).isEqualTo(fixGrammar.taskPrompt)
            assertThat(icon).isEqualTo(fixGrammar.icon)
        }

        vm.effects.test {
            vm.onAction(TransformationEditorAction.NameChanged("Grammar"))
            vm.onAction(TransformationEditorAction.IconSelected(TransformationIcon.Edit))
            vm.onAction(TransformationEditorAction.Save)
            assertThat(awaitItem()).isEqualTo(TransformationEditorEffect.Close)
        }
        assertThat(repository.transformation(fixGrammar.id))
            .isEqualTo(fixGrammar.copy(name = "Grammar", icon = TransformationIcon.Edit))
    }

    @Test
    fun `a new transformation needs a name and a task before it saves`() = runTest {
        val vm = viewModel(id = null)
        assertThat(vm.state.value.isNew).isTrue()
        assertThat(vm.state.value.isLoading).isFalse()

        vm.effects.test {
            vm.onAction(TransformationEditorAction.NameChanged("Tweet"))
            vm.onAction(TransformationEditorAction.Save)
            advanceUntilIdle()
            expectNoEvents()

            vm.onAction(TransformationEditorAction.TaskPromptChanged("Make it a tweet in {language}."))
            assertThat(vm.state.value.usesTargetLanguage).isTrue()
            vm.onAction(TransformationEditorAction.Save)
            assertThat(awaitItem()).isEqualTo(TransformationEditorEffect.Close)
        }
        assertThat(repository.transformations.value.last().name).isEqualTo("Tweet")
    }

    @Test
    fun `delete removes it and closes`() = runTest {
        val vm = viewModel(translate.id)
        advanceUntilIdle()

        vm.effects.test {
            vm.onAction(TransformationEditorAction.Delete)
            assertThat(awaitItem()).isEqualTo(TransformationEditorEffect.Close)
        }
        assertThat(repository.transformations.value).containsExactly(fixGrammar)
    }

    @Test
    fun `a transformation that no longer exists closes the editor`() = runTest {
        val vm = viewModel("gone")

        vm.effects.test {
            assertThat(awaitItem()).isEqualTo(TransformationEditorEffect.Close)
        }
    }
}
