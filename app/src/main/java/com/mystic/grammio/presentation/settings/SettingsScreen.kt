package com.mystic.grammio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.presentation.theme.GrammioTheme

private const val API_KEY_URL = "https://aistudio.google.com/apikey"

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.settings_how_to_use), style = MaterialTheme.typography.bodyLarge)

            Text(stringResource(R.string.settings_api_key_title), style = MaterialTheme.typography.titleMedium)
            KeyStatus(state.hasApiKey)

            OutlinedTextField(
                value = state.keyInput,
                onValueChange = { onAction(SettingsAction.KeyInputChanged(it)) },
                label = {
                    Text(
                        stringResource(
                            if (state.hasApiKey) R.string.settings_replace_key else R.string.settings_enter_key,
                        ),
                    )
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(SettingsAction.Save) }, enabled = state.canSave) {
                    Text(stringResource(R.string.action_save))
                }
                if (state.hasApiKey) {
                    OutlinedButton(onClick = { onAction(SettingsAction.Clear) }) {
                        Text(stringResource(R.string.action_remove_key))
                    }
                }
            }

            val uriHandler = LocalUriHandler.current
            TextButton(onClick = { uriHandler.openUri(API_KEY_URL) }) {
                Text(stringResource(R.string.settings_get_key))
            }
            Text(
                stringResource(R.string.settings_privacy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun KeyStatus(hasApiKey: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hasApiKey) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.settings_key_saved))
        } else {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(stringResource(R.string.settings_key_missing))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    GrammioTheme {
        SettingsScreen(state = SettingsUiState(hasApiKey = true), onAction = {})
    }
}
