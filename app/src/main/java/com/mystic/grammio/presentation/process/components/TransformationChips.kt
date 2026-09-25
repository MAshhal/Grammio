package com.mystic.grammio.presentation.process.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.presentation.process.model.labelRes

@Composable
fun TransformationChips(
    transformations: List<Transformation>,
    selected: Transformation?,
    onSelect: (Transformation) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        transformations.forEach { transformation ->
            FilterChip(
                selected = transformation == selected,
                onClick = { onSelect(transformation) },
                label = { Text(stringResource(transformation.labelRes())) },
            )
        }
    }
}
