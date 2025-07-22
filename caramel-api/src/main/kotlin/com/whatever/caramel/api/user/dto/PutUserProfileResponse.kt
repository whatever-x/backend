package com.whatever.caramel.api.user.dto

import com.whatever.caramel.domain.user.vo.UpdatedUserProfileVo
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "유저 프로필 수정 성공 응답 DTO")
data class PutUserProfileResponse(
    @Schema(description = "유저 아이디")
    val id: Long,
    @Schema(description = "현재 닉네임")
    val nickname: String,
    @Schema(description = "현재 생일")
    val birthday: LocalDate,
) {
    companion object {
        fun from(updatedUserProfileVo: UpdatedUserProfileVo): PutUserProfileResponse {
            return PutUserProfileResponse(
                id = updatedUserProfileVo.id,
                nickname = updatedUserProfileVo.nickname,
                birthday = updatedUserProfileVo.birthday,
            )
        }
    }
}
