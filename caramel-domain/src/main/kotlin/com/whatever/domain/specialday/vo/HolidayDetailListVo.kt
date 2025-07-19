package com.whatever.domain.specialday.vo

data class HolidayDetailListVo(
    val holidayList: List<HolidayDetailVo>,
) {
    companion object {
        fun from(holidayList: List<HolidayDetailVo>): HolidayDetailListVo {
            return HolidayDetailListVo(holidayList = holidayList)
        }
    }
}


