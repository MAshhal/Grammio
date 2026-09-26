package com.mystic.grammio.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.presentation.settings.model.label

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
                    .heightIn(min = 48.dp)
                    .selectable(
                        selected = provider == selected,
                        onClick = { onSelect(provider) },
                        role = Role.RadioButton,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // The row handles clicks, so the button itself stays passive for accessibility.
                RadioButton(selected = provider == selected, onClick = null)
                Text(provider.label())
            }
        }
    }
}
