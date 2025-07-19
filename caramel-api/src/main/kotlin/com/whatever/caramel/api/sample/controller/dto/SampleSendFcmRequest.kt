package com.whatever.com.whatever.caramel.api.sample.controller.dto

data class SampleSendFcmRequest(
    val targetUserIds: Set<Long>,
    val title: String,
    val body: String,
)
