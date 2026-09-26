package com.mystic.grammio.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mystic.grammio.presentation.settings.history.HistorySettingsScreen
import com.mystic.grammio.presentation.settings.history.HistorySettingsViewModel
import com.mystic.grammio.presentation.settings.prompt.SystemPromptScreen
import com.mystic.grammio.presentation.settings.prompt.SystemPromptViewModel
import com.mystic.grammio.presentation.settings.provider.ProviderSettingsScreen
import com.mystic.grammio.presentation.settings.provider.ProviderSettingsViewModel
import com.mystic.grammio.presentation.settings.transformations.TransformationsScreen
import com.mystic.grammio.presentation.settings.transformations.TransformationsViewModel
import com.mystic.grammio.presentation.settings.transformations.editor.TransformationEditorScreen
import com.mystic.grammio.presentation.settings.transformations.editor.TransformationEditorViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Settings as one back stack of pages. Each entry gets its own ViewModel store, so a page's
 * ViewModel lives exactly as long as the page is on the stack.
 */
@Composable
fun SettingsNavigation(initialBackStack: List<SettingsRoute>) {
    // Only the first composition uses the initial stack; after that the saved stack wins.
    val backStack = rememberNavBackStack(*initialBackStack.toTypedArray())
    val goBack: () -> Unit = { backStack.removeLastOrNull() }

    NavDisplay(
        backStack = backStack,
        onBack = goBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SettingsRoute.Home> {
                SettingsHomeScreen(onNavigate = { backStack.add(it) })
            }
            entry<SettingsRoute.Provider> {
                val viewModel: ProviderSettingsViewModel = koinViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                ProviderSettingsScreen(state = state, onAction = viewModel::onAction, onBack = goBack)
            }
            entry<SettingsRoute.Transformations> {
                val viewModel: TransformationsViewModel = koinViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                TransformationsScreen(
                    state = state,
                    onAction = viewModel::onAction,
                    onAdd = { backStack.add(SettingsRoute.TransformationEditor(id = null)) },
                    onEdit = { backStack.add(SettingsRoute.TransformationEditor(it)) },
                    onBack = goBack,
                )
            }
            entry<SettingsRoute.TransformationEditor> { route ->
                val viewModel: TransformationEditorViewModel = koinViewModel { parametersOf(route.id) }
                val state by viewModel.state.collectAsStateWithLifecycle()
                // Close is the only effect. Remove this entry, not whatever happens to be on top.
                LaunchedEffect(viewModel) { viewModel.effects.collect { backStack.remove(route) } }
                TransformationEditorScreen(state = state, onAction = viewModel::onAction, onBack = goBack)
            }
            entry<SettingsRoute.SystemPrompt> {
                val viewModel: SystemPromptViewModel = koinViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                SystemPromptScreen(state = state, onAction = viewModel::onAction, onBack = goBack)
            }
            entry<SettingsRoute.History> {
                val viewModel: HistorySettingsViewModel = koinViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                HistorySettingsScreen(state = state, onAction = viewModel::onAction, onBack = goBack)
            }
        },
    )
}
