package com.whatever.domain.calendarevent.scheduleevent.service

import com.whatever.domain.calendarevent.controller.dto.response.ScheduleDetailDto
import com.whatever.domain.calendarevent.scheduleevent.controller.dto.CreateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleAccessDeniedException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_CONTENT_ID
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_DURATION
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_PARTNER_STATUS
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.SCHEDULE_NOT_FOUND
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.UPDATE_CONFLICT
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalArgumentException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalStateException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleNotFoundException
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.controller.dto.response.ContentSummaryResponse
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.model.Tag
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.couple.exception.CoupleExceptionCode.COUPLE_NOT_FOUND
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.model.UserStatus.SINGLE
import com.whatever.global.exception.common.CaramelException
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.endOfDay
import com.whatever.util.findByIdAndNotDeleted
import com.whatever.util.toDateTime
import com.whatever.util.toZonId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

private val logger = KotlinLogging.logger {  }

@Service
class ScheduleEventService(
    private val scheduleEventRepository: ScheduleEventRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val tagRepository: TagRepository,
    private val coupleRepository: CoupleRepository,
    private val contentRepository: ContentRepository,
) {

    fun getSchedule(
        startDate: LocalDate,
        endDate: LocalDate,
        userTimeZone: String
    ): List<ScheduleDetailDto> {
        validateRequestDuration(
            startDateTime = startDate.toDateTime(),
            endDateTime = endDate.toDateTime()
        )

        val currentUserCoupleId = SecurityUtil.getCurrentUserCoupleId()
        val couple = coupleRepository.findByIdWithMembers(currentUserCoupleId)
            ?: throw CoupleException(errorCode = COUPLE_NOT_FOUND)

        if (couple.members.any { it.userStatus == SINGLE }) {
            throw ScheduleAccessDeniedException(errorCode = ILLEGAL_PARTNER_STATUS)
        }

        val memberIds = couple.members.map { it.id }.toSet()
        val coupleSchedules = scheduleEventRepository.findAllByDurationAndUsers(
            startDateTime = startDate.toDateTime(),
            endDateTime = endDate.toDateTime(),
            memberIds = memberIds
        )

        return coupleSchedules.map { se ->
            ScheduleDetailDto(
                scheduleId = se.id,
                startDateTime = se.startDateTime,
                endDateTime = se.endDateTime,
                startDateTimezone = se.startTimeZone.id,
                endDateTimezone = se.endTimeZone.id,
                isCompleted = se.content.contentDetail.isCompleted,
                title = se.content.contentDetail.title,
                description = se.content.contentDetail.description,
            )
        }
    }

    @Transactional
    fun createSchedule(
        request: CreateScheduleRequest,
    ): ContentSummaryResponse {
        request.apply {
            validateRequestContentDetail(title, description)
            validateRequestDuration(startDateTime, endDateTime)
        }

        val content = contentRepository.findByIdAndNotDeleted(request.contentId)
            ?.apply {
                if (type != ContentType.MEMO) throw ScheduleIllegalArgumentException(
                    errorCode = ILLEGAL_CONTENT_ID,
                    detailMessage = "Only content of type 'MEMO' can be converted to a Schedule. (contentId: $id, type: $type)"
                )
            }
            ?: throw ScheduleIllegalArgumentException(
                errorCode = ILLEGAL_CONTENT_ID,
                detailMessage = "Content not found or has been deleted. (contentId: ${request.contentId})"
            )

        val userCoupleId = SecurityUtil.getCurrentUserCoupleId()
        val contentOwnerCoupleId = content.user.couple?.id
        if (userCoupleId != contentOwnerCoupleId) {
            throw ScheduleAccessDeniedException(
                errorCode = COUPLE_NOT_MATCHED,
                detailMessage = "The current user's couple does not match the content owner's couple"
            )
        }

        val scheduleEvent = with(request) {
            content.type = ContentType.SCHEDULE
            ScheduleEvent(
                content = content,
                uid = UUID.randomUUID().toString(),
                startDateTime = startDateTime,
                endDateTime = endDateTime ?: startDateTime.endOfDay,
                startTimeZone = startTimeZone.toZonId(),
                endTimeZone = endTimeZone?.toZonId() ?: startTimeZone.toZonId(),
            )
        }
        val savedScheduleEvent = scheduleEventRepository.save(scheduleEvent)

        updateSchedule(
            scheduleId = savedScheduleEvent.id,
            request = request.toUpdateRequest(),
        )

        return ContentSummaryResponse(
            contentId = savedScheduleEvent.id,
            contentType = content.type,
        )
    }

    @Retryable(
        retryFor = [OptimisticLockingFailureException::class],
        notRecoverable = [CaramelException::class],
        maxAttempts = 1,
        recover = "updateRecover",
    )
    @Transactional
    fun updateSchedule(
        scheduleId: Long,
        request: UpdateScheduleRequest,
    ) {
        request.apply {
            validateRequestContentDetail(title, description)
            validateRequestDuration(startDateTime, endDateTime)
        }

        val scheduleEvent = scheduleEventRepository.findByIdWithContentAndUser(scheduleId)
            ?: throw ScheduleNotFoundException(errorCode = SCHEDULE_NOT_FOUND)

        validateUserAccess(scheduleEvent)

        with(request) {
            val contentDetail = ContentDetail(
                title = title,
                description = description,
                isCompleted = isCompleted
            )

            if (tagIds.isNotEmpty()) {
                val newTags = tagRepository.findAllByIdInAndIsDeleted(tagIds).toSet()
                updateTags(scheduleEvent.content, newTags)
            }

            when (startDateTime) {
                null -> scheduleEvent.convertToMemo(
                    contentDetail = contentDetail
                )

                else -> scheduleEvent.updateEvent(
                    contentDetail = contentDetail,
                    startDateTime = startDateTime,
                    startTimeZone = startTimeZone ?: DateTimeUtil.UTC_ZONE_ID.id,
                    endDateTime = endDateTime,
                    endTimeZone = endTimeZone,
                )
            }
        }
    }

    @Retryable(
        retryFor = [OptimisticLockingFailureException::class],
        notRecoverable = [CaramelException::class],
        backoff = Backoff(delay = 100, maxDelay = 300),
        maxAttempts = 3,
        recover = "deleteRecover",
    )
    @Transactional
    fun deleteSchedule(scheduleId: Long) {
        val scheduleEvent = scheduleEventRepository.findByIdWithContentAndUser(scheduleId)
            ?: throw ScheduleNotFoundException(errorCode = SCHEDULE_NOT_FOUND)
        validateUserAccess(scheduleEvent)
        scheduleEvent.apply {
            deleteEntity()
            content.deleteEntity()
            val tagMappings = tagContentMappingRepository.findAllByContent_IdAndIsDeleted(id)
            tagMappings.forEach(TagContentMapping::deleteEntity)
        }
    }

    @Recover
    fun updateRecover(
        e: OptimisticLockingFailureException,
        scheduleId: Long,
    ) {
        logger.error { "update schedule fail. schedule id: ${scheduleId}" }
        throw ScheduleIllegalStateException(errorCode = UPDATE_CONFLICT)
    }

    @Recover
    fun deleteRecover(
        e: OptimisticLockingFailureException,
        scheduleId: Long,
    ) {
        logger.error { "delete schedule fail. schedule id: ${scheduleId}" }
        throw ScheduleIllegalStateException(errorCode = UPDATE_CONFLICT)
    }

    private fun validateRequestContentDetail(title: String?, description: String?) {
        if (title == null && description == null) {
            throw ScheduleIllegalArgumentException(
                errorCode = ILLEGAL_CONTENT_DETAIL,
                detailMessage = "Both title and description cannot be Null."
            )
        }
        if ((title?.isBlank() == true) || (description?.isBlank() == true)) {
            throw ScheduleIllegalArgumentException(
                errorCode = ILLEGAL_CONTENT_DETAIL,
                detailMessage = "Title and description must not be blank."
            )
        }
    }

    private fun validateRequestDuration(startDateTime: LocalDateTime?, endDateTime: LocalDateTime?) {
        if (startDateTime != null && endDateTime?.isBefore(startDateTime) == true) {
            throw ScheduleIllegalArgumentException(
                errorCode = ILLEGAL_DURATION,
                detailMessage = "EndDateTime must not be before startDateTime."
            )
        }
    }

    private fun validateUserAccess(scheduleEvent: ScheduleEvent) {
        val scheduleOwnerUser = scheduleEvent.content.user
        val currentUserId = SecurityUtil.getCurrentUserId()

        if (currentUserId != scheduleOwnerUser.id) {
            val currentUserCoupleId = SecurityUtil.getCurrentUserCoupleId()
            if (scheduleOwnerUser.userStatus == SINGLE) {
                throw ScheduleIllegalStateException(
                    errorCode = ILLEGAL_PARTNER_STATUS,
                    detailMessage = "Schedule owner is single now."
                )
            }

            val scheduleOwnerCoupleId = scheduleOwnerUser.couple?.id
                ?: throw ScheduleIllegalStateException(
                    errorCode = ILLEGAL_PARTNER_STATUS,
                    detailMessage = "Schedule owner's couple data is missing."
                )

            if (currentUserCoupleId != scheduleOwnerCoupleId) {
                throw ScheduleAccessDeniedException(
                    errorCode = COUPLE_NOT_MATCHED,
                    detailMessage = "The current user's couple does not match the schedule owner's couple"
                )
            }
        }
    }

    private fun updateTags(content: Content, newTags: Set<Tag>) {
        val existingMappings = tagContentMappingRepository.findAllByContentIdWithTag(content.id)
        val currentTags = existingMappings.map { mapping -> mapping.tag }.toSet()

        val mappingToRemove = existingMappings.filter { mapping -> mapping.tag !in newTags }
        val tagsToAdd = newTags - currentTags

        if (mappingToRemove.isNotEmpty()) {
            mappingToRemove.forEach(TagContentMapping::deleteEntity)
        }

        if (tagsToAdd.isNotEmpty()) {
            val newMappings = tagsToAdd.map { tag -> TagContentMapping(tag = tag, content = content) }
            tagContentMappingRepository.saveAll(newMappings)
        }
    }
}

private fun CreateScheduleRequest.toUpdateRequest(): UpdateScheduleRequest {
    return UpdateScheduleRequest(
        selectedDate = selectedDate,
        title = title,
        description = description,
        isCompleted = isCompleted,
        startDateTime = startDateTime,
        startTimeZone = startTimeZone,
        endDateTime = endDateTime,
        endTimeZone = endTimeZone,
        tagIds = tagIds
    )
}