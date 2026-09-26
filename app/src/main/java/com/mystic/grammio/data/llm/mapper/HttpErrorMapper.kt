package com.mystic.grammio.data.llm.mapper

import com.mystic.grammio.domain.error.TransformError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import java.io.IOException

/**
 * Failures every HTTP LLM API shares → [TransformError]. Vendor mappers check their own error
 * bodies first and fall back to [fromStatus].
 */
internal object HttpErrorMapper {

    fun fromStatus(status: HttpStatusCode): TransformError = when {
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
}
