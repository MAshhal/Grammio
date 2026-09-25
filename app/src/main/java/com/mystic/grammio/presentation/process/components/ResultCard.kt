package com.mystic.grammio.presentation.process.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.presentation.process.ResultUiState

@Composable
fun ResultCard(
    result: ResultUiState,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Box(Modifier.fillMaxWidth().padding(16.dp).animateContentSize()) {
            when (result) {
                ResultUiState.Idle -> Text(
                    stringResource(R.string.process_pick_transformation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                ResultUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.size(32.dp).align(Alignment.Center),
                )

                is ResultUiState.Success -> SelectionContainer {
                    Text(result.text, style = MaterialTheme.typography.bodyLarge)
                }

                is ResultUiState.Failure -> ErrorContent(result.error, onRetry, onOpenSettings)
            }
        }
    }
}
