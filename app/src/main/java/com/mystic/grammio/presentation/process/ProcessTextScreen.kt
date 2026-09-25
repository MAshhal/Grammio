package com.mystic.grammio.presentation.process

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.presentation.process.components.LanguagePicker
import com.mystic.grammio.presentation.process.components.OriginalTextPreview
import com.mystic.grammio.presentation.process.components.ProcessTextActionBar
import com.mystic.grammio.presentation.process.components.ResultCard
import com.mystic.grammio.presentation.process.components.TransformationChips
import com.mystic.grammio.presentation.process.model.TransformationOptions
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

/** Sheet layout only; each section is a component in `components/`. */
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
            OriginalTextPreview(state.originalText)
            TransformationChips(
                transformations = state.transformations,
                selected = state.selected,
                onSelect = { onAction(ProcessTextAction.Select(it)) },
            )
            if (state.selected is Transformation.Translate) {
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

        ProcessTextActionBar(
            canReplace = state.canReplace,
            hasResult = state.resultText != null,
            onClose = { onAction(ProcessTextAction.Dismiss) },
            onCopy = { onAction(ProcessTextAction.Copy) },
            onReplace = { onAction(ProcessTextAction.Replace) },
        )
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
                transformations = TransformationOptions.all("en"),
                selected = Transformation.FixGrammar,
                result = ResultUiState.Success("Hello world, how are you doing today?"),
            ),
            onAction = {},
        )
    }
}
