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
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/client-versions")
class ClientVersionController(
    private val clientVersionService: ClientVersionService,
) {
    @Value("\${app-store.android-uri}")
    private lateinit var androidStoreUri: String
    @Value("\${app-store.ios-uri}")
    private lateinit var iosStoreUri: String

    @DisableSwaggerAuthButton
    @GetMapping("/update-policy")
    fun getUpdatePolicy(
        @RequestHeader(name = CaramelHttpHeaders.OS_TYPE) osType: OsType,
        @RequestParam currentVersionCode: Int,
    ): CaramelApiResponse<GetUpdatePolicyResponse> {
        val versionPolicyVo = clientVersionService.checkVersion(osType, currentVersionCode)

        return versionPolicyVo.toResponse(
            androidStoreUri = androidStoreUri,
            iosStoreUri = iosStoreUri,
        ).succeed()
    }
}

private fun VersionPolicyVo.toResponse(androidStoreUri: String, iosStoreUri: String): GetUpdatePolicyResponse {
    fun OsType.getStoreUri(): String {
        return when (this) {
            ANDROID -> androidStoreUri
            IOS -> iosStoreUri
        }
    }
    return when (this) {
        is ForceUpdate -> GetUpdatePolicyResponse(forceUpdate = true, storeUri = osType.getStoreUri())
        is RecommendUpdate -> GetUpdatePolicyResponse(forceUpdate = false)
        is NoUpdate -> GetUpdatePolicyResponse(forceUpdate = false)
    }
}