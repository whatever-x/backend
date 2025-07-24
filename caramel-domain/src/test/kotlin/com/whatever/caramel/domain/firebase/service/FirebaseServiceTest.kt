package com.whatever.caramel.domain.firebase.service

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.auth.service.createSingleUser
import com.whatever.caramel.domain.content.service.createCouple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.firebase.model.FcmToken
import com.whatever.caramel.domain.firebase.repository.FcmTokenRepository
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserSetting
import com.whatever.caramel.domain.user.repository.UserRepository
import com.whatever.caramel.domain.user.repository.UserSettingRepository
import com.whatever.caramel.infrastructure.firebase.FcmSender
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.only
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID
import kotlin.test.Test

@CaramelDomainSpringBootTest
class FirebaseServiceTest @Autowired constructor(
    private val firebaseService: FirebaseService,
    private val fcmTokenRepository: FcmTokenRepository,
    private val userRepository: UserRepository,
    private val coupleRepository: CoupleRepository,
    private val userSettingRepository: UserSettingRepository,
) {

    @MockitoBean
    private lateinit var fcmSender: FcmSender

    @AfterEach
    fun tearDown() {
        fcmTokenRepository.deleteAllInBatch()
        userSettingRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("새로운 디바이스에서 토큰을 등록하면 새로운 토큰이 생성된다.")
    @Test
    fun setFcmToken_WithNewDevice() {
        // given
        val user = createSingleUser(userRepository)
        fcmTokenRepository.save(FcmToken("old-token", "old-device", user))

        val deviceId = "new-device"
        val token = "new-token"

        // when
        firebaseService.setFcmToken(
            deviceId = deviceId,
            token = token,
            userId = user.id,
        )

        // then
        val savedToken = fcmTokenRepository.findAll().first { it.deviceId == deviceId }
        assertThat(savedToken.deviceId).isEqualTo(deviceId)
        assertThat(savedToken.token).isEqualTo(token)
        assertThat(savedToken.user.id).isEqualTo(user.id)
    }

    @DisplayName("같은 디바에스에서 새로운 토큰을 등록하면 토큰과 updatedAt이 갱신된다.")
    @Test
    fun setFcmToken_WithSameDeviceAndNewToken() {
        // given
        val user = createSingleUser(userRepository)
        val deviceId = "test-device"
        val oldToken = "old-token"
        val oldFcmToken = fcmTokenRepository.save(FcmToken(oldToken, deviceId, user))

        val newToken = "new-token"

        // when
        firebaseService.setFcmToken(
            deviceId = deviceId,
            token = newToken,
            userId = user.id,
        )

        // then
        val savedToken = fcmTokenRepository.findAll().single()
        assertThat(savedToken.token).isEqualTo(newToken)
        assertThat(savedToken.updatedAt).isAfter(oldFcmToken.updatedAt)
    }

    @DisplayName("같은 디바에스에서 같은 토큰을 등록하면 updatedAt이 갱신된다.")
    @Test
    fun setFcmToken_WithSameDeviceAndSameToken() {
        // given
        val user = createSingleUser(userRepository)
        val deviceId = "test-device"
        val oldToken = "old-token"
        val oldFcmToken = fcmTokenRepository.save(FcmToken(oldToken, deviceId, user))

        // when
        firebaseService.setFcmToken(
            deviceId = deviceId,
            token = oldToken,
            userId = user.id,
        )

        // then
        val savedToken = fcmTokenRepository.findAll().single()
        assertThat(savedToken.token).isEqualTo(oldToken)
        assertThat(savedToken.updatedAt).isAfter(oldFcmToken.updatedAt)
    }

    @DisplayName("타겟 유저의 fcm 토큰이 없다면 전송 함수가 실행되지 않는다.")
    @Test
    fun sendNotification_WithUsersWithoutTokens() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        setUserSetting(myUser, true)
        setUserSetting(partnerUser, true)

        // when
        firebaseService.sendNotification(
            targetUserIds = setOf(myUser.id, partnerUser.id),
            fcmNotification = FcmNotification("title", "body")
        )

        // then
        verify(fcmSender, never()).sendNotification(any(), any())
        verify(fcmSender, never()).sendNotificationAll(any(), any())
    }

    @DisplayName("타겟 유저에게 등록된 토큰이 하나라면 sendNotification()이 실행된다.")
    @Test
    fun sendNotification_WithSingleTokenUser() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        createFcmToken("test-device", myUser)

        // when
        firebaseService.sendNotification(
            targetUserIds = setOf(myUser.id),
            fcmNotification = FcmNotification("title", "body")
        )

        // then
        verify(fcmSender, only()).sendNotification(any(), any())
        verify(fcmSender, never()).sendNotificationAll(any(), any())
    }

    @DisplayName("타겟 유저에게 등록된 토큰이 여러개라면 sendNotificationAll()이 실행된다.")
    @Test
    fun sendNotification_WithMultipleTokensUser() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        createFcmToken("test-device-1", myUser)
        createFcmToken("test-device-2", myUser)

        // when
        firebaseService.sendNotification(
            targetUserIds = setOf(myUser.id),
            fcmNotification = FcmNotification("title", "body")
        )

        // then
        verify(fcmSender, never()).sendNotification(any(), any())
        verify(fcmSender, only()).sendNotificationAll(any(), any())
    }

    @DisplayName("타겟 유저들에게 등록된 토큰이 여러개라면 sendNotificationAll()이 실행된다.")
    @Test
    fun sendNotification_WithMultipleTokensUsers() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        createFcmToken("test-device-1", myUser)
        createFcmToken("test-device-2", myUser)
        createFcmToken("test-device-1", partnerUser)
        createFcmToken("test-device-2", partnerUser)

        // when
        firebaseService.sendNotification(
            targetUserIds = setOf(myUser.id, partnerUser.id),
            fcmNotification = FcmNotification("title", "body")
        )

        // then
        verify(fcmSender, never()).sendNotification(any(), any())
        verify(fcmSender, only()).sendNotificationAll(any(), any())
    }

    @DisplayName("타겟 유저들에게 등록된 토큰이 있지만, 알림 설정을 껐다면 전송되지 않는다.")
    @Test
    fun sendNotification_WithMultipleTokensUsersAndNotiDisabled() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        createFcmToken("test-device-1", myUser, false)
        createFcmToken("test-device-2", myUser, false)
        createFcmToken("test-device-1", partnerUser, false)
        createFcmToken("test-device-2", partnerUser, false)

        // when
        firebaseService.sendNotification(
            targetUserIds = setOf(myUser.id, partnerUser.id),
            fcmNotification = FcmNotification("title", "body")
        )

        // then
        verify(fcmSender, never()).sendNotification(any(), any())
        verify(fcmSender, never()).sendNotificationAll(any(), any())
    }

    @DisplayName("타겟 유저의 fcm 토큰이 없다면 데이터 전송 함수가 실행되지 않는다.")
    @Test
    fun sendData_WithUsersWithoutTokens() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        setUserSetting(myUser, true)
        setUserSetting(partnerUser, true)

        // when
        firebaseService.sendData(
            targetUserIds = setOf(myUser.id, partnerUser.id),
            data = mapOf("k" to "v"),
        )

        // then
        verify(fcmSender, never()).sendData(any(), any())
        verify(fcmSender, never()).sendDataAll(any(), any())
    }

    @DisplayName("타겟 유저에게 등록된 토큰이 하나라면 sendData()가 실행된다.")
    @Test
    fun sendData_WithSingleTokenUser() {
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        createFcmToken("test-device", myUser)

        // when
        firebaseService.sendData(
            targetUserIds = setOf(myUser.id),
            data = mapOf("a" to "1"),
        )

        // then
        verify(fcmSender, only()).sendData(any(), any())
        verify(fcmSender, never()).sendDataAll(any(), any())
    }

    @DisplayName("타겟 유저에게 등록된 토큰이 여러개라면 sendDataAll()이 실행된다.")
    @Test
    fun sendData_WithMultipleTokensUser() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        createFcmToken("test-device-1", myUser)
        createFcmToken("test-device-2", myUser)

        // when
        firebaseService.sendData(
            targetUserIds = setOf(myUser.id),
            data = mapOf("x" to "y"),
        )

        // then
        verify(fcmSender, never()).sendData(any(), any())
        verify(fcmSender, only()).sendDataAll(any(), any())
    }

    @DisplayName("타겟 유저들에게 등록된 토큰이 여러개라면 sendDataAll()이 실행된다.")
    @Test
    fun sendData_WithMultipleTokensUsers() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        createFcmToken("test-device-1", myUser)
        createFcmToken("test-device-2", myUser)
        createFcmToken("test-device-1", partnerUser)
        createFcmToken("test-device-2", partnerUser)

        // when
        firebaseService.sendData(
            targetUserIds = setOf(myUser.id, partnerUser.id),
            data = mapOf("x" to "y"),
        )

        // then
        verify(fcmSender, never()).sendData(any(), any())
        verify(fcmSender, only()).sendDataAll(any(), any())
    }

    private fun createFcmToken(
        deviceId: String,
        user: User,
        notificationEnabled: Boolean = true,
    ): FcmToken {
        setUserSetting(user, notificationEnabled)
        return fcmTokenRepository.save(
            FcmToken(
                initialToken = UUID.randomUUID().toString(),
                deviceId = deviceId,
                user = user,
            )
        )
    }

    private fun setUserSetting(user: User, notificationEnabled: Boolean) {
        if (!userSettingRepository.existsByUserAndIsDeleted(user)) {
            userSettingRepository.save(UserSetting(user, notificationEnabled))
        }
    }
}
