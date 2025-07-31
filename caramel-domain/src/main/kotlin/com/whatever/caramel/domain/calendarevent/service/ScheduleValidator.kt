package com.whatever.caramel.domain.calendarevent.service

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.domain.calendarevent.exception.ScheduleAccessDeniedException
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode
import com.whatever.caramel.domain.calendarevent.exception.ScheduleIllegalArgumentException
import com.whatever.caramel.domain.calendarevent.model.ScheduleEvent
import com.whatever.caramel.domain.couple.exception.CoupleException
import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.user.model.UserStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ScheduleValidator {
    fun validateContentDetail(title: String?, description: String?) {
        if (title == null && description == null) {
            throw ScheduleIllegalArgumentException(
                errorCode = ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL,
                errorUi = ErrorUi.Toast("제목이나 본문 중 하나는 입력해야 해요."),
            )
        }
        if ((title?.isBlank() == true) || (description?.isBlank() == true)) {
            throw ScheduleIllegalArgumentException(
                errorCode = ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL,
                errorUi = ErrorUi.Toast("공백은 입력할 수 없어요."),
            )
        }
    }

    fun validateDuration(startDateTime: LocalDateTime?, endDateTime: LocalDateTime?) {
        if (startDateTime != null && endDateTime?.isBefore(startDateTime) == true) {
            throw ScheduleIllegalArgumentException(
                errorCode = ScheduleExceptionCode.ILLEGAL_DURATION,
                errorUi = ErrorUi.Toast("시작일은 종료일보다 이전이어야 해요."),
            )
        }
    }

    fun validateUserAccess(
        scheduleEvent: ScheduleEvent,
        currentUserId: Long,
        currentUserCoupleId: Long,
    ) {
        val scheduleOwnerUser = scheduleEvent.content.user

        if (currentUserId == scheduleOwnerUser.id) {
            return
        }

        if (scheduleOwnerUser.userStatus == UserStatus.SINGLE) {
            throw ScheduleAccessDeniedException(errorCode = ScheduleExceptionCode.ILLEGAL_PARTNER_STATUS)
        }

        val scheduleOwnerCoupleId = scheduleOwnerUser.couple?.id
            ?: throw ScheduleAccessDeniedException(errorCode = ScheduleExceptionCode.ILLEGAL_PARTNER_STATUS)

        if (currentUserCoupleId != scheduleOwnerCoupleId) {
            throw ScheduleAccessDeniedException(errorCode = ScheduleExceptionCode.COUPLE_NOT_MATCHED)
        }
    }
}