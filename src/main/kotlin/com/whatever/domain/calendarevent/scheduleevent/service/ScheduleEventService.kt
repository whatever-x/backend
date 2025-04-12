package com.whatever.domain.calendarevent.scheduleevent.service

import com.whatever.domain.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleAccessDeniedException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_DURATION
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_OWNER_STATUS
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
import com.whatever.domain.user.model.UserStatus
import com.whatever.global.security.util.SecurityUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScheduleEventService(
    private val scheduleEventRepository: ScheduleEventRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val tagRepository: TagRepository,
) {

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
                title = title ?: description!!,
                description = description,
                isCompleted = isCompleted
            )
            scheduleEvent.updateEvent(
                contentDetail = contentDetail,
                startDateTime = startDateTime,
                startTimeZone = startTimeZone,
                endDateTime = endDateTime,
                endTimeZone = endTimeZone,
            )

            if (tagIds.isNotEmpty()) {
                val newTags = tagRepository.findAllById(tagIds).toSet()
                updateTags(scheduleEvent.content, newTags)
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
            if (endDateTime?.isBefore(startDateTime) == true) {
                throw ScheduleIllegalArgumentException(
                    errorCode = ILLEGAL_DURATION,
                    detailMessage = "EndDateTime must not be before startDateTime."
                )
            }
        }
    }

    private fun validateUserAccess(scheduleEvent: ScheduleEvent) {
        val scheduleOwnerUser = scheduleEvent.content.user
        val currentUserId = SecurityUtil.getCurrentUserId()

        if (currentUserId != scheduleOwnerUser.id) {
            val currentUserCoupleId = SecurityUtil.getCurrentUserCoupleId()
            if (scheduleOwnerUser.userStatus == UserStatus.SINGLE) {
                throw ScheduleIllegalStateException(
                    errorCode = ILLEGAL_OWNER_STATUS,
                    detailMessage = "Schedule owner is single now."
                )
            }

            val scheduleOwnerCoupleId = scheduleOwnerUser.couple?.id
                ?: throw ScheduleIllegalStateException(
                    errorCode = ILLEGAL_OWNER_STATUS,
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
            tagContentMappingRepository.deleteAllInBatch(mappingToRemove)
        }

        if (tagsToAdd.isNotEmpty()) {
            val newMappings = tagsToAdd.map { tag -> TagContentMapping(tag = tag, content = content) }
            tagContentMappingRepository.saveAll(newMappings)
        }
    }
}