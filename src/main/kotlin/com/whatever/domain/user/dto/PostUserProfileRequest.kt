package com.whatever.domain.user.dto

import com.whatever.domain.user.model.UserGender
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class PostUserProfileRequest(
    @Schema(description = "닉네임")
    @field:NotBlank(message = "닉네임은 공백일 수 없습니다.")
    @field:Size(min = 3, max = 10, message = "닉네임은 3~10자 이내로 입력해주세요.")
    @field:Pattern(regexp = "^[가-힣a-zA-Z0-9]+$")
    val nickname: String,
    @Schema(description = "YYYY-MM-DD 형식의 생일")
    val birthday: LocalDate,
    @Schema(description = "성별")
    val gender: UserGender,
    @Schema(description = "서비스/약관 동의 여부")
    @field:AssertTrue(message = "서비스 이용 약관에 동의해야 합니다.")
    val agreementServiceTerms: Boolean,
    @Schema(description = "개인정보 수집/이용 동의")
    @field:AssertTrue(message = "개인정보 수집 및 이용에 동의해야 합니다.")
    val agreementPrivatePolicy: Boolean,
)
