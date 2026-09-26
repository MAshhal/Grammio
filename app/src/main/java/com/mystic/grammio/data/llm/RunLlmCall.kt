package com.mystic.grammio.data.llm

import co.touchlab.kermit.Logger
import com.mystic.grammio.data.llm.mapper.HttpErrorMapper
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs one HTTP call to an LLM API, turning transport exceptions into [TransformError] while
 * letting cancellation through. [operation] names the call in logs.
 */
internal suspend fun <T> runLlmCall(
    log: Logger,
    operation: String,
    call: suspend () -> Outcome<T, TransformError>,
): Outcome<T, TransformError> = try {
    call()
} catch (e: HttpRequestTimeoutException) {
    // Caught before CancellationException, which some Ktor versions use as its supertype.
    exceptionFailure(log, operation, e)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    exceptionFailure(log, operation, e)
}

private fun exceptionFailure(
    log: Logger,
    operation: String,
    e: Exception,
): Outcome.Failure<TransformError> {
    log.w { "$operation failed: ${e::class.simpleName}" }
    return Outcome.Failure(HttpErrorMapper.fromException(e))
}
