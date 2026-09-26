package com.mystic.grammio.presentation.process

/** One-off events that need the Activity (clipboard, activity result, navigation). */
sealed interface ProcessTextEffect {
    data class CopyToClipboard(val text: String) : ProcessTextEffect

    data class ReturnResult(val text: String) : ProcessTextEffect

    data object Close : ProcessTextEffect

    /** Settings on the provider page, for errors about the key or endpoint. */
    data object OpenSettings : ProcessTextEffect

    data object OpenTransformationSettings : ProcessTextEffect
}
