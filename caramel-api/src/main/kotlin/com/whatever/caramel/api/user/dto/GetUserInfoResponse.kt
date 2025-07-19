package com.whatever.com.whatever.caramel.api.user.dto

import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "내 정보 조회 응답 DTO")
data class GetUserInfoResponse(
    @Schema(description = "유저의 id")
    val id: Long,

    @Schema(
        description = "어떤 유저라도 null이 나올 수 있습니다.",
        nullable = true
    )
    val email: String?,

    @Schema(
        description = "NEW 유저일 경우 null입니다.",
        nullable = true
    )
    val birthDate: LocalDate?,

    @Schema(description = "가입한 소셜 플랫폼")
    val signInPlatform: LoginPlatform,

    @Schema(
        description = "NEW 유저일 경우 null입니다.",
        nullable = true
    )
    val nickname: String?,

    @Schema(
        description = "NEW 유저일 경우 null입니다.",
        nullable = true
    )
    val gender: UserGender?,

    @Schema(description = "유저의 현재 상태")
    val userStatus: UserStatus,
) {
    companion object {
        fun from(user: User): GetUserInfoResponse {
            return GetUserInfoResponse(
                id = user.id,
                email = user.email,
                birthDate = user.birthDate,
                signInPlatform = user.platform,
                nickname = user.nickname,
                gender = user.gender,
                userStatus = user.userStatus,
            )
        }
    }
}
