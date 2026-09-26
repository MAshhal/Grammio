package com.mystic.grammio.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.presentation.settings.components.ApiKeyField
import com.mystic.grammio.presentation.settings.components.ApiKeyStatus
import com.mystic.grammio.presentation.settings.components.CustomEndpointField
import com.mystic.grammio.presentation.settings.components.HistoryToggle
import com.mystic.grammio.presentation.settings.components.ModelPicker
import com.mystic.grammio.presentation.settings.components.ProviderPicker
import com.mystic.grammio.presentation.settings.components.SettingsSection
import com.mystic.grammio.presentation.settings.model.keyLink
import com.mystic.grammio.presentation.theme.GrammioTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = { Text(stringResource(R.string.app_name)) }, scrollBehavior = scrollBehavior)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            HowToUseCard()

            SettingsSection(R.string.settings_provider_title, contentPadding = PaddingValues(vertical = 8.dp)) {
                ProviderPicker(selected = state.provider, onSelect = { onAction(SettingsAction.ProviderSelected(it)) })
            }

            AnimatedVisibility(visible = state.needsBaseUrl) {
                SettingsSection(R.string.settings_base_url_title) {
                    CustomEndpointField(
                        value = state.baseUrlInput,
                        isInvalid = state.isBaseUrlInvalid,
                        canSave = state.canSaveBaseUrl,
                        onValueChange = { onAction(SettingsAction.BaseUrlInputChanged(it)) },
                        onSave = { onAction(SettingsAction.SaveBaseUrl) },
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
                    onSelect = { onAction(SettingsAction.ModelSelected(it)) },
                    onRefresh = { onAction(SettingsAction.RefreshModels) },
                )
            }

            SettingsSection(R.string.settings_history_title) {
                HistoryToggle(
                    enabled = state.isHistoryEnabled,
                    onToggle = { onAction(SettingsAction.HistoryToggled(it)) },
                )
            }

            PrivacyNote()
        }
    }
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

@Composable
private fun ApiKeySection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    ApiKeyStatus(state.hasApiKey)
    ApiKeyField(
        value = state.keyInput,
        hasApiKey = state.hasApiKey,
        canSave = state.canSave,
        onValueChange = { onAction(SettingsAction.KeyInputChanged(it)) },
        onSave = { onAction(SettingsAction.Save) },
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.hasApiKey) {
            TextButton(
                onClick = { onAction(SettingsAction.Clear) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
            ) {
                ButtonIcon(Icons.Outlined.Delete)
                Text(stringResource(R.string.action_remove_key))
            }
        }
        Button(onClick = { onAction(SettingsAction.Save) }, enabled = state.canSave) {
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

@Composable
private fun PrivacyNote() {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Outlined.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            stringResource(R.string.settings_privacy_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, heightDp = 1250)
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

@Preview(showBackground = true, heightDp = 1350)
@Composable
private fun CustomEndpointPreview() {
    GrammioTheme {
        SettingsScreen(
            state = SettingsUiState(provider = AiProvider.OpenAiCompatible, baseUrlInput = "http://example"),
            onAction = {},
        )
    }
}
