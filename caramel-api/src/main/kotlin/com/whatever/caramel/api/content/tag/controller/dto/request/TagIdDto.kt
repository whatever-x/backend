package com.whatever.com.whatever.caramel.api.content.tag.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "태그 정보 모델")
data class TagIdDto(
    @Schema(description = "태그 id")
    val tagId: Long,
)