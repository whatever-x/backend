package com.whatever.caramel.common.global.cursor

data class Cursor(
    val next: String?,
) {
    companion object {
        fun empty() = Cursor(next = null)
    }
}
