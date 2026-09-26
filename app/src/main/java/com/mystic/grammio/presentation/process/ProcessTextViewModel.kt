package com.mystic.grammio.presentation.process

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.repository.TransformationRepository
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.domain.usecase.TransformTextUseCase
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProcessTextViewModel(
    input: ProcessTextInput,
    private val transformText: TransformTextUseCase,
    transformationRepository: TransformationRepository,
    defaultTargetLanguageTag: String = Locale.getDefault().language,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ProcessTextUiState(
            originalText = input.text,
            canReplace = input.canReplace,
            targetLanguageTag = defaultTargetLanguageTag,
        ),
    )
    val state: StateFlow<ProcessTextUiState> = _state.asStateFlow()

    private val _effects = Channel<ProcessTextEffect>(Channel.BUFFERED)
    val effects: Flow<ProcessTextEffect> = _effects.receiveAsFlow()

    private var transformJob: Job? = null

    init {
        // Listing has no rules to hold, so this reads the repository directly rather than via a use case.
        transformationRepository.transformations
            .onEach { all -> _state.update { it.copy(transformations = all.filter(Transformation::isEnabled)) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ProcessTextAction) {
        when (action) {
            is ProcessTextAction.Select -> run(action.transformation)

            is ProcessTextAction.ChangeTargetLanguage -> changeTargetLanguage(action.languageTag)

            ProcessTextAction.Retry -> _state.value.selected?.let(::run)

            ProcessTextAction.Copy -> _state.value.resultText?.let { emit(ProcessTextEffect.CopyToClipboard(it)) }

            ProcessTextAction.Replace -> {
                val current = _state.value
                val text = current.resultText
                if (current.canReplace && text != null) emit(ProcessTextEffect.ReturnResult(text))
            }

            ProcessTextAction.Dismiss -> emit(ProcessTextEffect.Close)

            ProcessTextAction.OpenSettings -> emit(ProcessTextEffect.OpenSettings)
        }
    }

    private fun changeTargetLanguage(languageTag: String) {
        _state.update { it.copy(targetLanguageTag = languageTag) }
        // Re-run only if the user is looking at a result that depends on the language.
        _state.value.selected?.takeIf { it.usesTargetLanguage }?.let(::run)
    }

    /** Starts a transformation, cancelling any in-flight one so only the latest choice wins. */
    private fun run(transformation: Transformation) {
        transformJob?.cancel()
        _state.update { it.copy(selected = transformation, result = ResultUiState.Loading) }
        transformJob = viewModelScope.launch {
            val current = _state.value
            val result = when (
                val outcome = transformText(
                    current.originalText,
                    transformation,
                    current.targetLanguageTag,
                )
            ) {
                is Outcome.Success -> ResultUiState.Success(outcome.value.text)
                is Outcome.Failure -> ResultUiState.Failure(outcome.error)
            }
            _state.update { it.copy(result = result) }
        }
    }

    private fun emit(effect: ProcessTextEffect) {
        _effects.trySend(effect)
    }
}
