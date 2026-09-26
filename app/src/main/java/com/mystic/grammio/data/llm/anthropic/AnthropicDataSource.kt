package com.mystic.grammio.data.llm.anthropic

import co.touchlab.kermit.Logger
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.anthropic.dto.AnthropicErrorResponseDto
import com.mystic.grammio.data.llm.anthropic.mapper.AnthropicErrorMapper
import com.mystic.grammio.data.llm.anthropic.mapper.AnthropicRequestMapper
import com.mystic.grammio.data.llm.anthropic.mapper.AnthropicResponseMapper
import com.mystic.grammio.data.llm.mapper.HttpErrorMapper
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/**
 * Anthropic's Claude via the REST Messages API, authenticated with the user's own key.
 * Only performs the HTTP call; building and reading payloads is left to the mappers.
 */
class AnthropicDataSource(private val httpClient: HttpClient) : LlmDataSource {

    private val log = Logger.withTag("Anthropic")

    /**
     * Models (per base URL) that refused a temperature this session. The one piece of state kept
     * here: a cache of server behaviour, not configuration, so each such model costs one retry, not one per call.
     */
    private val temperatureRejectedBy: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override suspend fun generate(
        prompt: LlmPrompt,
        connection: LlmConnection,
    ): Outcome<String, TransformError> = try {
        send(prompt, connection, includeTemperature = connection.temperatureKey !in temperatureRejectedBy)
    } catch (e: HttpRequestTimeoutException) {
        // Caught before CancellationException, which some Ktor versions use as its supertype.
        exceptionFailure(e)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        exceptionFailure(e)
    }

    private suspend fun send(
        prompt: LlmPrompt,
        connection: LlmConnection,
        includeTemperature: Boolean,
    ): Outcome<String, TransformError> {
        val response = httpClient.post("${connection.baseUrl}/messages") {
            header(API_KEY_HEADER, connection.apiKey)
            header(VERSION_HEADER, API_VERSION)
            contentType(ContentType.Application.Json)
            setBody(AnthropicRequestMapper.map(prompt, connection.modelId, MAX_OUTPUT_TOKENS, includeTemperature))
        }
        if (response.status.isSuccess()) return AnthropicResponseMapper.map(response.body())

        val error = runCatching { response.body<AnthropicErrorResponseDto>().error }.getOrNull()
        if (includeTemperature && AnthropicErrorMapper.rejectsTemperature(response.status, error)) {
            log.i { "Model rejected temperature; retrying without it" }
            temperatureRejectedBy += connection.temperatureKey
            return send(prompt, connection, includeTemperature = false)
        }
        // Only the status and error type are logged; the message can echo user text.
        log.w { "messages failed: HTTP ${response.status.value} ${error?.type.orEmpty()}" }
        return Outcome.Failure(AnthropicErrorMapper.fromHttpError(response.status, error))
    }

    private val LlmConnection.temperatureKey get() = "$baseUrl|$modelId"

    private fun exceptionFailure(e: Exception): Outcome.Failure<TransformError> {
        log.w { "messages failed: ${e::class.simpleName}" }
        return Outcome.Failure(HttpErrorMapper.fromException(e))
    }

    private companion object {
        const val API_KEY_HEADER = "x-api-key"
        const val VERSION_HEADER = "anthropic-version"
        const val API_VERSION = "2023-06-01"
        const val MAX_OUTPUT_TOKENS = 8_192
    }
}
