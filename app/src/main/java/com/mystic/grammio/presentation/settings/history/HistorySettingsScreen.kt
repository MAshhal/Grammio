package com.mystic.grammio.presentation.settings.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.presentation.settings.components.SettingsScaffold
import com.mystic.grammio.presentation.settings.components.SettingsSection
import com.mystic.grammio.presentation.settings.history.components.HistoryToggle
import com.mystic.grammio.presentation.theme.GrammioTheme

@Composable
fun HistorySettingsScreen(
    state: HistorySettingsUiState,
    onAction: (HistorySettingsAction) -> Unit,
    onBack: () -> Unit,
) {
    SettingsScaffold(title = stringResource(R.string.settings_history_page_title), onBack = onBack) {
        SettingsSection(R.string.settings_history_title) {
            HistoryToggle(
                enabled = state.isHistoryEnabled,
                onToggle = { onAction(HistorySettingsAction.Toggled(it)) },
            )
        }

        PrivacyNote()
    }
}

@Composable
private fun PrivacyNote() {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Outlined.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            stringResource(R.string.settings_privacy_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistorySettingsScreenPreview() {
    GrammioTheme {
        HistorySettingsScreen(state = HistorySettingsUiState(isHistoryEnabled = true), onAction = {}, onBack = {})
    }
}
