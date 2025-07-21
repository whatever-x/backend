package com.whatever.security.exception

import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class SecurityExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    FORBIDDEN(
        sequence = "000",
        message = "접근 권한이 없습니다.",
        status = HttpStatus.FORBIDDEN
    ),
    UNAUTHORIZED(
        sequence = "001",
        message = "인증이 실패했습니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    AUTHENTICATION_NOT_FOUND(
        sequence = "002",
        message = "인증 정보가 없습니다. 재로그인이 필요합니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    USER_NOT_FOUND(
        sequence = "003",
        message = "사용자 정보가 존재하지 않습니다. 재가입이 필요합니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    BLACK_LISTED_TOKEN(
        sequence = "004",
        message = "로그아웃하여 사용할 수 없는 토큰입니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    ;

    override val code = "SECURITY$sequence"
}
