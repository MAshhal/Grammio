package com.mystic.grammio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.presentation.settings.components.SettingsNavigationRow
import com.mystic.grammio.presentation.settings.components.SettingsScaffold
import com.mystic.grammio.presentation.theme.GrammioTheme

/**
 * The first Settings page: how to use Grammio, a row for each settings page, and one that opens the
 * system's Accessibility settings to turn the keyboard button on or off.
 */
@Composable
fun SettingsHomeScreen(
    keyboardButtonEnabled: Boolean,
    onNavigate: (SettingsRoute) -> Unit,
    onOpenKeyboardButtonSettings: () -> Unit,
) {
    SettingsScaffold(title = stringResource(R.string.app_name), onBack = null) {
        HowToUseCard()

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsNavigationRow(
                    icon = Icons.Outlined.Key,
                    title = stringResource(R.string.settings_provider_page_title),
                    description = stringResource(R.string.settings_provider_page_description),
                    onClick = { onNavigate(SettingsRoute.Provider) },
                    modifier = Modifier.morphsInto(SettingsRoute.Provider),
                )
                RowDivider()
                SettingsNavigationRow(
                    icon = Icons.Outlined.AutoAwesome,
                    title = stringResource(R.string.settings_transformations_page_title),
                    description = stringResource(R.string.settings_transformations_page_description),
                    onClick = { onNavigate(SettingsRoute.Transformations) },
                    modifier = Modifier.morphsInto(SettingsRoute.Transformations),
                )
                RowDivider()
                SettingsNavigationRow(
                    icon = Icons.Outlined.SmartToy,
                    title = stringResource(R.string.settings_system_prompt_page_title),
                    description = stringResource(R.string.settings_system_prompt_page_description),
                    onClick = { onNavigate(SettingsRoute.SystemPrompt) },
                    modifier = Modifier.morphsInto(SettingsRoute.SystemPrompt),
                )
                RowDivider()
                SettingsNavigationRow(
                    icon = Icons.Outlined.History,
                    title = stringResource(R.string.settings_history_page_title),
                    description = stringResource(R.string.settings_history_page_description),
                    onClick = { onNavigate(SettingsRoute.History) },
                    modifier = Modifier.morphsInto(SettingsRoute.History),
                )
                RowDivider()
                SettingsNavigationRow(
                    icon = Icons.Outlined.Keyboard,
                    title = stringResource(R.string.settings_keyboard_button_title),
                    description = stringResource(
                        if (keyboardButtonEnabled) {
                            R.string.settings_keyboard_button_on
                        } else {
                            R.string.settings_keyboard_button_off
                        },
                    ),
                    onClick = onOpenKeyboardButtonSettings,
                )
            }
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun HowToUseCard() {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(Icons.Outlined.TouchApp, contentDescription = null)
            Text(stringResource(R.string.settings_how_to_use), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsHomeScreenPreview() {
    GrammioTheme {
        SettingsHomeScreen(keyboardButtonEnabled = false, onNavigate = {}, onOpenKeyboardButtonSettings = {})
    }
}
