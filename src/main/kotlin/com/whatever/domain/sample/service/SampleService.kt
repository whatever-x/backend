package com.whatever.domain.sample.service

import com.whatever.config.properties.JwtProperties
import com.whatever.domain.auth.dto.ServiceToken
import com.whatever.domain.auth.dto.SignInResponse
import com.whatever.domain.auth.service.JwtHelper
import com.whatever.domain.sample.exception.SampleExceptionCode
import com.whatever.domain.sample.exception.SampleNotFoundException
import com.whatever.domain.sample.repository.SampleUserRepository
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.domain.user.service.UserService
import com.whatever.global.jwt.JwtProvider
import com.whatever.util.DateTimeUtil
import com.whatever.util.RedisUtil
import io.viascom.nanoid.NanoId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Profile("dev", "local-mem")
@Service
@Transactional(readOnly = true)
class SampleService(
    private val jwtHelper: JwtHelper,
    private val redisUtil: RedisUtil,
    private val jwtProperties: JwtProperties,
    private val jwtProvider: JwtProvider,
    private val sampleUserRepository: SampleUserRepository,
) {
    companion object {
        val randomEmailLength = 10
        val randomNicknameLength = 8
    }

    fun getSample(): String {
        return "sample String"
    }

    fun testSignIn(email: String, expSec: Long): SignInResponse {
        val testUser = sampleUserRepository.findByEmailAndIsDeleted(email)
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

    fun createNewDummyAccount(testEmail: String?): String {
        val dummyUser = User(
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString(),
            email = testEmail ?: "${NanoId.generate(randomEmailLength)}@test.test",
        )
        sampleUserRepository.save(dummyUser)
        return dummyUser.email!!
    }

    fun createSingleDummyAccount(
        testEmail: String?,
        testNickname: String?,
        testBirthDate: LocalDate?,
        testGender: UserGender?,
    ): String {
        val dummyUser = User(
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString(),
            email = testEmail ?: "${NanoId.generate(randomEmailLength)}@test.test",
            nickname = testNickname ?: NanoId.generate(randomNicknameLength),
            birthDate = testBirthDate ?: DateTimeUtil.localNow().toLocalDate(),
            gender = testGender ?: UserGender.MALE,
            userStatus = UserStatus.SINGLE,
        )
        sampleUserRepository.save(dummyUser)
        return dummyUser.email!!
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