package com.whatever.domain.content.service

import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.controller.dto.request.CreateContentRequest
import com.whatever.domain.content.controller.dto.request.GetContentListQueryParameter
import com.whatever.domain.content.controller.dto.request.UpdateContentRequest
import com.whatever.domain.content.controller.dto.response.ContentResponse
import com.whatever.domain.content.controller.dto.response.ContentSummaryResponse
import com.whatever.domain.content.controller.dto.response.TagDto
import com.whatever.domain.content.exception.ContentAccessDeniedException
import com.whatever.domain.content.exception.ContentExceptionCode
import com.whatever.domain.content.exception.ContentExceptionCode.CONTENT_NOT_FOUND
import com.whatever.domain.content.exception.ContentExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.domain.content.exception.ContentExceptionCode.MEMO_NOT_FOUND
import com.whatever.domain.content.exception.ContentIllegalStateException
import com.whatever.domain.content.exception.ContentNotFoundException
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.model.Tag
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.couple.exception.CoupleExceptionCode.COUPLE_NOT_FOUND
import com.whatever.domain.couple.exception.CoupleNotFoundException
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.firebase.service.event.dto.MemoCreateEvent
import com.whatever.domain.firebase.service.event.dto.ScheduleCreateEvent
import com.whatever.global.cursor.CursoredResponse
import com.whatever.global.exception.ErrorUi
import com.whatever.global.exception.common.CaramelException
import com.whatever.global.security.util.SecurityUtil.getCurrentUserCoupleId
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import com.whatever.util.CursorUtil
import com.whatever.util.toZoneId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
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
    private val contentRepository: ContentRepository,
    private val tagRepository: TagRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val coupleRepository: CoupleRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun getMemo(
        memoId: Long,
        ownerCoupleId: Long = getCurrentUserCoupleId(),
    ): ContentResponse {
        val memo = contentRepository.findContentByIdAndType(
            id = memoId,
            type = ContentType.MEMO
        ) ?: throw ContentNotFoundException(errorCode = MEMO_NOT_FOUND)

        val couple = coupleRepository.findByIdWithMembers(ownerCoupleId)
        if (couple == null || couple.members.none { it.id == memo.user.id }) {
            throw ContentAccessDeniedException(errorCode = COUPLE_NOT_MATCHED)
        }

        val tagDtos = tagContentMappingRepository.findAllWithTagByContentId(contentId = memo.id)
            .map { TagDto.from(it.tag) }
        return ContentResponse.from(
            content = memo,
            tagList = tagDtos,
        )
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    fun getContentList(queryParameter: GetContentListQueryParameter): CursoredResponse<ContentResponse> {
        val coupleId = getCurrentUserCoupleId()
        val memberIds = coupleRepository.findByIdWithMembers(coupleId)?.members?.map { it.id }
            ?: emptyList()

        val contentList = contentRepository.findByTypeWithCursor(
            type = ContentType.MEMO,
            queryParameter = queryParameter,
            memberIds = memberIds,
            tagId = queryParameter.tagId,
        )
        val tagContentMap = tagContentMappingRepository.findAllWithTagByContentIds(
            contentList.map { it.id }.toSet()
        ).groupBy { it.content.id }

        return contentList.let { contents: List<Content> ->
            CursoredResponse.from(
                list = contents,
                size = queryParameter.size,
                generateCursor = { content: Content ->
                    CursorUtil.toHash(content.id)
                }
            )
        }.map { content ->
            val existingTags = tagContentMap[content.id]?.map { it.tag.toTagDto() } ?: emptyList()
            ContentResponse.from(content, existingTags)
        }
    }

    @Transactional
    fun createContent(contentRequest: CreateContentRequest): ContentSummaryResponse {
        val couple = coupleRepository.findByIdWithMembers(getCurrentUserCoupleId())
            ?: throw CoupleNotFoundException(COUPLE_NOT_FOUND)

        val memo = memoCreator.createMemo(
            title = contentRequest.title,
            description = contentRequest.description,
            isCompleted = contentRequest.isCompleted,
            tagIds = contentRequest.tags.map { it.tagId }.toSet(),
        )

        applicationEventPublisher.publishEvent(
            MemoCreateEvent(
                userId = getCurrentUserId(),
                coupleId = couple.id,
                memberIds = couple.members.map { it.id }.toSet(),
                contentDetail = memo.contentDetail,
            )
        )
        return memo.toContentSummaryResponse()
    }

    @Retryable(
        retryFor = [OptimisticLockingFailureException::class],
        notRecoverable = [CaramelException::class],
        maxAttempts = 1,
        recover = "updateRecover",
    )
    @Transactional
    fun updateContent(contentId: Long, request: UpdateContentRequest): ContentSummaryResponse {
        val memo = contentRepository.findContentByIdAndType(
            id = contentId,
            type = ContentType.MEMO
        ) ?: throw ContentNotFoundException(errorCode = MEMO_NOT_FOUND)

        val couple = coupleRepository.findByIdWithMembers(getCurrentUserCoupleId())
            ?: throw CoupleNotFoundException(COUPLE_NOT_FOUND)

        val contentOwnerCoupleId = memo.user.couple?.id
        if (couple.id != contentOwnerCoupleId) {
            throw ContentAccessDeniedException(errorCode = COUPLE_NOT_MATCHED)
        }

        val newContentDetail = ContentDetail(
            title = request.title,
            description = request.description,
            isCompleted = request.isCompleted
        )
        memo.updateContentDetail(newContentDetail)

        updateTags(memo, request.tagList.map { it.tagId }.toSet())
        if (request.dateTimeInfo == null) {  // 날짜 정보가 없다면 메모 업데이트만 진행
            return memo.toContentSummaryResponse()
        }

        val scheduleEvent = with(request.dateTimeInfo) {
            ScheduleEvent.fromMemo(
                memo = memo,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                startTimeZone = startTimezone.toZoneId(),
                endTimeZone = endTimezone?.toZoneId(),
            )
        }
        val savedScheduleEvent = scheduleEventRepository.save(scheduleEvent)


        applicationEventPublisher.publishEvent(
            ScheduleCreateEvent(
                userId = getCurrentUserId(),
                coupleId = couple.id,
                memberIds = couple.members.map { it.id }.toSet(),
                contentDetail = savedScheduleEvent.content.contentDetail,
            )
        )

        return ContentSummaryResponse(
            contentId = savedScheduleEvent.id,
            contentType = ContentType.SCHEDULE,
        )
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
        ) ?: throw ContentNotFoundException(CONTENT_NOT_FOUND)

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
        logger.error { "update memo fail. content id: $contentId" }
        throw ContentIllegalStateException(
            errorCode = ContentExceptionCode.UPDATE_CONFLICT,
            errorUi = ErrorUi.Toast("메모 수정을 실패했어요. 다시 시도해주세요.")
        )
    }

    @Recover
    fun deleteRecover(
        e: OptimisticLockingFailureException,
        contentId: Long,
    ) {
        logger.error { "delete memo fail. content id: $contentId" }
        throw ContentIllegalStateException(
            errorCode = ContentExceptionCode.UPDATE_CONFLICT,
            errorUi = ErrorUi.Toast("메모 삭제를 실패했어요. 다시 시도해주세요.")
        )
    }
}

private fun Content.toContentSummaryResponse() = ContentSummaryResponse(
    contentId = id,
    contentType = type
)

private fun Tag.toTagDto() = TagDto(
    id = id,
    label = label,
)