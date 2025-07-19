package com.whatever.domain.user.service

import com.whatever.domain.content.service.createCouple
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.dto.GetUserInfoResponse
import com.whatever.domain.user.dto.PatchUserSettingRequest
import com.whatever.domain.user.dto.UserSettingResponse
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserSetting
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.domain.user.repository.UserSettingRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.caramel.common.util.DateTimeUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class UserServiceTest {

    @Autowired
    private lateinit var coupleRepository: CoupleRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userSettingRepository: UserSettingRepository

    private lateinit var mockSecurityUtil: AutoCloseable

    @BeforeEach
    fun setUp() {
        mockSecurityUtil = mockStatic(SecurityUtil::class.java)
    }

    @AfterEach
    fun tearDown() {
        mockSecurityUtil.close()
    }

    @DisplayName("상태가 NEW인 유저 정보를 확인한다.")
    @Test
    fun getUserInfo_WithNewUser() {
        // given
        val newUser = createNewUser(userRepository)
        val expectedResult = GetUserInfoResponse.from(newUser)

        // when
        val result = userService.getUserInfo(newUser.id)

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @DisplayName("상태가 SINGLE인 유저 정보를 확인한다.")
    @Test
    fun getUserInfo_WithSingleUser() {
        // given
        val singleUser = createSingleUser(
            userRepository = userRepository,
            birthDate = DateTimeUtil.localNow().toLocalDate(),
            nickname = "testuser",
            gender = UserGender.MALE,
        )
        val expectedResult = GetUserInfoResponse.from(singleUser)

        // when
        val result = userService.getUserInfo(singleUser.id)

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @DisplayName("상태가 COUPLED인 유저 정보를 확인한다.")
    @Test
    fun getUserInfo_WithCoupledUser() {
        // given
        val (coupleUser, _, _) = createCouple(userRepository, coupleRepository)
        val expectedResult = GetUserInfoResponse.from(coupleUser)

        // when
        val result = userService.getUserInfo(coupleUser.id)

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @DisplayName("updateUserSetting 을 수행하면, 요청대로 잘 업데이트 된다")
    @ParameterizedTest
    @CsvSource("true", "false")
    @Transactional
    fun updateUserSettingTest(notificationEnabled: Boolean) {
        val singleUser = createSingleUser(
            userRepository = userRepository,
            birthDate = DateTimeUtil.localNow().toLocalDate(),
            nickname = "testuser",
            gender = UserGender.MALE,
        )
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(singleUser.id)
        }
        val userSetting = UserSetting(singleUser, notificationEnabled)
        userSettingRepository.save(userSetting)

        val request = PatchUserSettingRequest(notificationEnabled)
        val expected = UserSettingResponse.from(userSetting)

        // when
        val result = userService.updateUserSetting(request = request)

        // then
        assertThat(result).isEqualTo(expected)
    }

    @DisplayName("updateUserSetting 을 수행, null 이면 기존 그대로 유지한다")
    @ParameterizedTest
    @CsvSource("true", "false")
    @Transactional
    fun updateUserSettingNullTest(notificationEnabled: Boolean) {
        val singleUser = createSingleUser(
            userRepository = userRepository,
            birthDate = DateTimeUtil.localNow().toLocalDate(),
            nickname = "testuser",
            gender = UserGender.MALE,
        )
        mockSecurityUtil.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(singleUser.id)
        }
        val userSetting = UserSetting(singleUser, notificationEnabled)
        userSettingRepository.save(userSetting)

        val request = PatchUserSettingRequest(null)
        val expected = UserSettingResponse.from(userSetting)

        // when
        val result = userService.updateUserSetting(request = request)

        // then
        assertThat(result.notificationEnabled).isEqualTo(expected.notificationEnabled)
    }
}

internal fun createNewUser(
    userRepository: UserRepository,
): User {
    return createUser(
        userRepository = userRepository,
        userStatus = UserStatus.NEW,
    )
}

internal fun createSingleUser(
    userRepository: UserRepository,
    birthDate: LocalDate,
    nickname: String,
    gender: UserGender,
): User {
    return createUser(
        userRepository = userRepository,
        birthDate = birthDate,
        nickname = nickname,
        gender = gender,
        userStatus = UserStatus.SINGLE,
    )
}

internal fun createUser(
    userRepository: UserRepository,
    email: String? = null,
    birthDate: LocalDate? = null,
    nickname: String? = null,
    gender: UserGender? = null,
    userStatus: UserStatus,
): User {
    return userRepository.save(
        User(
            email = email,
            birthDate = birthDate,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString(),
            nickname = nickname,
            gender = gender,
            userStatus = userStatus,
        )
    )
}
