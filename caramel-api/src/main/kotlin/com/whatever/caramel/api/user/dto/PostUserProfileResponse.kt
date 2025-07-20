package com.whatever.caramel.api.user.dto

import com.whatever.domain.user.model.UserStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "유저 프로필 생성 응답 DTO")
data class PostUserProfileResponse(
    @Schema(description = "유저 아이디")
    val id: Long,
    @Schema(description = "닉네임")
    val nickname: String,
    @Schema(description = "유저 상태 NEW/SINGLE/COUPLED")
    val userStatus: UserStatus,
)
