package com.mystic.grammio.presentation.settings.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.HistoryEntry
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.presentation.common.copyToClipboard
import com.mystic.grammio.presentation.settings.components.SettingsScaffold
import com.mystic.grammio.presentation.settings.components.SettingsSection
import com.mystic.grammio.presentation.settings.history.components.HistoryEntryRow
import com.mystic.grammio.presentation.settings.history.components.HistoryToggle
import com.mystic.grammio.presentation.theme.GrammioTheme
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun HistorySettingsScreen(
    state: HistorySettingsUiState,
    onAction: (HistorySettingsAction) -> Unit,
    onBack: () -> Unit,
) {
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.settings_history_page_title),
        onBack = onBack,
        actions = {
            if (!state.entries.isNullOrEmpty()) {
                IconButton(onClick = { confirmClear = true }) {
                    Icon(Icons.Outlined.DeleteSweep, stringResource(R.string.action_clear_history))
                }
            }
        },
    ) {
        SettingsSection(R.string.settings_history_title) {
            HistoryToggle(
                enabled = state.isHistoryEnabled,
                onToggle = { onAction(HistorySettingsAction.Toggled(it)) },
            )
        }

        PrivacyNote()

        state.entries?.let { RecentHistory(it, isHistoryEnabled = state.isHistoryEnabled) }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.settings_clear_history_title)) },
            text = { Text(stringResource(R.string.settings_clear_history_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        onAction(HistorySettingsAction.Cleared)
                    },
                ) { Text(stringResource(R.string.action_clear)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
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

/** The latest entries, one expanded at a time. */
@Composable
private fun RecentHistory(
    entries: List<HistoryItem>,
    isHistoryEnabled: Boolean,
) {
    val context = LocalContext.current
    var expandedId by rememberSaveable { mutableStateOf<Long?>(null) }

    SettingsSection(R.string.settings_history_recent_title, contentPadding = PaddingValues(vertical = 4.dp)) {
        if (entries.isEmpty()) {
            Text(
                stringResource(
                    if (isHistoryEnabled) R.string.settings_history_empty else R.string.settings_history_empty_off,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        } else {
            Column {
                entries.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                    val id = item.entry.id
                    HistoryEntryRow(
                        item = item,
                        expanded = id == expandedId,
                        onClick = { expandedId = if (id == expandedId) null else id },
                        onCopy = context::copyToClipboard,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HistorySettingsScreenPreview() {
    val now = Clock.System.now()
    val fixGrammar = Transformation("fix_grammar", "Fix grammar", "Fix the grammar.", TransformationIcon.Spellcheck)
    val translate = Transformation("translate", "Translate", "Translate into {language}.", TransformationIcon.Translate)
    GrammioTheme {
        HistorySettingsScreen(
            state = HistorySettingsUiState(
                isHistoryEnabled = true,
                entries = listOf(
                    HistoryItem(
                        HistoryEntry(
                            id = 3,
                            startedAt = now - 2.minutes,
                            transformationId = "fix_grammar",
                            targetLanguageTag = null,
                            modelId = "gemini-2.5-flash",
                            inputText = "their going to the park tomorow if it dont rain",
                            result = Outcome.Success("They're going to the park tomorrow if it doesn't rain."),
                        ),
                        fixGrammar,
                    ),
                    HistoryItem(
                        HistoryEntry(
                            id = 2,
                            startedAt = now - 3.hours,
                            transformationId = "translate",
                            targetLanguageTag = "es",
                            modelId = null,
                            inputText = "See you tomorrow.",
                            result = Outcome.Failure(TransformError.MissingApiKey),
                        ),
                        translate,
                    ),
                    HistoryItem(
                        HistoryEntry(
                            id = 1,
                            startedAt = now - 30.hours,
                            transformationId = "gone",
                            targetLanguageTag = null,
                            modelId = "gpt-5-mini",
                            inputText = "An old note.",
                            result = Outcome.Success("A note from a while back."),
                        ),
                        transformation = null,
                    ),
                ),
            ),
            onAction = {},
            onBack = {},
        )
    }
}
