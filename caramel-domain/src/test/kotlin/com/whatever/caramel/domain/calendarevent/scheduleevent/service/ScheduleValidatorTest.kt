package com.whatever.caramel.domain.calendarevent.scheduleevent.service

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.calendarevent.exception.ScheduleAccessDeniedException
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode.ILLEGAL_DURATION
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode.ILLEGAL_PARTNER_STATUS
import com.whatever.caramel.domain.calendarevent.exception.ScheduleIllegalArgumentException
import com.whatever.caramel.domain.calendarevent.model.ScheduleEvent
import com.whatever.caramel.domain.calendarevent.service.ScheduleValidator
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.model.ContentDetail
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserGender
import com.whatever.caramel.domain.user.model.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime
import java.util.*

class ScheduleValidatorTest {

    private val validator = ScheduleValidator()

    @DisplayName("제목과 본문 둘 중 하나라도 값이 있다면 검증에 성공한다.")
    @ParameterizedTest
    @CsvSource(
        "test title, test description",
        "test title, ",
        ", test description",
    )
    fun validateContentDetail_whenValidTitleAndDescription(
        title: String?,
        description: String?,
    ) {
        // given, when, then
        assertDoesNotThrow {
            validator.validateContentDetail(title, description)
        }
    }

    @DisplayName("제목과 본문이 모두 null이거나, 하나라도 공백이라면 예외를 반환한다.")
    @ParameterizedTest
    @CsvSource(
        ",",         // (null, null)
        ", '  '",             // (null, blank)
        "'' , ",              // (blank, null)
        "'' , ' '",           // (blank, blank)
        "test title, '   '",  // (non-null, blank)
        "'    ', test desc"  // (blank, non-null)
    )
    fun validateContentDetail_whenInvalidTitleAndDescription_thenThrowException(
        title: String?,
        description: String?,
    ) {
        // given, when
        val result = assertThrows<ScheduleIllegalArgumentException> {
            validator.validateContentDetail(title, description)
        }

        // then
        assertThat(result.errorCode).isEqualTo(ILLEGAL_CONTENT_DETAIL)
    }

    private val now = LocalDateTime.now()

    @DisplayName("시작일이 종료일보다 이전일 때 검증에 성공한다.")
    @Test
    fun validateDuration_whenStartIsBeforeEnd() {
        // given, when, then
        assertDoesNotThrow {
            validator.validateDuration(now, now.plusNanos(1))
        }
    }

    @DisplayName("시작일과 종료일이 같을 때 검증에 성공한다.")
    @Test
    fun validateDuration_whenStartEqualsEnd() {
        // given, when, then
        assertDoesNotThrow {
            validator.validateDuration(now, now)
        }
    }

    @DisplayName("시작일이 종료일보다 이후라면 예외를 반환한다.")
    @Test
    fun validateDuration_whenStartIsAfterEnd_thenThrowException() {
        // given, when
        val result = assertThrows<ScheduleIllegalArgumentException> {
            validator.validateDuration(now, now.minusDays(1))
        }

        // then
        assertThat(result.errorCode).isEqualTo(ILLEGAL_DURATION)
    }

    @DisplayName("시작일이 null일 경우 검증에 성공한다.")
    @Test
    fun validateDuration_whenStartIsNull() {
        // given, when, then
        assertDoesNotThrow {
            validator.validateDuration(null, now)
        }
    }

    @DisplayName("종료일이 null일 경우 검증에 성공한다.")
    @Test
    fun validateDuration_whenEndIsNull() {
        // given, when, then
        assertDoesNotThrow {
            validator.validateDuration(now, null)
        }
    }

    @Test
    @DisplayName("사용자가 일정 소유자일 때 검증이 성공한다.")
    fun validateUserAccess_whenUserIsOwner() {
        // given
        val couple = createDummyCouple()
        val owner = couple.members.first()
        val schedule = createDummyScheduleEvent(owner)

        // when & then
        assertDoesNotThrow {
            validator.validateUserAccess(
                scheduleEvent = schedule,
                currentUserId = owner.id,
                currentUserCoupleId = couple.id,
            )
        }
    }

