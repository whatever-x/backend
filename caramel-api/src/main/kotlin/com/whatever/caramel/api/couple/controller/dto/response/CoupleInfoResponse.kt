package com.whatever.caramel.api.couple.controller.dto.response

import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.model.CoupleStatus
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "커플 상세 응답 DTO")
data class CoupleDetailResponse(
    @Schema(description = "커플 id")
    val coupleId: Long,

    @Schema(description = "시작일", nullable = true)
    val startDate: LocalDate?,

    @Schema(description = "공유 메시지", nullable = true)
    val sharedMessage: String?,

    @Schema(description = "커플 상태")
    val status: CoupleStatus,

    @Schema(description = "내 정보")
    val myInfo: CoupleUserInfoDto,

    @Schema(description = "상대방 정보")
    val partnerInfo: CoupleUserInfoDto,
) {
    companion object {
        fun from(couple: Couple, myUser: User, partnerUser: User): CoupleDetailResponse {
            return CoupleDetailResponse(
                coupleId = couple.id,
                startDate = couple.startDate,
                sharedMessage = couple.sharedMessage,
                status = couple.status,
                myInfo = CoupleUserInfoDto.from(myUser),
                partnerInfo = CoupleUserInfoDto.from(partnerUser),
            )
        }
    }
}

@Schema(description = "커플 정보(유저 제외) 응답 DTO")
data class CoupleBasicResponse(
    @Schema(description = "커플 id")
    val coupleId: Long,

    @Schema(description = "시작일", nullable = true)
    val startDate: LocalDate?,

    @Schema(description = "공유 메시지", nullable = true)
    val sharedMessage: String?,

    @Schema(description = "커플 상태")
    val status: CoupleStatus,
) {
    companion object {
        fun from(couple: Couple): CoupleBasicResponse {
            return CoupleBasicResponse(
                coupleId = couple.id,
                startDate = couple.startDate,
                sharedMessage = couple.sharedMessage,
                status = couple.status,
            )
        }
    }
}

@Schema(description = "커플 유저 정보 DTO")
data class CoupleUserInfoDto(
    @Schema(description = "유저 id")
    val id: Long,

    @Schema(description = "유저 상태")
    val userStatus: UserStatus,

    @Schema(description = "닉네임")
    val nickname: String,

    @Schema(description = "생년월일")
    val birthDate: LocalDate,

    @Schema(description = "성별")
    val gender: UserGender,
) {
    companion object {
        fun from(user: User): CoupleUserInfoDto {
            return CoupleUserInfoDto(
                id = user.id,
                userStatus = user.userStatus,
                nickname = user.nickname!!,
                birthDate = user.birthDate!!,
                gender = user.gender!!,
            )
        }
    }
}
