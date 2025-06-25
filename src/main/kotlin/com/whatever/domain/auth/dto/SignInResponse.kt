package com.whatever.domain.auth.dto

import com.whatever.domain.user.model.UserStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "로그인 성공 응답 DTO")
data class SignInResponse(
    @Schema(description = "서버에서 발급한 JWT(access, refresh) 정보")
    val serviceToken: ServiceToken,

    @Schema(description = "유저의 현재 상태")
    val userStatus: UserStatus,

    @Schema(description = "유저의 닉네임. null일 수 있음.", nullable = true)
    val nickname: String?,

    @Schema(description = "유저의 생일. null일 수 있음.", nullable = true)
    val birthDay: LocalDate?,

    @Schema(description = "유저가 속한 커플 id. null일 수 있음.", nullable = true)
    val coupleId: Long?,
)