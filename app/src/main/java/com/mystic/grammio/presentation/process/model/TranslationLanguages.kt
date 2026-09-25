package com.mystic.grammio.presentation.process.model

import java.util.Locale

/** Target languages offered for Translate, and how their names are shown. */
object TranslationLanguages {

    /** The device language first, then common languages, without duplicates. */
    fun tags(deviceLanguageTag: String): List<String> = (listOf(deviceLanguageTag) + COMMON_TAGS).distinct()

    /** The language's name in the device language, e.g. "Spanish" or "Español". */
    fun displayName(tag: String): String {
        val locale = Locale.getDefault()
        return Locale.forLanguageTag(tag).getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }
    }

    private val COMMON_TAGS = listOf(
        "en", "es", "fr", "de", "it", "pt", "nl", "ru", "tr", "ar", "ur", "hi", "bn", "zh", "ja", "ko", "id",
    )
}
