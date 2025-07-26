package com.whatever.caramel.domain.calendarevent.service

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.common.util.endOfDay
import com.whatever.caramel.common.util.toDateTime
import com.whatever.caramel.common.util.withoutNano
import com.whatever.caramel.domain.calendarevent.exception.ScheduleAccessDeniedException
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode.ILLEGAL_PARTNER_STATUS
import com.whatever.caramel.domain.calendarevent.exception.ScheduleIllegalStateException
import com.whatever.caramel.domain.calendarevent.exception.ScheduleNotFoundException
import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.caramel.domain.calendarevent.vo.CreateScheduleVo
import com.whatever.caramel.domain.calendarevent.vo.DateTimeInfoVo
import com.whatever.caramel.domain.calendarevent.vo.GetScheduleVo
import com.whatever.caramel.domain.calendarevent.vo.ScheduleDetailsVo
import com.whatever.caramel.domain.calendarevent.vo.UpdateScheduleVo
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.model.ContentDetail
import com.whatever.caramel.domain.content.service.ScheduleCreator
import com.whatever.caramel.domain.content.tag.model.Tag
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.vo.ContentSummaryVo
import com.whatever.caramel.domain.couple.exception.CoupleException
import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode.COUPLE_NOT_FOUND
import com.whatever.caramel.domain.couple.exception.CoupleNotFoundException
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.firebase.service.event.dto.ScheduleCreateEvent
import com.whatever.caramel.domain.user.model.UserStatus.SINGLE
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val logger = KotlinLogging.logger { }

