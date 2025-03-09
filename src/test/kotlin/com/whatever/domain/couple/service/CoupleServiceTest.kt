package com.whatever.domain.couple.service

import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.couple.exception.CoupleExceptionCode
import com.whatever.domain.user.model.UserStatus
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.RedisUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.Duration
import java.time.temporal.ChronoUnit

@ActiveProfiles("test")
@SpringBootTest
class CoupleServiceTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val coupleService: CoupleService,
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
        securityUtilMock.apply {
            `when`(SecurityUtil.getCurrentUserStatus()).thenReturn(UserStatus.COUPLED)
            `when`(SecurityUtil.getCurrentUserId()).thenReturn(1L)
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
        securityUtilMock.apply {
            `when`(SecurityUtil.getCurrentUserStatus()).thenReturn(UserStatus.SINGLE)
            `when`(SecurityUtil.getCurrentUserId()).thenReturn(1L)
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
        securityUtilMock.apply {
            `when`(SecurityUtil.getCurrentUserStatus()).thenReturn(UserStatus.SINGLE)
            `when`(SecurityUtil.getCurrentUserId()).thenReturn(1L)
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

    @DisplayName("신규 초대 코드 저장 실패 시 예외가 발생한다.")
    @Test
    fun createInvitationCode_WithSaveFailure() {
        // given
        securityUtilMock.apply {
            `when`(SecurityUtil.getCurrentUserStatus()).thenReturn(UserStatus.SINGLE)
            `when`(SecurityUtil.getCurrentUserId()).thenReturn(1L)
        }
        `when`(redisUtil.getCoupleInvitationCode(1L)).thenReturn(null)
        `when`(redisUtil.getCoupleInvitationUser(anyString())).thenReturn(null)
        `when`(redisUtil.saveCoupleInvitationCode(
            eq(1L),
            anyString(),
            org.mockito.kotlin.eq(Duration.ofDays(1))
        )).thenReturn(false)

        // when, then
        val exception = assertThrows<CoupleException> {
            coupleService.createInvitationCode()
        }
        assertThat(exception.errorCode).isEqualTo(CoupleExceptionCode.INVITATION_CODE_GENERATION_FAIL)
        assertThat(exception.detailMessage).contains("invitation code conflict")

        assertThatThrownBy { coupleService.createInvitationCode() }
            .isInstanceOf(CoupleException::class.java)
            .hasMessage("초대 코드 생성에 실패했습니다.")
    }
}