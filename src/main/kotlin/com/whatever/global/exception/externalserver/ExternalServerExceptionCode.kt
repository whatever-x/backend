package com.whatever.global.exception.externalserver

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class ExternalServerExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    UNKNOWN("000", "알 수 없는 에러입니다. 담당자에게 문의해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    override val code = "EXTERNAL_SERVER$sequence"
}