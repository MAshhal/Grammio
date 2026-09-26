package com.mystic.grammio.presentation.process.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.presentation.common.message
import com.mystic.grammio.presentation.process.model.ErrorRecovery
import com.mystic.grammio.presentation.process.model.recovery

/** The error message plus the one recovery action that makes sense for it, if any. */
@Composable
fun ErrorContent(
    error: TransformError,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(error.message(), color = MaterialTheme.colorScheme.error)
        when (error.recovery) {
            ErrorRecovery.Retry -> OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.action_retry))
            }

            ErrorRecovery.OpenSettings -> OutlinedButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.action_open_settings))
            }

            ErrorRecovery.None -> Unit
        }
    }
}
