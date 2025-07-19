package com.whatever.com.whatever.caramel.api.content.tag.controller

import com.whatever.CaramelApiResponse
import com.whatever.caramel.common.global.annotation.DisableSwaggerAuthButton
import com.whatever.com.whatever.caramel.api.content.tag.controller.dto.response.TagDetailListResponse
import com.whatever.domain.content.tag.controller.dto.response.TagDetailListResponse
import com.whatever.domain.content.tag.service.TagService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "태그 API",
    description = "태그 관련 기능을 제공하는 API"
)
@RestController
@RequestMapping("/v1/tags")
class TagController(
    private val tagService: TagService,
) {

    @DisableSwaggerAuthButton
    @Operation(
        summary = "태그 조회",
        description = """### 태그 리스트를 조회합니다.""",
        responses = [
            ApiResponse(responseCode = "200", description = "태그 상세 정보 리스트")
        ]
    )
    @GetMapping
    fun getTags(): CaramelApiResponse<TagDetailListResponse> {
        val tags = tagService.getTags()
        return tags.succeed()
    }
}
