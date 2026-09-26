package com.mystic.grammio.data.transform.log

import com.mystic.grammio.domain.error.TransformError

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
