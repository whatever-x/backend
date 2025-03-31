package com.whatever.domain.content.service

import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.controller.dto.request.DateTimeInfoDto
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentDetail.Companion.MAX_TITLE_LENGTH
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import com.whatever.util.endOfDay
import com.whatever.util.toZonId
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

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
        tagIds: List<Long>,
        dateTimeInfo: DateTimeInfoDto,
    ): Content {
        val replacedTitle = when {
            title.isNullOrBlank() && !description.isNullOrBlank() -> description.take(MAX_TITLE_LENGTH)
            else -> title!!
        }
        val contentDetail = ContentDetail(
            title = replacedTitle,
            description = description,
            isCompleted = isCompleted
        )

        val scheduleEvent = ScheduleEvent(
            contentDetail = contentDetail,
            uid = UUID.randomUUID().toString(),
            startDateTime = dateTimeInfo.startDateTime,
            endDateTime = dateTimeInfo.endDateTime ?: dateTimeInfo.startDateTime.endOfDay,
            startTimeZone = dateTimeInfo.startTimezone.toZonId(),
            endTimeZone = dateTimeInfo.endTimezone?.toZonId() ?: dateTimeInfo.startTimezone.toZonId(),
        )

        val userId = getCurrentUserId()
        val user = userRepository.getReferenceById(userId)

        val content = Content(
            user = user,
            contentDetail = contentDetail,
            type = ContentType.SCHEDULE
        )

        val savedContent = contentRepository.save(content)
        scheduleEventRepository.save(scheduleEvent)

        if (tagIds.isNotEmpty()) {
            val mappings = tagIds.mapNotNull { tagId ->
                tagRepository.findById(tagId).map { tag ->
                    TagContentMapping(
                        tag = tag,
                        content = savedContent
                    )
                }.orElse(null)
            }

            if (mappings.isNotEmpty()) {
                tagContentMappingRepository.saveAll(mappings)
            }
        }

        return content
    }
}


