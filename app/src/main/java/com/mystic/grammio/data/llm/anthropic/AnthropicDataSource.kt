package com.mystic.grammio.data.llm.anthropic

import co.touchlab.kermit.Logger
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.LlmEndpoint
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.anthropic.dto.AnthropicErrorBodyDto
import com.mystic.grammio.data.llm.anthropic.dto.AnthropicErrorResponseDto
import com.mystic.grammio.data.llm.anthropic.mapper.AnthropicErrorMapper
import com.mystic.grammio.data.llm.anthropic.mapper.AnthropicModelListMapper
import com.mystic.grammio.data.llm.anthropic.mapper.AnthropicRequestMapper
import com.mystic.grammio.data.llm.anthropic.mapper.AnthropicResponseMapper
import com.mystic.grammio.data.llm.runLlmCall
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.result.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.util.concurrent.ConcurrentHashMap

/**
 * Anthropic's Claude via the REST API, authenticated with the user's own key.
 * Only performs the HTTP calls; building and reading payloads is left to the mappers.
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
    ): Outcome<String, TransformError> = runLlmCall(log, "messages") {
        send(prompt, connection, includeTemperature = connection.temperatureKey !in temperatureRejectedBy)
    }

    override suspend fun listModels(endpoint: LlmEndpoint): Outcome<List<AiModel>, TransformError> =
        runLlmCall(log, "models") {
            val response = httpClient.get("${endpoint.baseUrl}/models") {
                authenticate(endpoint.apiKey)
                parameter("limit", MAX_PAGE_SIZE)
            }
            if (response.status.isSuccess()) {
                Outcome.Success(AnthropicModelListMapper.map(response.body()))
            } else {
                httpFailure(response, "models", response.errorBody())
            }
        }

    private suspend fun send(
        prompt: LlmPrompt,
        connection: LlmConnection,
        includeTemperature: Boolean,
    ): Outcome<String, TransformError> {
        val response = httpClient.post("${connection.baseUrl}/messages") {
            authenticate(connection.apiKey)
            contentType(ContentType.Application.Json)
            setBody(AnthropicRequestMapper.map(prompt, connection.modelId, MAX_OUTPUT_TOKENS, includeTemperature))
        }
        if (response.status.isSuccess()) return AnthropicResponseMapper.map(response.body())

        val error = response.errorBody()
        if (includeTemperature && AnthropicErrorMapper.rejectsTemperature(response.status, error)) {
            log.i { "Model rejected temperature; retrying without it" }
            temperatureRejectedBy += connection.temperatureKey
            return send(prompt, connection, includeTemperature = false)
        }
        return httpFailure(response, "messages", error)
    }

    private fun HttpRequestBuilder.authenticate(apiKey: String) {
        header(API_KEY_HEADER, apiKey)
        header(VERSION_HEADER, API_VERSION)
    }

    private suspend fun HttpResponse.errorBody(): AnthropicErrorBodyDto? =
        runCatching { body<AnthropicErrorResponseDto>().error }.getOrNull()

    private fun httpFailure(
        response: HttpResponse,
        operation: String,
        error: AnthropicErrorBodyDto?,
    ): Outcome.Failure<TransformError> {
        // Only the status and error type are logged; the message can echo user text.
        log.w { "$operation failed: HTTP ${response.status.value} ${error?.type.orEmpty()}" }
        return Outcome.Failure(AnthropicErrorMapper.fromHttpError(response.status, error))
    }

    private val LlmConnection.temperatureKey get() = "$baseUrl|$modelId"

    private companion object {
        const val API_KEY_HEADER = "x-api-key"
        const val VERSION_HEADER = "anthropic-version"
        const val API_VERSION = "2023-06-01"
        const val MAX_OUTPUT_TOKENS = 8_192

        /** The API's maximum: every model fits on one page. */
        const val MAX_PAGE_SIZE = 1_000
    }
}
