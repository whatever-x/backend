package com.whatever.domain.user.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class UserExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    INVALID_NICKNAME_LENGTH("001", "닉네임은 2~10자 이내로 입력해주세요."),
    INVALID_NICKNAME_CHARACTER("002", "닉네임은 한글, 영문, 숫자만 사용 가능합니다."),
    NICKNAME_REQUIRED("003", "닉네임은 필수 입력값입니다."),
    SERVICE_TERMS_AGREEMENT_REQUIRED("005", "서비스 이용약관 동의가 필요합니다."),
    PRIVATE_POLICY_AGREEMENT_REQUIRED("006", "개인정보 수집 및 이용 동의가 필요합니다."),
    ;

    override val code = "USER$sequence"
}