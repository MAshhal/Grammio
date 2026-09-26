package com.mystic.grammio.testing

import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.LlmEndpoint
import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.result.Outcome

/** Returns canned replies and records what it was called with. */
class StubLlmDataSource(
    var reply: Outcome<String, TransformError> = Outcome.Success("reply"),
    var models: Outcome<List<AiModel>, TransformError> = Outcome.Success(emptyList()),
) : LlmDataSource {
    var lastPrompt: LlmPrompt? = null
    var lastConnection: LlmConnection? = null
    var lastEndpoint: LlmEndpoint? = null

    override suspend fun generate(
        prompt: LlmPrompt,
        connection: LlmConnection,
    ) = reply.also {
        lastPrompt = prompt
        lastConnection = connection
    }

    override suspend fun listModels(endpoint: LlmEndpoint) = models.also { lastEndpoint = endpoint }
}
