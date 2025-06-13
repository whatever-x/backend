package com.whatever.domain.sample.controller.dto

data class SampleSendFcmRequest(
    val targetUserIds: Set<Long>,
    val title: String,
    val body: String,
)
