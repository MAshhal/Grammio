package com.mystic.grammio.presentation.process

import android.content.ComponentName
import android.content.Intent

/**
 * Framework-free snapshot of an [Intent.ACTION_PROCESS_TEXT] request, so everything past the
 * Activity (ViewModel, tests) never has to touch [Intent].
 *
 * @property canReplace true only when the result has somewhere to go: the caller started us for a
 * result, or the accessibility service holds an editable field, and the selection isn't read-only.
 */
data class ProcessTextInput(
    val text: String,
    val canReplace: Boolean,
) {
    companion object {
        /**
         * The non-exported alias of [ProcessTextActivity] the accessibility service opens. Only this
         * app can start it, so unlike an extra, another app can't use it to claim it came from there.
         */
        const val ACCESSIBILITY_ALIAS = "com.mystic.grammio.presentation.process.AccessibilityProcessTextActivity"

        fun from(
            intent: Intent,
            callingActivity: ComponentName?,
        ): ProcessTextInput = of(
            text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT),
            readOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false),
            startedForResult = callingActivity != null,
            fromAccessibility = intent.isFromAccessibility(),
        )

        fun of(
            text: CharSequence?,
            readOnly: Boolean,
            startedForResult: Boolean,
            fromAccessibility: Boolean,
        ): ProcessTextInput = ProcessTextInput(
            // Spans (bold, links, …) are intentionally dropped; transformations work on plain text.
            text = text?.toString().orEmpty(),
            canReplace = !readOnly && (startedForResult || fromAccessibility),
        )

        fun Intent.isFromAccessibility(): Boolean = component?.className == ACCESSIBILITY_ALIAS
    }
}
