package com.whatever.caramel.common.response

import com.whatever.caramel.common.global.cursor.Cursor
import com.whatever.caramel.common.global.cursor.PagedSlice
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "커서 기반 조회 공통 응답 DTO")
data class CursoredResponse<T>(
    @Schema(description = "데이터 목록")
    val list: List<T>,
    @Schema(description = "다음 요청에 들어갈 커서")
    val cursor: Cursor,
) {
    companion object {
        fun <T> from(pagedSlice: PagedSlice<T>): CursoredResponse<T> {
            return CursoredResponse(
                list = pagedSlice.list,
                cursor = pagedSlice.cursor
            )
        }
    }
}
