package com.mystic.grammio.presentation.process.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R

/** Copy and, when the caller accepts edits, Replace as the primary action; both need a result. */
@Composable
fun ProcessTextActionBar(
    canReplace: Boolean,
    hasResult: Boolean,
    onCopy: () -> Unit,
    onReplace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (canReplace) {
            OutlinedButton(
                onClick = onCopy,
                enabled = hasResult,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier.weight(1f),
            ) {
                ButtonLabel(Icons.Outlined.ContentCopy, stringResource(R.string.action_copy))
            }
            Button(
                onClick = onReplace,
                enabled = hasResult,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier.weight(1f),
            ) {
                ButtonLabel(Icons.Filled.Done, stringResource(R.string.action_replace))
            }
        } else {
            Button(
                onClick = onCopy,
                enabled = hasResult,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier.weight(1f),
            ) {
                ButtonLabel(Icons.Outlined.ContentCopy, stringResource(R.string.action_copy))
            }
        }
    }
}

@Composable
private fun ButtonLabel(
    icon: ImageVector,
    text: String,
) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
    Text(text)
}
