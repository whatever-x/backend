package com.whatever.caramel.api.user.dto

import com.whatever.caramel.domain.user.model.UserStatus
import com.whatever.caramel.domain.user.vo.CreatedUserProfileVo
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "유저 프로필 생성 응답 DTO")
data class PostUserProfileResponse(
    @Schema(description = "유저 아이디")
    val id: Long,
    @Schema(description = "닉네임")
    val nickname: String,
    @Schema(description = "유저 상태 NEW/SINGLE/COUPLED")
    val userStatus: UserStatus,
) {
    companion object {
        fun from(createdUserProfileVo: CreatedUserProfileVo): PostUserProfileResponse {
            return PostUserProfileResponse(
                id = createdUserProfileVo.id,
                nickname = createdUserProfileVo.nickname,
                userStatus = createdUserProfileVo.userStatus,
            )
        }
    }
}
