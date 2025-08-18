package com.whatever.caramel.api.clientversion.controller

import com.whatever.caramel.api.clientversion.controller.dto.GetUpdatePolicyResponse
import com.whatever.caramel.common.global.annotation.DisableSwaggerAuthButton
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders
import com.whatever.caramel.common.response.CaramelApiResponse
import com.whatever.caramel.common.response.succeed
import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.model.OsType.ANDROID
import com.whatever.caramel.domain.clientversion.model.OsType.IOS
import com.whatever.caramel.domain.clientversion.service.ClientVersionService
import com.whatever.caramel.domain.clientversion.vo.ForceUpdate
import com.whatever.caramel.domain.clientversion.vo.NoUpdate
import com.whatever.caramel.domain.clientversion.vo.RecommendUpdate
import com.whatever.caramel.domain.clientversion.vo.VersionPolicyVo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "클라이언트 버전 API",
    description = "클라이언트 버전 체크 등 관련 기능을 제공하는 API"
)
@RestController
@RequestMapping("/v1/client-versions")
class ClientVersionController(
    private val clientVersionService: ClientVersionService,
) {
    @Value("\${app-store.android-uri}")
    private lateinit var androidUpdateUri: String
    @Value("\${app-store.ios-uri}")
    private lateinit var iosUpdateUri: String

    @DisableSwaggerAuthButton
    @Operation(
        summary = "강제 업데이트 필요 여부 조회",
        description = """
            ### 전송한 버전을 기준으로, 강제 업데이트가 필요한지 조회합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "강제 업데이트 필요 여부, store uri"),
        ]
    )
    @GetMapping("/update-policy")
    fun getUpdatePolicy(
        @RequestHeader(name = CaramelHttpHeaders.OS_TYPE) osType: OsType,
        @RequestParam currentVersionCode: Int,
    ): CaramelApiResponse<GetUpdatePolicyResponse> {
        val versionPolicyVo = clientVersionService.checkVersion(osType, currentVersionCode)

        return versionPolicyVo.toResponse(
            androidUpdateUri = androidUpdateUri,
            iosUpdateUri = iosUpdateUri,
        ).succeed()
    }
}

private fun VersionPolicyVo.toResponse(androidUpdateUri: String, iosUpdateUri: String): GetUpdatePolicyResponse {
    fun OsType.getUpdateUri(): String {
        return when (this) {
            ANDROID -> androidUpdateUri
            IOS -> iosUpdateUri
        }
    }
    return when (this) {
        is ForceUpdate -> GetUpdatePolicyResponse(forceUpdate = true, updateUri = osType.getUpdateUri())
        is RecommendUpdate -> GetUpdatePolicyResponse(forceUpdate = false)
        is NoUpdate -> GetUpdatePolicyResponse(forceUpdate = false)
    }
}