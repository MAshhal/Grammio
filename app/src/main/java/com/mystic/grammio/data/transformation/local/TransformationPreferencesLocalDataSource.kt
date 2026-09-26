package com.mystic.grammio.data.transformation.local

/** Bookkeeping for the stored transformations. */
interface TransformationPreferencesLocalDataSource {
    /** Whether the built-in transformations were ever added, so deleting them all doesn't bring them back. */
    suspend fun areDefaultsSeeded(): Boolean

    suspend fun markDefaultsSeeded()
}
