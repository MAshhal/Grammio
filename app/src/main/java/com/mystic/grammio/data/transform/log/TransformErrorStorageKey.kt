package com.mystic.grammio.data.transform.log

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.usecase.TransformTextUseCase

/** Stable name for [TransformError] in anything persisted. */
val TransformError.storageKey: String
    get() = when (this) {
        TransformError.EmptyInput -> "empty_input"
        is TransformError.InputTooLong -> "input_too_long"
        TransformError.MissingApiKey -> "missing_api_key"
        TransformError.InvalidApiKey -> "invalid_api_key"
        TransformError.ProviderNotConfigured -> "provider_not_configured"
        TransformError.RateLimited -> "rate_limited"
        TransformError.ContentBlocked -> "content_blocked"
        TransformError.Network -> "network"
        TransformError.Timeout -> "timeout"
        TransformError.ServiceUnavailable -> "service_unavailable"
        TransformError.Unknown -> "unknown"
    }

/**
 * The error stored under [key], or null for a key this version doesn't know. The key doesn't keep
 * [TransformError.InputTooLong.maxChars], so that comes back as today's limit.
 */
fun transformErrorForStorageKey(key: String): TransformError? = when (key) {
    "empty_input" -> TransformError.EmptyInput
    "input_too_long" -> TransformError.InputTooLong(TransformTextUseCase.MAX_INPUT_CHARS)
    "missing_api_key" -> TransformError.MissingApiKey
    "invalid_api_key" -> TransformError.InvalidApiKey
    "provider_not_configured" -> TransformError.ProviderNotConfigured
    "rate_limited" -> TransformError.RateLimited
    "content_blocked" -> TransformError.ContentBlocked
    "network" -> TransformError.Network
    "timeout" -> TransformError.Timeout
    "service_unavailable" -> TransformError.ServiceUnavailable
    "unknown" -> TransformError.Unknown
    else -> null
}
