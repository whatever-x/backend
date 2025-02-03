package com.whatever.domain.auth.client

import com.whatever.config.KakaoKauthConfig
import com.whatever.config.KakaoOAuthConfig
import com.whatever.domain.auth.client.dto.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
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
    fun unlinkUser(@RequestHeader("Authorization") accessToken: String): KakaoUnlinkUserResponse

    /**
     * @param targetIdType kakao 문서에 따라 항상 "user_id"로 고정
     */
    @PostMapping(
        path = ["/v1/user/unlink"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun unlinkUserByAdminKey(
        @RequestHeader("Authorization") appAdminKeyWithPrefix: String,
        @ModelAttribute unlinkUser: KakaoUnlinkUserRequest,
    ): KakaoUnlinkUserResponse

}

@FeignClient(
    name = "KakaoOIDCClient",
    url = "https://kauth.kakao.com",
    configuration = [KakaoKauthConfig::class]
)
interface KakaoOIDCClient {

    @GetMapping(
        path = ["/.well-known/jwks.json"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun getOIDCPublicKey(): KakaoOIDCPublicKeysResponse

}