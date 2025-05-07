package com.whatever.global.cursor

data class Cursor(
    val next: String?,
) {
    companion object {
        fun empty() = Cursor(next = null)
    }
}