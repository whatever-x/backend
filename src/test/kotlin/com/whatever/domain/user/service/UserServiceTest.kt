package com.whatever.domain.user.service

import com.whatever.domain.content.service.createCouple
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.dto.GetUserInfoResponse
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.util.DateTimeUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
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
