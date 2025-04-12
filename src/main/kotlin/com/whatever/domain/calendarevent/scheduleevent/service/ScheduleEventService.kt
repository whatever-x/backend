package com.whatever.domain.calendarevent.scheduleevent.service

import com.whatever.domain.calendarevent.controller.dto.response.ScheduleDetailDto
import com.whatever.domain.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleAccessDeniedException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_DURATION
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_PARTNER_STATUS
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.SCHEDULE_NOT_FOUND
import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalArgumentException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalStateException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleNotFoundException
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.tag.model.Tag
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.couple.exception.CoupleExceptionCode.COUPLE_NOT_FOUND
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.model.UserStatus.SINGLE
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.toDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ScheduleEventService(
    private val scheduleEventRepository: ScheduleEventRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val tagRepository: TagRepository,
    private val coupleRepository: CoupleRepository,
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
    fun updateSchedule(
        scheduleId: Long,
        request: UpdateScheduleRequest,
    ) {
        validateUpdateRequest(request)

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

    private fun validateUpdateRequest(request: UpdateScheduleRequest) {
        with(request) {
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
            validateRequestDuration(startDateTime, endDateTime)
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