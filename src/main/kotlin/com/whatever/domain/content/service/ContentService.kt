package com.whatever.domain.content.service

import com.whatever.domain.content.controller.dto.request.CreateContentRequest
import com.whatever.domain.content.controller.dto.response.ContentSummaryResponse
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentType
import org.springframework.stereotype.Component

@Component
class ContentService(
    private val memoCreator: MemoCreator,
) {
    fun createContent(contentRequest: CreateContentRequest): ContentSummaryResponse {
        return if (contentRequest.dateTimeInfo == null) {
            memoCreator.createMemo(
                title = contentRequest.title,
                description = contentRequest.description,
                isCompleted = contentRequest.isCompleted,
                tagIds = contentRequest.tags.map { it.tagId },
            ).toContentSummaryResponse()
        } else {
            // TODO(evergreentree97) : need schedule creator
            ContentSummaryResponse(
                contentId = 1L,
                contentType = ContentType.SCHEDULE,
            )
        }
    }
}

private fun Content.toContentSummaryResponse() = ContentSummaryResponse(
    contentId = id,
    contentType = type
)
