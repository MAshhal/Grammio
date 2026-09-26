package com.mystic.grammio.domain.repository

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.result.Outcome

/** The models a provider offers to the user's key. Fails the same ways a transformation's call can. */
interface ModelCatalogRepository {
    suspend fun listModels(provider: AiProvider): Outcome<List<AiModel>, TransformError>
}
