package com.whatever.domain.couple.service

import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.couple.controller.dto.request.CreateCoupleRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleSharedMessageRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleStartDateRequest
import com.whatever.domain.couple.exception.CoupleAccessDeniedException
import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.couple.exception.CoupleExceptionCode
import com.whatever.domain.couple.exception.CoupleIllegalArgumentException
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.model.CoupleStatus
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.couple.repository.InvitationCodeRedisRepository
import com.whatever.domain.couple.service.event.ExcludeAsyncConfigBean
import com.whatever.domain.firebase.service.FirebaseService
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserStatus.COUPLED
import com.whatever.domain.user.model.UserStatus.SINGLE
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.util.toZoneId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class CoupleServiceTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val coupleService: CoupleService,
    private val userRepository: UserRepository,
    private val coupleRepository: CoupleRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val tagRepository: TagRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
    private val contentRepository: ContentRepository,
) : ExcludeAsyncConfigBean() {

    @MockitoSpyBean
    private lateinit var inviCodeRedisRepository: InvitationCodeRedisRepository

    @MockitoBean
    private lateinit var firebaseService: FirebaseService

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
        reset(inviCodeRedisRepository)  // redisUtil mock 초기화

        tagContentMappingRepository.deleteAllInBatch()
        scheduleEventRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
    }

    @DisplayName("사용자 상태가 SINGLE이 아니면 예외가 발생한다.")
    @Test
    fun createInvitationCode_WithInvalidUserStatus() {
        // given
        val user = userRepository.save(
            User(
                platform = LoginPlatform.KAKAO,
                platformUserId = "test-user-id",
                userStatus = COUPLED
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
                userStatus = SINGLE
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
                userStatus = SINGLE
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
    fun getCoupleAndMemberInfo() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }

        // when
        val result = coupleService.getCoupleAndMemberInfo(savedCouple.id)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.myInfo.id).isEqualTo(myUser.id)
        assertThat(result.myInfo.gender).isEqualTo(myUser.gender!!)
        assertThat(result.partnerInfo.id).isEqualTo(partnerUser.id)
        assertThat(result.partnerInfo.gender).isEqualTo(partnerUser.gender!!)
    }

    @DisplayName("Couple의 member가 아닌 유저가 정보를 조회할 경우 예외를 반환한다.")
    @Test
    fun getCoupleAndMemberInfo_ByOtherUser() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)

        val otherUser = userRepository.save(
            User(
                nickname = "other",
                birthDate = DateTimeUtil.localNow().toLocalDate(),
                platform = LoginPlatform.KAKAO,
                platformUserId = "other-user-id",
                userStatus = COUPLED
            )
        )

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(otherUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(otherUser.id)
        }

        // when, then
        assertThatThrownBy { coupleService.getCoupleAndMemberInfo(savedCouple.id) }
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
                userStatus = SINGLE,
                gender = UserGender.MALE,
            )
        )
        val hostUser = userRepository.save(
            User(
                nickname = "host",
                birthDate = DateTimeUtil.localNow().toLocalDate(),
                platform = LoginPlatform.KAKAO,
                platformUserId = "host-user-id",
                userStatus = SINGLE,
                gender = UserGender.FEMALE,
            )
        )
        val request = CreateCoupleRequest("test-invitation-code")

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }
        whenever(inviCodeRedisRepository.getInvitationUser(request.invitationCode)).doReturn(hostUser.id)

        // when
        val result = coupleService.createCouple(request)

        // then
        assertThat(result.status).isEqualTo(CoupleStatus.ACTIVE)
        assertThat(result.myInfo.id).isEqualTo(myUser.id)
        assertThat(result.partnerInfo.id).isEqualTo(hostUser.id)

        verify(firebaseService, times(1))
            .sendNotification(
                targetUserIds = eq(setOf(myUser.id, hostUser.id)),
                fcmNotification = any(),
            )
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
                userStatus = SINGLE
            )
        )
        val request = CreateCoupleRequest("test-invitation-code")

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(hostUser.userStatus)
            whenever(SecurityUtil.getCurrentUserId()).doReturn(hostUser.id)
        }
        whenever(inviCodeRedisRepository.getInvitationUser(request.invitationCode)).doReturn(hostUser.id)

        // when, then
        assertThatThrownBy { coupleService.createCouple(request) }
            .isInstanceOf(CoupleException::class.java)
            .hasMessage(CoupleExceptionCode.INVITATION_CODE_SELF_GENERATED.message)
    }

    @DisplayName("커플 시작일을 업데이트시 변경된 응답이 반환된다.")
    @Test
    fun updateStartDate() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserCoupleId()).doReturn(savedCouple.id)
        }
        val request = UpdateCoupleStartDateRequest(DateTimeUtil.localNow().toLocalDate())
        val timeZone = "Asia/Seoul"
        // when
        val result = coupleService.updateStartDate(savedCouple.id, request, timeZone)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.startDate).isEqualTo(request.startDate)
    }

    @DisplayName("커플 시작일을 업데이트시 미래가 주어진다면 예외가 반환된다.")
    @Test
    fun updateStartDate_WithFutureDate() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserCoupleId()).doReturn(savedCouple.id)
        }

        // 미래 시간으로 설정
        val zonedStartDateTime = DateTimeUtil.zonedNow("Asia/Seoul".toZoneId()).plusDays(1)
        val request = UpdateCoupleStartDateRequest(
            startDate = zonedStartDateTime.toLocalDate()
        )

        // when, then
        val exception = assertThrows<CoupleIllegalArgumentException> {
            coupleService.updateStartDate(
                coupleId = savedCouple.id,
                request = request,
                timeZone = zonedStartDateTime.zone.id
            )
        }

        // then
        assertThat(exception).hasMessage(CoupleExceptionCode.ILLEGAL_START_DATE.message)
    }

    @DisplayName("커플 공유메시지를 업데이트시 변경된 응답이 반환된다.")
    @Test
    fun updateSharedMessage() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserCoupleId()).doReturn(savedCouple.id)
        }
        val request = UpdateCoupleSharedMessageRequest("new message")

        // when
        val result = coupleService.updateSharedMessage(savedCouple.id, request)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.sharedMessage).isEqualTo(request.sharedMessage)
    }

    @DisplayName("Blank인 커플 공유메시지를 업데이트시 null인 공유 메시지가 반환된다.")
    @Test
    fun updateSharedMessage_WithBlankMessage() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserCoupleId()).doReturn(savedCouple.id)
        }
        val request = UpdateCoupleSharedMessageRequest("           ")

        // when
        val result = coupleService.updateSharedMessage(savedCouple.id, request)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.sharedMessage).isNull()
    }

    @DisplayName("null인 커플 공유메시지를 업데이트시 null인 공유 메시지가 반환된다.")
    @Test
    fun updateSharedMessage_WithNullMessage() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserStatus()).doReturn(myUser.userStatus)
            whenever(SecurityUtil.getCurrentUserCoupleId()).doReturn(savedCouple.id)
        }
        val request = UpdateCoupleSharedMessageRequest(null)

        // when
        val result = coupleService.updateSharedMessage(savedCouple.id, request)

        // then
        assertThat(result.coupleId).isEqualTo(savedCouple.id)
        assertThat(result.sharedMessage).isNull()
    }
}

internal fun makeCouple(
    userRepository: UserRepository,
    coupleRepository: CoupleRepository,
    startDate: LocalDate = DateTimeUtil.localNow().toLocalDate(),
): Triple<User, User, Couple> {
    val myUser = userRepository.save(
        User(
            nickname = "my",
            birthDate = DateTimeUtil.localNow().toLocalDate(),
            platform = LoginPlatform.KAKAO,
            platformUserId = "my-user-id",
            userStatus = SINGLE,
            gender = UserGender.MALE,
        )
    )
    val partnerUser = userRepository.save(
        User(
            nickname = "partner",
            birthDate = DateTimeUtil.localNow().toLocalDate(),
            platform = LoginPlatform.KAKAO,
            platformUserId = "partner-user-id",
            userStatus = SINGLE,
            gender = UserGender.FEMALE,
        )
    )

    val sharedMessage = "test message"
    val savedCouple = coupleRepository.save(
        Couple(
            startDate = startDate,
            sharedMessage = sharedMessage
        )
    )
    savedCouple.addMembers(myUser, partnerUser)

    userRepository.save(myUser)
    userRepository.save(partnerUser)
    return Triple(myUser, partnerUser, savedCouple)
}
