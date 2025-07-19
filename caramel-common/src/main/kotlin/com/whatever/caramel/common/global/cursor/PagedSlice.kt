package com.whatever.caramel.common.global.cursor

data class PagedSlice<T>(
    val list: List<T>,
    val cursor: Cursor
) {
    fun <R> map(transform: (T) -> R): PagedSlice<R> {
        return PagedSlice(
            list = list.map(transform),
            cursor = cursor
        )
    }

    companion object {
        fun <T> empty(): PagedSlice<T> {
            return PagedSlice(
                list = emptyList(),
                cursor = Cursor.empty()
            )
        }

        fun <T> from(
            list: List<T>,
            size: Int,
            generateCursor: ((T) -> String)
        ): PagedSlice<T> {
            return when {
                size <= 0 -> empty()
                list.size <= size -> {
                    PagedSlice(
                        list = list,
                        cursor = Cursor(next = null)
                    )
                }

                else -> {
                    val subList = list.take(size)
                    val nextCursor = Cursor(
                        next = generateCursor(subList.last())
                    )
                    PagedSlice(
                        list = subList,
                        cursor = nextCursor
                    )
                }
            }
        }
    }
}