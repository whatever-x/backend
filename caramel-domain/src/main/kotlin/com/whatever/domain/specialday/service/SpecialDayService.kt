package com.whatever.domain.specialday.service

import com.whatever.caramel.common.global.exception.externalserver.specialday.SpecialDayApiExceptionCode.EMPTY_HEADER
import com.whatever.caramel.common.global.exception.externalserver.specialday.SpecialDayApiExceptionCode.FAILED_RESPONSE_CODE
import com.whatever.caramel.common.global.exception.externalserver.specialday.SpecialDayApiExceptionCode.RESPONSE_TYPE_UNMATCHED
import com.whatever.caramel.common.global.exception.externalserver.specialday.SpecialDayDecodeException
import com.whatever.caramel.common.global.exception.externalserver.specialday.SpecialDayFailedOperationException
import com.whatever.caramel.infrastructure.client.SpecialDayApiFeignClient
import com.whatever.caramel.infrastructure.client.dto.HolidayApiResponse
import com.whatever.caramel.infrastructure.client.dto.HolidayInfoRequestParams
import com.whatever.caramel.infrastructure.properties.SpecialDayApiProperties
import com.whatever.domain.specialday.model.SpecialDayType
import com.whatever.domain.specialday.repository.SpecialDayRepository
import com.whatever.domain.specialday.vo.HolidayDetailListVo
import com.whatever.domain.specialday.vo.HolidayDetailVo
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.websocket.DecodeException
import org.springframework.stereotype.Service
import java.time.MonthDay
import java.time.Year
import java.time.YearMonth

private val logger = KotlinLogging.logger { }

@Service
class SpecialDayService(
    private val apiProperties: SpecialDayApiProperties,
    private val specialDayFeignClient: SpecialDayApiFeignClient,
    private val specialDayRepository: SpecialDayRepository,
) {
    companion object {
        private const val HOLIDAY_INFO_SUCCESS_CODE = "00"
    }

    fun getHolidaysInYear(year: Year): HolidayDetailListVo {
        val startDate = year.atMonthDay(MonthDay.of(1, 1))
        val endDate = year.atMonthDay(MonthDay.of(12, 31))
        val holidays = specialDayRepository.findAllByTypeAndBetweenStartDateAndEndDate(
            type = SpecialDayType.HOLI,
            startDate = startDate,
            endDate = endDate,
        )

        val holidayDetailVoList = holidays.mapNotNull { HolidayDetailVo.from(it) }
        return HolidayDetailListVo.from(holidayList = holidayDetailVoList)
    }

    fun getHolidays(
        startYearMonth: YearMonth,
        endYearMonth: YearMonth,
    ): HolidayDetailListVo {
        val startDate = startYearMonth.atDay(1)
        var endDate = endYearMonth.atEndOfMonth()
        if (endDate.isBefore(startDate)) {
            endDate = startYearMonth.atEndOfMonth()
        }
        val holidays = specialDayRepository.findAllByTypeAndBetweenStartDateAndEndDate(
            type = SpecialDayType.HOLI,
            startDate = startDate,
            endDate = endDate,
        )

        val holidayDetailVoList = holidays.mapNotNull { HolidayDetailVo.from(it) }
        return HolidayDetailListVo.from(holidayList = holidayDetailVoList)
    }

    private fun getHolidayInfo(yearMonth: YearMonth): HolidayApiResponse {
        val req = HolidayInfoRequestParams.fromYearMonth(
            yearMonth = yearMonth,
            serviceKey = apiProperties.key,
        )

        return runCatching {
            specialDayFeignClient.getHolidayInfo(req).apply {
                val header = response?.header
                    ?: throw SpecialDayDecodeException(errorCode = EMPTY_HEADER)

                if (header.resultCode != HOLIDAY_INFO_SUCCESS_CODE) {
                    throw SpecialDayFailedOperationException(
                        errorCode = FAILED_RESPONSE_CODE,
                        resultCode = header.resultCode,
                        resultMsg = header.resultMsg,
                    )
                }
            }
        }.onFailure { e ->
            when (e) {
                is SpecialDayFailedOperationException -> {
                    logger.error(e) { "getHolidayInfo failed. SpecialDayApi returned error. Code: ${e.resultCode}, Message: ${e.resultMsg}" }
                }

                is SpecialDayDecodeException -> {
                    logger.error(e) { "getHolidayInfo failed due to internal decoding logic." }
                }

                is DecodeException -> {
                    logger.error(e) { "getHolidayInfo failed due to Feign decoding error." }
                    throw SpecialDayDecodeException(errorCode = RESPONSE_TYPE_UNMATCHED)
                }

                else -> {
                    logger.error(e) { "getHolidayInfo failed due to unexpected exception." }
                }
            }
        }.getOrThrow()
    }
}
