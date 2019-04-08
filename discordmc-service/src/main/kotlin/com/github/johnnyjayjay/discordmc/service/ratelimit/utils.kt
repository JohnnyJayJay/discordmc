package com.github.johnnyjayjay.discordmc.service.ratelimit

import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

// Util to convert TimeUnit to ChronoUnit
internal fun chronoUnitOf(unit: TimeUnit): ChronoUnit = when(unit) {
    TimeUnit.NANOSECONDS -> ChronoUnit.NANOS
    TimeUnit.MICROSECONDS -> ChronoUnit.MICROS
    TimeUnit.MILLISECONDS -> ChronoUnit.MILLIS
    TimeUnit.SECONDS -> ChronoUnit.SECONDS
    TimeUnit.MINUTES -> ChronoUnit.MINUTES
    TimeUnit.HOURS -> ChronoUnit.HOURS
    TimeUnit.DAYS -> ChronoUnit.DAYS
}