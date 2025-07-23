package com.whatever.caramel.domain.user.service

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.user.exception.UserExceptionCode
import com.whatever.caramel.domain.user.exception.UserExceptionCode.NOT_FOUND
import com.whatever.caramel.domain.user.exception.UserIllegalStateException
import com.whatever.caramel.domain.user.exception.UserNotFoundException
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.UserGender
import com.whatever.caramel.domain.user.model.UserSetting
import com.whatever.caramel.domain.user.model.UserStatus
import com.whatever.caramel.domain.user.repository.UserRepository
import com.whatever.caramel.domain.user.repository.UserSettingRepository
import com.whatever.caramel.domain.user.vo.CreateUserProfileVo
import com.whatever.caramel.domain.user.vo.UpdateUserProfileVo
import com.whatever.caramel.domain.user.vo.UpdateUserSettingVo
import com.whatever.caramel.domain.user.vo.UserInfoVo
import com.whatever.caramel.domain.user.vo.UserSettingVo
import com.whatever.caramel.domain.user.vo.UserVo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
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
    private val mockkUserRepository = mockk<UserRepository>()
    private val mockkUserSettingRepository = mockk<UserSettingRepository>()
    private val spykUserService = spyk(UserService(mockkUserRepository, mockkUserSettingRepository))

    private lateinit var user: com.whatever.caramel.domain.user.model.User
    private var userId: Long = Long.MIN_VALUE

    @BeforeEach
    fun setUp() {
        user = spyk(createUser())
        userId = user.id
    }

    @ParameterizedTest
    @CsvSource(
        "MALE, true, true",
        "MALE, true, false",
        "MALE, false, true",
        "MALE, false, false",
        "FEMALE, true, true",
        "FEMALE, true, false",
        "FEMALE, false, true",
        "FEMALE, false, false",
    )
    fun `유저 프로필 생성 - user가 기존에 존재`(
        gender: UserGender,
        agreementServiceTerms: Boolean,
        agreementPrivatePolicy: Boolean,
    ) {
        val vo = CreateUserProfileVo(
            nickname = "pita",
            birthday = LocalDate.now(),
            gender = gender,
            agreementServiceTerms = agreementServiceTerms,
            agreementPrivatePolicy = agreementPrivatePolicy,
        )
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)
        every { mockkUserSettingRepository.existsByUserAndIsDeleted(any()) } returns true

        val result = spykUserService.createProfile(vo, DateTimeUtil.KST_ZONE_ID, userId)

        with(result) {
            assertThat(id).isEqualTo(user.id)
            assertThat(nickname).isEqualTo(vo.nickname)
            assertThat(userStatus).isEqualTo(UserStatus.SINGLE)
            assertThat(user.birthDate).isEqualTo(vo.birthday)
            assertThat(user.gender).isEqualTo(vo.gender)
        }
        verify(exactly = 1) {
            mockkUserSettingRepository.existsByUserAndIsDeleted(eq(user))
            user.register(eq(vo.nickname), eq(vo.birthday), eq(vo.gender), eq(DateTimeUtil.KST_ZONE_ID))
        }
        verify(exactly = 0) {
            mockkUserSettingRepository.save(any())
        }
    }

    @ParameterizedTest
    @CsvSource(
        "MALE, true, true",
        "MALE, true, false",
        "MALE, false, true",
        "MALE, false, false",
        "FEMALE, true, true",
        "FEMALE, true, false",
        "FEMALE, false, true",
        "FEMALE, false, false",
    )
    fun `유저 프로필 생성 - user가 기존에 미존재`(
        gender: UserGender,
        agreementServiceTerms: Boolean,
        agreementPrivatePolicy: Boolean,
    ) {
        val vo = CreateUserProfileVo(
            nickname = "pita",
            birthday = LocalDate.now(),
            gender = gender,
            agreementServiceTerms = agreementServiceTerms,
            agreementPrivatePolicy = agreementPrivatePolicy,
        )
        val slot = slot<UserSetting>()
        val userSetting = UserSetting(user)
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)
        every { mockkUserSettingRepository.existsByUserAndIsDeleted(any()) } returns false
        every { mockkUserSettingRepository.save(any()) } returns userSetting

        val result = spykUserService.createProfile(vo, DateTimeUtil.KST_ZONE_ID, userId)

        with(result) {
            assertThat(id).isEqualTo(user.id)
            assertThat(nickname).isEqualTo(vo.nickname)
            assertThat(userStatus).isEqualTo(UserStatus.SINGLE)
            assertThat(user.birthDate).isEqualTo(vo.birthday)
            assertThat(user.gender).isEqualTo(vo.gender)
        }
        verify(exactly = 1) {
            mockkUserSettingRepository.existsByUserAndIsDeleted(eq(user))
            mockkUserSettingRepository.save(capture(slot))
            user.register(eq(vo.nickname), eq(vo.birthday), eq(vo.gender), eq(DateTimeUtil.KST_ZONE_ID))
        }
        assertThat(slot.captured.user).isEqualTo(userSetting.user)
    }

    @ParameterizedTest
    @CsvSource(
        "MALE, true, true",
        "MALE, true, false",
        "MALE, false, true",
        "MALE, false, false",
        "FEMALE, true, true",
        "FEMALE, true, false",
        "FEMALE, false, true",
        "FEMALE, false, false",
    )
    fun `유저 프로필 생성 - findByIdAndNotDeleted 가 null 인 경우`(
        gender: UserGender,
        agreementServiceTerms: Boolean,
        agreementPrivatePolicy: Boolean,
    ) {
        val vo = CreateUserProfileVo(
            nickname = "pita",
            birthday = LocalDate.now(),
            gender = gender,
            agreementServiceTerms = agreementServiceTerms,
            agreementPrivatePolicy = agreementPrivatePolicy,
        )
        every { mockkUserRepository.findById(any()) } returns Optional.empty()

        val result = assertThrows<UserNotFoundException> {
            spykUserService.createProfile(vo, DateTimeUtil.KST_ZONE_ID, userId)
        }
        assertThat(result.errorCode).isEqualTo(NOT_FOUND)

        verify(exactly = 1) {
            mockkUserRepository.findById(eq(userId))
        }
        verify(exactly = 0) {
            user.register(any(), any(), any(), eq(DateTimeUtil.KST_ZONE_ID))
        }
    }

    @Test
    fun `user 의 프로필을 업데이트- nickname, birthdate 존재`() {
        val vo = UpdateUserProfileVo(nickname = "pita", birthday = LocalDate.now())
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)

        val result = spykUserService.updateProfile(vo, DateTimeUtil.KST_ZONE_ID, userId)

        with(result) {
            assertThat(id).isEqualTo(userId)
            assertThat(nickname).isEqualTo(vo.nickname)
            assertThat(birthday).isEqualTo(vo.birthday)
        }
        verify(exactly = 1) {
            mockkUserRepository.findById(eq(userId))
            user.updateBirthDate(eq(result.birthday), eq(DateTimeUtil.KST_ZONE_ID))
        }
    }

    @Test
    fun `user 의 프로필을 업데이트 - nickname null`() {
        val vo = UpdateUserProfileVo(nickname = null, birthday = LocalDate.now())
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)

        val result = spykUserService.updateProfile(vo, DateTimeUtil.KST_ZONE_ID, userId)

        with(result) {
            assertThat(id).isEqualTo(userId)
            assertThat(nickname).isEqualTo(user.nickname)
            assertThat(nickname).isNotNull()
            assertThat(birthday).isEqualTo(vo.birthday)
        }
        verify(exactly = 1) {
            mockkUserRepository.findById(eq(userId))
            user.updateBirthDate(eq(result.birthday), eq(DateTimeUtil.KST_ZONE_ID))
        }
    }

    @Test
    fun `user 의 프로필을 업데이트 - nickname 이 "" 로 빈값`() {
        val vo = UpdateUserProfileVo(nickname = "", birthday = LocalDate.now())
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)

        val result = spykUserService.updateProfile(vo, DateTimeUtil.KST_ZONE_ID, userId)

        with(result) {
            assertThat(id).isEqualTo(userId)
            assertThat(nickname).isEqualTo(user.nickname)
            assertThat(nickname).isNotNull()
            assertThat(birthday).isEqualTo(vo.birthday)
        }
        verify(exactly = 1) {
            mockkUserRepository.findById(eq(userId))
            user.updateBirthDate(eq(result.birthday), eq(DateTimeUtil.KST_ZONE_ID))
        }
    }

    /**
     * user 내부의 함수가 안 불렸는지 체크하기 위해 spyk 로 user 를 감쌌습니다
     */
    @Test
    fun `user 의 프로필을 업데이트 - birthday 가 null`() {
        val vo = UpdateUserProfileVo(nickname = "pita", birthday = null)
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)

        val result = spykUserService.updateProfile(vo, DateTimeUtil.KST_ZONE_ID, userId)

        with(result) {
            assertThat(id).isEqualTo(userId)
            assertThat(nickname).isEqualTo(vo.nickname)
            assertThat(birthday).isNotNull()
            assertThat(birthday).isNotEqualTo(vo.birthday)
            assertThat(birthday).isEqualTo(user.birthDate)
        }
        verify(exactly = 1) {
            mockkUserRepository.findById(eq(userId))
        }
        verify(exactly = 0) {
            user.updateBirthDate(result.birthday, DateTimeUtil.KST_ZONE_ID)
        }
    }

    @Test
    fun `user 의 프로필을 업데이트 - findByIdAndNotDeleted가 null 반환`() {
        val vo = UpdateUserProfileVo(nickname = "", birthday = LocalDate.now())
        every { mockkUserRepository.findById(any()) } returns Optional.empty()

        val result = assertThrows<UserNotFoundException> {
            spykUserService.updateProfile(vo, DateTimeUtil.KST_ZONE_ID, userId)
        }

        assertThat(result.errorCode).isEqualTo(NOT_FOUND)

        verify(exactly = 1) {
            mockkUserRepository.findById(eq(userId))
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
        val vo = UpdateUserSettingVo(notificationEnabled = setting.not())
        val result = userService.updateUserSetting(updateUserSettingVo = vo, userId = userId)

        // then
        assertThat(result.notificationEnabled).isEqualTo(setting.not())
    }

    @ParameterizedTest
    @CsvSource(
        "true", "false"
    )
    fun `유저의 세팅을 업데이트 - vo null 이면 기존 그대로 응답`(setting: Boolean) {
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
        val vo = UpdateUserSettingVo(notificationEnabled = null)
        val result = userService.updateUserSetting(updateUserSettingVo = vo, userId = userId)

        // then
        assertThat(result.notificationEnabled).isEqualTo(setting)
    }

    @Test
    fun `유저의 세팅을 업데이트 하려하지만, 유저 설정 정보를 찾을 수 없음`() {
        // given
        whenever(mockUserRepository.getReferenceById(Mockito.anyLong()))
            .thenReturn(user)

        // when
        val vo = UpdateUserSettingVo(notificationEnabled = true)
        val result = assertThrows<UserIllegalStateException> {
            userService.updateUserSetting(updateUserSettingVo = vo, userId = userId)
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
        val vo = UpdateUserSettingVo(notificationEnabled = true)
        val result = assertThrows<UserIllegalStateException> {
            userService.updateUserSetting(updateUserSettingVo = vo, userId = userId)
        }

        // then
        assertThat(result.errorCode).isEqualTo(UserExceptionCode.SETTING_DATA_NOT_FOUND)
    }

    @Test
    fun `유저의 세팅을 가져옵니다`() {
        // given
        val response = UserSetting(user = user, notificationEnabled = false)
        val expected = UserSettingVo(notificationEnabled = false)
        every { mockkUserRepository.getReferenceById(any()) } returns user
        every { mockkUserSettingRepository.findByUserAndIsDeleted(user = any(), isDeleted = any()) } returns response

        // when
        val result = spykUserService.getUserSetting(userId = userId)

        // then
        assertThat(result).isEqualTo(expected)
        verify(exactly = 1) {
            spykUserService.getUserSetting(userId = eq(userId))
        }
    }

    @Test
    fun `유저의 세팅을 가져오는데, null이 나온 경우 UserIllegalStateException 을 받는다`() {
        // given
        every { mockkUserRepository.getReferenceById(any()) } returns user
        every { mockkUserSettingRepository.findByUserAndIsDeleted(user = any(), isDeleted = any()) } returns null

        // when
        val result = assertThrows<UserIllegalStateException> {
            spykUserService.getUserSetting(userId = userId)
        }

        // then
        assertThat(result.errorCode).isEqualTo(UserExceptionCode.SETTING_DATA_NOT_FOUND)

        verify(exactly = 1) {
            spykUserService.getUserSetting(userId = eq(userId))
        }
    }

    @Test
    fun `내 정보를 가져오는데 성공`() {
        // given
        val expected = UserInfoVo(
            id = user.id,
            email = user.email,
            birthDate = user.birthDate,
            signInPlatform = user.platform,
            nickname = user.nickname,
            gender = user.gender,
            userStatus = user.userStatus
        )
        every { mockkUserRepository.findById(any()) } returns Optional.of(user)

        // when
        val result = spykUserService.getUserInfo(userId = userId)

        assertThat(result).isEqualTo(expected)
        verify(exactly = 1) {
            mockkUserRepository.findById(eq(userId))
        }
    }

    @Test
    fun `내 정보를 가져오는데, null을 반환하는 경우`() {
        // given
        every { mockkUserRepository.findById(any()) } returns Optional.empty()

        // when
        val result = assertThrows<UserNotFoundException> { spykUserService.getUserInfo(userId = userId) }

        assertThat(result.errorCode).isEqualTo(NOT_FOUND)

        verify(exactly = 1) {
            mockkUserRepository.findById(eq(userId))
        }
    }

    @Test
    @DisplayName("Couple 정보가 담긴 User 정보를 받는다")
    fun getUserWithCoupleInformation() {
        // given
        every { mockkUserRepository.findByIdWithCouple(any()) } returns user
        val expected = UserVo.from(user)

        // when
        val userVo = spykUserService.getUserWithCouple(userId = userId)

        // then
        assertThat(userVo).isNotNull
        assertThat(userVo).isEqualTo(expected)
        verify(exactly = 1) {
            mockkUserRepository.findByIdWithCouple(eq(userId))
        }
    }

    @Test
    @DisplayName("Couple 정보가 담긴 User 정보를 요청했지만 null 인 경우")
    fun getNullUserWithCoupleInformation() {
        // given
        every { mockkUserRepository.findByIdWithCouple(any()) } returns null

        // when
        val userVo = spykUserService.getUserWithCouple(userId = userId)

        // then
        assertThat(userVo).isNull()
        verify(exactly = 1) {
            mockkUserRepository.findByIdWithCouple(eq(userId))
        }
    }

    private fun createUser() = com.whatever.caramel.domain.user.model.User(
        id = 1L,
        platform = LoginPlatform.TEST,
        platformUserId = UUID.randomUUID().toString(),
        nickname = "tjrwn",
        birthDate = LocalDate.now(),
    )
}
