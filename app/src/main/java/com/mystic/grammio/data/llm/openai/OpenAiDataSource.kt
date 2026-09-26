package com.mystic.grammio.data.llm.openai

import co.touchlab.kermit.Logger
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.mapper.HttpErrorMapper
import com.mystic.grammio.data.llm.openai.dto.OpenAiErrorResponseDto
import com.mystic.grammio.data.llm.openai.mapper.OpenAiErrorMapper
import com.mystic.grammio.data.llm.openai.mapper.OpenAiRequestMapper
import com.mystic.grammio.data.llm.openai.mapper.OpenAiResponseMapper
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/**
 * The OpenAI Chat Completions API, which also serves every OpenAI-compatible endpoint: only the
 * base URL in the [LlmConnection] differs. Only performs the HTTP call; payloads are left to the mappers.
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
    ): Outcome<String, TransformError> = try {
        complete(prompt, connection, includeTemperature = connection.temperatureKey !in temperatureRejectedBy)
    } catch (e: HttpRequestTimeoutException) {
        // Caught before CancellationException, which some Ktor versions use as its supertype.
        exceptionFailure(e)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        exceptionFailure(e)
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

        val error = runCatching { response.body<OpenAiErrorResponseDto>().error }.getOrNull()
        if (includeTemperature && OpenAiErrorMapper.rejectsTemperature(response.status, error)) {
            log.i { "Model rejected temperature; retrying without it" }
            temperatureRejectedBy += connection.temperatureKey
            return complete(prompt, connection, includeTemperature = false)
        }
        // Only the status and error type are logged; the message can echo user text.
        log.w { "chat/completions failed: HTTP ${response.status.value} ${error?.type.orEmpty()}" }
        return Outcome.Failure(OpenAiErrorMapper.fromHttpError(response.status))
    }

    private val LlmConnection.temperatureKey get() = "$baseUrl|$modelId"

    private fun exceptionFailure(e: Exception): Outcome.Failure<TransformError> {
        log.w { "chat/completions failed: ${e::class.simpleName}" }
        return Outcome.Failure(HttpErrorMapper.fromException(e))
    }
}
