package com.mystic.grammio.feature.process

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.ui.theme.GrammioTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessTextSheet(
    state: ProcessTextUiState,
    onAction: (ProcessTextAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(ProcessTextAction.Dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        ProcessTextContent(state, onAction)
    }
}

@Composable
fun ProcessTextContent(
    state: ProcessTextUiState,
    onAction: (ProcessTextAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)

        if (!state.hasInput) {
            Text(stringResource(R.string.process_no_text), style = MaterialTheme.typography.bodyLarge)
        } else {
            OriginalText(state.originalText)
            TransformationChips(state, onAction)
            if (state.selected is Transformation.Translate) {
                LanguagePicker(state.targetLanguageTag) { onAction(ProcessTextAction.ChangeTargetLanguage(it)) }
            }
            ResultCard(state.result, onAction)
        }

        ActionRow(state, onAction)
    }
}

@Composable
private fun OriginalText(text: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = if (expanded) Int.MAX_VALUE else 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
    )
}

@Composable
private fun TransformationChips(
    state: ProcessTextUiState,
    onAction: (ProcessTextAction) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.transformations.forEach { transformation ->
            FilterChip(
                selected = transformation == state.selected,
                onClick = { onAction(ProcessTextAction.Select(transformation)) },
                label = { Text(stringResource(transformation.labelRes())) },
            )
        }
    }
}

@Composable
private fun LanguagePicker(
    selectedTag: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val tags = remember { translationLanguageTags(Locale.getDefault().language) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(stringResource(R.string.process_target_language, languageDisplayName(selectedTag)))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            tags.forEach { tag ->
                DropdownMenuItem(
                    text = { Text(languageDisplayName(tag)) },
                    onClick = {
                        expanded = false
                        onSelect(tag)
                    },
                )
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: ResultState,
    onAction: (ProcessTextAction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Box(Modifier.fillMaxWidth().padding(16.dp).animateContentSize()) {
            when (result) {
                ResultState.Idle -> Text(
                    stringResource(R.string.process_pick_transformation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                ResultState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.size(32.dp).align(Alignment.Center),
                )

                is ResultState.Success -> SelectionContainer {
                    Text(result.text, style = MaterialTheme.typography.bodyLarge)
                }

                is ResultState.Failure -> ErrorContent(result.error, onAction)
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: TransformError,
    onAction: (ProcessTextAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(error.message(), color = MaterialTheme.colorScheme.error)
        when (error.recovery) {
            ErrorRecovery.Retry -> OutlinedButton(onClick = { onAction(ProcessTextAction.Retry) }) {
                Text(stringResource(R.string.action_retry))
            }

            ErrorRecovery.OpenSettings -> OutlinedButton(onClick = { onAction(ProcessTextAction.OpenSettings) }) {
                Text(stringResource(R.string.action_open_settings))
            }

            ErrorRecovery.None -> Unit
        }
    }
}

@Composable
private fun ActionRow(
    state: ProcessTextUiState,
    onAction: (ProcessTextAction) -> Unit,
) {
    val hasResult = state.resultText != null
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = { onAction(ProcessTextAction.Dismiss) }) {
            Text(stringResource(R.string.action_close))
        }
        Spacer(Modifier.weight(1f))
        if (state.canReplace) {
            OutlinedButton(onClick = { onAction(ProcessTextAction.Copy) }, enabled = hasResult) {
                Text(stringResource(R.string.action_copy))
            }
            Button(onClick = { onAction(ProcessTextAction.Replace) }, enabled = hasResult) {
                Text(stringResource(R.string.action_replace))
            }
        } else {
            Button(onClick = { onAction(ProcessTextAction.Copy) }, enabled = hasResult) {
                Text(stringResource(R.string.action_copy))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProcessTextContentPreview() {
    GrammioTheme {
        ProcessTextContent(
            state = ProcessTextUiState(
                originalText = "helo wrld, how r u doing today",
                canReplace = true,
                targetLanguageTag = "en",
                selected = Transformation.FixGrammar,
                result = ResultState.Success("Hello world, how are you doing today?"),
            ),
            onAction = {},
        )
    }
}
