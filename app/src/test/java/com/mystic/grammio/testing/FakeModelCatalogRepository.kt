package com.mystic.grammio.testing

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.repository.ModelCatalogRepository
import com.mystic.grammio.domain.result.Outcome

/** Answers every listing with [result] and records which providers were asked. */
class FakeModelCatalogRepository(var result: Outcome<List<AiModel>, TransformError> = Outcome.Success(emptyList())) :
    ModelCatalogRepository {
    val requested = mutableListOf<AiProvider>()

    override suspend fun listModels(provider: AiProvider): Outcome<List<AiModel>, TransformError> {
        requested += provider
        return result
    }
}
