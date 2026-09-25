package com.mystic.grammio.data.llm.gemini.mapper

import com.mystic.grammio.data.llm.gemini.dto.GeminiErrorBodyDto
import com.mystic.grammio.domain.error.TransformError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import java.io.IOException

/** Gemini HTTP failures and transport exceptions → [TransformError]. */
internal object GeminiErrorMapper {

    fun fromHttpError(
        status: HttpStatusCode,
        error: GeminiErrorBodyDto?,
    ): TransformError = when {
        status == HttpStatusCode.BadRequest && error.hasReason(API_KEY_INVALID) -> TransformError.InvalidApiKey
        status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Forbidden -> TransformError.InvalidApiKey
        status == HttpStatusCode.TooManyRequests -> TransformError.RateLimited
        status.value >= 500 -> TransformError.ServiceUnavailable
        else -> TransformError.Unknown
    }

    fun fromException(e: Exception): TransformError = when (e) {
        is HttpRequestTimeoutException,
        is ConnectTimeoutException,
        is SocketTimeoutException,
        is java.net.SocketTimeoutException,
        -> TransformError.Timeout

        is IOException -> TransformError.Network

        else -> TransformError.Unknown
    }

    private fun GeminiErrorBodyDto?.hasReason(reason: String): Boolean =
        this?.details.orEmpty().any { it.reason == reason }

    private const val API_KEY_INVALID = "API_KEY_INVALID"
}
