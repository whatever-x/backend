package com.whatever.domain.content.tag.controller

import com.whatever.domain.content.tag.controller.dto.response.TagDetailListResponse
import com.whatever.domain.content.tag.service.TagService
import com.whatever.global.annotation.DisableSwaggerAuthButton
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
@RequestMapping("v1/tags")
class TagController(
    private val tagService: TagService,
) {

    @DisableSwaggerAuthButton
    @Operation(
        summary = "태그 조회",
        description = "태그 리스트를 조회합니다.",
    )
    @GetMapping
    fun getTags(): CaramelApiResponse<TagDetailListResponse> {
        val tags = tagService.getTags()
        return tags.succeed()
    }
}