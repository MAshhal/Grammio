package com.mystic.grammio.data.llm.gemini

import co.touchlab.kermit.Logger
import com.mystic.grammio.data.apikey.ApiKeyProvider
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.LlmProvider
import com.mystic.grammio.domain.model.Outcome
import com.mystic.grammio.domain.model.TransformError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Google Gemini via the REST `generateContent` API, authenticated with the user's own key. */
class GeminiProvider(
    private val httpClient: HttpClient,
    private val apiKeyProvider: ApiKeyProvider,
) : LlmProvider {

    private val log = Logger.withTag("Gemini")

    override suspend fun generate(prompt: LlmPrompt): Outcome<String, TransformError> {
        val apiKey = apiKeyProvider.apiKey() ?: return Outcome.Failure(TransformError.MissingApiKey)
        return try {
            val response = httpClient.post("$BASE_URL/models/$MODEL:generateContent") {
                // Header rather than ?key= so the key never appears in URLs or logs.
                header(API_KEY_HEADER, apiKey)
                contentType(ContentType.Application.Json)
                setBody(prompt.toRequest())
            }
            if (response.status.isSuccess()) {
                parseSuccess(response.body())
            } else {
                Outcome.Failure(mapHttpError(response))
            }
        } catch (e: HttpRequestTimeoutException) {
            Outcome.Failure(TransformError.Timeout)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Outcome.Failure(mapException(e))
        }
    }

    private fun parseSuccess(response: GenerateContentResponse): Outcome<String, TransformError> {
        if (response.promptFeedback?.blockReason != null) return Outcome.Failure(TransformError.ContentBlocked)
        val candidate = response.candidates.firstOrNull() ?: return Outcome.Failure(TransformError.Unknown)
        if (candidate.finishReason in BLOCKING_FINISH_REASONS) return Outcome.Failure(TransformError.ContentBlocked)

        val text = candidate.content?.parts.orEmpty()
            .filter { it.thought != true }
            .mapNotNull { it.text }
            .joinToString("")
        return if (text.isBlank()) Outcome.Failure(TransformError.Unknown) else Outcome.Success(text)
    }

    private suspend fun mapHttpError(response: HttpResponse): TransformError {
        val status = response.status
        // Only the status and error codes are logged; the body can echo user text.
        val error = runCatching { response.body<GeminiErrorResponse>().error }.getOrNull()
        log.w { "generateContent failed: HTTP ${status.value} ${error?.status.orEmpty()}" }
        return when {
            status == HttpStatusCode.BadRequest && error?.details.orEmpty().any { it.reason == "API_KEY_INVALID" } ->
                TransformError.InvalidApiKey

            status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Forbidden -> TransformError.InvalidApiKey

            status == HttpStatusCode.TooManyRequests -> TransformError.RateLimited

            status.value >= 500 -> TransformError.ServiceUnavailable

            else -> TransformError.Unknown
        }
    }

    private fun mapException(e: Exception): TransformError {
        log.w { "generateContent failed: ${e::class.simpleName}" }
        return when (e) {
            is ConnectTimeoutException, is SocketTimeoutException, is java.net.SocketTimeoutException ->
                TransformError.Timeout

            is IOException -> TransformError.Network

            else -> TransformError.Unknown
        }
    }

    private fun LlmPrompt.toRequest() = GenerateContentRequest(
        systemInstruction = ContentDto(parts = listOf(PartDto(text = systemInstruction))),
        contents = listOf(ContentDto(role = "user", parts = listOf(PartDto(text = userText)))),
        generationConfig = GenerationConfigDto(temperature = temperature, maxOutputTokens = MAX_OUTPUT_TOKENS),
    )

    companion object {
        /** Fast, low-cost model; thinking is minimal by default, which suits short rewrites. */
        const val MODEL = "gemini-3.5-flash-lite"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        private const val API_KEY_HEADER = "x-goog-api-key"
        private const val MAX_OUTPUT_TOKENS = 8_192
        private val BLOCKING_FINISH_REASONS =
            setOf("SAFETY", "PROHIBITED_CONTENT", "BLOCKLIST", "SPII", "RECITATION")
    }
}
