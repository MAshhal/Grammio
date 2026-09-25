package com.mystic.grammio.presentation.process

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.TransformError
import com.mystic.grammio.domain.model.Transformation
import java.util.Locale

@StringRes
fun Transformation.labelRes(): Int = when (this) {
    Transformation.FixGrammar -> R.string.transformation_fix_grammar
    Transformation.Rephrase -> R.string.transformation_rephrase
    Transformation.Professional -> R.string.transformation_professional
    Transformation.Casual -> R.string.transformation_casual
    Transformation.Shorten -> R.string.transformation_shorten
    Transformation.Expand -> R.string.transformation_expand
    Transformation.Summarize -> R.string.transformation_summarize
    is Transformation.Translate -> R.string.transformation_translate
}

/** What the error UI offers the user besides reading the message. */
enum class ErrorRecovery { Retry, OpenSettings, None }

val TransformError.recovery: ErrorRecovery
    get() = when (this) {
        TransformError.MissingApiKey, TransformError.InvalidApiKey -> ErrorRecovery.OpenSettings

        TransformError.EmptyInput, is TransformError.InputTooLong, TransformError.ContentBlocked -> ErrorRecovery.None

        TransformError.RateLimited, TransformError.Network, TransformError.Timeout,
        TransformError.ServiceUnavailable, TransformError.Unknown,
        -> ErrorRecovery.Retry
    }

@Composable
fun TransformError.message(): String = when (this) {
    TransformError.EmptyInput -> stringResource(R.string.error_empty_input)
    is TransformError.InputTooLong -> stringResource(R.string.error_input_too_long, maxChars)
    TransformError.MissingApiKey -> stringResource(R.string.error_missing_api_key)
    TransformError.InvalidApiKey -> stringResource(R.string.error_invalid_api_key)
    TransformError.RateLimited -> stringResource(R.string.error_rate_limited)
    TransformError.ContentBlocked -> stringResource(R.string.error_content_blocked)
    TransformError.Network -> stringResource(R.string.error_network)
    TransformError.Timeout -> stringResource(R.string.error_timeout)
    TransformError.ServiceUnavailable -> stringResource(R.string.error_service_unavailable)
    TransformError.Unknown -> stringResource(R.string.error_unknown)
}

/** Target languages offered for Translate; the device language is always included first. */
fun translationLanguageTags(deviceLanguageTag: String): List<String> =
    (listOf(deviceLanguageTag) + COMMON_LANGUAGE_TAGS).distinct()

fun languageDisplayName(tag: String): String {
    val locale = Locale.getDefault()
    return Locale.forLanguageTag(tag).getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }
}

private val COMMON_LANGUAGE_TAGS = listOf(
    "en", "es", "fr", "de", "it", "pt", "nl", "ru", "tr", "ar", "ur", "hi", "bn", "zh", "ja", "ko", "id",
)
