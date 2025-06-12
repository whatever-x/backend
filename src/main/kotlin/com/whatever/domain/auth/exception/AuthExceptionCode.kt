package com.whatever.domain.auth.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class AuthExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    UNKNOWN(
        sequence = "000",
        message = "알 수 없는 에러입니다.",
        status = HttpStatus.INTERNAL_SERVER_ERROR
    ),
    USER_NOT_FOUND(
        sequence = "001",
        message = "해당 소셜 계정으로 가입된 유저가 없습니다.",
        status = HttpStatus.NOT_FOUND
    ),
    UNAUTHORIZED(
        sequence = "002",
        message = "발급받은 인증 정보가 유효하지 않습니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    ILLEGAL_KID(
        sequence = "003",
        message = "id token의 kid와 일치하는 key를 찾을 수 없습니다.",
    ),
    USER_PROVIDER_NOT_FOUND(
        sequence = "004",
        message = "지원되지 않는 로그인 플랫폼입니다.",
    ),
    ;

    override val code = "AUTH$sequence"
}
