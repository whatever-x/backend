package com.whatever.caramel.infrastructure.client.dto

import feign.form.FormProperty

/**
 * Sign in with Apple REST API의 Generate and validate tokens에 사용되는 dto
 */
data class AppleGenerateAndValidateTokenDto(
    @FormProperty("client_id")
    val clientId: String,
    @FormProperty("client_secret")
    val clientSecret: String,
    @FormProperty("code")
    val code: String? = null,
    @FormProperty("grant_type")
    val grantType: String,
    @FormProperty("refresh_token")
    val refreshToken: String? = null,
    @FormProperty("redirect_uri")
    val redirectUri: String? = null,
) {

    companion object {
        fun createForAuthorizationCodeValidation(
            clientId: String,
            clientSecret: String,
            code: String,
            redirectUri: String,
        ): AppleGenerateAndValidateTokenDto {
            return AppleGenerateAndValidateTokenDto(
                clientId = clientId,
                clientSecret = clientSecret,
                code = code,
                grantType = "authorization_code",
                redirectUri = redirectUri,
            )
        }

        fun createForRefreshTokenValidation(
            clientId: String,
            clientSecret: String,
            refreshToken: String,
        ): AppleGenerateAndValidateTokenDto {
            return AppleGenerateAndValidateTokenDto(
                clientId = clientId,
                clientSecret = clientSecret,
                grantType = "refresh_token",
                refreshToken = refreshToken,
            )
        }
    }
}
