package com.whatever.domain.user

import com.whatever.domain.user.dto.PostUserProfileRequest
import com.whatever.domain.user.exception.UserException
import com.whatever.domain.user.exception.UserExceptionCode
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.domain.user.service.UserService
import com.whatever.global.security.util.getCurrentUserId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.LocalDate
import java.util.*
import kotlin.reflect.jvm.javaMethod


class UserServiceTest {
    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var userService: UserService

    private val getCurrentUserIdMock = Mockito.mockStatic(::getCurrentUserId.javaMethod?.declaringClass)

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        userService = UserService(userRepository)

        getCurrentUserIdMock.`when`<Long> { getCurrentUserId() }.thenReturn(1L)

        `when`(userRepository.findById(1L)).thenReturn(
            Optional.of(
                User(
                    id = 1L,
                    userStatus = UserStatus.NEW,
                    platform = LoginPlatform.LOCAL,
                    platformUserId = "platformUserId"
                )
            )
        )
    }

    @AfterEach
    fun tearDown() {
        getCurrentUserIdMock.close()
    }

    @Test
    @DisplayName("유효한 요청으로 프로필 생성 성공")
    fun createProfileSuccess() {
        // given
        val request = PostUserProfileRequest(
            nickname = "evergreen",
            birthday = LocalDate.of(1997, 10, 29),
            agreementServiceTerms = true,
            agreementPrivatePolicy = true
        )

        // when
        val response = userService.createProfile(request)

        // then
        assertThat(response).isNotNull
        assertThat(response.id).isEqualTo(1L)
        assertThat(response.nickname).isEqualTo("evergreen")
        assertThat(response.userStatus).isEqualTo(UserStatus.NEW)
    }

    @Test
    @DisplayName("닉네임이 빈 값일 경우 예외 발생")
    fun emptyNickname() {
        // given
        val request = PostUserProfileRequest(
            nickname = "",
            birthday = LocalDate.of(1997, 10, 29),
            agreementServiceTerms = true,
            agreementPrivatePolicy = true
        )

        // when & then
        assertThatThrownBy { userService.createProfile(request) }
            .isInstanceOf(UserException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UserExceptionCode.NICKNAME_REQUIRED)
    }

    @Test
    @DisplayName("닉네임 길이가 2자 미만일 경우 예외 발생")
    fun nicknameTooShort() {
        // given
        val request = PostUserProfileRequest(
            nickname = "e",
            birthday = LocalDate.of(1997, 10, 29),
            agreementServiceTerms = true,
            agreementPrivatePolicy = true
        )

        // when & then
        assertThatThrownBy { userService.createProfile(request) }
            .isInstanceOf(UserException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UserExceptionCode.INVALID_NICKNAME_LENGTH)
    }

    @Test
    @DisplayName("닉네임 길이가 10자 초과일 경우 예외 발생")
    fun nicknameTooLong() {
        // given
        val request = PostUserProfileRequest(
            nickname = "evergreenever",
            birthday = LocalDate.of(1997, 10, 29),
            agreementServiceTerms = true,
            agreementPrivatePolicy = true
        )

        // when & then
        assertThatThrownBy { userService.createProfile(request) }
            .isInstanceOf(UserException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UserExceptionCode.INVALID_NICKNAME_LENGTH)
    }

    @Test
    @DisplayName("닉네임에 허용되지 않는 문자 포함시 예외 발생")
    fun invalidNicknameCharacter() {
        // given
        val request = PostUserProfileRequest(
            nickname = "테스트!",
            birthday = LocalDate.of(1997, 10, 29),
            agreementServiceTerms = true,
            agreementPrivatePolicy = true
        )

        // when & then
        assertThatThrownBy { userService.createProfile(request) }
            .isInstanceOf(UserException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UserExceptionCode.INVALID_NICKNAME_CHARACTER)
    }

    @Test
    @DisplayName("서비스 약관 동의가 false일 경우 예외 발생")
    fun serviceTermsNotAgreed() {
        // given
        val request = PostUserProfileRequest(
            nickname = "테스트유저",
            birthday = LocalDate.of(1997, 10, 29),
            agreementServiceTerms = false,
            agreementPrivatePolicy = true
        )

        // when & then
        assertThatThrownBy { userService.createProfile(request) }
            .isInstanceOf(UserException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UserExceptionCode.SERVICE_TERMS_AGREEMENT_REQUIRED)
    }

    @Test
    @DisplayName("개인정보 수집 동의가 false일 경우 예외 발생")
    fun privatePolicyNotAgreed() {
        // given
        val request = PostUserProfileRequest(
            nickname = "테스트유저",
            birthday = LocalDate.of(1997, 10, 29),
            agreementServiceTerms = true,
            agreementPrivatePolicy = false
        )

        // when & then
        assertThatThrownBy { userService.createProfile(request) }
            .isInstanceOf(UserException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UserExceptionCode.PRIVATE_POLICY_AGREEMENT_REQUIRED)
    }

}