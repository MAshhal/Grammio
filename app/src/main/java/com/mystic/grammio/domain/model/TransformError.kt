package com.mystic.grammio.domain.model

/**
 * Every way a transformation can fail, expressed in application terms. Data implementations map
 * their transport/vendor failures into these so the UI never sees HTTP codes or provider errors.
 */
sealed interface TransformError {
    data object EmptyInput : TransformError
    data class InputTooLong(val maxChars: Int) : TransformError
    data object MissingApiKey : TransformError
    data object InvalidApiKey : TransformError
    data object RateLimited : TransformError
    data object ContentBlocked : TransformError
    data object Network : TransformError
    data object Timeout : TransformError
    data object ServiceUnavailable : TransformError
    data object Unknown : TransformError
}
