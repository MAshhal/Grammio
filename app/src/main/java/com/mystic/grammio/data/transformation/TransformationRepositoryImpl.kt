package com.mystic.grammio.data.transformation

import com.mystic.grammio.data.transformation.local.TransformationLocalDataSource
import com.mystic.grammio.data.transformation.local.TransformationPreferencesLocalDataSource
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.repository.TransformationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Stored transformations. The first time anything reads or changes them, the built-in ones are
 * added; a flag remembers that, so a user who deletes every transformation isn't handed the defaults again.
 */
class TransformationRepositoryImpl(
    private val localDataSource: TransformationLocalDataSource,
    private val preferences: TransformationPreferencesLocalDataSource,
) : TransformationRepository {

    private val seedLock = Mutex()

    @Volatile
    private var isSeeded = false

    override val transformations: Flow<List<Transformation>> = flow {
        seedDefaultsOnce()
        emitAll(localDataSource.transformations)
    }

    override suspend fun transformation(id: String): Transformation? {
        seedDefaultsOnce()
        return localDataSource.get(id)
    }

    override suspend fun save(transformation: Transformation) {
        seedDefaultsOnce()
        if (localDataSource.get(transformation.id) == null) {
            localDataSource.insert(transformation)
        } else {
            localDataSource.update(transformation)
        }
    }

    override suspend fun delete(id: String) {
        seedDefaultsOnce()
        localDataSource.delete(id)
    }

    override suspend fun moveUp(id: String) = move(id, offset = -1)

    override suspend fun moveDown(id: String) = move(id, offset = 1)

    override suspend fun setEnabled(
        id: String,
        enabled: Boolean,
    ) {
        seedDefaultsOnce()
        localDataSource.setEnabled(id, enabled)
    }

    override suspend fun restoreDefaults() {
        seedDefaultsOnce()
        DefaultTransformations.all.forEach { save(it) }
    }

    private suspend fun move(
        id: String,
        offset: Int,
    ) {
        seedDefaultsOnce()
        val ids = localDataSource.transformations.first().map { it.id }.toMutableList()
        val from = ids.indexOf(id)
        val to = from + offset
        if (from == -1 || to !in ids.indices) return
        ids[from] = ids[to].also { ids[to] = ids[from] }
        localDataSource.setOrder(ids)
    }

    private suspend fun seedDefaultsOnce() {
        if (isSeeded) return
        seedLock.withLock {
            if (isSeeded) return
            if (!preferences.areDefaultsSeeded()) {
                DefaultTransformations.all
                    .filter { localDataSource.get(it.id) == null }
                    .forEach { localDataSource.insert(it) }
                preferences.markDefaultsSeeded()
            }
            isSeeded = true
        }
    }
}
