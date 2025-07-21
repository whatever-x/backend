package com.whatever.caramel.api.couple.controller.dto.response

import com.whatever.domain.couple.model.CoupleAnniversaryType
import com.whatever.domain.couple.vo.AnniversaryVo
import com.whatever.domain.couple.vo.CoupleAnniversaryVo
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "커플 기념일 응답 DTO")
data class CoupleAnniversaryResponse(
    @Schema(description = "커플 id")
    val coupleId: Long,
    @Schema(description = "시작일", nullable = true)
    val startDate: LocalDate?,
    @Schema(description = "공유 메시지", nullable = true)
    val sharedMessage: String?,

    @Schema(description = "100일 단위 기념일")
    val hundredDayAnniversaries: List<CoupleAnniversaryDto>,
    @Schema(description = "1주년 단위 기념일")
    val yearlyAnniversaries: List<CoupleAnniversaryDto>,
    @Schema(description = "내 생일")
    val myBirthDates: List<CoupleAnniversaryDto>,
    @Schema(description = "상대방 생일")
    val partnerBirthDates: List<CoupleAnniversaryDto>,
) {
    companion object {
        fun from(
            coupleAnniversaryVo: CoupleAnniversaryVo,
        ): CoupleAnniversaryResponse {
            return with(coupleAnniversaryVo) {
                CoupleAnniversaryResponse(
                    coupleId = coupleId,
                    startDate = startDate,
                    sharedMessage = sharedMessage,
                    hundredDayAnniversaries = hundredDayAnniversaries.map { CoupleAnniversaryDto.from(it) },
                    yearlyAnniversaries = yearlyAnniversaries.map { CoupleAnniversaryDto.from(it) },
                    myBirthDates = myBirthDates.map { CoupleAnniversaryDto.from(it) },
                    partnerBirthDates = partnerBirthDates.map { CoupleAnniversaryDto.from(it) },
                )
            }
        }
    }
}

@Schema(description = "커플 기념일 DTO")
data class CoupleAnniversaryDto(
    @Schema(description = "커플 기념일의 종류")
    val type: CoupleAnniversaryType,
    @Schema(description = "커플 기념일")
    val date: LocalDate,
    @Schema(description = "커플 기념일 라벨")
    val label: String,
    @Schema(
        description =
            "기념일이 원래 2월 29일이었으나, 해당 연도가 윤년이 아니어서 날짜가 조정되었는지 여부 (예: 2월 28일로 조정될 때 ture)"
    )
    val isAdjustedForNonLeapYear: Boolean = false,
) {
    companion object {
        fun from(anniversaryVo: AnniversaryVo): CoupleAnniversaryDto {
            return CoupleAnniversaryDto(
                type = anniversaryVo.type,
                date = anniversaryVo.date,
                label = anniversaryVo.label,
                isAdjustedForNonLeapYear = anniversaryVo.isAdjustedForNonLeapYear,
            )
        }
    }
}
