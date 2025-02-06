package com.whatever.util

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.Date

class DateTimeUtilTest {

    @DisplayName("UTC LocalDateTime을 생성한다.")
    @Test
    fun localNow() {
        // given
        val utcNow = DateTimeUtil.localNow()

        // when & then
        assertThat(utcNow).isCloseToUtcNow(TemporalUnitWithinOffset(1L, ChronoUnit.SECONDS))
    }

    @DisplayName("UTC ZonedDateTime을 생성한다.")
    @Test
    fun zonedNow() {
        // given
        val utcNow = DateTimeUtil.zonedNow()

        // when & then
        assertThat(utcNow.zone).isEqualTo(ZoneId.of("UTC"))
        assertThat(utcNow.toLocalDateTime()).isCloseToUtcNow(TemporalUnitWithinOffset(1L, ChronoUnit.SECONDS))
    }

    @DisplayName("LocalDateTime을 기본 targetZone(UTC)으로 변경한다.")
    @ParameterizedTest
    @ValueSource(strings = ["Asia/Seoul", "America/New_York", "Europe/London", "Australia/Sydney"])
    fun changeTimeZone_FromLocalDateTimeToUTC(zone: String) {
        // given
        val sourceZone = ZoneId.of(zone)
        val sourceNow = LocalDateTime.now(sourceZone)

        // when
        val resultDate = DateTimeUtil.changeTimeZone(sourceNow, sourceZone)

        // then
        assertThat(resultDate).isCloseToUtcNow(TemporalUnitWithinOffset(1L, ChronoUnit.SECONDS))
    }

    @DisplayName("LocalDateTime을 지정한 targetZone으로 변경한다.")
    @ParameterizedTest
    @ValueSource(strings = ["Asia/Seoul", "America/New_York", "Europe/London", "Australia/Sydney"])
    fun changeTimeZone_FromLocalDateTimeToTargetZone(zone: String) {
        // given
        val targetZone = ZoneId.of(zone)
        val expectedDate = LocalDateTime.now(targetZone)

        // when
        val resultDate = DateTimeUtil.changeTimeZone(
            sourceLocalDateTime = DateTimeUtil.localNow(),
            sourceZone = DateTimeUtil.UTC_ZONE_ID,
            targetZone = targetZone
        )

        // then
        assertThat(resultDate).isCloseTo(expectedDate, TemporalUnitWithinOffset(1, ChronoUnit.SECONDS))
    }

    @DisplayName("ZonedDateTime을 기본 targetZone(UTC)으로 변경한다.")
    @ParameterizedTest
    @ValueSource(strings = ["Asia/Seoul", "America/New_York", "Europe/London", "Australia/Sydney"])
    fun changeTimeZone_FromZonedDateTimeToUTC(zone: String) {
        // given
        val sourceZone = ZoneId.of(zone)
        val sourceDate = DateTimeUtil.zonedNow(sourceZone)

        // when
        val resultDate = DateTimeUtil.changeTimeZone(sourceDate)

        // then
        assertThat(resultDate.zone).isEqualTo(ZoneId.of("UTC"))
        assertThat(resultDate.toLocalDateTime()).isCloseToUtcNow(TemporalUnitWithinOffset(1L, ChronoUnit.SECONDS))
    }

    @DisplayName("ZonedDateTime을 지정한 targetZone으로 변경한다.")
    @ParameterizedTest
    @ValueSource(strings = ["Asia/Seoul", "America/New_York", "Europe/London", "Australia/Sydney"])
    fun changeTimeZone_FromZonedDateTimeToTargetZone(zone: String) {
        // given
        val sourceDate = DateTimeUtil.zonedNow()

        val targetZone = ZoneId.of(zone)
        val expectedDate = DateTimeUtil.zonedNow(targetZone)

        // when
        val resultDate = DateTimeUtil.changeTimeZone(sourceDate, targetZone)

        // then
        assertThat(resultDate).isCloseTo(expectedDate, TemporalUnitWithinOffset(1, ChronoUnit.SECONDS))
    }

    @DisplayName("LocalDateTime을 Date(SystemDefault) 타입으로 변경한다.")
    @ParameterizedTest
    @ValueSource(strings = ["Asia/Seoul", "America/New_York", "Europe/London", "Australia/Sydney"])
    fun toDate(zoneId: String) {
        // given
        val sourceZone = ZoneId.of(zoneId)
        val sourceDate = LocalDateTime.now(sourceZone)

        val expectedDate = Date.from(Instant.now())

        // when
        val resultDate = DateTimeUtil.toDate(sourceDate, sourceZone)

        // then
        assertThat(resultDate)
            .isCloseTo(
                expectedDate,
                Duration.ofSeconds(1).toMillis()
            )
    }

}