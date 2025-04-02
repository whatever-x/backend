package com.whatever.domain.content.service

import com.whatever.domain.content.controller.dto.request.CreateContentRequest
import com.whatever.domain.content.controller.dto.response.ContentSummaryResponse
import com.whatever.domain.content.model.Content
import org.springframework.stereotype.Component

@Component
class ContentService(
    private val memoCreator: MemoCreator,
    private val scheduleCreator: ScheduleCreator,
) {
    fun createContent(contentRequest: CreateContentRequest): ContentSummaryResponse {
        return if (contentRequest.dateTimeInfo == null) {
            memoCreator.createMemo(
                title = contentRequest.title,
                description = contentRequest.description,
                isCompleted = contentRequest.isCompleted,
                tagIds = contentRequest.tags.map { it.tagId }.toSet(),
            )
        } else {
            scheduleCreator.createSchedule(
                title = contentRequest.title,
                description = contentRequest.description,
                isCompleted = contentRequest.isCompleted,
                tagIds = contentRequest.tags.map { it.tagId }.toSet(),
                dateTimeInfo = contentRequest.dateTimeInfo,
            )
        }.toContentSummaryResponse()
    }
}

private fun Content.toContentSummaryResponse() = ContentSummaryResponse(
    contentId = id,
    contentType = type
)
