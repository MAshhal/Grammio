package com.mystic.grammio.domain.model

/**
 * What the user wants done to the selected text. A few come with the app; the user can edit,
 * disable, reorder or delete them and add their own.
 *
 * @property id stable across edits, so the history log can tell which transformation ran.
 * @property taskPrompt what to do with the text, sent after the system prompt. When it contains
 *   [LANGUAGE_PLACEHOLDER], the user picks a target language and its name takes the placeholder's place.
 * @property temperature not shown to the user: the built-in ones keep tuned values, new ones get
 *   [DEFAULT_TEMPERATURE].
 */
data class Transformation(
    val id: String,
    val name: String,
    val taskPrompt: String,
    val icon: TransformationIcon,
    val isEnabled: Boolean = true,
    val temperature: Double = DEFAULT_TEMPERATURE,
) {
    val usesTargetLanguage: Boolean get() = LANGUAGE_PLACEHOLDER in taskPrompt

    companion object {
        const val LANGUAGE_PLACEHOLDER = "{language}"
        const val DEFAULT_TEMPERATURE = 0.5
    }
}
