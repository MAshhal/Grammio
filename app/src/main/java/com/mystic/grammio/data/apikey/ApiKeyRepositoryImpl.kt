package com.mystic.grammio.data.apikey

import com.mystic.grammio.data.apikey.local.ApiKeyLocalDataSource
import com.mystic.grammio.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.Flow

/** Exposes the stored key to the app without ever handing the key itself back. */
class ApiKeyRepositoryImpl(private val localDataSource: ApiKeyLocalDataSource) : ApiKeyRepository {

    override val hasApiKey: Flow<Boolean> = localDataSource.hasKey

    override suspend fun save(apiKey: String) = localDataSource.write(apiKey)

    override suspend fun clear() = localDataSource.clear()
}
