package com.mystic.grammio.data.llm.gemini

import com.google.common.truth.Truth.assertThat
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
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GeminiProviderTest {

    private val prompt = LlmPrompt(systemInstruction = "SYSTEM", userText = "<text>\nhi\n</text>", temperature = 0.1)
    private val requests = mutableListOf<HttpRequestData>()

    private fun provider(
        apiKey: String? = "test-key",
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ) = GeminiProvider(
        httpClient = HttpClientFactory.create(
            enableLogging = false,
            engine = MockEngine { request ->
                requests += request
                handler(request)
            },
        ),
        apiKeyProvider = { apiKey },
    )

    private fun MockRequestHandleScope.json(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun `success joins candidate text parts and skips thoughts`() = runTest {
        val provider = provider {
            json(
                """
                {"candidates":[{"content":{"role":"model","parts":[
                  {"text":"thinking…","thought":true},{"text":"Hello "},{"text":"world"}
                ]},"finishReason":"STOP"}],"usageMetadata":{"totalTokenCount":5}}
                """,
            )
        }

        val result = provider.generate(prompt)

        assertThat(result).isEqualTo(Outcome.Success("Hello world"))
    }

    @Test
    fun `request uses the key header, model endpoint and prompt`() = runTest {
        val provider = provider { json("""{"candidates":[{"content":{"parts":[{"text":"ok"}]}}]}""") }

        provider.generate(prompt)

        val request = requests.single()
        assertThat(request.url.toString()).endsWith("/v1beta/models/${GeminiProvider.MODEL}:generateContent")
        assertThat(request.url.parameters.names()).isEmpty()
        assertThat(request.headers["x-goog-api-key"]).isEqualTo("test-key")
        val body = request.body.toByteArray().decodeToString()
        assertThat(body).contains("\"systemInstruction\"")
        assertThat(body).contains("SYSTEM")
        assertThat(body).contains("\"role\":\"user\"")
        assertThat(body).contains("\"temperature\":0.1")
    }

    @Test
    fun `missing key fails without a network call`() = runTest {
        val provider = provider(apiKey = null) { error("should not be called") }

        assertThat(provider.generate(prompt)).isEqualTo(Outcome.Failure(TransformError.MissingApiKey))
        assertThat(requests).isEmpty()
    }

    @Test
    fun `invalid key is recognised from the error details`() = runTest {
        val provider = provider {
            json(
                """{"error":{"code":400,"message":"API key not valid.","status":"INVALID_ARGUMENT",
                   "details":[{"@type":"type.googleapis.com/google.rpc.ErrorInfo","reason":"API_KEY_INVALID"}]}}""",
                HttpStatusCode.BadRequest,
            )
        }

        assertThat(provider.generate(prompt)).isEqualTo(Outcome.Failure(TransformError.InvalidApiKey))
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
            val provider = provider { json("""{"error":{"code":${status.value},"status":"X"}}""", status) }
            assertThat(provider.generate(prompt)).isEqualTo(Outcome.Failure(expected))
        }
    }

    @Test
    fun `blocked prompt and safety finish map to content blocked`() = runTest {
        val blockedPrompt = provider { json("""{"promptFeedback":{"blockReason":"SAFETY"}}""") }
        val safetyFinish = provider { json("""{"candidates":[{"finishReason":"SAFETY"}]}""") }

        assertThat(blockedPrompt.generate(prompt)).isEqualTo(Outcome.Failure(TransformError.ContentBlocked))
        assertThat(safetyFinish.generate(prompt)).isEqualTo(Outcome.Failure(TransformError.ContentBlocked))
    }

    @Test
    fun `empty candidates map to unknown`() = runTest {
        val provider = provider { json("""{"candidates":[]}""") }

        assertThat(provider.generate(prompt)).isEqualTo(Outcome.Failure(TransformError.Unknown))
    }

    @Test
    fun `timeouts and io errors map to timeout and network`() = runTest {
        val timeout = provider { throw HttpRequestTimeoutException(it.url.toString(), 30_000) }
        val offline = provider { throw IOException("no route") }

        assertThat(timeout.generate(prompt)).isEqualTo(Outcome.Failure(TransformError.Timeout))
        assertThat(offline.generate(prompt)).isEqualTo(Outcome.Failure(TransformError.Network))
    }
}
