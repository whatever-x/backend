package client

import client.dto.KakaoIdTokenInfoRequest
import client.dto.KakaoIdTokenPayload
import client.dto.KakaoUnlinkUserRequest
import client.dto.KakaoUnlinkUserResponse
import client.dto.KakaoUserInfoResponse
import client.dto.OIDCPublicKeysResponse
import openfeign.KakaoKapiConfig
import openfeign.KakaoKauthConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "KakaoKapiClient",
    url = "https://kapi.kakao.com",
    configuration = [KakaoKapiConfig::class]
)
interface KakaoKapiClient {

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
        unlinkUser: KakaoUnlinkUserRequest,
    ): KakaoUnlinkUserResponse
}

@FeignClient(
    name = "KakaoOIDCClient",
    url = "https://kauth.kakao.com",
    configuration = [KakaoKauthConfig::class]
)
interface KakaoOIDCClient {

    @Cacheable(
        cacheNames = ["oidc-public-key"],
        key = "'KAKAO'",
        cacheManager = "oidcCacheManager"
    )
    @GetMapping(
        path = ["/.well-known/jwks.json"],
    )
    fun getOIDCPublicKey(): OIDCPublicKeysResponse

    /**
     * 토큰의 유효성 검증이 불가능하므로 디버깅 용도로만 사용해야 한다.
     */
    @PostMapping(
        path = ["/oauth/tokeninfo"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun getIdTokenInfo(idToken: KakaoIdTokenInfoRequest): KakaoIdTokenPayload
}
