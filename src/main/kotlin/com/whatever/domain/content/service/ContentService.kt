package com.whatever.domain.content.service

import com.whatever.domain.content.controller.dto.request.CreateContentRequest
import com.whatever.domain.content.controller.dto.request.GetContentListQueryParameter
import com.whatever.domain.content.controller.dto.request.UpdateContentRequest
import com.whatever.domain.content.controller.dto.response.ContentResponse
import com.whatever.domain.content.controller.dto.response.ContentSummaryResponse
import com.whatever.domain.content.exception.ContentExceptionCode
import com.whatever.domain.content.exception.ContentIllegalStateException
import com.whatever.domain.content.exception.ContentNotFoundException
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.global.cursor.CursoredResponse
import com.whatever.global.exception.common.CaramelException
import com.whatever.util.CursorUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger { }

@Service
class ContentService(
    private val memoCreator: MemoCreator,
    private val scheduleCreator: ScheduleCreator,
    private val contentRepository: ContentRepository,
    private val tagRepository: TagRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
) {
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    fun getContentList(queryParameter: GetContentListQueryParameter): CursoredResponse<ContentResponse> {

        return contentRepository.findByTypeWithCursor(
            type = ContentType.MEMO,
            queryParameter = queryParameter,
        ).let { contents: List<Content> ->
            CursoredResponse.from(
                list = contents,
                size = queryParameter.size,
                generateCursor = { content: Content ->
                    CursorUtil.toHash(content.id)
                }
            )
        }.map {
            ContentResponse.from(it)
        }
    }

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

    @Retryable(
        retryFor = [OptimisticLockingFailureException::class],
        notRecoverable = [CaramelException::class],
        maxAttempts = 1,
        recover = "updateRecover",
    )
    @Transactional
    fun updateContent(contentId: Long, request: UpdateContentRequest): ContentSummaryResponse {
        val content = contentRepository.findContentByIdAndType(
            id = contentId,
            type = ContentType.MEMO
        ) ?: throw ContentNotFoundException(ContentExceptionCode.CONTENT_NOT_FOUND)

        val newContentDetail = ContentDetail(
            title = request.title,
            description = request.description,
            isCompleted = request.isCompleted
        )
        content.updateContentDetail(newContentDetail)

        updateTags(content, request.tagList.map { it.tagId }.toSet())

        return content.toContentSummaryResponse()
    }

    @Retryable(
        retryFor = [OptimisticLockingFailureException::class],
        notRecoverable = [CaramelException::class],
        backoff = Backoff(delay = 100, maxDelay = 300),
        maxAttempts = 3,
        recover = "deleteRecover",
    )
    @Transactional
    fun deleteContent(contentId: Long) {
        val content = contentRepository.findContentByIdAndType(
            id = contentId,
            type = ContentType.MEMO
        ) ?: throw ContentNotFoundException(ContentExceptionCode.CONTENT_NOT_FOUND)

        val tagMappings = tagContentMappingRepository.findAllByContent_IdAndIsDeleted(contentId)
        tagMappings.forEach(TagContentMapping::deleteEntity)

        content.deleteEntity()
    }

    private fun updateTags(content: Content, requestedTagIds: Set<Long>) {
        val existingMappings = tagContentMappingRepository.findAllByContent_IdAndIsDeleted(content.id)
        val existingTagIds = existingMappings.map { it.tag.id }.toSet()

        val mappingsToDelete = existingMappings.filter { it.tag.id !in requestedTagIds }
        mappingsToDelete.forEach(TagContentMapping::deleteEntity)

        val tagIdsToAdd = requestedTagIds - existingTagIds
        if (tagIdsToAdd.isNotEmpty()) {
            val tagsToAdd = tagRepository.findAllById(tagIdsToAdd)
            val newMappings = tagsToAdd.map { TagContentMapping(tag = it, content = content) }
            tagContentMappingRepository.saveAll(newMappings)
        }
    }

    @Recover
    fun updateRecover(
        e: OptimisticLockingFailureException,
        contentId: Long,
    ) {
        logger.error { "update schedule fail. content id: $contentId" }
        throw ContentIllegalStateException(errorCode = ContentExceptionCode.UPDATE_CONFLICT)
    }

    @Recover
    fun deleteRecover(
        e: OptimisticLockingFailureException,
        contentId: Long,
    ) {
        logger.error { "delete content fail. content id: $contentId" }
        throw ContentIllegalStateException(errorCode = ContentExceptionCode.UPDATE_CONFLICT)
    }
}

private fun Content.toContentSummaryResponse() = ContentSummaryResponse(
    contentId = id,
    contentType = type
)
