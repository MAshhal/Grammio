package com.mystic.grammio.presentation.accessibility

/**
 * Framework-free snapshot of text selected in another app, as the accessibility service sees it.
 *
 * @property text the whole text of the field or view the selection is in.
 * @property start first selected index, always before [end].
 * @property end index just past the last selected character.
 * @property editable true when the field accepts new text, so the result can replace the selection.
 */
data class TextSelection(
    val text: String,
    val start: Int,
    val end: Int,
    val editable: Boolean,
) {
    init {
        require(start in 0 until end && end <= text.length) { "invalid selection $start..$end of ${text.length}" }
    }

    val selectedText: String get() = text.substring(start, end)

    /** The whole text with the selection swapped for [replacement], and where the cursor goes after it. */
    fun replacedWith(replacement: String): Replaced = Replaced(
        text = text.replaceRange(start, end, replacement),
        cursor = start + replacement.length,
    )

    data class Replaced(
        val text: String,
        val cursor: Int,
    )

    companion object {
        /**
         * The selection of [start]..[end] in [text], or null when nothing usable is selected. Accepts
         * what views report as is: a selection made backwards (start after end), -1 for "no selection",
         * and indices past the end of text that changed since they were reported.
         */
        fun of(
            text: CharSequence?,
            start: Int,
            end: Int,
            editable: Boolean,
        ): TextSelection? {
            if (text == null || start < 0 || end < 0) return null
            val from = minOf(start, end).coerceAtMost(text.length)
            val to = maxOf(start, end).coerceAtMost(text.length)
            if (from == to) return null
            // Spans (bold, links, …) are dropped, as on the PROCESS_TEXT path.
            val selection = TextSelection(text.toString(), from, to, editable)
            return selection.takeIf { it.selectedText.isNotBlank() }
        }

        /**
         * What the button works on in a text field: the selection if there is one, otherwise all of
         * the field's text, so Replace rewrites the whole field. Null when the field is empty.
         */
        fun selectionOrAll(
            text: CharSequence?,
            start: Int,
            end: Int,
            editable: Boolean,
        ): TextSelection? = of(text, start, end, editable) ?: text?.let { of(it, 0, it.length, editable) }
    }
}
