package com.whatever.global.cursor

data class CursoredResponse<T>(
    val list: List<T>,
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