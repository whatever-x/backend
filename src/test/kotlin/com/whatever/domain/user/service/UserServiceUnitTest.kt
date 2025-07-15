package com.whatever.domain.user.service

import com.whatever.domain.user.dto.GetUserInfoResponse
import com.whatever.domain.user.dto.PatchUserSettingRequest
import com.whatever.domain.user.dto.UserSettingResponse
import com.whatever.domain.user.exception.UserExceptionCode
import com.whatever.domain.user.exception.UserExceptionCode.NOT_FOUND
import com.whatever.domain.user.exception.UserIllegalStateException
import com.whatever.domain.user.exception.UserNotFoundException
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserSetting
import com.whatever.domain.user.repository.UserRepository
import com.whatever.domain.user.repository.UserSettingRepository
import com.whatever.global.security.util.SecurityUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import java.util.Optional
import java.util.UUID
import kotlin.test.Test

@ActiveProfiles("test")
@ExtendWith(MockitoExtension::class)
class UserServiceUnitTest {

    @Mock
    private lateinit var mockUserRepository: UserRepository

    @Mock
    private lateinit var mockUserSettingRepository: UserSettingRepository

    @InjectMocks
    private lateinit var userService: UserService

    private lateinit var mockSecurityUtil: AutoCloseable

    private val mockkUserRepository = mockk<UserRepository>()
    private val mockkUserSettingRepository = mockk<UserSettingRepository>()
    private val spykUserService = spyk(UserService(mockkUserRepository, mockkUserSettingRepository))

    private val user: User = createUser()

    @BeforeEach
    fun setUp() {
        mockSecurityUtil = mockStatic(SecurityUtil::class.java)
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }
    }

    @AfterEach
    fun tearDown() {
        mockSecurityUtil.close()
    }

    @ParameterizedTest
    @CsvSource(
        "true", "false"
    )
    fun `유저의 세팅을 업데이트 합니다`(setting: Boolean) {
        // given
        val userSetting = UserSetting(user = user, notificationEnabled = setting)
        whenever(
            mockUserSettingRepository.findByUserAndIsDeleted(
                user = user,
                isDeleted = false,
            )
        ).thenReturn(userSetting)

        whenever(mockUserRepository.getReferenceById(Mockito.anyLong()))
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
        val userSetting = UserSetting(user = user, notificationEnabled = setting)
        whenever(
            mockUserSettingRepository.findByUserAndIsDeleted(
                user = user,
                isDeleted = false,
            )
        ).thenReturn(userSetting)

        whenever(mockUserRepository.getReferenceById(Mockito.anyLong()))
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
        whenever(mockUserRepository.getReferenceById(Mockito.anyLong()))
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
        whenever(mockUserRepository.getReferenceById(Mockito.anyLong()))
            .thenReturn(user)

        // when
        val request = PatchUserSettingRequest(notificationEnabled = true)
        val result = assertThrows<UserIllegalStateException> {
            userService.updateUserSetting(request = request)
        }

        // then
        assertThat(result.errorCode).isEqualTo(UserExceptionCode.SETTING_DATA_NOT_FOUND)
    }

    @Test
    fun `유저의 세팅을 가져옵니다`() {
        // given
        val response = UserSetting(user = user, notificationEnabled = false)
        val expected = UserSettingResponse.from(response)
        every { mockkUserRepository.getReferenceById(any()) } returns user
        every { mockkUserSettingRepository.findByUserAndIsDeleted(user = user, isDeleted = any()) } returns response

        // when
        val result = spykUserService.getUserSetting(userId = user.id)

        // then
        assertThat(result).isEqualTo(expected)
        verify(exactly = 1) {
            spykUserService.getUserSetting(userId = eq(user.id))
        }
    }

    @Test
    fun `유저의 세팅을 가져옵니다 - 기본값 userId 기본 세팅`() {
        // given
        val response = UserSetting(user = user, notificationEnabled = false)
        val expected = UserSettingResponse.from(response)
        every { mockkUserRepository.getReferenceById(any()) } returns user
        every { mockkUserSettingRepository.findByUserAndIsDeleted(user = user, isDeleted = any()) } returns response

        // when
        val result = spykUserService.getUserSetting()

        // then
        assertThat(result).isEqualTo(expected)
        verify(exactly = 1) {
            spykUserService.getUserSetting()
        }
    }

    @Test
    fun `유저의 세팅을 가져오는데, null이 나온 경우 UserIllegalStateException 을 받는다`() {
        // given
        every { mockkUserRepository.getReferenceById(any()) } returns user
        every { mockkUserSettingRepository.findByUserAndIsDeleted(user = user, isDeleted = any()) } returns null

        // when
        val result = assertThrows<UserIllegalStateException> {
            spykUserService.getUserSetting(userId = user.id)
        }

        // then
        assertThat(result.errorCode).isEqualTo(UserExceptionCode.SETTING_DATA_NOT_FOUND)

        verify(exactly = 1) {
            spykUserService.getUserSetting(userId = eq(user.id))
        }
    }

    @Test
    fun `내 정보를 가져오는데 성공`() {
        // given
        val expected = GetUserInfoResponse.from(user)
        every { mockkUserRepository.findById(user.id) } returns Optional.of(user)

        // when
        val result = spykUserService.getUserInfo(userId = user.id)

        assertThat(result).isEqualTo(expected)
        verify(exactly = 1) {
            mockkUserRepository.findById(eq(user.id))
        }
    }

    @Test
    fun `내 정보를 가져오는데, null을 반화나는 경우`() {
        // given
        every { mockkUserRepository.findById(user.id) } returns Optional.empty()

        // when
        val result = assertThrows<UserNotFoundException> { spykUserService.getUserInfo(userId = user.id) }

        assertThat(result.errorCode).isEqualTo(NOT_FOUND)

        verify(exactly = 1) {
            mockkUserRepository.findById(eq(user.id))
        }
    }

    private fun createUser() = User(
        id = 1L,
        platform = LoginPlatform.TEST,
        platformUserId = UUID.randomUUID().toString()
    )
}
