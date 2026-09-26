package com.mystic.grammio.di

import com.mystic.grammio.presentation.process.ProcessTextViewModel
import com.mystic.grammio.presentation.settings.history.HistorySettingsViewModel
import com.mystic.grammio.presentation.settings.prompt.SystemPromptViewModel
import com.mystic.grammio.presentation.settings.provider.ProviderSettingsViewModel
import com.mystic.grammio.presentation.settings.transformations.TransformationsViewModel
import com.mystic.grammio.presentation.settings.transformations.editor.TransformationEditorViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val presentationModule = module {
    // Classic DSL: this ViewModel takes a runtime parameter (the parsed Intent) via parametersOf.
    viewModel { params ->
        ProcessTextViewModel(input = params.get(), transformText = get(), transformationRepository = get())
    }
    viewModel<ProviderSettingsViewModel>()
    viewModel<TransformationsViewModel>()
    // Classic DSL: the id to edit (null for a new transformation) arrives via parametersOf.
    viewModel { params ->
        TransformationEditorViewModel(id = params.getOrNull(), repository = get(), saveTransformation = get())
    }
    viewModel<SystemPromptViewModel>()
    viewModel<HistorySettingsViewModel>()
}
