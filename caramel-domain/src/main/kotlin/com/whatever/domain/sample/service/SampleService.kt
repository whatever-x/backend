package com.whatever.domain.sample.service

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.jwt.JwtProperties
import com.whatever.caramel.common.global.jwt.JwtProvider
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import com.whatever.domain.auth.repository.AuthRedisRepository
import com.whatever.domain.auth.service.JwtHelper
import com.whatever.domain.auth.vo.ServiceTokenVo
import com.whatever.domain.auth.vo.SignInVo
import com.whatever.domain.sample.exception.SampleExceptionCode
import com.whatever.domain.sample.exception.SampleNotFoundException
import com.whatever.domain.sample.repository.SampleUserRepository
import com.whatever.domain.sample.repository.SampleUserSettingRepository
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserSetting
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.vo.UserInfoVo
import com.whatever.firebase.service.FirebaseService
import io.viascom.nanoid.NanoId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Profile("dev", "local-mem")
@Service
@Transactional(readOnly = true)
class SampleService(
    private val jwtHelper: JwtHelper,
    private val authRedisRepository: AuthRedisRepository,
    private val jwtProperties: JwtProperties,
    private val jwtProvider: JwtProvider,
    private val sampleUserRepository: SampleUserRepository,
    private val firebaseService: FirebaseService,
    private val sampleUserSettingRepository: SampleUserSettingRepository,
) {

    fun sendTestFcmNotification(
        targetUserIds: Set<Long>,
        title: String,
        body: String,
    ) {
        firebaseService.sendNotification(
            targetUserIds = targetUserIds,
            fcmNotification = FcmNotification(
                title = title,
                body = body,
            )
        )
    }

    fun getSample(): String {
        return "sample String"
    }

    fun testSignIn(email: String, expSec: Long): SignInVo {
        val testUser = sampleUserRepository.findByEmailAndIsDeleted(email)
            ?: throw SampleNotFoundException(
                errorCode = SampleExceptionCode.SAMPLE_CODE,
                errorUi = ErrorUi.Toast("테스트 유저를 찾을 수 없습니다. 관리자에게 문의해주세요."),
            )

        val serviceToken = createTokenAndSave(userId = testUser.id, expSec)
        UserInfoVo.from(testUser)
        return SignInVo.from(
            serviceToken = serviceToken,
            user = testUser,
        )
    }

    @Transactional
    fun createNewDummyAccount(testEmail: String?): String {
        val dummyUser = User(
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString(),
            email = testEmail ?: "${NanoId.generate(randomEmailLength)}@test.test",
        )
        sampleUserRepository.save(dummyUser)
        return dummyUser.email!!
    }

    @Transactional
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
        val user = sampleUserRepository.save(dummyUser)
        sampleUserSettingRepository.save(UserSetting(user))
        return dummyUser.email!!
    }

    private fun createTokenAndSave(userId: Long, expSec: Long): ServiceTokenVo {
        val accessToken = jwtHelper.createAccessToken(userId, expSec)  // access token 발행
        val refreshToken = jwtHelper.createRefreshToken()  // refresh token 발행
        authRedisRepository.saveRefreshToken(
            userId = userId,
            deviceId = "tempDeviceId",  // TODO(준용): Client에서 Device Id를 받아와 저장 필요
            refreshToken = refreshToken,
            ttlSeconds = jwtProperties.refreshExpirationSec
        )
        return ServiceTokenVo(
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

    companion object {
        val randomEmailLength = 10
        val randomNicknameLength = 8
    }
}
