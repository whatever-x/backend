package com.whatever.global.exception

sealed class ErrorUi(open val title: String) {
    data class Toast(
        override val title: String
    ) : ErrorUi(title)
    data class Dialog(
        override val title: String,
        val description: String? = null,
    ) : ErrorUi(title)
}