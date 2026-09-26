package com.mystic.grammio.data.transformation.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mystic.grammio.data.db.GrammioDatabase
import com.mystic.grammio.data.transformation.mapper.toDomain
import com.mystic.grammio.data.transformation.mapper.toSqlFlag
import com.mystic.grammio.data.transformation.storageKey
import com.mystic.grammio.domain.model.Transformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Transformations in the app's SQLite database. */
class SqlDelightTransformationLocalDataSource(private val database: GrammioDatabase) :
    TransformationLocalDataSource {

    private val queries get() = database.transformationQueries

    override val transformations: Flow<List<Transformation>> = queries.selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toDomain() } }

    override suspend fun get(id: String): Transformation? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insert(transformation: Transformation) {
        withContext(Dispatchers.IO) {
            with(transformation) {
                queries.insert(id, name, taskPrompt, icon.storageKey, isEnabled.toSqlFlag(), temperature)
            }
        }
    }

    override suspend fun update(transformation: Transformation) {
        withContext(Dispatchers.IO) {
            with(transformation) {
                queries.update(
                    name = name,
                    task_prompt = taskPrompt,
                    icon = icon.storageKey,
                    is_enabled = isEnabled.toSqlFlag(),
                    temperature = temperature,
                    id = id,
                )
            }
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) { queries.delete(id) }
    }

    override suspend fun setEnabled(
        id: String,
        enabled: Boolean,
    ) {
        withContext(Dispatchers.IO) { queries.setEnabled(enabled.toSqlFlag(), id) }
    }

    override suspend fun setOrder(ids: List<String>) {
        withContext(Dispatchers.IO) {
            queries.transaction {
                ids.forEachIndexed { index, id -> queries.setPosition(index.toLong(), id) }
            }
        }
    }
}
