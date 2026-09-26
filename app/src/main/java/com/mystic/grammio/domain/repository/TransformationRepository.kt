package com.mystic.grammio.domain.repository

import com.mystic.grammio.domain.model.Transformation
import kotlinx.coroutines.flow.Flow

/** The transformations the user can pick from: the built-in ones as they left them, plus their own. */
interface TransformationRepository {
    /** Every transformation, enabled or not, in the order the user arranged them. */
    val transformations: Flow<List<Transformation>>

    suspend fun transformation(id: String): Transformation?

    /** Updates the transformation with the same id, or adds it at the end. */
    suspend fun save(transformation: Transformation)

    suspend fun delete(id: String)

    /** Swaps it with the one before it; the first stays put. */
    suspend fun moveUp(id: String)

    /** Swaps it with the one after it; the last stays put. */
    suspend fun moveDown(id: String)

    suspend fun setEnabled(
        id: String,
        enabled: Boolean,
    )

    /** Puts the built-in transformations back as they shipped, turned on. The user's own ones stay. */
    suspend fun restoreDefaults()
}
