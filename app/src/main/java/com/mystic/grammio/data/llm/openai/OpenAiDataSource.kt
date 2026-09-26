package com.mystic.grammio.data.llm.openai

import co.touchlab.kermit.Logger
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.LlmEndpoint
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.openai.dto.OpenAiErrorBodyDto
import com.mystic.grammio.data.llm.openai.dto.OpenAiErrorResponseDto
import com.mystic.grammio.data.llm.openai.mapper.OpenAiErrorMapper
import com.mystic.grammio.data.llm.openai.mapper.OpenAiModelListMapper
import com.mystic.grammio.data.llm.openai.mapper.OpenAiRequestMapper
import com.mystic.grammio.data.llm.openai.mapper.OpenAiResponseMapper
import com.mystic.grammio.data.llm.runLlmCall
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.result.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.util.concurrent.ConcurrentHashMap

/**
 * The OpenAI API, which also serves every OpenAI-compatible endpoint: only the base URL differs.
 * Only performs the HTTP calls; payloads are left to the mappers.
 */
class OpenAiDataSource(private val httpClient: HttpClient) : LlmDataSource {

    private val log = Logger.withTag("OpenAi")

    /**
     * Models (per base URL) that refused a temperature this session. The one piece of state kept
     * here: a cache of server behaviour, not configuration, so each such model costs one retry, not one per call.
     */
    private val temperatureRejectedBy: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override suspend fun generate(
        prompt: LlmPrompt,
        connection: LlmConnection,
    ): Outcome<String, TransformError> = runLlmCall(log, "chat/completions") {
        complete(prompt, connection, includeTemperature = connection.temperatureKey !in temperatureRejectedBy)
    }

    override suspend fun listModels(endpoint: LlmEndpoint): Outcome<List<AiModel>, TransformError> =
        runLlmCall(log, "models") {
            val response = httpClient.get("${endpoint.baseUrl}/models") { bearerAuth(endpoint.apiKey) }
            if (response.status.isSuccess()) {
                Outcome.Success(OpenAiModelListMapper.map(response.body()))
            } else {
                httpFailure(response, "models", response.errorBody())
            }
        }

    private suspend fun complete(
        prompt: LlmPrompt,
        connection: LlmConnection,
        includeTemperature: Boolean,
    ): Outcome<String, TransformError> {
        val response = httpClient.post("${connection.baseUrl}/chat/completions") {
            bearerAuth(connection.apiKey)
            contentType(ContentType.Application.Json)
            setBody(OpenAiRequestMapper.map(prompt, connection.modelId, includeTemperature))
        }
        if (response.status.isSuccess()) return OpenAiResponseMapper.map(response.body())

        val error = response.errorBody()
        if (includeTemperature && OpenAiErrorMapper.rejectsTemperature(response.status, error)) {
            log.i { "Model rejected temperature; retrying without it" }
            temperatureRejectedBy += connection.temperatureKey
            return complete(prompt, connection, includeTemperature = false)
        }
        return httpFailure(response, "chat/completions", error)
    }

    private suspend fun HttpResponse.errorBody(): OpenAiErrorBodyDto? =
        runCatching { body<OpenAiErrorResponseDto>().error }.getOrNull()

    private fun httpFailure(
        response: HttpResponse,
        operation: String,
        error: OpenAiErrorBodyDto?,
    ): Outcome.Failure<TransformError> {
        // Only the status and error type are logged; the message can echo user text.
        log.w { "$operation failed: HTTP ${response.status.value} ${error?.type.orEmpty()}" }
        return Outcome.Failure(OpenAiErrorMapper.fromHttpError(response.status))
    }

    private val LlmConnection.temperatureKey get() = "$baseUrl|$modelId"
}
