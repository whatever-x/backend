package com.whatever.domain.calendarevent.specialday.client.dto.request

import java.time.YearMonth

data class HolidayInfoRequestParams(
    val solYear: Int,
    val solMonth: String,
    val ServiceKey: String,
    val numOfRows: Int = 100
) {
    val _type: String = "json"

    companion object {
        fun fromYearMonth(
            yearMonth: YearMonth,
            serviceKey: String,
        ): HolidayInfoRequestParams {
            return HolidayInfoRequestParams(
                solYear = yearMonth.year,
                solMonth = String.format("%02d", yearMonth.monthValue),
                ServiceKey = serviceKey,
            )
        }
    }
}