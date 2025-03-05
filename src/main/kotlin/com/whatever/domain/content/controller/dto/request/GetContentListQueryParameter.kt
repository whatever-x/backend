package com.whatever.domain.content.controller.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class GetContentListQueryParameter(
    @field:Parameter(
        name = "pageSize",
        description = "조회할 크기(5 ~ 20). 기본 10",
        `in` = ParameterIn.QUERY,
        required = false,
    )
    @field:Min(5) @field:Max(20)
    val pageSize: Int = 10,

    @field:Parameter(
        name = "lastId",
        description = "이전 리스트의 마지막 id. 기본 -1",
        `in` = ParameterIn.QUERY,
        required = false,
    )
    @field:Min(-1)
    val lastId: Long = -1
)
