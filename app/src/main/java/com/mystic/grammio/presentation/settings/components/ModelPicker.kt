package com.mystic.grammio.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.presentation.common.message
import com.mystic.grammio.presentation.settings.ModelListUiState

/**
 * The model to use: the provider's default (when it has one) or any model its API listed.
 * A saved choice stays selectable even before the list has loaded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelPicker(
    selectedModelId: String?,
    defaultModelId: String?,
    models: ModelListUiState,
    onSelect: (String?) -> Unit,
    onRefresh: () -> Unit,
) {
    val listed = (models as? ModelListUiState.Loaded)?.models.orEmpty()
    val selectedLabel = when {
        selectedModelId != null -> listed.firstOrNull { it.id == selectedModelId }?.displayName ?: selectedModelId
        defaultModelId != null -> stringResource(R.string.settings_model_default, defaultModelId)
        else -> stringResource(R.string.settings_model_none)
    }
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text(stringResource(R.string.settings_model_title)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (defaultModelId != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings_model_default, defaultModelId)) },
                        onClick = {
                            onSelect(null)
                            expanded = false
                        },
                    )
                }
                listed.forEach { model ->
                    DropdownMenuItem(
                        text = { ModelItem(model) },
                        onClick = {
                            onSelect(model.id)
                            expanded = false
                        },
                    )
                }
            }
        }
        ModelListStatus(models, onRefresh)
    }
}

@Composable
private fun ModelItem(model: AiModel) {
    Column {
        Text(model.displayName)
        if (model.displayName != model.id) {
            Text(
                model.id,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ModelListStatus(
    models: ModelListUiState,
    onRefresh: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (models) {
            ModelListUiState.Idle -> Text(
                stringResource(R.string.settings_models_need_setup),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ModelListUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(stringResource(R.string.settings_models_loading), style = MaterialTheme.typography.bodySmall)
            }

            is ModelListUiState.Loaded -> {
                Text(
                    pluralStringResource(R.plurals.settings_models_count, models.models.size, models.models.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRefresh) { Text(stringResource(R.string.action_refresh)) }
            }

            is ModelListUiState.Failed -> {
                Text(
                    stringResource(R.string.settings_models_failed, models.error.message()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onRefresh) { Text(stringResource(R.string.action_retry)) }
            }
        }
    }
}
