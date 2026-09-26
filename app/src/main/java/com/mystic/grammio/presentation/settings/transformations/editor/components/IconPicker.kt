package com.mystic.grammio.presentation.settings.transformations.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.presentation.common.imageVector
import com.mystic.grammio.presentation.common.labelRes

/** Every icon a transformation can have; the selected one is filled. */
@Composable
fun IconPicker(
    selected: TransformationIcon,
    onSelect: (TransformationIcon) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TransformationIcon.entries.forEach { icon ->
            FilledTonalIconToggleButton(checked = icon == selected, onCheckedChange = { onSelect(icon) }) {
                Icon(icon.imageVector, contentDescription = stringResource(icon.labelRes))
            }
        }
    }
}
