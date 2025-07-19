package com.whatever

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "커서 기반 조회 공통 응답 DTO")
data class CursoredResponse<T>(
    @Schema(description = "데이터 목록")
    val list: List<T>,
    @Schema(description = "다음 요청에 들어갈 커서")
    val cursor: Cursor,
) {
    fun <R> map(transform: (T) -> R): CursoredResponse<R> {
        return CursoredResponse(
            list = list.map(transform),
            cursor = cursor,
        )
    }

    companion object {
        fun <T> empty(): CursoredResponse<T> {
            return CursoredResponse(
                list = emptyList(),
                cursor = Cursor.empty(),
            )
        }

        fun <T> from(
            list: List<T>,
            size: Int,
            generateCursor: ((T) -> String),
        ): CursoredResponse<T> {
            return when {
                size <= 0 -> empty()
                list.size <= size -> {
                    CursoredResponse(
                        list = list,
                        cursor = Cursor(next = null)
                    )
                }

                else -> {
                    val subList = list.take(size)
                    // 다음 커서는 현재 서브리스트의 마지막 요소를 기준으로 만들어짐!
                    val nextCursor = Cursor(
                        next = generateCursor(subList.last())
                    )
                    CursoredResponse(
                        list = subList,
                        cursor = nextCursor,
                    )
                }
            }
        }
    }
}
