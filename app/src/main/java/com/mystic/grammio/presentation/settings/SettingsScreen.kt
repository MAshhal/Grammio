package com.mystic.grammio.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.presentation.settings.components.ApiKeyStatus
import com.mystic.grammio.presentation.settings.components.CustomEndpointField
import com.mystic.grammio.presentation.settings.components.ModelPicker
import com.mystic.grammio.presentation.settings.components.ProviderPicker
import com.mystic.grammio.presentation.settings.model.keyLink
import com.mystic.grammio.presentation.theme.GrammioTheme

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

            SectionTitle(R.string.settings_provider_title)
            ProviderPicker(selected = state.provider, onSelect = { onAction(SettingsAction.ProviderSelected(it)) })

            if (state.needsBaseUrl) {
                SectionTitle(R.string.settings_base_url_title)
                CustomEndpointField(
                    value = state.baseUrlInput,
                    isInvalid = state.isBaseUrlInvalid,
                    canSave = state.canSaveBaseUrl,
                    onValueChange = { onAction(SettingsAction.BaseUrlInputChanged(it)) },
                    onSave = { onAction(SettingsAction.SaveBaseUrl) },
                )
            }

            SectionTitle(R.string.settings_api_key_title)
            ApiKeySection(state, onAction)

            SectionTitle(R.string.settings_model_title)
            ModelPicker(
                selectedModelId = state.selectedModelId,
                defaultModelId = state.defaultModelId,
                models = state.models,
                onSelect = { onAction(SettingsAction.ModelSelected(it)) },
                onRefresh = { onAction(SettingsAction.RefreshModels) },
            )

            Text(
                stringResource(R.string.settings_privacy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int) {
    Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ApiKeySection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ApiKeyStatus(state.hasApiKey)
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
        state.provider.keyLink?.let { link ->
            val uriHandler = LocalUriHandler.current
            TextButton(onClick = { uriHandler.openUri(link.url) }) {
                Text(stringResource(link.label))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    GrammioTheme {
        SettingsScreen(
            state = SettingsUiState(
                provider = AiProvider.Anthropic,
                hasApiKey = true,
                defaultModelId = "claude-haiku-4-5",
                models = ModelListUiState.Loaded(listOf(AiModel("claude-opus-5", "Claude Opus 5"))),
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomEndpointPreview() {
    GrammioTheme {
        SettingsScreen(
            state = SettingsUiState(provider = AiProvider.OpenAiCompatible, baseUrlInput = "http://example"),
            onAction = {},
        )
    }
}
