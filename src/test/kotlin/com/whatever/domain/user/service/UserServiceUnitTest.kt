package com.whatever.domain.user.service

import com.whatever.domain.user.dto.PatchUserSettingRequest
import com.whatever.domain.user.exception.UserExceptionCode
import com.whatever.domain.user.exception.UserIllegalStateException
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserSetting
import com.whatever.domain.user.repository.UserRepository
import com.whatever.domain.user.repository.UserSettingRepository
import com.whatever.global.security.util.SecurityUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class UserServiceUnitTest {

    @MockitoBean
    private lateinit var mockUserRepository: UserRepository

    @MockitoBean
    private lateinit var mockUserSettingRepository: UserSettingRepository

    @Autowired
    private lateinit var userService: UserService

    private lateinit var mockSecurityUtil: AutoCloseable

    @BeforeEach
    fun setUp() {
        mockSecurityUtil = mockStatic(SecurityUtil::class.java)
    }

    @AfterEach
    fun tearDown() {
        mockSecurityUtil.close()  // static mock 초기화
    }

    @ParameterizedTest
    @CsvSource(
        "true", "false"
    )
    fun `유저의 세팅을 업데이트 합니다`(setting: Boolean) {
        // given
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
        Mockito.`when`(mockUserRepository.save(Mockito.any(User::class.java)))
            .thenReturn(user)

        val userSetting = UserSetting(user = user, notificationEnabled = setting)
        Mockito.`when`(
            mockUserSettingRepository.findByUserAndIsDeleted(
                user = user,
                isDeleted = false,
            )
        ).thenReturn(userSetting)

        Mockito.`when`(mockUserRepository.getReferenceById(Mockito.anyLong()))
            .thenReturn(user)

        /**
         * 요청에서 setting을 반대로 주면?
         */
        // when
        val request = PatchUserSettingRequest(notificationEnabled = setting.not())
        val result = userService.updateUserSetting(request = request, userId = user.id)

        // then
        assertThat(result.notificationEnabled).isEqualTo(setting.not())
    }

    @ParameterizedTest
    @CsvSource(
        "true", "false"
    )
    fun `유저의 세팅을 업데이트 - Request null 이면 기존 그대로 응답`(setting: Boolean) {
        // given
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
        Mockito.`when`(mockUserRepository.save(Mockito.any(User::class.java)))
            .thenReturn(user)

        val userSetting = UserSetting(user = user, notificationEnabled = setting)
        Mockito.`when`(
            mockUserSettingRepository.findByUserAndIsDeleted(
                user = user,
                isDeleted = false,
            )
        ).thenReturn(userSetting)

        Mockito.`when`(mockUserRepository.getReferenceById(Mockito.anyLong()))
            .thenReturn(user)

        // when
        val request = PatchUserSettingRequest(notificationEnabled = null)
        val result = userService.updateUserSetting(request = request, userId = user.id)

        // then
        assertThat(result.notificationEnabled).isEqualTo(setting)
    }

    @Test
    fun `유저의 세팅을 업데이트 하려하지만, 유저 설정 정보를 찾을 수 없음`() {
        // given
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
        Mockito.`when`(mockUserRepository.save(Mockito.any(User::class.java)))
            .thenReturn(user)

        Mockito.`when`(mockUserRepository.getReferenceById(Mockito.anyLong()))
            .thenReturn(user)

        // when
        val request = PatchUserSettingRequest(notificationEnabled = true)
        val result = assertThrows<UserIllegalStateException> {
            userService.updateUserSetting(request = request, userId = user.id)
        }
        // then
        assertThat(result.errorCode).isEqualTo(UserExceptionCode.SETTING_DATA_NOT_FOUND)
    }

    @Test
    fun `유저의 세팅을 업데이트 - getCurrentUserId() 에 문제 없음`() {
        // given
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
        mockSecurityUtil.apply {
            Mockito.`when`(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }

        Mockito.`when`(mockUserRepository.getReferenceById(Mockito.anyLong()))
            .thenReturn(user)

        // when
        val request = PatchUserSettingRequest(notificationEnabled = true)
        val result = assertThrows<UserIllegalStateException> {
            userService.updateUserSetting(request = request)
        }

        // then
        assertThat(result.errorCode).isEqualTo(UserExceptionCode.SETTING_DATA_NOT_FOUND)
    }
}
