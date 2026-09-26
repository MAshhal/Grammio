package com.mystic.grammio.testing

import com.mystic.grammio.data.transform.log.TransformationLogEntry
import com.mystic.grammio.data.transform.log.local.TransformationLogLocalDataSource

/** Keeps recorded entries in memory, in order. */
class RecordingTransformationLogLocalDataSource : TransformationLogLocalDataSource {
    val entries = mutableListOf<TransformationLogEntry>()

    override suspend fun record(entry: TransformationLogEntry) {
        entries += entry
    }
}
