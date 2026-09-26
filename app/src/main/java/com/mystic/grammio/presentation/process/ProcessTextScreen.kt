package com.mystic.grammio.presentation.process

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.presentation.process.components.LanguagePicker
import com.mystic.grammio.presentation.process.components.OriginalTextPreview
import com.mystic.grammio.presentation.process.components.ProcessTextActionBar
import com.mystic.grammio.presentation.process.components.ProcessTextHeader
import com.mystic.grammio.presentation.process.components.ResultCard
import com.mystic.grammio.presentation.process.components.TransformationChips
import com.mystic.grammio.presentation.theme.GrammioTheme

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

/**
 * Sheet layout only; each section is a component in `components/`. The header and action bar stay
 * put while the middle scrolls, so Copy and Replace are reachable however long the text is.
 */
@Composable
fun ProcessTextContent(
    state: ProcessTextUiState,
    onAction: (ProcessTextAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        ProcessTextHeader(
            onClose = { onAction(ProcessTextAction.Dismiss) },
            modifier = Modifier.padding(start = 24.dp, end = 12.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!state.hasInput) {
                NoInputMessage()
            } else {
                OriginalTextPreview(state.originalText)
                if (state.hasNoTransformations) {
                    NoTransformationsMessage(onManage = { onAction(ProcessTextAction.ManageTransformations) })
                } else {
                    TransformationChips(
                        transformations = state.transformations.orEmpty(),
                        selected = state.selected,
                        onSelect = { onAction(ProcessTextAction.Select(it)) },
                    )
                }
                AnimatedVisibility(visible = state.showsLanguagePicker) {
                    LanguagePicker(
                        selectedTag = state.targetLanguageTag,
                        onSelect = { onAction(ProcessTextAction.ChangeTargetLanguage(it)) },
                    )
                }
                ResultCard(
                    result = state.result,
                    onRetry = { onAction(ProcessTextAction.Retry) },
                    onOpenSettings = { onAction(ProcessTextAction.OpenSettings) },
                )
            }
        }

        if (state.hasInput) {
            ProcessTextActionBar(
                canReplace = state.canReplace,
                hasResult = state.resultText != null,
                onCopy = { onAction(ProcessTextAction.Copy) },
                onReplace = { onAction(ProcessTextAction.Replace) },
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun NoInputMessage() {
    Row(
        modifier = Modifier.padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.process_no_text), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun NoTransformationsMessage(onManage: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(R.string.process_no_transformations),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onManage, contentPadding = ButtonDefaults.TextButtonWithIconContentPadding) {
            Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.action_manage_transformations))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProcessTextContentPreview() {
    GrammioTheme {
        ProcessTextContent(
            state = previewState(ResultUiState.Success("Hello world, how are you doing today?")),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProcessTextLoadingPreview() {
    GrammioTheme {
        ProcessTextContent(state = previewState(ResultUiState.Loading), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ProcessTextErrorPreview() {
    GrammioTheme {
        ProcessTextContent(
            state = previewState(ResultUiState.Failure(TransformError.Network)).copy(canReplace = false),
            onAction = {},
        )
    }
}

private fun previewState(result: ResultUiState) = ProcessTextUiState(
    originalText = "helo wrld, how r u doing today",
    canReplace = true,
    targetLanguageTag = "en",
    transformations = previewTransformations,
    selected = previewTransformations.first(),
    result = result,
)

private val previewTransformations = listOf(
    Transformation("fix_grammar", "Fix grammar", "Fix it.", TransformationIcon.Spellcheck),
    Transformation("summarize", "Summarize", "Summarize it.", TransformationIcon.Summarize),
    Transformation("translate", "Translate", "Translate into {language}.", TransformationIcon.Translate),
)
