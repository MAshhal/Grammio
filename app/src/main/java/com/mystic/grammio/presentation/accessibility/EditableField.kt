package com.mystic.grammio.presentation.accessibility

/** A text field in another app that Grammio may write back to. */
fun interface EditableField {
    /** Replaces the field's whole text and puts the cursor at [cursor]. False when the app refused. */
    fun setText(
        text: String,
        cursor: Int,
    ): Boolean
}
