package com.whatever.domain.auth.client

import com.whatever.domain.auth.client.dto.AppleGenerateAndValidateTokenDto
import com.whatever.domain.auth.client.dto.AppleTokenResponse
import com.whatever.domain.auth.client.dto.OIDCPublicKeysResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    name = "AppleOIDCClient",
    url = "https://appleid.apple.com",
)
interface AppleOIDCClient {

    @Cacheable(
        cacheNames = ["oidc-public-key"],
        key = "'APPLE'",
        cacheManager = "oidcCacheManager"
    )
    @GetMapping(
        path = ["/auth/keys"],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getOIDCPublicKey(): OIDCPublicKeysResponse

    @PostMapping(
        path = ["/auth/token"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun generateAndValidateToken(
        tokenDto: AppleGenerateAndValidateTokenDto
    ): AppleTokenResponse
}