package com.whatever.domain.auth.dto

import com.whatever.domain.user.model.LoginPlatform

data class SignInRequest(
    val loginPlatform: LoginPlatform,
    val idToken: String,
)