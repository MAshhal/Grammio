package com.mystic.grammio.presentation.accessibility

/**
 * Carries the selection the accessibility service handed to the process sheet, so the sheet's
 * Replace can write the result back into the other app's field. An Activity started by a service
 * has no caller to return a result to, which is how the PROCESS_TEXT path replaces text instead.
 */
class SelectionReplacer {

    private var pending: Pending? = null

    /** Remembers [selection] in [field] for [replace]; a null [field] forgets any earlier one. */
    @Synchronized
    fun hold(
        field: EditableField?,
        selection: TextSelection,
    ) {
        pending = field?.takeIf { selection.editable }?.let { Pending(it, selection) }
    }

    /** Puts [replacement] where the held selection was. False when nothing is held or the field refused. */
    @Synchronized
    fun replace(replacement: String): Boolean {
        val (field, selection) = pending ?: return false
        pending = null
        val replaced = selection.replacedWith(replacement)
        return field.setText(replaced.text, replaced.cursor)
    }

    @Synchronized
    fun clear() {
        pending = null
    }

    private data class Pending(
        val field: EditableField,
        val selection: TextSelection,
    )
}
