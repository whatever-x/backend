package com.whatever.caramel.common.global.cursor

data class Cursor(
    val next: String?,
) {
    companion object {
        fun empty() = com.whatever.caramel.common.global.cursor.Cursor(next = null)
    }
}
