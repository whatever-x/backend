package com.whatever.domain.auth.service

import com.whatever.config.properties.OauthProperties
import com.whatever.domain.auth.client.dto.JsonWebKey
import com.whatever.domain.auth.client.dto.KakaoIdTokenPayload
import com.whatever.global.jwt.JwtProvider
import io.jsonwebtoken.Claims
import io.jsonwebtoken.InvalidClaimException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*

@Component
class OIDCHelper(
    private val jwtProvider: JwtProvider,
    private val oauthProperties: OauthProperties,
) {

    fun parseKakaoIdToken(
        idToken: String,
        oidcPublicKeys: List<JsonWebKey>,
    ): KakaoIdTokenPayload {
        val kid = getKid(idToken)
        val webKey = oidcPublicKeys.firstOrNull { jsonWebKey -> jsonWebKey.kid == kid }
            ?: throw IllegalArgumentException("kid(${kid})에 해당하는 공개키를 찾을 수 없습니다.")  // TODO(준용) CustomException으로 변경

        val jws = parseIdToken(
            idToken = idToken,
            rsaPublicKey = getRsaPublicKey(webKey.n, webKey.e),
            issuer = oauthProperties.kakao.baseUrl,
            audience = oauthProperties.kakao.clientId,
        )

        return jws.toKakaoIdTokenPayload()
    }

    private fun parseIdToken(
        idToken: String,
        rsaPublicKey: PublicKey,
        issuer: String,
        audience: String,
    ): Jws<Claims> {
        val idTokenParser = getIdTokenParser(
            issuer = issuer,
            audience = audience,
            rsaPublicKey = rsaPublicKey
        )
        return jwtProvider.parseJwt(
            jwtParser = idTokenParser,
            token = idToken
        )
    }

    private fun getIdTokenParser(
        issuer: String,
        audience: String,
        rsaPublicKey: PublicKey
    ): JwtParser {
        try {
            return Jwts.parser()
                .requireIssuer(issuer)
                .requireAudience(audience)
                .verifyWith(rsaPublicKey)
                .build()
        } catch (e: InvalidClaimException) {  // TODO(준용) CustomException으로 변경
            throw IllegalArgumentException("IdToken의 필수 클레임이 누락되었거나 올바르지 않습니다.")
        }
    }

    private fun getKid(idToken: String): String {
        return jwtProvider.getUnsecuredHeader(idToken)["kid"]
            ?: throw IllegalArgumentException("kid가 존재하지 않습니다. 올바르지 않은 idToken입니다.")
    }

    /**
     * @param n modulus
     * @param e exponent
     */
    private fun getRsaPublicKey(n: String, e: String): PublicKey {
        val decodedN = Base64.getUrlDecoder().decode(n)
        val decodedE = Base64.getUrlDecoder().decode(e)

        val modulus = BigInteger(1, decodedN)
        val exponent = BigInteger(1, decodedE)

        val rsaPublicKeySpec = RSAPublicKeySpec(modulus, exponent)  // RSA 공개키 사양 생성

        val keyFactory = KeyFactory.getInstance("RSA")  // RSA KeyFactory 인스턴스화
        return keyFactory.generatePublic(rsaPublicKeySpec)  // 공개키 생성
    }

}

private fun Jws<Claims>.toKakaoIdTokenPayload(): KakaoIdTokenPayload {
    return KakaoIdTokenPayload(
        iss = payload.issuer as String,
        aud = payload.audience.joinToString(),
        sub = payload.subject as String,
        iat = payload["iat"].toString().toLong(),
        exp = payload["exp"].toString().toLong(),
        authTime = payload["auth_time"].toString().toLong(),
        nonce = payload["nonce"] as String?,
        nickname = payload["nickname"] as String?,
        picture = payload["picture"] as String?,
        email = payload["email"] as String?,
    )
}