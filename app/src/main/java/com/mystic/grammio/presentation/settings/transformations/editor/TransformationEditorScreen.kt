package com.mystic.grammio.presentation.settings.transformations.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.presentation.settings.components.SettingsScaffold
import com.mystic.grammio.presentation.settings.components.SettingsSection
import com.mystic.grammio.presentation.settings.transformations.editor.components.IconPicker
import com.mystic.grammio.presentation.theme.GrammioTheme

@Composable
fun TransformationEditorScreen(
    state: TransformationEditorUiState,
    onAction: (TransformationEditorAction) -> Unit,
    onBack: () -> Unit,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    val title = when {
        state.isNew -> R.string.settings_transformation_new_title
        else -> R.string.settings_transformation_edit_title
    }
    SettingsScaffold(
        title = stringResource(title),
        onBack = onBack,
        actions = {
            if (!state.isNew) {
                IconButton(onClick = { confirmDelete = true }, enabled = !state.isLoading) {
                    Icon(Icons.Outlined.Delete, stringResource(R.string.action_delete))
                }
            }
        },
    ) {
        SettingsSection(R.string.settings_transformation_name) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(TransformationEditorAction.NameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
        }

        SettingsSection(R.string.settings_transformation_task) {
            Text(
                stringResource(R.string.settings_transformation_task_explanation, Transformation.LANGUAGE_PLACEHOLDER),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = state.taskPrompt,
                onValueChange = { onAction(TransformationEditorAction.TaskPromptChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                placeholder = { Text(stringResource(R.string.settings_transformation_task_placeholder)) },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            AnimatedVisibility(visible = state.usesTargetLanguage) {
                UsesTargetLanguageNote()
            }
        }

        SettingsSection(R.string.settings_transformation_icon) {
            IconPicker(selected = state.icon, onSelect = { onAction(TransformationEditorAction.IconSelected(it)) })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { onAction(TransformationEditorAction.Save) }, enabled = state.canSave) {
                Text(stringResource(R.string.action_save))
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.settings_delete_transformation_title, state.name)) },
            text = { Text(stringResource(R.string.settings_delete_transformation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onAction(TransformationEditorAction.Delete)
                    },
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun UsesTargetLanguageNote() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Translate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            stringResource(R.string.settings_transformation_uses_language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TransformationEditorScreenPreview() {
    GrammioTheme {
        TransformationEditorScreen(
            state = TransformationEditorUiState(
                isNew = false,
                name = "Translate",
                taskPrompt = "Translate the text into {language}, preserving meaning and tone.",
                icon = TransformationIcon.Translate,
            ),
            onAction = {},
            onBack = {},
        )
    }
}
