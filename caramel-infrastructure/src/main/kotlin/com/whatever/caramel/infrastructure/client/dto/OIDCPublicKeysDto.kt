package com.whatever.caramel.infrastructure.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OIDCPublicKeysResponse(
    val keys: List<JsonWebKey> = emptyList(),
)

data class JsonWebKey(
    @JsonProperty("kid") val kid: String,
    @JsonProperty("kty") val kty: String,
    @JsonProperty("alg") val alg: String,
    @JsonProperty("use") val use: String,
    @JsonProperty("n") val n: String,
    @JsonProperty("e") val e: String,
)
