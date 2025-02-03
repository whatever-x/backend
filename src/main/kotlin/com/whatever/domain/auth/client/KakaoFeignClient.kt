package com.whatever.domain.auth.client

import com.whatever.config.KakaoOAuthConfig
import com.whatever.domain.auth.client.dto.KakaoUnlinkUser
import com.whatever.domain.auth.client.dto.KakaoUnlinkUserResponse
import com.whatever.domain.auth.client.dto.KakaoUserInfoResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "KakaoOAuthClient",
    url = "https://kapi.kakao.com",
    configuration = [KakaoOAuthConfig::class]
)
interface KakaoOAuthClient {

    @GetMapping(
        path = ["/v2/user/me"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun getUserInfo(@RequestHeader("Authorization") accessToken: String): KakaoUserInfoResponse

    @PostMapping(
        path = ["/v1/user/unlink"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun unlinkUser(
        @RequestHeader("Authorization") accessToken: String,
        unlinkUser: KakaoUnlinkUser,
    ): KakaoUnlinkUserResponse

}
