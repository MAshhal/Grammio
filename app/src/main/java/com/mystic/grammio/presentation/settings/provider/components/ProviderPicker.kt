package com.mystic.grammio.presentation.settings.provider.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.presentation.settings.provider.model.descriptionRes
import com.mystic.grammio.presentation.settings.provider.model.label

/** One radio row per provider; the selected one runs transformations. */
@Composable
fun ProviderPicker(
    selected: AiProvider,
    onSelect: (AiProvider) -> Unit,
) {
    Column(Modifier.selectableGroup()) {
        AiProvider.entries.forEach { provider ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .selectable(
                        selected = provider == selected,
                        onClick = { onSelect(provider) },
                        role = Role.RadioButton,
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // The row handles clicks, so the button itself stays passive for accessibility.
                RadioButton(selected = provider == selected, onClick = null)
                Column {
                    Text(provider.label(), style = MaterialTheme.typography.bodyLarge)
                    provider.descriptionRes?.let { description ->
                        Text(
                            stringResource(description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
