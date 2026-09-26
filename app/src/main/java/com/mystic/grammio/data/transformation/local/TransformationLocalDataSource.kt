package com.mystic.grammio.data.transformation.local

import com.mystic.grammio.domain.model.Transformation
import kotlinx.coroutines.flow.Flow

/** Stored transformations, in display order. */
interface TransformationLocalDataSource {
    val transformations: Flow<List<Transformation>>

    suspend fun get(id: String): Transformation?

    /** Adds [transformation] after all the others. */
    suspend fun insert(transformation: Transformation)

    /** Replaces everything but the position of the transformation with the same id. */
    suspend fun update(transformation: Transformation)

    suspend fun delete(id: String)

    suspend fun setEnabled(
        id: String,
        enabled: Boolean,
    )

    /** Renumbers the transformations in [ids] in that order, in one transaction. */
    suspend fun setOrder(ids: List<String>)
}
