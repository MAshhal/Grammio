package com.mystic.grammio.data.apikey

import com.mystic.grammio.data.apikey.local.ApiKeyLocalDataSource
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.Flow

/** Exposes the stored keys to the app without ever handing a key itself back. */
class ApiKeyRepositoryImpl(private val localDataSource: ApiKeyLocalDataSource) : ApiKeyRepository {

    override fun hasApiKey(provider: AiProvider): Flow<Boolean> = localDataSource.hasKey(provider)

    override suspend fun save(
        provider: AiProvider,
        apiKey: String,
    ) = localDataSource.write(provider, apiKey)

    override suspend fun clear(provider: AiProvider) = localDataSource.clear(provider)
}
