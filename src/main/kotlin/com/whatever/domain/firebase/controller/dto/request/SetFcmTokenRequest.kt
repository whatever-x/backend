package com.whatever.domain.firebase.controller.dto.request

data class SetFcmTokenRequest(
    val userId: Long,
    val token: String,
)