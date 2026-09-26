package com.mystic.grammio.presentation.process.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mystic.grammio.R
import com.mystic.grammio.domain.error.TransformError

/** User-facing explanation of a failed transformation. */
@Composable
fun TransformError.message(): String = when (this) {
    TransformError.EmptyInput -> stringResource(R.string.error_empty_input)
    is TransformError.InputTooLong -> stringResource(R.string.error_input_too_long, maxChars)
    TransformError.MissingApiKey -> stringResource(R.string.error_missing_api_key)
    TransformError.InvalidApiKey -> stringResource(R.string.error_invalid_api_key)
    TransformError.ProviderNotConfigured -> stringResource(R.string.error_provider_not_configured)
    TransformError.RateLimited -> stringResource(R.string.error_rate_limited)
    TransformError.ContentBlocked -> stringResource(R.string.error_content_blocked)
    TransformError.Network -> stringResource(R.string.error_network)
    TransformError.Timeout -> stringResource(R.string.error_timeout)
    TransformError.ServiceUnavailable -> stringResource(R.string.error_service_unavailable)
    TransformError.Unknown -> stringResource(R.string.error_unknown)
}
