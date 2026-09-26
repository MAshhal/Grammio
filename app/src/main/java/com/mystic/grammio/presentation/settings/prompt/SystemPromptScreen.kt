package com.mystic.grammio.presentation.settings.prompt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.presentation.settings.components.SettingsScaffold
import com.mystic.grammio.presentation.settings.components.SettingsSection
import com.mystic.grammio.presentation.theme.GrammioTheme

@Composable
fun SystemPromptScreen(
    state: SystemPromptUiState,
    onAction: (SystemPromptAction) -> Unit,
    onBack: () -> Unit,
) {
    SettingsScaffold(title = stringResource(R.string.settings_system_prompt_page_title), onBack = onBack) {
        SettingsSection(
            if (state.isDefault) R.string.settings_system_prompt_default else R.string.settings_system_prompt_custom,
        ) {
            Text(
                stringResource(R.string.settings_system_prompt_explanation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = state.input,
                onValueChange = { onAction(SystemPromptAction.InputChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 8,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { onAction(SystemPromptAction.ResetToDefault) }, enabled = state.canReset) {
                    Text(stringResource(R.string.action_reset_to_default))
                }
                Button(onClick = { onAction(SystemPromptAction.Save) }, enabled = state.canSave) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SystemPromptScreenPreview() {
    val prompt = "You are a text transformation engine.\n\nRules:\n- Output only the transformed text."
    GrammioTheme {
        SystemPromptScreen(
            state = SystemPromptUiState(input = prompt, savedPrompt = prompt, defaultPrompt = prompt),
            onAction = {},
            onBack = {},
        )
    }
}
