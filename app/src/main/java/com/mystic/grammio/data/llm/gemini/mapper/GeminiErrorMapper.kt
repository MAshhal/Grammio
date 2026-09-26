package com.mystic.grammio.data.llm.gemini.mapper

import com.mystic.grammio.data.llm.gemini.dto.GeminiErrorBodyDto
import com.mystic.grammio.data.llm.mapper.HttpErrorMapper
import com.mystic.grammio.domain.error.TransformError
import io.ktor.http.HttpStatusCode

/** Gemini HTTP failures → [TransformError]. Gemini reports a bad key as a 400 with a reason. */
internal object GeminiErrorMapper {

    fun fromHttpError(
        status: HttpStatusCode,
        error: GeminiErrorBodyDto?,
    ): TransformError = when {
        status == HttpStatusCode.BadRequest && error.hasReason(API_KEY_INVALID) -> TransformError.InvalidApiKey
        else -> HttpErrorMapper.fromStatus(status)
    }

    private fun GeminiErrorBodyDto?.hasReason(reason: String): Boolean =
        this?.details.orEmpty().any { it.reason == reason }

    private const val API_KEY_INVALID = "API_KEY_INVALID"
}
