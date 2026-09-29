package com.mystic.grammio.presentation.accessibility

/** The field that has input focus in another app, as far as deciding whether to offer Grammio goes. */
data class TypingField(
    val packageName: String?,
    val editable: Boolean,
    val password: Boolean,
) {
    /**
     * Whether the keyboard showing for this field should bring up the Grammio button: only for text
     * the user can edit, never for passwords, and never over Grammio itself.
     */
    fun offersButton(ownPackage: String): Boolean = editable && !password && packageName != ownPackage
}
