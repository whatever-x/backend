package com.whatever.caramel.api.couple.controller.dto.response

import com.whatever.caramel.domain.couple.vo.CoupleInvitationCodeVo
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "커플 초대코드 응답 DTO")
data class CoupleInvitationCodeResponse(
    @Schema(description = "초대 코드")
    val invitationCode: String,

    @Schema(description = "만료 시점", nullable = true)
    val expirationDateTime: LocalDateTime?,
) {
    companion object {
        fun from(coupleInvitationCodeVo: CoupleInvitationCodeVo): CoupleInvitationCodeResponse {
            return CoupleInvitationCodeResponse(
                invitationCode = coupleInvitationCodeVo.invitationCode,
                expirationDateTime = coupleInvitationCodeVo.expirationDateTime,
            )
        }
    }
}
