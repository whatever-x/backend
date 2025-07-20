package com.whatever.com.whatever.caramel.api.content.controller

import com.whatever.CaramelApiResponse
import com.whatever.CursoredResponse
import com.whatever.SecurityUtil
import com.whatever.caramel.api.content.controller.dto.response.ContentSummaryResponse
import com.whatever.com.whatever.caramel.api.content.controller.dto.request.CreateContentRequest
import com.whatever.com.whatever.caramel.api.content.controller.dto.request.GetContentListQueryParameter
import com.whatever.com.whatever.caramel.api.content.controller.dto.request.UpdateContentRequest
import com.whatever.com.whatever.caramel.api.content.controller.dto.response.ContentResponse
import com.whatever.domain.content.exception.ContentException
import com.whatever.domain.content.exception.ContentExceptionCode
import com.whatever.domain.content.service.ContentService
import com.whatever.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.*

@Tag(
    name = "콘텐츠(메모) API",
    description = "메모 관련 기능을 제공하는 API"
)
@RestController
@RequestMapping("/v1/content")
class ContentController(
    private val contentService: ContentService,
) {

    @Operation(
        summary = "커플 메모 목록 조회",
        description = """
            ### 메모 목록을 조회합니다.
            
            - 커플에 속한 유저가 작성한 메모 목록을 반환합니다.
            - response로 제공된 커서를 다음 요청에 그대로 넣어서 요청해야 합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "메모 리스트 + 다음 요청에 사용될 커서"),
        ]
    )
    @GetMapping("/memo")
    fun getContents(
        @ParameterObject queryParameter: GetContentListQueryParameter,
    ): CaramelApiResponse<CursoredResponse<ContentResponse>> {
        val coupleId = SecurityUtil.getCurrentUserCoupleId()
        val contentListVo = contentService.getContentList(queryParameter.toVo(), coupleId)
        val responsePagedSlice = contentListVo.map { ContentResponse.from(it) }
        val response = CursoredResponse.from(responsePagedSlice)
        return response.succeed()
    }

    @Operation(
        summary = "메모 단건 조회",
        description = """
            ### memo-id에 해당하는 메모를 조회합니다.
            
            - 같은 커플에 속한 유저의 메모만 조회할 수 있습니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "메모 정보"),
        ]
    )
    @GetMapping("/memo/{memo-id}")
    fun getMemo(
        @PathVariable("memo-id") memoId: Long,
    ): CaramelApiResponse<ContentResponse> {
        val coupleId = SecurityUtil.getCurrentUserCoupleId()
        val contentResponseVo = contentService.getMemo(memoId, coupleId)
        val response = ContentResponse.from(contentResponseVo)
        return response.succeed()
    }

    @Operation(
        summary = "메모 생성",
        description = """
            ### 메모를 생성합니다.
            
            - `title`과 `description` 둘 중 하나는 입력값이 있어야합니다.
            
            - `title`은 Blank일 수 없습니다.
            
            - `description`은 Blank일 수 없습니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "생성 정보 요약"),
        ]
    )
    @PostMapping("/memo")
    fun createContent(
        @Valid @RequestBody request: CreateContentRequest,
    ): CaramelApiResponse<ContentSummaryResponse> {
        if (request.title.isNullOrBlank() && request.description.isNullOrBlank()) {
            throw ContentException(errorCode = ContentExceptionCode.TITLE_OR_DESCRIPTION_REQUIRED)
        }
        val userId = SecurityUtil.getCurrentUserId()
        val contentSummaryVo = contentService.createContent(request.toVo(), userId)
        val response = ContentSummaryResponse.from(contentSummaryVo)
        return response.succeed()
    }

    @Operation(
        summary = "메모 수정",
        description = """
            ### 메모 수정합니다.
            
            1. 메모의 정보를 단순히 수정하고 싶을 경우
                - `dateTimeInfo`를 전송하지 않거나 null로 설정하고, 나머지 값을 자유롭게 전달합니다.
            
            2. 메모를 일정으로 전환하고 싶은 경우
                - `dateTimeInfo`를 설정하고, 나머지 값을 자유롭게 전달합니다.
                - 일정으로 전환되면 해당 메모는 조회할 수 없습니다.
            
            - title과 description의 제약 조건은 `메모 생성`과 동일합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "콘텐츠 id 정보"),
        ]
    )
    @PutMapping("/memo/{memo-id}")
    fun updateContent(
        @PathVariable("memo-id") contentId: Long,
        @Valid @RequestBody request: UpdateContentRequest,
    ): CaramelApiResponse<ContentSummaryResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val coupleId = SecurityUtil.getCurrentUserCoupleId()
        val contentSummaryVo = contentService.updateContent(contentId, request.toVo(), coupleId, userId)
        val response =
            ContentSummaryResponse.from(contentSummaryVo)
        return response.succeed()
    }

    @Operation(
        summary = "메모 삭제",
        description = """
            ### memo-id에 해당하는 메모를 삭제합니다.
            
            - 같은 커플에 속한 유저의 메모만 삭제할 수 있습니다. 
        """
    )
    @DeleteMapping("/memo/{memo-id}")
    fun deleteContent(@PathVariable("memo-id") contentId: Long): CaramelApiResponse<Unit> {
        contentService.deleteContent(contentId)
        return CaramelApiResponse.succeed()
    }
}
