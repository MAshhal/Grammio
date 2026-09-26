package com.mystic.grammio.data.transform.log.local

import com.mystic.grammio.data.transform.log.TransformationLogEntry

/** On-device log of every transformation attempt. */
interface TransformationLogLocalDataSource {
    /** Best effort: a failure to record is logged and swallowed, never surfaced to the caller. */
    suspend fun record(entry: TransformationLogEntry)
}
