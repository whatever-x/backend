package com.whatever.caramel.domain.content.service

import com.whatever.caramel.common.util.endOfDay
import com.whatever.caramel.common.util.toZoneId
import com.whatever.caramel.domain.calendarevent.model.ScheduleEvent
import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.caramel.domain.calendarevent.vo.DateTimeInfoVo
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.model.ContentDetail
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.vo.ContentAssignee
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.content.vo.ScheduleEventVo
import com.whatever.caramel.domain.user.repository.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class ScheduleCreator(
    private val contentRepository: ContentRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
) {
    @Transactional
    fun createSchedule(
        title: String?,
        description: String?,
        isCompleted: Boolean,
        tagIds: Set<Long>,
        dateTimeInfo: DateTimeInfoVo,
        getCurrentUserId: Long,
        contentAssignee: ContentAssignee,
    ): ScheduleEventVo {
        val contentDetail = ContentDetail(
            title = title,
            description = description,
            isCompleted = isCompleted
        )

        val user = userRepository.getReferenceById(getCurrentUserId)

        val content = Content(
            user = user,
            contentDetail = contentDetail,
            type = ContentType.SCHEDULE,
            contentAssignee = contentAssignee
        )
        val savedContent = contentRepository.save(content)

        val scheduleEvent = ScheduleEvent(
            content = savedContent,
            uid = UUID.randomUUID().toString(),
            startDateTime = dateTimeInfo.startDateTime,
            endDateTime = dateTimeInfo.endDateTime ?: dateTimeInfo.startDateTime.endOfDay,
            startTimeZone = dateTimeInfo.startTimezone.toZoneId(),
            endTimeZone = dateTimeInfo.endTimezone?.toZoneId() ?: dateTimeInfo.startTimezone.toZoneId(),
        )
        scheduleEventRepository.save(scheduleEvent)

        if (tagIds.isNotEmpty()) {
            val tags = tagRepository.findAllById(tagIds)
            val mappings = tags.map { tag -> TagContentMapping(tag = tag, content = savedContent) }

            if (mappings.isNotEmpty()) {
                tagContentMappingRepository.saveAll(mappings)
            }
        }

        return ScheduleEventVo.from(scheduleEvent)
    }
}


