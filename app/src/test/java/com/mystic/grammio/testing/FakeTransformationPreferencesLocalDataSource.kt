package com.mystic.grammio.testing

import com.mystic.grammio.data.transformation.local.TransformationPreferencesLocalDataSource

/** The seeded-defaults flag held in memory. */
class FakeTransformationPreferencesLocalDataSource(var seeded: Boolean = false) :
    TransformationPreferencesLocalDataSource {
    override suspend fun areDefaultsSeeded(): Boolean = seeded

    override suspend fun markDefaultsSeeded() {
        seeded = true
    }
}
