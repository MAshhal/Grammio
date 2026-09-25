package com.mystic.grammio.data.llm.gemini

import co.touchlab.kermit.Logger
import com.mystic.grammio.data.apikey.ApiKeyProvider
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.gemini.dto.GeminiErrorResponseDto
import com.mystic.grammio.data.llm.gemini.mapper.GeminiErrorMapper
import com.mystic.grammio.data.llm.gemini.mapper.GeminiRequestMapper
import com.mystic.grammio.data.llm.gemini.mapper.GeminiResponseMapper
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException

/**
 * Google Gemini via the REST `generateContent` API, authenticated with the user's own key.
 * Only performs the HTTP call; building and reading payloads is left to the mappers.
 */
class GeminiDataSource(
    private val httpClient: HttpClient,
    private val apiKeyProvider: ApiKeyProvider,
) : LlmDataSource {

    private val log = Logger.withTag("Gemini")

    override suspend fun generate(prompt: LlmPrompt): Outcome<String, TransformError> {
        val apiKey = apiKeyProvider.apiKey() ?: return Outcome.Failure(TransformError.MissingApiKey)
        return try {
            val response = httpClient.post("$BASE_URL/models/$MODEL:generateContent") {
                // Header rather than ?key= so the key never appears in URLs or logs.
                header(API_KEY_HEADER, apiKey)
                contentType(ContentType.Application.Json)
                setBody(GeminiRequestMapper.map(prompt, MAX_OUTPUT_TOKENS))
            }
            if (response.status.isSuccess()) {
                GeminiResponseMapper.map(response.body())
            } else {
                httpFailure(response)
            }
        } catch (e: HttpRequestTimeoutException) {
            // Caught before CancellationException, which some Ktor versions use as its supertype.
            exceptionFailure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            exceptionFailure(e)
        }
    }

    private suspend fun httpFailure(response: HttpResponse): Outcome.Failure<TransformError> {
        val error = runCatching { response.body<GeminiErrorResponseDto>().error }.getOrNull()
        // Only the status and error codes are logged; the body can echo user text.
        log.w { "generateContent failed: HTTP ${response.status.value} ${error?.status.orEmpty()}" }
        return Outcome.Failure(GeminiErrorMapper.fromHttpError(response.status, error))
    }

    private fun exceptionFailure(e: Exception): Outcome.Failure<TransformError> {
        log.w { "generateContent failed: ${e::class.simpleName}" }
        return Outcome.Failure(GeminiErrorMapper.fromException(e))
    }

    companion object {
        /** Fast, low-cost model; thinking is minimal by default, which suits short rewrites. */
        const val MODEL = "gemini-3.5-flash-lite"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        private const val API_KEY_HEADER = "x-goog-api-key"
        private const val MAX_OUTPUT_TOKENS = 8_192
    }
}
