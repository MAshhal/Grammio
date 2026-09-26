package com.mystic.grammio.data.llm.anthropic.mapper

import com.mystic.grammio.data.llm.anthropic.dto.AnthropicErrorBodyDto
import com.mystic.grammio.data.llm.mapper.HttpErrorMapper
import com.mystic.grammio.domain.error.TransformError
import io.ktor.http.HttpStatusCode

/** Messages API HTTP failures → [TransformError]. The error type is more precise than the status. */
internal object AnthropicErrorMapper {

    fun fromHttpError(
        status: HttpStatusCode,
        error: AnthropicErrorBodyDto?,
    ): TransformError = when (error?.type) {
        "authentication_error", "permission_error" -> TransformError.InvalidApiKey
        "rate_limit_error" -> TransformError.RateLimited
        "overloaded_error", "api_error" -> TransformError.ServiceUnavailable
        else -> HttpErrorMapper.fromStatus(status)
    }

    /** Newer Claude models reject sampling parameters outright. */
    fun rejectsTemperature(
        status: HttpStatusCode,
        error: AnthropicErrorBodyDto?,
    ): Boolean = status == HttpStatusCode.BadRequest &&
        error?.message.orEmpty().contains(TEMPERATURE, ignoreCase = true)

    private const val TEMPERATURE = "temperature"
}
