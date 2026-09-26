package com.mystic.grammio.presentation.process.model

import com.mystic.grammio.domain.error.TransformError

/** What the error UI offers the user besides reading the message. */
enum class ErrorRecovery { Retry, OpenSettings, None }

val TransformError.recovery: ErrorRecovery
    get() = when (this) {
        TransformError.MissingApiKey, TransformError.InvalidApiKey, TransformError.ProviderNotConfigured,
        -> ErrorRecovery.OpenSettings

        TransformError.EmptyInput, is TransformError.InputTooLong, TransformError.ContentBlocked -> ErrorRecovery.None

        TransformError.RateLimited, TransformError.Network, TransformError.Timeout,
        TransformError.ServiceUnavailable, TransformError.Unknown,
        -> ErrorRecovery.Retry
    }
