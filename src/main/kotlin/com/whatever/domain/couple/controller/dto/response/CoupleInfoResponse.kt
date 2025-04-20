package com.whatever.domain.couple.controller.dto.response

import com.whatever.domain.couple.model.Couple
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "커플 상세 응답 모델")
data class CoupleDetailResponse(
    val coupleId: Long,
    @Schema(description = "커플 시작일")
    val startDate: LocalDate?,
    val sharedMessage: String?,
    @Schema(description = "내 정보")
    val myInfo: CoupleUserInfoDto,
    @Schema(description = "상대방 정보")
    val partnerInfo: CoupleUserInfoDto
) {
    companion object {
        fun from(couple: Couple, myUser: User, partnerUser: User): CoupleDetailResponse {
            return CoupleDetailResponse(
                coupleId = couple.id,
                startDate = couple.startDate,
                sharedMessage = couple.sharedMessage,
                myInfo = CoupleUserInfoDto.from(myUser),
                partnerInfo = CoupleUserInfoDto.from(partnerUser),
            )
        }
    }
}

@Schema(description = "커플 정보(유저 제외) 응답 모델")
data class CoupleBasicResponse(
    val coupleId: Long,
    val startDate: LocalDate?,
    val sharedMessage: String?,
) {
    companion object {
        fun from(couple: Couple): CoupleBasicResponse {
            return CoupleBasicResponse(
                coupleId = couple.id,
                startDate = couple.startDate,
                sharedMessage = couple.sharedMessage,
            )
        }
    }
}

@Schema(description = "커플 유저 정보 모델")
data class CoupleUserInfoDto(
    val id: Long,
    val nickname: String,
    val birthDate: LocalDate,
    val gender: UserGender,
) {
    companion object {
        fun from(user: User): CoupleUserInfoDto {
            return CoupleUserInfoDto(
                id = user.id,
                nickname = user.nickname!!,
                birthDate = user.birthDate!!,
                gender = user.gender!!,
            )
        }
    }
}
