package com.whatever.domain.auth.client

import com.whatever.domain.auth.client.dto.OIDCPublicKeysResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping

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

}