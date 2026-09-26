package com.mystic.grammio.presentation.settings.history.components

import android.text.format.DateUtils
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.presentation.common.imageVector
import com.mystic.grammio.presentation.common.message
import com.mystic.grammio.presentation.process.model.TranslationLanguages
import com.mystic.grammio.presentation.settings.history.HistoryItem
import kotlin.time.Instant

/**
 * One saved transformation: what ran and when, the original text, and the result or error. Collapsed,
 * it shows the start of each; expanded, all of it, plus the model and a copy button.
 */
@Composable
fun HistoryEntryRow(
    item: HistoryItem,
    expanded: Boolean,
    onClick: () -> Unit,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entry = item.entry
    val output = (entry.result as? Outcome.Success)?.value
    val maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(if (expanded) R.string.process_show_less else R.string.process_show_more),
                onClick = onClick,
            )
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            (item.transformation?.icon ?: TransformationIcon.Sparkle).imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listOfNotNull(
                        item.transformation?.name ?: stringResource(R.string.settings_history_deleted_transformation),
                        entry.targetLanguageTag?.let(TranslationLanguages::displayName),
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    relativeTime(entry.startedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                entry.inputText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
            when (val result = entry.result) {
                is Outcome.Success -> Text(
                    result.value,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                )

                is Outcome.Failure -> Text(
                    result.error.message(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (expanded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.modelId.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    if (output != null) {
                        TextButton(onClick = {
                            onCopy(output)
                        }, contentPadding = ButtonDefaults.TextButtonWithIconContentPadding) {
                            Icon(
                                Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Text(
                                stringResource(R.string.action_copy),
                                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** "Just now", "5 min. ago", "Yesterday", then a date. Doesn't tick while the page is open. */
@Composable
private fun relativeTime(instant: Instant): String {
    val then = instant.toEpochMilliseconds()
    val now = System.currentTimeMillis()
    if (now - then < DateUtils.MINUTE_IN_MILLIS) return stringResource(R.string.settings_history_just_now)
    return DateUtils.getRelativeTimeSpanString(then, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
        .toString()
}

private const val COLLAPSED_LINES = 2
