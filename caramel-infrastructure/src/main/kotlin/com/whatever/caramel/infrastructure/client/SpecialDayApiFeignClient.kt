package com.whatever.caramel.infrastructure.client

import com.whatever.domain.calendarevent.specialday.client.dto.request.HolidayInfoRequestParams
import com.whatever.domain.calendarevent.specialday.client.dto.response.HolidayApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(
    name = "SpecialDayClient",
    url = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService",
)
interface SpecialDayApiFeignClient {
    @GetMapping(
        path = ["/getHoliDeInfo"],
    )
    fun getHolidayInfo(
        @SpringQueryMap queryParams: HolidayInfoRequestParams,
    ): HolidayApiResponse
}
