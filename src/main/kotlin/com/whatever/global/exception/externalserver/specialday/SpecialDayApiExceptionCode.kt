package com.whatever.global.exception.externalserver.specialday

import com.whatever.global.exception.externalserver.ExternalServerExceptionCode
import org.springframework.http.HttpStatus

enum class SpecialDayApiExceptionCode(
    val sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : ExternalServerExceptionCode {
    UNKNOWN(
        sequence = "000",
        message = "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
        status = HttpStatus.INTERNAL_SERVER_ERROR
    ),
    EMPTY_HEADER(
        sequence = "001",
        message = "확인할 수 없는 공휴일 정보 응답입니다.",
        status = HttpStatus.BAD_GATEWAY
    ),
    FAILED_RESPONSE_CODE(
        sequence = "002",
        message = "공휴일 정보 요청에 실패했습니다.",
        status = HttpStatus.BAD_GATEWAY
    ),
    RESPONSE_TYPE_UNMATCHED(
        sequence = "003",
        message = "알 수 없는 응답 타입입니다.",
        status = HttpStatus.BAD_GATEWAY
    ),
    ;

    override val code: String
        get() = "SDA$sequence"
}