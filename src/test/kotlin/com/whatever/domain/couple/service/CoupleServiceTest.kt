package com.whatever.domain.couple.service

import com.whatever.domain.couple.controller.dto.request.CreateCoupleRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleSharedMessageRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleStartDateRequest
import com.whatever.domain.couple.exception.CoupleAccessDeniedException
import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.repository.CoupleRepository
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
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
    private val coupleRepository: CoupleRepository,
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
        val user = userRepository.save(
            User(
                platform = LoginPlatform.KAKAO,
                platformUserId = "test-user-id",
                userStatus = UserStatus.COUPLED
            )
        )
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
        val user = userRepository.save(
            User(
                platform = LoginPlatform.KAKAO,
                platformUserId = "test-user-id",
                userStatus = UserStatus.SINGLE
            )
        )
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
        val user = userRepository.save(
            User(
                platform = LoginPlatform.KAKAO,
                platformUserId = "test-user-id",
                userStatus = UserStatus.SINGLE
            )
        )
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

    @DisplayName("Couple의 member가 정보를 조회할 경우 멤버정보가 포함된 커플정보를 반환한다.")
    @Test
    fun getCoupleInfo() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple()

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }

        // when
        val result = coupleService.getCoupleInfo(savedCouple.id)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.myInfo.id).isEqualTo(myUser.id)
        assertThat(result.partnerInfo.id).isEqualTo(partnerUser.id)
    }

    @DisplayName("Couple의 member가 아닌 유저가 정보를 조회할 경우 예외를 반환한다.")
    @Test
    fun getCoupleInfo_ByOtherUser() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple()

        val otherUser = userRepository.save(
            User(
                nickname = "other",
                birthDate = DateTimeUtil.localNow().toLocalDate(),
                platform = LoginPlatform.KAKAO,
                platformUserId = "other-user-id",
                userStatus = UserStatus.COUPLED
            )
        )

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(otherUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(otherUser.id)
        }

        // when, then
        assertThatThrownBy { coupleService.getCoupleInfo(savedCouple.id) }
            .isInstanceOf(CoupleAccessDeniedException::class.java)
            .hasMessage("커플에 속한 유저가 아닙니다.")
    }

    @DisplayName("각각 Single인 초대유저의 코드로, 등록유저가 등록하면 커플이 생성된다.")
    @Test
    fun createCouple() {
        // given
        val myUser = userRepository.save(
            User(
                nickname = "my",
                birthDate = DateTimeUtil.localNow().toLocalDate(),
                platform = LoginPlatform.KAKAO,
                platformUserId = "my-user-id",
                userStatus = UserStatus.SINGLE
            )
        )
        val hostUser = userRepository.save(
            User(
                nickname = "host",
                birthDate = DateTimeUtil.localNow().toLocalDate(),
                platform = LoginPlatform.KAKAO,
                platformUserId = "host-user-id",
                userStatus = UserStatus.SINGLE
            )
        )
        val request = CreateCoupleRequest("test-invitation-code")

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }
        whenever(redisUtil.getCoupleInvitationUser(request.invitationCode)).doReturn(hostUser.id)


        // when
        val result = coupleService.createCouple(request)


        // then
        assertThat(result.myInfo.id).isEqualTo(myUser.id)
        assertThat(result.partnerInfo.id).isEqualTo(hostUser.id)
    }

    @DisplayName("자신이 만든 초대 코드를 등록시 예외를 반환한다.")
    @Test
    fun createCouple_BySelfCreateInvitationCode() {
        // given
        val hostUser = userRepository.save(
            User(
                nickname = "host",
                birthDate = DateTimeUtil.localNow().toLocalDate(),
                platform = LoginPlatform.KAKAO,
                platformUserId = "host-user-id",
                userStatus = UserStatus.SINGLE
            )
        )
        val request = CreateCoupleRequest("test-invitation-code")

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(hostUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(hostUser.id)
        }
        whenever(redisUtil.getCoupleInvitationUser(request.invitationCode)).doReturn(hostUser.id)


        // when, then
        assertThatThrownBy { coupleService.createCouple(request) }
            .isInstanceOf(CoupleException::class.java)
            .hasMessage("스스로 생성한 코드는 사용할 수 없습니다.")
    }

    @DisplayName("커플 시작일을 업데이트시 변경된 응답이 반환된다.")
    @Test
    fun updateStartDate() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple()
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }
        val request = UpdateCoupleStartDateRequest(DateTimeUtil.localNow().toLocalDate())

        // when
        val result = coupleService.updateStartDate(request)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.startDate).isEqualTo(request.startDate)
    }

    @DisplayName("커플 공유메시지를 업데이트시 변경된 응답이 반환된다.")
    @Test
    fun updateSharedMessage() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple()
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }
        val request = UpdateCoupleSharedMessageRequest("new message")

        // when
        val result = coupleService.updateSharedMessage(request)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.sharedMessage).isEqualTo(request.sharedMessage)
    }

    @DisplayName("Blank인 커플 공유메시지를 업데이트시 null인 공유 메시지가 반환된다.")
    @Test
    fun updateSharedMessage_WithBlankMessage() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple()
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }
        val request = UpdateCoupleSharedMessageRequest("           ")

        // when
        val result = coupleService.updateSharedMessage(request)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.sharedMessage).isNull()
    }

    @DisplayName("null인 커플 공유메시지를 업데이트시 null인 공유 메시지가 반환된다.")
    @Test
    fun updateSharedMessage_WithNullMessage() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple()
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }
        val request = UpdateCoupleSharedMessageRequest(null)

        // when
        val result = coupleService.updateSharedMessage(request)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.sharedMessage).isNull()
    }

    private fun makeCouple(): Triple<User, User, Couple> {
        val myUser = userRepository.save(
            User(
                nickname = "my",
                birthDate = DateTimeUtil.localNow().toLocalDate(),
                platform = LoginPlatform.KAKAO,
                platformUserId = "my-user-id",
                userStatus = UserStatus.SINGLE
            )
        )
        val partnerUser = userRepository.save(
            User(
                nickname = "partner",
                birthDate = DateTimeUtil.localNow().toLocalDate(),
                platform = LoginPlatform.KAKAO,
                platformUserId = "partner-user-id",
                userStatus = UserStatus.SINGLE
            )
        )

        val startDate = DateTimeUtil.localNow().toLocalDate()
        val sharedMessage = "test message"
        val savedCouple = coupleRepository.save(
            Couple(
                startDate = startDate,
                sharedMessage = sharedMessage
            )
        )
        myUser.setCouple(savedCouple)
        partnerUser.setCouple(savedCouple)

        userRepository.save(myUser)
        userRepository.save(partnerUser)
        return Triple(myUser, partnerUser, savedCouple)
    }
}
