package com.whatever.domain.auth.client

import com.whatever.config.KakaoKauthConfig
import com.whatever.config.KakaoOAuthConfig
import com.whatever.domain.auth.client.dto.*
import org.springframework.cache.annotation.Cacheable
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

    @Cacheable(
        cacheNames = ["kakao-oidc"],
        cacheManager = "oidcCacheManager"
    )
    @GetMapping(
        path = ["/.well-known/jwks.json"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun getOIDCPublicKey(): KakaoOIDCPublicKeysResponse

    /**
     * 토큰의 유효성 검증이 불가능하므로 디버깅 용도로만 사용해야 한다.
     */
    @PostMapping(
        path = ["/oauth/tokeninfo"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun getIdTokenInfo(@ModelAttribute idToken: KakaoIdTokenInfoRequest): KakaoTokenInfoResponse

}