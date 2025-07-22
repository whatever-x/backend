package com.whatever.caramel.domain.user.service

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.content.service.createCouple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserGender
import com.whatever.caramel.domain.user.model.UserSetting
import com.whatever.caramel.domain.user.model.UserStatus
import com.whatever.caramel.domain.user.repository.UserRepository
import com.whatever.caramel.domain.user.repository.UserSettingRepository
import com.whatever.caramel.domain.user.vo.UpdateUserSettingVo
import com.whatever.caramel.domain.user.vo.UserInfoVo
import com.whatever.caramel.domain.user.vo.UserSettingVo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test

@CaramelDomainSpringBootTest
class UserServiceTest {

    @Autowired
    private lateinit var coupleRepository: CoupleRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userSettingRepository: UserSettingRepository

    @DisplayName("상태가 NEW인 유저 정보를 확인한다.")
    @Test
    fun getUserInfo_WithNewUser() {
        // given
        val newUser = createNewUser(userRepository)
        val expectedResult = UserInfoVo(
            id = newUser.id,
            email = null,
            birthDate = null,
            signInPlatform = LoginPlatform.TEST,
            nickname = null,
            gender = null,
            userStatus = UserStatus.NEW
        )

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
        val expectedResult = UserInfoVo(
            id = singleUser.id,
            email = null,
            birthDate = DateTimeUtil.localNow().toLocalDate(),
            signInPlatform = LoginPlatform.TEST,
            nickname = "testuser",
            gender = UserGender.MALE,
            userStatus = UserStatus.SINGLE
        )

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
        val expectedResult = UserInfoVo(
            id = coupleUser.id,
            email = null,
            birthDate = coupleUser.birthDate,
            signInPlatform = LoginPlatform.TEST,
            nickname = coupleUser.nickname,
            gender = coupleUser.gender,
            userStatus = UserStatus.COUPLED
        )

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
        val userSetting = UserSetting(singleUser, notificationEnabled)
        userSettingRepository.save(userSetting)

        val vo = UpdateUserSettingVo(notificationEnabled)
        val expected = UserSettingVo(notificationEnabled)

        // when
        val result = userService.updateUserSetting(updateUserSettingVo = vo, userId = singleUser.id)

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

        val userSetting = UserSetting(singleUser, notificationEnabled)
        userSettingRepository.save(userSetting)

        val vo = UpdateUserSettingVo(null)
        val expected = UserSettingVo(notificationEnabled)

        // when
        val result = userService.updateUserSetting(updateUserSettingVo = vo, userId = singleUser.id)

        // then
        assertThat(result.notificationEnabled).isEqualTo(expected.notificationEnabled)
    }
}

internal fun createNewUser(
    userRepository: UserRepository,
): com.whatever.caramel.domain.user.model.User {
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
): com.whatever.caramel.domain.user.model.User {
    return userRepository.save(
        com.whatever.caramel.domain.user.model.User(
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
