package com.mystic.grammio.data.network

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    /** Headers that carry secrets and must never reach logs. */
    val SENSITIVE_HEADERS = setOf("x-goog-api-key", "x-api-key", "authorization")

    fun create(
        enableLogging: Boolean,
        engine: HttpClientEngine = OkHttp.create(),
    ): HttpClient = HttpClient(engine) {
        // Status codes are mapped to domain errors explicitly by each API client.
        expectSuccess = false

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 30_000
        }
        if (enableLogging) {
            install(Logging) {
                // INFO = method, URL and status only. Never BODY: bodies contain the user's text.
                level = LogLevel.INFO
                logger = object : KtorLogger {
                    private val log = Logger.withTag("Http")
                    override fun log(message: String) = log.d { message }
                }
                sanitizeHeader { it.lowercase() in SENSITIVE_HEADERS }
            }
        }
    }
}
