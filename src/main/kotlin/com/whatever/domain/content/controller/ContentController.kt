package com.whatever.domain.content.controller

import com.whatever.domain.content.controller.dto.request.CreateContentRequest
import com.whatever.domain.content.controller.dto.request.GetContentListQueryParameter
import com.whatever.domain.content.controller.dto.request.UpdateContentRequest
import com.whatever.domain.content.controller.dto.response.ContentDetailListResponse
import com.whatever.domain.content.controller.dto.response.ContentDetailResponse
import com.whatever.domain.content.controller.dto.response.ContentSummaryResponse
import com.whatever.domain.content.controller.dto.response.TagDto
import com.whatever.domain.content.exception.ContentException
import com.whatever.domain.content.exception.ContentExceptionCode
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.service.ContentService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import com.whatever.util.DateTimeUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.*

@Tag(
    name = "Content",
    description = "콘텐츠 API"
)
@RestController
@RequestMapping("/v1/content")
class ContentController(
    private val contentService: ContentService,
) {

    @Operation(
        summary = "더미 콘텐츠 조회",
        description = "콘텐츠를 조회합니다.",
    )
    @GetMapping
    fun getContents(
        @ParameterObject queryParameter: GetContentListQueryParameter,
    ): CaramelApiResponse<ContentDetailListResponse> {

        // TODO(준용): 구현 필요
        val start = queryParameter.lastId + 1
        val end = start + queryParameter.pageSize
        val contentList = (start..end).map { id ->
            ContentDetailResponse(
                contentId = id,
                title = "Title $id",
                description = "Description for content $id, link: https://youtu.be/FEcfXRPaO90?si=alix-sIZkIjOzfmx",
                wishDate = DateTimeUtil.localNow().toLocalDate(),
                isCompleted = false,
                tagList = if (id % 2L == 0L) listOf(TagDto(tagId = id, tagLabel = "Tag for $id")) else emptyList()
            )
        }
        return ContentDetailListResponse(
            contentList = contentList
        ).succeed()
    }

    @Operation(
        summary = "콘텐츠 생성",
        description = "콘텐츠를 생성합니다. 날짜 정보를 보내지 않으면 Memo, 날짜 정보가 포함되면 Schedule로 생성됩니다."
    )
    @PostMapping
    fun createContent(
        @Valid @RequestBody request: CreateContentRequest
    ): CaramelApiResponse<ContentSummaryResponse> {
        if (request.title.isNullOrBlank() && request.description.isNullOrBlank()) {
            throw ContentException(errorCode = ContentExceptionCode.TITLE_OR_DESCRIPTION_REQUIRED)
        }
        return contentService.createContent(request).succeed()
    }

    @Operation(
        summary = "더미 콘텐츠 수정",
        description = "콘텐츠를 수정합니다. 수정된 값을 포함하여 기존을 값을 모두 전달합니다.",
    )
    @PutMapping("/{content-id}")
    fun updateContent(
        @PathVariable("content-id") contentId: Long,
        @RequestBody request: UpdateContentRequest,
    ): CaramelApiResponse<ContentSummaryResponse> {

        // TODO(준용): 구현 필요
        return ContentSummaryResponse(
            contentId = contentId,
            contentType = request.dateTimeInfo?.let { ContentType.SCHEDULE }
                ?: ContentType.MEMO,
        ).succeed()
    }

    @Operation(
        summary = "더미 콘텐츠 삭제",
        description = "콘텐츠를 삭제합니다.",
    )
    @DeleteMapping("/{content-id}")
    fun deleteContent(@PathVariable("content-id") contentId: Long): CaramelApiResponse<Unit> {

        // TODO(준용): 구현 필요
        return CaramelApiResponse.succeed()
    }
}