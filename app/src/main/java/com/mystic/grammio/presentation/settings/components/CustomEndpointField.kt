package com.mystic.grammio.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R

/** Base URL of an OpenAI-compatible server, such as `https://openrouter.ai/api/v1`. */
@Composable
fun CustomEndpointField(
    value: String,
    isInvalid: Boolean,
    canSave: Boolean,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.settings_base_url_label)) },
            placeholder = { Text(stringResource(R.string.settings_base_url_hint)) },
            leadingIcon = { Icon(Icons.Outlined.Link, contentDescription = null) },
            supportingText = if (isInvalid) {
                { Text(stringResource(R.string.settings_base_url_invalid)) }
            } else {
                null
            },
            isError = isInvalid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { if (canSave) onSave() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onSave, enabled = canSave, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.action_save))
        }
    }
}
