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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        summary = "메모 목록 조회",
        description = "메모 목록을 조회합니다.",
    )
    @GetMapping("/memo")
    fun getContents(
        @ParameterObject queryParameter: GetContentListQueryParameter,
    ): CaramelApiResponse<CursoredResponse<ContentResponse>> {
        return contentService.getContentList(queryParameter).succeed()
    }

    @Operation(
        summary = "메모 조회",
        description = "메모를 조회합니다. 커플의 멤버가 작성한 메모만 조회할 수 있습니다.",
    )
    @GetMapping("/memo/{memo-id}")
    fun getMemo(
        @PathVariable("memo-id") memoId: Long,
    ): CaramelApiResponse<ContentResponse> {
        val response = contentService.getMemo(memoId)
        return response.succeed()
    }

    @Operation(
        summary = "메모 생성",
        description = "메모를 생성합니다."
    )
    @PostMapping("/memo")
    fun createContent(
        @Valid @RequestBody request: CreateContentRequest
    ): CaramelApiResponse<ContentSummaryResponse> {
        if (request.title.isNullOrBlank() && request.description.isNullOrBlank()) {
            throw ContentException(errorCode = ContentExceptionCode.TITLE_OR_DESCRIPTION_REQUIRED)
        }
        return contentService.createContent(request).succeed()
    }

    @Operation(
        summary = "메모 수정",
        description = "메모 콘텐츠를 수정합니다. 수정된 값을 포함하여 기존을 값을 모두 전달합니다. 메모를 일정으로 변경할 때 사용할 수 있습니다.",
    )
    @PutMapping("/memo/{memo-id}")
    fun updateContent(
        @PathVariable("memo-id") contentId: Long,
        @Valid @RequestBody request: UpdateContentRequest,
    ): CaramelApiResponse<ContentSummaryResponse> {
        return contentService.updateContent(contentId, request).succeed()
    }

    @Operation(
        summary = "메모 삭제",
        description = "메모 콘텐츠를 삭제합니다.",
    )
    @DeleteMapping("/memo/{memo-id}")
    fun deleteContent(@PathVariable("memo-id") contentId: Long): CaramelApiResponse<Unit> {
        contentService.deleteContent(contentId)
        return CaramelApiResponse.succeed()
    }
}