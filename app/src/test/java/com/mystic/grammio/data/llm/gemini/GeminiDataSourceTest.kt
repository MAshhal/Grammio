package com.mystic.grammio.data.llm.gemini

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmEndpoint
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.network.HttpClientFactory
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.result.Outcome
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GeminiDataSourceTest {

    private val prompt = LlmPrompt(systemInstruction = "SYSTEM", userText = "<text>\nhi\n</text>", temperature = 0.1)
    private val connection =
        LlmConnection(apiKey = "test-key", modelId = "test-model", baseUrl = "https://gemini.test/v1beta")
    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        GeminiDataSource(
            httpClient = HttpClientFactory.create(
                enableLogging = false,
                engine = MockEngine { request ->
                    requests += request
                    handler(request)
                },
            ),
        )

    private suspend fun GeminiDataSource.generate() = generate(prompt, connection)

    private fun MockRequestHandleScope.json(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun `success joins candidate text parts and skips thoughts`() = runTest {
        val dataSource = dataSource {
            json(
                """
                {"candidates":[{"content":{"role":"model","parts":[
                  {"text":"thinking…","thought":true},{"text":"Hello "},{"text":"world"}
                ]},"finishReason":"STOP"}],"usageMetadata":{"totalTokenCount":5}}
                """,
            )
        }

        val result = dataSource.generate()

        assertThat(result).isEqualTo(Outcome.Success("Hello world"))
    }

    @Test
    fun `request uses the connection's key, base URL and model, and the prompt`() = runTest {
        val dataSource = dataSource { json("""{"candidates":[{"content":{"parts":[{"text":"ok"}]}}]}""") }

        dataSource.generate()

        val request = requests.single()
        assertThat(request.url.toString()).isEqualTo("https://gemini.test/v1beta/models/test-model:generateContent")
        assertThat(request.url.parameters.names()).isEmpty()
        assertThat(request.headers["x-goog-api-key"]).isEqualTo("test-key")
        val body = request.body.toByteArray().decodeToString()
        assertThat(body).contains("\"systemInstruction\"")
        assertThat(body).contains("SYSTEM")
        assertThat(body).contains("\"role\":\"user\"")
        assertThat(body).contains("\"temperature\":0.1")
    }

    @Test
    fun `invalid key is recognised from the error details`() = runTest {
        val dataSource = dataSource {
            json(
                """{"error":{"code":400,"message":"API key not valid.","status":"INVALID_ARGUMENT",
                   "details":[{"@type":"type.googleapis.com/google.rpc.ErrorInfo","reason":"API_KEY_INVALID"}]}}""",
                HttpStatusCode.BadRequest,
            )
        }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Failure(TransformError.InvalidApiKey))
    }

    @Test
    fun `http statuses map to domain errors`() = runTest {
        val cases = mapOf(
            HttpStatusCode.BadRequest to TransformError.Unknown,
            HttpStatusCode.Forbidden to TransformError.InvalidApiKey,
            HttpStatusCode.TooManyRequests to TransformError.RateLimited,
            HttpStatusCode.InternalServerError to TransformError.ServiceUnavailable,
            HttpStatusCode.ServiceUnavailable to TransformError.ServiceUnavailable,
            HttpStatusCode.NotFound to TransformError.Unknown,
        )
        cases.forEach { (status, expected) ->
            val dataSource = dataSource { json("""{"error":{"code":${status.value},"status":"X"}}""", status) }
            assertThat(dataSource.generate()).isEqualTo(Outcome.Failure(expected))
        }
    }

    @Test
    fun `blocked prompt and safety finish map to content blocked`() = runTest {
        val blockedPrompt = dataSource { json("""{"promptFeedback":{"blockReason":"SAFETY"}}""") }
        val safetyFinish = dataSource { json("""{"candidates":[{"finishReason":"SAFETY"}]}""") }

        assertThat(blockedPrompt.generate()).isEqualTo(Outcome.Failure(TransformError.ContentBlocked))
        assertThat(safetyFinish.generate()).isEqualTo(Outcome.Failure(TransformError.ContentBlocked))
    }

    @Test
    fun `empty candidates map to unknown`() = runTest {
        val dataSource = dataSource { json("""{"candidates":[]}""") }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Failure(TransformError.Unknown))
    }

    @Test
    fun `timeouts and io errors map to timeout and network`() = runTest {
        val timeout = dataSource { throw HttpRequestTimeoutException(it.url.toString(), 30_000) }
        val offline = dataSource { throw IOException("no route") }

        assertThat(timeout.generate()).isEqualTo(Outcome.Failure(TransformError.Timeout))
        assertThat(offline.generate()).isEqualTo(Outcome.Failure(TransformError.Network))
    }

    @Test
    fun `models are listed with their ids and only if they generate content`() = runTest {
        val dataSource = dataSource {
            json(
                """{"models":[
                  {"name":"models/gemini-3.5-flash-lite","displayName":"Gemini 3.5 Flash-Lite",
                   "supportedGenerationMethods":["generateContent","countTokens"]},
                  {"name":"models/text-embedding-004","supportedGenerationMethods":["embedContent"]},
                  {"name":"models/gemini-x","supportedGenerationMethods":["generateContent"]}
                ]}""",
            )
        }

        val result = dataSource.listModels(LlmEndpoint(apiKey = "test-key", baseUrl = "https://gemini.test/v1beta"))

        assertThat(result).isEqualTo(
            Outcome.Success(
                listOf(AiModel("gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite"), AiModel("gemini-x", "gemini-x")),
            ),
        )
        val request = requests.single()
        assertThat(request.url.encodedPath).isEqualTo("/v1beta/models")
        assertThat(request.headers["x-goog-api-key"]).isEqualTo("test-key")
    }
}
