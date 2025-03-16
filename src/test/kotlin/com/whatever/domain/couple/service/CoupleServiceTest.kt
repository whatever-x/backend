package com.whatever.domain.couple.service

import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.RedisUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.temporal.ChronoUnit

@ActiveProfiles("test")
@SpringBootTest
class CoupleServiceTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val coupleService: CoupleService,
    private val userRepository: UserRepository,
) {

    @MockitoSpyBean
    private lateinit var redisUtil: RedisUtil

    private lateinit var securityUtilMock: AutoCloseable

    @BeforeEach
    fun setUp() {
        val connectionFactory = redisTemplate.connectionFactory
        check(connectionFactory != null)
        connectionFactory.connection.serverCommands().flushAll()
        securityUtilMock = mockStatic(SecurityUtil::class.java)

        userRepository.deleteAllInBatch()
    }

    @AfterEach
    fun tearDown() {
        securityUtilMock.close()  // static mock 초기화
        reset(redisUtil)  // redisUtil mock 초기화
    }

    @DisplayName("사용자 상태가 SINGLE이 아니면 예외가 발생한다.")
    @Test
    fun createInvitationCode_WithInvalidUserStatus() {
        // given
        val user = userRepository.save(User(
            platform = LoginPlatform.KAKAO,
            platformUserId = "test-user-id",
            userStatus = UserStatus.COUPLED
        ))
        securityUtilMock.apply {
            `when`(SecurityUtil.getCurrentUserStatus()).thenReturn(user.userStatus)
            `when`(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }

        // when, then
        assertThatThrownBy { coupleService.createInvitationCode() }
            .isInstanceOf(CoupleException::class.java)
            .hasMessage("기능을 이용할 수 없는 유저 상태입니다.")
    }

    @DisplayName("신규 초대 코드 생성 및 저장 성공 시 초대 코드를 반환한다.")
    @Test
    fun createInvitationCode() {
        // given
        val user = userRepository.save(User(
            platform = LoginPlatform.KAKAO,
            platformUserId = "test-user-id",
            userStatus = UserStatus.SINGLE
        ))
        securityUtilMock.apply {
            `when`(SecurityUtil.getCurrentUserStatus()).thenReturn(user.userStatus)
            `when`(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }
        val expectExpirationDateTime = DateTimeUtil.localNow().plusDays(1)

        // when
        val result = coupleService.createInvitationCode()

        // then
        assertThat(result.invitationCode).isNotBlank()
        assertThat(result.expirationDateTime).isCloseTo(
            expectExpirationDateTime,
            TemporalUnitWithinOffset(1L, ChronoUnit.SECONDS)
        )
    }

    @DisplayName("초대 코드가 만료되지 않았을 경우 같은 코드를 반환한다.")
    @Test
    fun createInvitationCode_WithSameRequestBeforeExpiration() {
        // given
        val user = userRepository.save(User(
            platform = LoginPlatform.KAKAO,
            platformUserId = "test-user-id",
            userStatus = UserStatus.SINGLE
        ))
        securityUtilMock.apply {
            `when`(SecurityUtil.getCurrentUserStatus()).thenReturn(user.userStatus)
            `when`(SecurityUtil.getCurrentUserId()).thenReturn(user.id)
        }
        val expectExpirationDateTime = DateTimeUtil.localNow().plusDays(1)

        // when
        val first = coupleService.createInvitationCode()
        val second = coupleService.createInvitationCode()

        // then
        assertThat(first.invitationCode).isNotBlank()
        assertThat(first.expirationDateTime).isCloseTo(
            expectExpirationDateTime,
            TemporalUnitWithinOffset(1L, ChronoUnit.SECONDS)
        )

        assertThat(second.invitationCode).isEqualTo(first.invitationCode)
        assertThat(second.expirationDateTime).isCloseTo(
            first.expirationDateTime,
            TemporalUnitWithinOffset(1L, ChronoUnit.SECONDS)
        )
    }
}