@Service
class ScheduleEventService(
    private val scheduleEventRepository: ScheduleEventRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val tagRepository: TagRepository,
    private val coupleRepository: CoupleRepository,
    private val scheduleCreator: ScheduleCreator,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val scheduleValidator: ScheduleValidator,
) {

    fun getSchedule(
        scheduleId: Long,
        ownerCoupleId: Long,
    ): GetScheduleVo {
        val schedule = scheduleEventRepository.findByIdWithContent(scheduleId)
            ?: throw ScheduleNotFoundException(errorCode = ScheduleExceptionCode.SCHEDULE_NOT_FOUND)

        val couple = coupleRepository.findByIdWithMembers(ownerCoupleId)
        if (couple == null || !couple.members.map { it.id }.contains(schedule.content.user.id)) {
            throw ScheduleAccessDeniedException(errorCode = COUPLE_NOT_MATCHED)
        }

        val tags = tagContentMappingRepository.findAllWithTagByContentId(schedule.content.id)
            .map { it.tag }
        return GetScheduleVo.from(
            schedule = schedule,
            content = schedule.content,
            tags = tags,
        )
    }

    fun getSchedules(
        startDate: LocalDate,
        endDate: LocalDate,
        userTimeZone: String,
        currentUserCoupleId: Long,
    ): ScheduleDetailsVo {
        val startDateTime = startDate.toDateTime().withoutNano
        val endDateTime = endDate.toDateTime().endOfDay.withoutNano

        scheduleValidator.validateDuration(
            startDateTime = startDateTime,
            endDateTime = endDateTime,
        )

        val couple = coupleRepository.findByIdWithMembers(currentUserCoupleId)
            ?: throw CoupleException(errorCode = COUPLE_NOT_FOUND)

        if (couple.members.any { it.userStatus == SINGLE }) {
            throw ScheduleAccessDeniedException(errorCode = ILLEGAL_PARTNER_STATUS)
        }

        val memberIds = couple.members.map { it.id }.toSet()
        val coupleSchedules = scheduleEventRepository.findAllByDurationAndUsers(
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            memberIds = memberIds
        )

        return ScheduleDetailsVo.from(coupleSchedules = coupleSchedules)
    }

    @Transactional
    fun createSchedule(
        scheduleVo: CreateScheduleVo,
        currentUserId: Long,
        currentUserCoupleId: Long,
    ): ContentSummaryVo {
        scheduleVo.apply {
            scheduleValidator.validateContentDetail(title, description)
            scheduleValidator.validateDuration(startDateTime, endDateTime)
        }

        val couple = coupleRepository.findByIdWithMembers(currentUserCoupleId)
            ?: throw CoupleNotFoundException(COUPLE_NOT_FOUND)

        val savedScheduleEvent = scheduleCreator.createSchedule(
            title = scheduleVo.title,
            description = scheduleVo.description,
            isCompleted = scheduleVo.isCompleted,
            tagIds = scheduleVo.tagIds,
            dateTimeInfo = DateTimeInfoVo.from(
                startTimezone = scheduleVo.startTimeZone,
                startDateTime = scheduleVo.startDateTime,
                endTimezone = scheduleVo.endTimeZone,
                endDateTime = scheduleVo.endDateTime,
            ),
            getCurrentUserId = currentUserId,
            contentAssignee = scheduleVo.contentAssignee,
        )

        applicationEventPublisher.publishEvent(
            ScheduleCreateEvent(
                userId = currentUserId,
                coupleId = couple.id,
                memberIds = couple.members.map { it.id }.toSet(),
                contentDetail = savedScheduleEvent.content.contentDetail,
            )
        )

        return ContentSummaryVo(
            id = savedScheduleEvent.id,
            contentType = savedScheduleEvent.content.type,
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
        currentUserId: Long,
        currentUserCoupleId: Long,
        scheduleVo: UpdateScheduleVo,
    ) {
        scheduleVo.apply {
            scheduleValidator.validateContentDetail(title, description)
            scheduleValidator.validateDuration(startDateTime, endDateTime)
        }

        val scheduleEvent = scheduleEventRepository.findByIdWithContentAndUser(scheduleId)
            ?: throw ScheduleNotFoundException(errorCode = ScheduleExceptionCode.SCHEDULE_NOT_FOUND)

        scheduleValidator.validateUserAccess(
            scheduleEvent = scheduleEvent,
            currentUserId = currentUserId,
            currentUserCoupleId = currentUserCoupleId,
        )

        with(scheduleVo) {
            val contentDetail = ContentDetail(
                title = title,
                description = description,
                isCompleted = isCompleted
            )

            if (tagIds.isNotEmpty()) {
                val newTags = tagRepository.findAllByIdInAndIsDeleted(tagIds).toSet()
                updateTags(scheduleEvent.content, newTags)
            }

            scheduleEvent.content.updateContentAssignee(contentAssignee)

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
    fun deleteSchedule(scheduleId: Long, currentUserId: Long, currentUserCoupleId: Long) {
        val scheduleEvent = scheduleEventRepository.findByIdWithContentAndUser(scheduleId)
            ?: throw ScheduleNotFoundException(errorCode = ScheduleExceptionCode.SCHEDULE_NOT_FOUND)
        scheduleValidator.validateUserAccess(
            scheduleEvent = scheduleEvent,
            currentUserId = currentUserId,
            currentUserCoupleId = currentUserCoupleId,
        )
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
        currentUserId: Long,
        currentUserCoupleId: Long,
        scheduleVo: UpdateScheduleVo,
    ) {
        logger.error { "update schedule fail. schedule id: ${scheduleId}" }
        throw ScheduleIllegalStateException(
            errorCode = ScheduleExceptionCode.UPDATE_CONFLICT,
            errorUi = ErrorUi.Toast("일정 수정을 실패했어요. 다시 시도해주세요.")
        )
    }

    @Recover
    fun deleteRecover(
        e: OptimisticLockingFailureException,
        scheduleId: Long,
        currentUserId: Long,
        currentUserCoupleId: Long,
    ) {
        logger.error { "delete schedule fail. schedule id: ${scheduleId}" }
        throw ScheduleIllegalStateException(
            errorCode = ScheduleExceptionCode.UPDATE_CONFLICT,
            errorUi = ErrorUi.Toast("일정 삭제를 실패했어요. 다시 시도해주세요.")
        )
    }

    private fun updateTags(content: Content, newTags: Set<Tag>) {
        val existingMappings = tagContentMappingRepository.findAllWithTagByContentId(content.id)
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
