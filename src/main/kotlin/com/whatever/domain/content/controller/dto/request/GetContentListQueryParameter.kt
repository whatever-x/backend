package com.whatever.domain.content.controller.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class GetContentListQueryParameter(
    @field:Parameter(
        name = "pageSize",
        description = "조회할 크기(5 ~ 20). 기본 10",
        required = true,
        `in` = ParameterIn.QUERY,
        schema = Schema(
            minimum = "5",
            maximum = "20",
            defaultValue = "10"
        )
    )
    @field:Min(5) @field:Max(20)
    val pageSize: Int = 10,

    @field:Parameter(
        name = "lastId",
        description = "이전 리스트의 마지막 id",
        `in` = ParameterIn.QUERY,
        schema = Schema(
            required = false,
            defaultValue = "-1",
        )
    )
    @field:Min(-1)
    val lastId: Long = -1
)
