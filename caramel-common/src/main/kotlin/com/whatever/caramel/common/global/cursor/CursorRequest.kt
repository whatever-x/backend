package com.whatever.caramel.common.global.cursor

import org.springframework.data.domain.PageRequest

interface CursorRequest {
    val cursor: String?
    val size: Int
    val sortType: com.whatever.caramel.common.global.cursor.Sortables

    fun cursorAwarePageSize(): Int = size + 1

    fun toPageable(): PageRequest {
        return PageRequest.of(0, cursorAwarePageSize(), sortType.toSort())
    }
}
