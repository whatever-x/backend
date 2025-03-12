package com.whatever.domain.couple.controller.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "커플 상세 응답 모델")
data class CoupleDetailResponse(
    val coupleId: Long,
    @Schema(description = "커플 시작일")
    val startDate: LocalDate?,
    val sharedMessage: String?,
    @Schema(description = "내 정보")
    val hostInfo: CoupleUserInfoDto,
    @Schema(description = "상대방 정보")
    val partnerInfo: CoupleUserInfoDto
)

@Schema(description = "커플 정보(유저 제외) 응답 모델")
data class CoupleBasicResponse(
    val coupleId: Long,
    val startDate: LocalDate?,
    val sharedMessage: String?,
)

@Schema(description = "커플 유저 정보 모델")
data class CoupleUserInfoDto(
    val id: Long,
    val nickname: String,
    val birthDate: LocalDate
)
