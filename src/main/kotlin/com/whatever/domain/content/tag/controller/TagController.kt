package com.whatever.domain.content.tag.controller

import com.whatever.domain.content.tag.controller.dto.response.TagDetailDto
import com.whatever.domain.content.tag.controller.dto.response.TagDetailListResponse
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Tag",
    description = "태그 API"
)
@RestController
@RequestMapping("/tags")
class TagController {

    @Operation(
        summary = "더미 태그 조회",
        description = "태그 리스트를 조회합니다.",
    )
    @GetMapping
    fun getTags(): CaramelApiResponse<TagDetailListResponse> {

        // TODO(준용): 구현 필요
        return TagDetailListResponse(
            tagList = listOf(
                TagDetailDto(1L, "맛집"),
                TagDetailDto(2L, "데이트"),
            )
        ).succeed()
    }
}