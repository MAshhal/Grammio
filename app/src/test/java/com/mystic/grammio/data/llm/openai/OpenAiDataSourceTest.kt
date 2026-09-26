package com.mystic.grammio.data.llm.openai

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
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OpenAiDataSourceTest {

    private val prompt = LlmPrompt(systemInstruction = "SYSTEM", userText = "<text>\nhi\n</text>", temperature = 0.1)
    private val connection = LlmConnection(apiKey = "test-key", modelId = "test-model", baseUrl = "https://llm.test/v1")
    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        OpenAiDataSource(
            httpClient = HttpClientFactory.create(
                enableLogging = false,
                engine = MockEngine { request ->
                    requests += request
                    handler(request)
                },
            ),
        )

    private suspend fun OpenAiDataSource.generate() = generate(prompt, connection)

    private fun MockRequestHandleScope.json(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun reply(
        content: String?,
        finishReason: String = "stop",
    ) = """{"choices":[{"index":0,"message":{"role":"assistant","content":${content?.let { "\"$it\"" }}},
          "finish_reason":"$finishReason"}]}"""

    @Test
    fun `request goes to the connection's base URL with bearer auth, model and prompt`() = runTest {
        val dataSource = dataSource { json(reply("ok")) }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Success("ok"))

        val request = requests.single()
        assertThat(request.url.toString()).isEqualTo("https://llm.test/v1/chat/completions")
        assertThat(request.headers[HttpHeaders.Authorization]).isEqualTo("Bearer test-key")
        val body = request.body.toByteArray().decodeToString()
        assertThat(body).contains("\"model\":\"test-model\"")
        assertThat(body).contains("{\"role\":\"system\",\"content\":\"SYSTEM\"}")
        assertThat(body).contains("\"role\":\"user\"")
        assertThat(body).contains("\"temperature\":0.1")
    }

    @Test
    fun `rejected temperature is retried once without it`() = runTest {
        val dataSource = dataSource {
            if (requests.size == 1) {
                json(
                    """{"error":{"message":"Unsupported value: 'temperature' does not support 0.1.",
                       "type":"invalid_request_error","param":"temperature","code":"unsupported_value"}}""",
                    HttpStatusCode.BadRequest,
                )
            } else {
                json(reply("ok"))
            }
        }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Success("ok"))
        assertThat(requests).hasSize(2)
        assertThat(requests[1].body.toByteArray().decodeToString()).doesNotContain("temperature")

        // Remembered for this model, so the next call skips the doomed first attempt.
        assertThat(dataSource.generate()).isEqualTo(Outcome.Success("ok"))
        assertThat(requests).hasSize(3)
        assertThat(requests[2].body.toByteArray().decodeToString()).doesNotContain("temperature")
    }

    @Test
    fun `other bad requests are not retried`() = runTest {
        val dataSource = dataSource {
            json("""{"error":{"message":"bad model","type":"invalid_request_error"}}""", HttpStatusCode.BadRequest)
        }

        assertThat(dataSource.generate()).isEqualTo(Outcome.Failure(TransformError.Unknown))
        assertThat(requests).hasSize(1)
    }

    @Test
    fun `content filter and refusals map to content blocked`() = runTest {
        val filtered = dataSource { json(reply(null, finishReason = "content_filter")) }
        val refused = dataSource {
            json("""{"choices":[{"message":{"content":null,"refusal":"I can't help."},"finish_reason":"stop"}]}""")
        }

        assertThat(filtered.generate()).isEqualTo(Outcome.Failure(TransformError.ContentBlocked))
        assertThat(refused.generate()).isEqualTo(Outcome.Failure(TransformError.ContentBlocked))
    }

    @Test
    fun `empty reply maps to unknown`() = runTest {
        assertThat(dataSource { json("""{"choices":[]}""") }.generate())
            .isEqualTo(Outcome.Failure(TransformError.Unknown))
        assertThat(dataSource { json(reply(" ")) }.generate())
            .isEqualTo(Outcome.Failure(TransformError.Unknown))
    }

    @Test
    fun `http statuses map to domain errors`() = runTest {
        val cases = mapOf(
            HttpStatusCode.Unauthorized to TransformError.InvalidApiKey,
            HttpStatusCode.TooManyRequests to TransformError.RateLimited,
            HttpStatusCode.InternalServerError to TransformError.ServiceUnavailable,
            HttpStatusCode.NotFound to TransformError.Unknown,
        )
        cases.forEach { (status, expected) ->
            // Compatible servers don't always send a JSON error body.
            val dataSource = dataSource { respond("nope", status) }
            assertThat(dataSource.generate()).isEqualTo(Outcome.Failure(expected))
        }
    }

    @Test
    fun `io errors map to network`() = runTest {
        assertThat(dataSource { throw IOException("no route") }.generate())
            .isEqualTo(Outcome.Failure(TransformError.Network))
    }
}
