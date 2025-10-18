package com.whatever.caramel.api.admob.controller

import com.whatever.caramel.common.global.annotation.DisableSwaggerAuthButton
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "AdMob API",
    description = "AdMob 관련 API"
)
@RestController
class AdMobController(
    @Value("\${admob.app-ads-txt}")
    private val appAdsTxt: String
) {

    @DisableSwaggerAuthButton
    @Operation(
        summary = "app-ads.txt 제공",
        description = "Google AdMob의 app-ads.txt 파일을 제공합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "app-ads.txt 반환")
        ]
    )
    @GetMapping("/app-ads.txt", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getAppAdsTxt(): String {
        return appAdsTxt
    }
}

