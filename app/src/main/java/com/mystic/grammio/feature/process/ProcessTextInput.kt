package com.mystic.grammio.feature.process

import android.content.ComponentName
import android.content.Intent

/**
 * Framework-free snapshot of an [Intent.ACTION_PROCESS_TEXT] request, so everything past the
 * Activity (ViewModel, tests) never has to touch [Intent].
 *
 * @property canReplace true only when the caller can accept edited text back: it started us for a
 * result and did not mark the selection read-only.
 */
data class ProcessTextInput(
    val text: String,
    val canReplace: Boolean,
) {
    companion object {
        fun from(intent: Intent, callingActivity: ComponentName?): ProcessTextInput {
            val readOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
            return ProcessTextInput(
                // Spans (bold, links, …) are intentionally dropped; transformations work on plain text.
                text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty(),
                canReplace = !readOnly && callingActivity != null,
            )
        }
    }
}
