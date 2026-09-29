package com.mystic.grammio.presentation.accessibility

/** A field in another app the user may be typing in, as far as deciding whether to offer Grammio goes. */
data class TypingField(
    val packageName: String?,
    val className: String?,
    val editable: Boolean,
    val acceptsSetText: Boolean,
    val password: Boolean,
) {
    /**
     * Whether it holds text the user can edit. Apps with custom text fields (X, for one) don't always
     * mark them editable, so a field that accepts new text or is an EditText by name counts too.
     */
    val isTextField: Boolean
        get() = editable || acceptsSetText || className?.endsWith("EditText") == true

    /**
     * Whether the keyboard showing for this field should bring up the Grammio button: only for text
     * the user can edit, never for passwords, and never over Grammio itself.
     */
    fun offersButton(ownPackage: String): Boolean = isTextField && !password && packageName != ownPackage
}
