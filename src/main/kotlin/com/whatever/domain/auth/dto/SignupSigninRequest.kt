package com.whatever.domain.auth.dto

import com.whatever.domain.user.model.LoginPlatform

data class SignupSigninRequest(
    val loginPlatform: LoginPlatform,
    val accessToken: String,
)