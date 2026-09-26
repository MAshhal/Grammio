package com.mystic.grammio.data.llm.openai.mapper

import com.mystic.grammio.data.llm.mapper.HttpErrorMapper
import com.mystic.grammio.data.llm.openai.dto.OpenAiErrorBodyDto
import com.mystic.grammio.domain.error.TransformError
import io.ktor.http.HttpStatusCode

/** Chat Completions HTTP failures → [TransformError]. */
internal object OpenAiErrorMapper {

    fun fromHttpError(status: HttpStatusCode): TransformError = HttpErrorMapper.fromStatus(status)

    /** Reasoning models (and some compatible servers) refuse any temperature other than their default. */
    fun rejectsTemperature(
        status: HttpStatusCode,
        error: OpenAiErrorBodyDto?,
    ): Boolean = status == HttpStatusCode.BadRequest &&
        (error?.param == TEMPERATURE || error?.message.orEmpty().contains(TEMPERATURE, ignoreCase = true))

    private const val TEMPERATURE = "temperature"
}
