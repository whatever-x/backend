package com.whatever.domain.auth.client.dto

data class KakaoOIDCPublicKeysResponse(
    val keys: List<JsonWebKey> = emptyList()
)

data class JsonWebKey(
    val kid: String? = null,
    val kty: String? = null,
    val alg: String? = null,
    val use: String? = null,
    val n: String? = null,
    val e: String? = null,
)
