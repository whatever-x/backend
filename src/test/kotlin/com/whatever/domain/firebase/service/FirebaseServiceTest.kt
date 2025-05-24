package com.whatever.domain.firebase.service

import com.whatever.domain.auth.service.createSingleUser
import com.whatever.domain.firebase.model.FcmToken
import com.whatever.domain.firebase.repository.FcmTokenRepository
import com.whatever.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class FirebaseServiceTest @Autowired constructor(
    private val firebaseService: FirebaseService,
    private val fcmTokenRepository: FcmTokenRepository,
    private val userRepository: UserRepository,
) {

    @AfterEach
    fun tearDown() {
        fcmTokenRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
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

}