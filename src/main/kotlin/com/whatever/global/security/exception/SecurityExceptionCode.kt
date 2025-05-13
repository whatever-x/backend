package com.whatever.global.security.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class SecurityExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    FORBIDDEN("000", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("001", "인증이 실패했습니다.", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_NOT_FOUND("002", "인증 정보가 없습니다. 재로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("003", "사용자 정보가 존재하지 않습니다. 재가입이 필요합니다.", HttpStatus.UNAUTHORIZED),
    BLACK_LISTED_TOKEN("004", "로그아웃하여 사용할 수 없는 토큰입니다.", HttpStatus.UNAUTHORIZED),
    ;

    override val code = "SECURITY$sequence"
}
