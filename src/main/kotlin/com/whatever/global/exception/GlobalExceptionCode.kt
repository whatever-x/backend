package com.whatever.global.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class GlobalExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
    override val errorUiType: ErrorUiType = ErrorUiType.TOAST,
) : CaramelExceptionCode {

    UNKNOWN( "000", "알 수 없는 에러입니다. 담당자에게 문의해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    NO_RESOURCE( "001", "요청 경로가 잘못되었습니다.", HttpStatus.NOT_FOUND),
    ARGS_VALIDATION_FAILED( "002", "요청 형식이 올바르지 않습니다."),
    ARGS_TYPE_MISMATCH( "003", "요청 타입이 올바르지 않습니다."),
    INVALID_ARGUMENT("004", "잘못된 인자가 전달되었습니다."),
    ILLEGAL_STATE("005", "잘못된 상태입니다.", HttpStatus.CONFLICT),
    ACCESS_DENIED("006", "잘못된 접근 입니다.", HttpStatus.FORBIDDEN),
    ;

    override val code = "GLOBAL$sequence"
}
