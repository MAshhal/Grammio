package com.mystic.grammio.data.llm.gemini

import co.touchlab.kermit.Logger
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.LlmEndpoint
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.gemini.dto.GeminiErrorResponseDto
import com.mystic.grammio.data.llm.gemini.mapper.GeminiErrorMapper
import com.mystic.grammio.data.llm.gemini.mapper.GeminiModelListMapper
import com.mystic.grammio.data.llm.gemini.mapper.GeminiRequestMapper
import com.mystic.grammio.data.llm.gemini.mapper.GeminiResponseMapper
import com.mystic.grammio.data.llm.runLlmCall
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.result.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Google Gemini via the REST API, authenticated with the user's own key.
 * Only performs the HTTP calls; building and reading payloads is left to the mappers.
 */
class GeminiDataSource(private val httpClient: HttpClient) : LlmDataSource {

    private val log = Logger.withTag("Gemini")

    override suspend fun generate(
        prompt: LlmPrompt,
        connection: LlmConnection,
    ): Outcome<String, TransformError> = runLlmCall(log, "generateContent") {
        val response = httpClient.post("${connection.baseUrl}/models/${connection.modelId}:generateContent") {
            // Header rather than ?key= so the key never appears in URLs or logs.
            header(API_KEY_HEADER, connection.apiKey)
            contentType(ContentType.Application.Json)
            setBody(GeminiRequestMapper.map(prompt, MAX_OUTPUT_TOKENS))
        }
        if (response.status.isSuccess()) {
            GeminiResponseMapper.map(response.body())
        } else {
            httpFailure(response, "generateContent")
        }
    }

    override suspend fun listModels(endpoint: LlmEndpoint): Outcome<List<AiModel>, TransformError> =
        runLlmCall(log, "models.list") {
            val response = httpClient.get("${endpoint.baseUrl}/models") {
                header(API_KEY_HEADER, endpoint.apiKey)
                parameter("pageSize", MAX_PAGE_SIZE)
            }
            if (response.status.isSuccess()) {
                Outcome.Success(GeminiModelListMapper.map(response.body()))
            } else {
                httpFailure(response, "models.list")
            }
        }

    private suspend fun httpFailure(
        response: HttpResponse,
        operation: String,
    ): Outcome.Failure<TransformError> {
        val error = runCatching { response.body<GeminiErrorResponseDto>().error }.getOrNull()
        // Only the status and error codes are logged; the body can echo user text.
        log.w { "$operation failed: HTTP ${response.status.value} ${error?.status.orEmpty()}" }
        return Outcome.Failure(GeminiErrorMapper.fromHttpError(response.status, error))
    }

    private companion object {
        const val API_KEY_HEADER = "x-goog-api-key"
        const val MAX_OUTPUT_TOKENS = 8_192

        /** The API's maximum: every model fits on one page. */
        const val MAX_PAGE_SIZE = 1_000
    }
}
