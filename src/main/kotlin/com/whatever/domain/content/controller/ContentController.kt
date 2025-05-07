package com.whatever.domain.content.controller

import com.whatever.domain.content.controller.dto.request.CreateContentRequest
import com.whatever.domain.content.controller.dto.request.GetContentListQueryParameter
import com.whatever.domain.content.controller.dto.request.UpdateContentRequest
import com.whatever.domain.content.controller.dto.response.ContentResponse
import com.whatever.domain.content.controller.dto.response.ContentSummaryResponse
import com.whatever.domain.content.exception.ContentException
import com.whatever.domain.content.exception.ContentExceptionCode
import com.whatever.domain.content.service.ContentService
import com.whatever.global.cursor.CursoredResponse
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
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
        summary = "콘텐츠 조회",
        description = "콘텐츠를 조회합니다.",
    )
    @GetMapping
    fun getContents(
        @ParameterObject queryParameter: GetContentListQueryParameter,
    ): CaramelApiResponse<CursoredResponse<ContentResponse>> {
        return contentService.getContentList(queryParameter).succeed()
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
        summary = "콘텐츠 수정(메모)",
        description = "메모 콘텐츠를 수정합니다. 수정된 값을 포함하여 기존을 값을 모두 전달합니다. 메모를 스케줄로 변경할 때 사용하면 안됩니다.",
    )
    @PutMapping("/{content-id}")
    fun updateContent(
        @PathVariable("content-id") contentId: Long,
        @RequestBody request: UpdateContentRequest,
    ): CaramelApiResponse<ContentSummaryResponse> {
        return contentService.updateContent(contentId, request).succeed()
    }

    @Operation(
        summary = "콘텐츠 삭제(메모)",
        description = "메모 콘텐츠를 삭제합니다.",
    )
    @DeleteMapping("/{content-id}")
    fun deleteContent(@PathVariable("content-id") contentId: Long): CaramelApiResponse<Unit> {
        contentService.deleteContent(contentId)
        return CaramelApiResponse.succeed()
    }
}