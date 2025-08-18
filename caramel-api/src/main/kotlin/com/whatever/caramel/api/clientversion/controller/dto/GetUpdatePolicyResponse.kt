package com.whatever.caramel.api.clientversion.controller.dto

data class GetUpdatePolicyResponse(
    val forceUpdate: Boolean,
    val updateUri: String? = null,
)
