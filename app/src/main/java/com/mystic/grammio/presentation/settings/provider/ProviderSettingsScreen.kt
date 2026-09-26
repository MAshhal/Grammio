package com.mystic.grammio.presentation.settings.provider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.presentation.settings.components.SettingsScaffold
import com.mystic.grammio.presentation.settings.components.SettingsSection
import com.mystic.grammio.presentation.settings.provider.components.ApiKeyField
import com.mystic.grammio.presentation.settings.provider.components.ApiKeyStatus
import com.mystic.grammio.presentation.settings.provider.components.CustomEndpointField
import com.mystic.grammio.presentation.settings.provider.components.ModelPicker
import com.mystic.grammio.presentation.settings.provider.components.ProviderPicker
import com.mystic.grammio.presentation.settings.provider.model.keyLink
import com.mystic.grammio.presentation.theme.GrammioTheme

@Composable
fun ProviderSettingsScreen(
    state: ProviderSettingsUiState,
    onAction: (ProviderSettingsAction) -> Unit,
    onBack: () -> Unit,
) {
    SettingsScaffold(title = stringResource(R.string.settings_provider_page_title), onBack = onBack) {
        SettingsSection(R.string.settings_provider_title, contentPadding = PaddingValues(vertical = 8.dp)) {
            ProviderPicker(selected = state.provider, onSelect = {
                onAction(ProviderSettingsAction.ProviderSelected(it))
            })
        }

        AnimatedVisibility(visible = state.needsBaseUrl) {
            SettingsSection(R.string.settings_base_url_title) {
                CustomEndpointField(
                    value = state.baseUrlInput,
                    isInvalid = state.isBaseUrlInvalid,
                    canSave = state.canSaveBaseUrl,
                    onValueChange = { onAction(ProviderSettingsAction.BaseUrlInputChanged(it)) },
                    onSave = { onAction(ProviderSettingsAction.SaveBaseUrl) },
                )
            }
        }

        SettingsSection(R.string.settings_api_key_title) {
            ApiKeySection(state, onAction)
        }

        SettingsSection(R.string.settings_model_title) {
            ModelPicker(
                selectedModelId = state.selectedModelId,
                defaultModelId = state.defaultModelId,
                models = state.models,
                onSelect = { onAction(ProviderSettingsAction.ModelSelected(it)) },
                onRefresh = { onAction(ProviderSettingsAction.RefreshModels) },
            )
        }
    }
}

@Composable
private fun ApiKeySection(
    state: ProviderSettingsUiState,
    onAction: (ProviderSettingsAction) -> Unit,
) {
    ApiKeyStatus(state.hasApiKey)
    ApiKeyField(
        value = state.keyInput,
        hasApiKey = state.hasApiKey,
        canSave = state.canSave,
        onValueChange = { onAction(ProviderSettingsAction.KeyInputChanged(it)) },
        onSave = { onAction(ProviderSettingsAction.Save) },
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.hasApiKey) {
            TextButton(
                onClick = { onAction(ProviderSettingsAction.Clear) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
            ) {
                ButtonIcon(Icons.Outlined.Delete)
                Text(stringResource(R.string.action_remove_key))
            }
        }
        Button(onClick = { onAction(ProviderSettingsAction.Save) }, enabled = state.canSave) {
            Text(stringResource(R.string.action_save))
        }
    }
    state.provider.keyLink?.let { link ->
        val uriHandler = LocalUriHandler.current
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        TextButton(
            onClick = { uriHandler.openUri(link.url) },
            contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
        ) {
            Text(stringResource(link.label))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Icon(
                Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
        }
    }
}

@Composable
private fun ButtonIcon(icon: ImageVector) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun SettingsScreenPreview() {
    GrammioTheme {
        ProviderSettingsScreen(
            state = ProviderSettingsUiState(
                provider = AiProvider.Anthropic,
                hasApiKey = true,
                defaultModelId = "claude-haiku-4-5",
                models = ModelListUiState.Loaded(listOf(AiModel("claude-opus-5", "Claude Opus 5"))),
            ),
            onAction = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun CustomEndpointPreview() {
    GrammioTheme {
        ProviderSettingsScreen(
            state = ProviderSettingsUiState(provider = AiProvider.OpenAiCompatible, baseUrlInput = "http://example"),
            onAction = {},
            onBack = {},
        )
    }
}
