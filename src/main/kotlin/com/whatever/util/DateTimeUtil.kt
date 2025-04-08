package com.whatever.util

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

object DateTimeUtil {
    val UTC_ZONE_ID: ZoneId = ZoneId.of("UTC")

    fun localNow(zoneId: ZoneId = UTC_ZONE_ID): LocalDateTime {
        return LocalDateTime.now(zoneId)
    }

    fun zonedNow(zoneId: ZoneId = UTC_ZONE_ID): ZonedDateTime {
        return ZonedDateTime.now(zoneId)
    }

    fun changeTimeZone(
        sourceLocalDateTime: LocalDateTime,
        sourceZone: ZoneId,
        targetZone: ZoneId = UTC_ZONE_ID,
    ): LocalDateTime {
        return ZonedDateTime.of(sourceLocalDateTime, sourceZone)
            .withZoneSameInstant(targetZone)
            .toLocalDateTime()
    }

    fun changeTimeZone(
        sourceZonedDateTime: ZonedDateTime,
        targetZone: ZoneId = UTC_ZONE_ID,
    ): ZonedDateTime {
        return sourceZonedDateTime
            .withZoneSameInstant(targetZone)
    }

    fun toDate(
        sourceLocalDateTime: LocalDateTime,
        sourceZone: ZoneId = UTC_ZONE_ID,
    ): Date {
        val instant = ZonedDateTime.of(sourceLocalDateTime, sourceZone).toInstant()
        return Date.from(instant)
    }
}

fun LocalDateTime.toDate(
    sourceZone: ZoneId = DateTimeUtil.UTC_ZONE_ID
): Date {
    return DateTimeUtil.toDate(this, sourceZone)
}

fun ZonedDateTime.toDate(): Date {
    return DateTimeUtil.toDate(toLocalDateTime(), zone)
}

val LocalDateTime.endOfDay: LocalDateTime
    get() = toLocalDate().atTime(LocalTime.MAX)

val LocalDateTime.withoutNano: LocalDateTime
    get() = withNano(0)

fun String.toZonId(): ZoneId {
    return ZoneId.of(this)
}