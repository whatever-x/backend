package util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

object DateTimeUtil {
    val SYS_ZONE_ID = ZoneId.systemDefault()
    val UTC_ZONE_ID: ZoneId = ZoneId.of("UTC")
    val KST_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")

    @JvmStatic
    fun localNow(zoneId: ZoneId = UTC_ZONE_ID): LocalDateTime {
        return LocalDateTime.now(zoneId)
    }

    @JvmStatic
    fun zonedNow(zoneId: ZoneId = UTC_ZONE_ID): ZonedDateTime {
        return ZonedDateTime.now(zoneId)
    }

    @JvmStatic
    fun changeTimeZone(
        sourceLocalDateTime: LocalDateTime,
        sourceZone: ZoneId,
        targetZone: ZoneId = UTC_ZONE_ID,
    ): LocalDateTime {
        return ZonedDateTime.of(sourceLocalDateTime, sourceZone)
            .withZoneSameInstant(targetZone)
            .toLocalDateTime()
    }

    @JvmStatic
    fun changeTimeZone(
        sourceZonedDateTime: ZonedDateTime,
        targetZone: ZoneId = UTC_ZONE_ID,
    ): ZonedDateTime {
        return sourceZonedDateTime
            .withZoneSameInstant(targetZone)
    }

    @JvmStatic
    fun toDate(
        sourceLocalDateTime: LocalDateTime,
        sourceZone: ZoneId = UTC_ZONE_ID,
    ): Date {
        val instant = ZonedDateTime.of(sourceLocalDateTime, sourceZone).toInstant()
        return Date.from(instant)
    }

    /**
     * 같은 zone을 가진 두 시간의 Duration를 반환합니다.
     * @param startDateTime 시작 시간. null일 경우 endDateTime.zone의 현재 시간을 사용합니다.
     * @param endDateTime 종료 시간.
     * @return 두 시간의 Duration. 두 시간의 zone이 다를 경우 Duration.ZERO를 반환.
     */
    @JvmStatic
    fun getDuration(
        endDateTime: ZonedDateTime,
        startDateTime: ZonedDateTime? = null,
    ): Duration {
        val nonNullStartDateTime = startDateTime ?: zonedNow(endDateTime.zone)
        if (nonNullStartDateTime.zone != endDateTime.zone) {
            return Duration.ZERO
        }
        return Duration.between(nonNullStartDateTime, endDateTime)
    }
}

fun LocalDate.toDateTime(localTime: LocalTime = LocalTime.MIDNIGHT): LocalDateTime {
    return LocalDateTime.of(this, localTime)
}

fun LocalDateTime.toDate(
    sourceZone: ZoneId = DateTimeUtil.UTC_ZONE_ID,
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
val ZonedDateTime.withoutNano: ZonedDateTime
    get() = withNano(0)

fun String.toZoneId(): ZoneId {
    return ZoneId.of(this)
}
