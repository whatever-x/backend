package com.whatever.domain.auth.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class AuthExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    UNKNOWN( "000", "알 수 없는 에러입니다. 담당자에게 문의해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND( "001", "해당 소셜 계정으로 가입된 유저가 없습니다.", HttpStatus.NOT_FOUND),
    ;

    override val code = "AUTH$sequence"
}
