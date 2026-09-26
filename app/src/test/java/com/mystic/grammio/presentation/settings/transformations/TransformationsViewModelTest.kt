package com.mystic.grammio.presentation.settings.transformations

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.testing.InMemoryTransformationRepository
import com.mystic.grammio.testing.MainDispatcherRule
import com.mystic.grammio.testing.TestTransformations.casual
import com.mystic.grammio.testing.TestTransformations.fixGrammar
import com.mystic.grammio.testing.TestTransformations.shorten
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TransformationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = InMemoryTransformationRepository(
        initial = listOf(fixGrammar, shorten),
        defaults = listOf(fixGrammar, shorten, casual),
    )

    // Lazy: must be created after MainDispatcherRule has installed the test Main dispatcher.
    private val viewModel by lazy { TransformationsViewModel(repository) }

    private fun TestScope.collectState() {
        val state = viewModel.state
        backgroundScope.launch { state.collect {} }
    }

    @Test
    fun `lists every transformation, including disabled ones`() = runTest {
        collectState()
        assertThat(viewModel.state.value.transformations).isNull()

        viewModel.onAction(TransformationsAction.EnabledChanged(shorten.id, false))
        advanceUntilIdle()

        assertThat(viewModel.state.value.transformations)
            .containsExactly(fixGrammar, shorten.copy(isEnabled = false))
            .inOrder()
    }

    @Test
    fun `moves, and restores the defaults`() = runTest {
        collectState()

        viewModel.onAction(TransformationsAction.MoveDown(fixGrammar.id))
        advanceUntilIdle()
        assertThat(viewModel.state.value.transformations).containsExactly(shorten, fixGrammar).inOrder()

        viewModel.onAction(TransformationsAction.MoveUp(fixGrammar.id))
        viewModel.onAction(TransformationsAction.RestoreDefaults)
        advanceUntilIdle()
        assertThat(viewModel.state.value.transformations).containsExactly(fixGrammar, shorten, casual).inOrder()
    }

    @Test
    fun `an empty list says so`() = runTest {
        collectState()
        repository.delete(fixGrammar.id)
        repository.delete(shorten.id)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isEmpty).isTrue()
    }
}
