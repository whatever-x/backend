package com.whatever.domain.user.service

import com.whatever.domain.user.dto.GetUserInfoResponse
import com.whatever.domain.user.dto.PatchUserSettingRequest
import com.whatever.domain.user.dto.PutUserProfileRequest
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
import com.whatever.util.DateTimeUtil
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
import java.time.LocalDate
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

    @BeforeEach
    fun setUp() {
        mockSecurityUtil = mockStatic(SecurityUtil::class.java)
    }

    @AfterEach
    fun tearDown() {
        mockSecurityUtil.close()
    }

    @Test
    fun `user 의 프로필을 업데이트- nickname, birthdate 존재`() {
        val request = PutUserProfileRequest(nickname = "pita", birthday = LocalDate.now())
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString(),
            nickname = "tjrwn",
        )
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)

        val result = spykUserService.updateProfile(request, DateTimeUtil.KST_ZONE_ID)

        with(result) {
            assertThat(id).isEqualTo(user.id)
            assertThat(nickname).isEqualTo(request.nickname)
            assertThat(birthday).isEqualTo(request.birthday)
        }
        verify(exactly = 1) {
            SecurityUtil.getCurrentUserId()
            mockkUserRepository.findById(any())
            user.updateBirthDate(result.birthday, DateTimeUtil.KST_ZONE_ID)
        }
    }

    @Test
    fun `user 의 프로필을 업데이트 - nickname null`() {
        val request = PutUserProfileRequest(nickname = null, birthday = LocalDate.now())
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString(),
            nickname = "tjrwn"
        )
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)

        val result = spykUserService.updateProfile(request, DateTimeUtil.KST_ZONE_ID)

        with(result) {
            assertThat(id).isEqualTo(user.id)
            assertThat(nickname).isEqualTo(user.nickname)
            assertThat(nickname).isNotNull()
            assertThat(birthday).isEqualTo(request.birthday)
        }
        verify(exactly = 1) {
            SecurityUtil.getCurrentUserId()
            mockkUserRepository.findById(any())
            user.updateBirthDate(result.birthday, DateTimeUtil.KST_ZONE_ID)
        }
    }

    @Test
    fun `user 의 프로필을 업데이트 - nickname 이 "" 로 빈값`() {
        val request = PutUserProfileRequest(nickname = "", birthday = LocalDate.now())
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString(),
            nickname = "tjrwn"
        )
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)

        val result = spykUserService.updateProfile(request, DateTimeUtil.KST_ZONE_ID)

        with(result) {
            assertThat(id).isEqualTo(user.id)
            assertThat(nickname).isEqualTo(user.nickname)
            assertThat(nickname).isNotNull()
            assertThat(birthday).isEqualTo(request.birthday)
        }
        verify(exactly = 1) {
            SecurityUtil.getCurrentUserId()
            mockkUserRepository.findById(any())
            user.updateBirthDate(result.birthday, DateTimeUtil.KST_ZONE_ID)
        }
    }

    /**
     * user 내부의 함수가 안 불렸는지 체크하기 위해 spyk 로 user 를 감쌌습니다
     */
    @Test
    fun `user 의 프로필을 업데이트 - birthday 가 null`() {
        val request = PutUserProfileRequest(nickname = "pita", birthday = null)
        val user = spyk(
            User(
                id = 1L,
                platform = LoginPlatform.TEST,
                platformUserId = UUID.randomUUID().toString(),
                nickname = "tjrwn",
                birthDate = LocalDate.now(),
            )
        )
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)

        val result = spykUserService.updateProfile(request, DateTimeUtil.KST_ZONE_ID)

        with(result) {
            assertThat(id).isEqualTo(user.id)
            assertThat(nickname).isEqualTo(request.nickname)
            assertThat(birthday).isNotNull()
            assertThat(birthday).isNotEqualTo(request.birthday)
            assertThat(birthday).isEqualTo(user.birthDate)
        }
        verify(exactly = 1) {
            SecurityUtil.getCurrentUserId()
            mockkUserRepository.findById(any())
        }
        verify(exactly = 0) {
            user.updateBirthDate(result.birthday, DateTimeUtil.KST_ZONE_ID)
        }
    }

    @Test
    fun `user 의 프로필을 업데이트 - findByIdAndNotDeleted가 null 반환`() {
        val request = PutUserProfileRequest(nickname = "", birthday = LocalDate.now())
        val user = spyk(
            User(
                id = 1L,
                platform = LoginPlatform.TEST,
                platformUserId = UUID.randomUUID().toString(),
                nickname = "tjrwn",
            )
        )
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }
        every { mockkUserRepository.findById(any()) } returns Optional.empty()

        val result = kotlin.runCatching {
            spykUserService.updateProfile(request, DateTimeUtil.KST_ZONE_ID)
        }.exceptionOrNull() as? UserNotFoundException

        assertThat(result).isNotNull()
        assertThat(result!!.errorCode).isEqualTo(NOT_FOUND)

        verify(exactly = 1) {
            SecurityUtil.getCurrentUserId()
            mockkUserRepository.findById(any())
        }
        verify(exactly = 0) {
            user.updateBirthDate(any(), any())
        }
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
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )

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
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )

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
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }

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
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
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
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }
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
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
        every { mockkUserRepository.getReferenceById(any()) } returns user
        every { mockkUserSettingRepository.findByUserAndIsDeleted(user = user, isDeleted = any()) } returns null

        // when
        val result = runCatching {
            spykUserService.getUserSetting(userId = user.id)
        }.exceptionOrNull() as? UserIllegalStateException

        // then
        assertThat(result).isNotNull()
        assertThat(result!!.errorCode).isEqualTo(UserExceptionCode.SETTING_DATA_NOT_FOUND)

        verify(exactly = 1) {
            spykUserService.getUserSetting(userId = eq(user.id))
        }
    }

    @Test
    fun `내 정보를 가져오는데 성공`() {
        // given
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
        val expected = GetUserInfoResponse.from(user)
        every { mockkUserRepository.findById(user.id) } returns Optional.of(user)

        // when
        val result = spykUserService.getUserInfo(userId = user.id)

        assertThat(result).isEqualTo(expected)
        verify(exactly = 1) {
            mockkUserRepository.findById(any())
        }
    }

    @Test
    fun `내 정보를 가져오는데, null을 반화나는 경우`() {
        // given
        val user = User(
            id = 1L,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString()
        )
        every { mockkUserRepository.findById(user.id) } returns Optional.empty()

        // when
        val result = kotlin.runCatching { spykUserService.getUserInfo(userId = user.id) }
            .exceptionOrNull() as? UserNotFoundException

        assertThat(result).isNotNull()
        assertThat(result!!.errorCode).isEqualTo(NOT_FOUND)

        verify(exactly = 1) {
            mockkUserRepository.findById(any())
        }
    }
}