    @Test
    @DisplayName("파트너가 일정 소유자일 때 검증이 성공한다.")
    fun validateUserAccess_whenUserIsPartnerInSameCouple_thenDoesNotThrowException() {
        // given
        val couple = createDummyCouple()
        val myUser = couple.members.first()
        val partnerUser = couple.members.last()
        val schedule = createDummyScheduleEvent(owner = partnerUser)

        // when & then
        assertDoesNotThrow {
            validator.validateUserAccess(
                scheduleEvent = schedule,
                currentUserId = myUser.id,
                currentUserCoupleId = couple.id,
            )
        }
    }

    @Test
    @DisplayName("일정 소유자가 SINGLE 상태이고, 다른 유저가 접근한다면 예외를 반환한다.")
    fun validateUserAccess_whenOwnerIsSingleAndUserIsNotOwner_thenThrowException() {
        // given
        val couple = createDummyCouple()
        val myUser = couple.members.first()
        val partnerUser = couple.members.last()
        val schedule = createDummyScheduleEvent(owner = partnerUser)
        partnerUser.updateUserStatus(UserStatus.SINGLE)

        // when
        val result = assertThrows<ScheduleAccessDeniedException> {
            validator.validateUserAccess(
                scheduleEvent = schedule,
                currentUserId = myUser.id,
                currentUserCoupleId = couple.id,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(ILLEGAL_PARTNER_STATUS)
    }

    @Test
    @DisplayName("다른 커플에 속한 사용자가 일정에 접근할 경우 예외를 반환한다.")
    fun validateUserAccess_whenUserIsInDifferentCouple_thenThrowException() {
        // given
        val couple = createDummyCouple(
            id = 0,
            user1Id = 0,
            user2Id = 1,
        )
        val schedule = createDummyScheduleEvent(owner = couple.members.first())

        val otherCouple = createDummyCouple(
            id = 1,
            user1Id = 2,
            user2Id = 3,
        )
        val otherUser = otherCouple.members.first()

        // when
        val result = assertThrows<ScheduleAccessDeniedException> {
            validator.validateUserAccess(
                scheduleEvent = schedule,
                currentUserId = otherUser.id,
                currentUserCoupleId = otherCouple.id,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(COUPLE_NOT_MATCHED)
    }

    @Test
    @DisplayName("소유자가 커플이지만 커플 정보가 null일 때 다른 사용자가 접근하면 예외를 던짐")
    fun validateUserAccess_whenOwnerCoupleInfoIsNull_thenThrowException() {
        // given
        val couple = createDummyCouple()
        val owner = couple.members.first()
        val schedule = createDummyScheduleEvent(owner)
        owner.leaveFromCouple()
        owner.updateUserStatus(UserStatus.COUPLED)  // DB 정합성에 문제가 발생했을 경우

        val otherUser = couple.members.last()

        // when
        val result = assertThrows<ScheduleAccessDeniedException> {
            validator.validateUserAccess(
                scheduleEvent = schedule,
                currentUserId = otherUser.id,
                currentUserCoupleId = couple.id,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(ILLEGAL_PARTNER_STATUS)
    }

    private fun createDummyCouple(id: Long = 0L, user1Id: Long = 0L, user2Id: Long = 1L): Couple {
        val user1 = createDummyUser(user1Id)
        val user2 = createDummyUser(user2Id)
        return Couple(
            id = id,
        ).apply { addMembers(user1, user2) }
    }

    private fun createDummyUser(id: Long = 0L): User {
        return User(
            id = id,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString(),
            nickname = "testnick",
            gender = UserGender.FEMALE,
            userStatus = UserStatus.SINGLE,
        )
    }

    private fun createDummyScheduleEvent(owner: User): ScheduleEvent {
        val content = Content(
            id = 0L, user = owner,
            contentDetail = ContentDetail(title = "title", description = "desc"),
            type = ContentType.SCHEDULE,
        )
        return ScheduleEvent(
            id = 0L, content = content,
            uid = UUID.randomUUID().toString(),
            startDateTime = now,
            endDateTime = now.plusDays(7),
            startTimeZone = DateTimeUtil.KST_ZONE_ID,
            endTimeZone = DateTimeUtil.KST_ZONE_ID,
        )
    }
}