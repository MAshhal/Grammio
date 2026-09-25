package com.mystic.grammio.presentation.process.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R

/** Close on the left; Copy and, when the caller accepts edits, Replace as the primary action. */
@Composable
fun ProcessTextActionBar(
    canReplace: Boolean,
    hasResult: Boolean,
    onClose: () -> Unit,
    onCopy: () -> Unit,
    onReplace: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onClose) {
            Text(stringResource(R.string.action_close))
        }
        Spacer(Modifier.weight(1f))
        if (canReplace) {
            OutlinedButton(onClick = onCopy, enabled = hasResult) {
                Text(stringResource(R.string.action_copy))
            }
            Button(onClick = onReplace, enabled = hasResult) {
                Text(stringResource(R.string.action_replace))
            }
        } else {
            Button(onClick = onCopy, enabled = hasResult) {
                Text(stringResource(R.string.action_copy))
            }
        }
    }
}
