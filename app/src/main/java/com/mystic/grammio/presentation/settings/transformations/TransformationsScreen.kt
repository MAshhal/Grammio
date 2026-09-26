package com.mystic.grammio.presentation.settings.transformations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.presentation.settings.components.SettingsScaffold
import com.mystic.grammio.presentation.settings.transformations.components.TransformationRow
import com.mystic.grammio.presentation.theme.GrammioTheme

@Composable
fun TransformationsScreen(
    state: TransformationsUiState,
    onAction: (TransformationsAction) -> Unit,
    onAdd: () -> Unit,
    onEdit: (id: String) -> Unit,
    onBack: () -> Unit,
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var confirmRestore by rememberSaveable { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.settings_transformations_page_title),
        onBack = onBack,
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Outlined.MoreVert, stringResource(R.string.action_more_options))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_restore_defaults)) },
                    leadingIcon = { Icon(Icons.Outlined.SettingsBackupRestore, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        confirmRestore = true
                    },
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.action_add_transformation)) },
            )
        },
    ) {
        Text(
            stringResource(
                if (state.isEmpty) R.string.settings_transformations_empty else R.string.settings_transformations_hint,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        val transformations = state.transformations.orEmpty()
        if (transformations.isNotEmpty()) {
            TransformationList(transformations, onAction, onEdit)
        }

        // Keeps the last row clear of the floating button.
        Spacer(Modifier.height(72.dp))
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text(stringResource(R.string.settings_restore_defaults_title)) },
            text = { Text(stringResource(R.string.settings_restore_defaults_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRestore = false
                        onAction(TransformationsAction.RestoreDefaults)
                    },
                ) { Text(stringResource(R.string.action_restore)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun TransformationList(
    transformations: List<Transformation>,
    onAction: (TransformationsAction) -> Unit,
    onEdit: (id: String) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            transformations.forEachIndexed { index, transformation ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                TransformationRow(
                    transformation = transformation,
                    canMoveUp = index > 0,
                    canMoveDown = index < transformations.lastIndex,
                    onClick = { onEdit(transformation.id) },
                    onMoveUp = { onAction(TransformationsAction.MoveUp(transformation.id)) },
                    onMoveDown = { onAction(TransformationsAction.MoveDown(transformation.id)) },
                    onEnabledChange = { onAction(TransformationsAction.EnabledChanged(transformation.id, it)) },
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun TransformationsScreenPreview() {
    GrammioTheme {
        TransformationsScreen(
            state = TransformationsUiState(
                listOf(
                    Transformation("a", "Fix grammar", "Correct spelling and grammar.", TransformationIcon.Spellcheck),
                    Transformation(
                        "b",
                        "Summarize",
                        "Summarize the text.",
                        TransformationIcon.Summarize,
                        isEnabled = false,
                    ),
                    Transformation(
                        "c",
                        "Translate",
                        "Translate the text into {language}.",
                        TransformationIcon.Translate,
                    ),
                ),
            ),
            onAction = {},
            onAdd = {},
            onEdit = {},
            onBack = {},
        )
    }
}
