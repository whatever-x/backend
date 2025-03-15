package com.whatever.domain.sample.service

import com.whatever.config.properties.JwtProperties
import com.whatever.domain.auth.dto.ServiceToken
import com.whatever.domain.auth.dto.SignInResponse
import com.whatever.domain.auth.service.JwtHelper
import com.whatever.domain.sample.exception.SampleExceptionCode
import com.whatever.domain.sample.exception.SampleNotFoundException
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.jwt.JwtProvider
import com.whatever.util.RedisUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SampleService(
    private val userRepository: UserRepository,
    private val jwtHelper: JwtHelper,
    private val redisUtil: RedisUtil,
    private val jwtProperties: JwtProperties,
    private val jwtProvider: JwtProvider,
) {

    fun getSample(): String {
        return "sample String"
    }

    fun testSignIn(gender: UserGender, expSec: Long): SignInResponse {

        val testPlatformId = when (gender) {
            UserGender.MALE -> "test-social-id"
            UserGender.FEMALE -> "test-social-id-2"
        }
        val testUser = userRepository.findByPlatformUserId(testPlatformId)
            ?: throw SampleNotFoundException(SampleExceptionCode.SAMPLE_CODE, "테스트 유저를 찾을 수 없습니다. 관리자에게 문의해주세요.")

        val serviceToken = createTokenAndSave(userId = testUser.id, expSec)
        return SignInResponse(
            serviceToken = serviceToken,
            userStatus = testUser.userStatus,
            nickname = testUser.nickname,
            birthDay = testUser.birthDate,
            coupleId = testUser.couple?.id,
        )
    }

    private fun createTokenAndSave(userId: Long, expSec: Long): ServiceToken {
        val accessToken = jwtHelper.createAccessToken(userId, expSec)  // access token 발행
        val refreshToken = jwtHelper.createRefreshToken()  // refresh token 발행
        redisUtil.saveRefreshToken(
            userId = userId,
            deviceId = "tempDeviceId",  // TODO(준용): Client에서 Device Id를 받아와 저장 필요
            refreshToken = refreshToken,
            ttlSeconds = jwtProperties.refreshExpirationSec
        )
        return ServiceToken(
            accessToken,
            refreshToken,
        )
    }

    private fun JwtHelper.createAccessToken(userId: Long, expSec: Long): String {
        val claims = mutableMapOf<String, String>()
        claims["userId"] = userId.toString()

        return jwtProvider.createJwt(
            subject = "access",
            expirationSec = expSec,
            claims = claims,
        )
    }
}