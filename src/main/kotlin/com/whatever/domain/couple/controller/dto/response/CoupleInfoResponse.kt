package com.whatever.domain.couple.controller.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class CoupleDetailResponse(
    val coupleId: Long,
    @Schema(description = "커플 시작일")
    val startDate: LocalDate?,
    val sharedMessage: String?,
    @Schema(description = "내 정보")
    val myInfo: CoupleUserInfoDto,
    @Schema(description = "상대방 정보")
    val partnerInfo: CoupleUserInfoDto
)

data class CoupleBasicResponse(
    val coupleId: Long,
    val startDate: LocalDate?,
    val sharedMessage: String?,
)


data class CoupleUserInfoDto(
    val id: Long,
    val nickname: String,
    val birthDate: LocalDate
)
