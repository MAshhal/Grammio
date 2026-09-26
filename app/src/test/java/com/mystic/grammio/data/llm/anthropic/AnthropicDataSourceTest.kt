package com.mystic.grammio.data.llm.anthropic

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.network.HttpClientFactory
import com.mystic.grammio.domain.error.TransformError
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
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AnthropicDataSourceTest {

    private val prompt = LlmPrompt(systemInstruction = "SYSTEM", userText = "<text>\nhi\n</text>", temperature = 0.1)
    private val connection =
        LlmConnection(apiKey = "test-key", modelId = "test-model", baseUrl = "https://claude.test/v1")
    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        AnthropicDataSource(
            httpClient = HttpClientFactory.create(
                enableLogging = false,
                engine = MockEngine { request ->
                    requests += request
                    handler(request)
                },
            ),
        )

    private suspend fun AnthropicDataSource.generate() = generate(prompt, connection)

    private fun MockRequestHandleScope.json(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun MockRequestHandleScope.error(
        status: HttpStatusCode,
        type: String,
        message: String = "x",
    ) = json("""{"type":"error","error":{"type":"$type","message":"$message"}}""", status)

    @Test
    fun `request carries key, version, model, system prompt and temperature`() = runTest {
        val dataSource = dataSource { json("""{"content":[{"type":"text","text":"ok"}],"stop_reason":"end_turn"}""") }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Success("ok"))

        val request = requests.single()
        assertThat(request.url.toString()).isEqualTo("https://claude.test/v1/messages")
        assertThat(request.headers["x-api-key"]).isEqualTo("test-key")
        assertThat(request.headers["anthropic-version"]).isEqualTo("2023-06-01")
        val body = request.body.toByteArray().decodeToString()
        assertThat(body).contains("\"model\":\"test-model\"")
        assertThat(body).contains("\"system\":\"SYSTEM\"")
        assertThat(body).contains("\"max_tokens\":")
        assertThat(body).contains("\"temperature\":0.1")
    }

    @Test
    fun `only text blocks are joined, thinking is skipped`() = runTest {
        val dataSource = dataSource {
            json(
                """{"content":[{"type":"thinking","thinking":"","signature":"s"},
                   {"type":"text","text":"Hello "},{"type":"text","text":"world"}],"stop_reason":"end_turn"}""",
            )
        }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Success("Hello world"))
    }

    @Test
    fun `refusal maps to content blocked`() = runTest {
        val dataSource = dataSource { json("""{"content":[],"stop_reason":"refusal"}""") }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Failure(TransformError.ContentBlocked))
    }

    @Test
    fun `rejected temperature is retried once without it`() = runTest {
        val dataSource = dataSource {
            if (requests.size == 1) {
                error(HttpStatusCode.BadRequest, "invalid_request_error", "temperature is not supported for this model")
            } else {
                json("""{"content":[{"type":"text","text":"ok"}],"stop_reason":"end_turn"}""")
            }
        }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Success("ok"))
        assertThat(requests).hasSize(2)
        assertThat(requests[1].body.toByteArray().decodeToString()).doesNotContain("temperature")
    }

    @Test
    fun `error types map to domain errors`() = runTest {
        val cases = listOf(
            Triple(HttpStatusCode.Unauthorized, "authentication_error", TransformError.InvalidApiKey),
            Triple(HttpStatusCode.TooManyRequests, "rate_limit_error", TransformError.RateLimited),
            Triple(HttpStatusCode(529, "Overloaded"), "overloaded_error", TransformError.ServiceUnavailable),
            Triple(HttpStatusCode.NotFound, "not_found_error", TransformError.Unknown),
            Triple(HttpStatusCode.BadRequest, "invalid_request_error", TransformError.Unknown),
        )
        cases.forEach { (status, type, expected) ->
            assertThat(dataSource { error(status, type) }.generate()).isEqualTo(Outcome.Failure(expected))
        }
    }

    @Test
    fun `timeouts map to timeout`() = runTest {
        val dataSource = dataSource { throw HttpRequestTimeoutException(it.url.toString(), 30_000) }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Failure(TransformError.Timeout))
    }
}
