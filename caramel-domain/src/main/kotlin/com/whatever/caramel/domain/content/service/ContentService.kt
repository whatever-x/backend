package com.whatever.caramel.domain.content.service

import com.whatever.caramel.common.global.cursor.PagedSlice
import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException
import com.whatever.caramel.common.util.CursorUtil
import com.whatever.caramel.domain.calendarevent.model.ScheduleEvent
import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.caramel.domain.content.exception.ContentAccessDeniedException
import com.whatever.caramel.domain.content.exception.ContentExceptionCode
import com.whatever.caramel.domain.content.exception.ContentExceptionCode.CONTENT_NOT_FOUND
import com.whatever.caramel.domain.content.exception.ContentExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.caramel.domain.content.exception.ContentExceptionCode.MEMO_NOT_FOUND
import com.whatever.caramel.domain.content.exception.ContentIllegalStateException
import com.whatever.caramel.domain.content.exception.ContentNotFoundException
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.model.ContentDetail
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.tag.vo.TagVo
import com.whatever.caramel.domain.content.vo.ContentDetailVo
import com.whatever.caramel.domain.content.vo.ContentQueryVo
import com.whatever.caramel.domain.content.vo.ContentResponseVo
import com.whatever.caramel.domain.content.vo.ContentSummaryVo
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.content.vo.CreateContentRequestVo
import com.whatever.caramel.domain.content.vo.UpdateContentRequestVo
import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode.COUPLE_NOT_FOUND
import com.whatever.caramel.domain.couple.exception.CoupleNotFoundException
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.firebase.service.event.dto.MemoCreateEvent
import com.whatever.caramel.domain.firebase.service.event.dto.ScheduleCreateEvent
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
        ownerCoupleId: Long,
    ): ContentResponseVo {
        val memo = contentRepository.findContentByIdAndType(
            id = memoId,
            type = ContentType.MEMO
        ) ?: throw ContentNotFoundException(errorCode = MEMO_NOT_FOUND)

        val couple = coupleRepository.findByIdWithMembers(ownerCoupleId)
        if (couple == null || couple.members.none { it.id == memo.user.id }) {
            throw ContentAccessDeniedException(errorCode = COUPLE_NOT_MATCHED)
        }

        val tagVos = tagContentMappingRepository.findAllWithTagByContentId(contentId = memo.id)
            .map { TagVo.from(it.tag) }
        return ContentResponseVo.from(
            content = memo,
            tagList = tagVos,
        )
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    fun getContentList(
        queryParameterVo: ContentQueryVo,
        coupleId: Long,
    ): PagedSlice<ContentResponseVo> {
        val couple = coupleRepository.findByIdWithMembers(coupleId)
            ?: throw CoupleNotFoundException(errorCode = COUPLE_NOT_FOUND)
        val memberIds = couple.members.map { it.id }

        val contentList = contentRepository.findByTypeWithCursor(
            type = ContentType.MEMO,
            queryParameter = queryParameterVo,
            memberIds = memberIds,
            tagId = queryParameterVo.tagId,
        )
        val tagContentMap = tagContentMappingRepository.findAllWithTagByContentIds(
            contentList.map { it.id }.toSet()
        ).groupBy { it.content.id }

        return contentList.let { contents: List<Content> ->
            PagedSlice.from(
                list = contents,
                size = queryParameterVo.size,
                generateCursor = { content: Content ->
                    CursorUtil.toHash(content.id)
                }
            )
        }.map { content ->
            val existingTags = tagContentMap[content.id]?.map { TagVo.from(it.tag) } ?: emptyList()
            ContentResponseVo.from(content, existingTags)
        }
    }

    @Transactional
    fun createContent(
        contentRequestVo: CreateContentRequestVo,
        userId: Long,
        coupleId: Long,
    ): ContentSummaryVo {
        val couple = coupleRepository.findByIdWithMembers(coupleId = coupleId)
            ?: throw CoupleNotFoundException(COUPLE_NOT_FOUND)

        val memo = memoCreator.createMemo(
            title = contentRequestVo.title,
            description = contentRequestVo.description,
            isCompleted = contentRequestVo.isCompleted,
            tagIds = contentRequestVo.tags.toSet(),
            currentUserId = userId,
            contentAsignee = contentRequestVo.contentAsignee
        )

        applicationEventPublisher.publishEvent(
            MemoCreateEvent(
                userId = userId,
                coupleId = couple.id,
                memberIds = couple.members.map { it.id }.toSet(),
                contentDetail = memo.contentDetail,
            )
        )
        return ContentSummaryVo.from(memo)
    }

    @Retryable(
        retryFor = [OptimisticLockingFailureException::class],
        notRecoverable = [CaramelException::class],
        maxAttempts = 1,
        recover = "updateRecover",
    )
    @Transactional
    fun updateContent(
        contentId: Long,
        requestVo: UpdateContentRequestVo,
        userCoupleId: Long,
        userId: Long,
    ): ContentSummaryVo {
        val memo = contentRepository.findContentByIdAndType(
            id = contentId,
            type = ContentType.MEMO
        ) ?: throw ContentNotFoundException(errorCode = MEMO_NOT_FOUND)

        val couple = coupleRepository.findByIdWithMembers(userCoupleId)
            ?: throw CoupleNotFoundException(COUPLE_NOT_FOUND)

        val contentOwnerCoupleId = memo.user.couple?.id
        if (couple.id != contentOwnerCoupleId) {
            throw ContentAccessDeniedException(errorCode = COUPLE_NOT_MATCHED)
        }

        val newContentDetail = ContentDetail(
            title = requestVo.title,
            description = requestVo.description,
            isCompleted = requestVo.isCompleted
        )
        memo.updateContentDetail(newContentDetail)
        memo.updatecontentAsignee(requestVo.contentAsignee)

        updateTags(memo, requestVo.tagList.toSet())
        if (requestVo.dateTimeInfo == null) {  // 날짜 정보가 없다면 메모 업데이트만 진행
            return ContentSummaryVo(
                id = memo.id,
                contentType = memo.type
            )
        }

        memo.updatecontentAsignee(requestVo.contentAsignee)
        
        val scheduleEvent = with(requestVo.dateTimeInfo) {
            ScheduleEvent.fromMemo(
                memo = memo,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                startTimeZone = java.time.ZoneId.of(startTimezone),
                endTimeZone = endTimezone?.let { java.time.ZoneId.of(it) },
            )
        }
        val savedScheduleEvent = scheduleEventRepository.save(scheduleEvent)

        applicationEventPublisher.publishEvent(
            ScheduleCreateEvent(
                userId = userId,
                coupleId = couple.id,
                memberIds = couple.members.map { it.id }.toSet(),
                contentDetail = ContentDetailVo.from(savedScheduleEvent.content.contentDetail)
            )
        )
        return ContentSummaryVo(
            id = savedScheduleEvent.id,
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
        requestVo: UpdateContentRequestVo,
        userCoupleId: Long,
        userId: Long,
    ): ContentSummaryVo {
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
