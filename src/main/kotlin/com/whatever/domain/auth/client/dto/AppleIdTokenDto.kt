package com.whatever.domain.auth.client.dto

data class AppleIdTokenPayload(
    val iss: String,
    val aud: String,
    val exp: Long,
    val iat: Long,
    val sub: String,
    val cHash: String,
    val email: String? = null,
    val emailVerified: Boolean,
    val isPrivateEmail: Boolean,
    val authTime: Long,
    val nonceSupported: Boolean,
    val nonce: String? = null,
) {
    val platformUserId
        get() = sub
}
