package com.whatever.domain.couple.controller.dto.response

import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.model.CoupleAnniversaryType
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
        fun of(
            couple: Couple,
            hundredDayAnniversaries: List<CoupleAnniversaryDto>,
            yearlyAnniversaries: List<CoupleAnniversaryDto>,
            myBirthDates: List<CoupleAnniversaryDto>,
            partnerBirthDates: List<CoupleAnniversaryDto>,
        ): CoupleAnniversaryResponse {
            return CoupleAnniversaryResponse(
                coupleId = couple.id,
                startDate = couple.startDate,
                sharedMessage = couple.sharedMessage,
                hundredDayAnniversaries = hundredDayAnniversaries,
                yearlyAnniversaries = yearlyAnniversaries,
                myBirthDates = myBirthDates,
                partnerBirthDates = partnerBirthDates,
            )
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
)
