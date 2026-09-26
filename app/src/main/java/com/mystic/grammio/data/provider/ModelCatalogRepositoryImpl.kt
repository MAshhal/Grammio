package com.mystic.grammio.data.provider

import com.mystic.grammio.data.llm.LlmDataSourceRegistry
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.repository.ModelCatalogRepository
import com.mystic.grammio.domain.result.Outcome

/** Asks the provider's API which models the stored key can use. */
class ModelCatalogRepositoryImpl(
    private val connectionResolver: ProviderConnectionResolver,
    private val llmDataSources: LlmDataSourceRegistry,
) : ModelCatalogRepository {

    override suspend fun listModels(provider: AiProvider): Outcome<List<AiModel>, TransformError> =
        when (val endpoint = connectionResolver.endpoint(provider)) {
            is Outcome.Failure -> endpoint
            is Outcome.Success -> llmDataSources.forProvider(provider).listModels(endpoint.value)
        }
}
