package com.mystic.grammio.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mystic.grammio.presentation.settings.history.HistorySettingsScreen
import com.mystic.grammio.presentation.settings.history.HistorySettingsViewModel
import com.mystic.grammio.presentation.settings.provider.ProviderSettingsScreen
import com.mystic.grammio.presentation.settings.provider.ProviderSettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

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
            entry<SettingsRoute.History> {
                val viewModel: HistorySettingsViewModel = koinViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                HistorySettingsScreen(state = state, onAction = viewModel::onAction, onBack = goBack)
            }
        },
    )
}
