package com.mystic.grammio.testing

import kotlin.time.Clock
import kotlin.time.Instant

/** A clock stopped at [now]. */
class FixedClock(var now: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)) : Clock {
    override fun now(): Instant = now
}
