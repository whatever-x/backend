package com.whatever.domain.sample.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class SampleExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus,
) : CaramelExceptionCode {

    SAMPLE_CODE("000", "샘플 예외입니다.",  HttpStatus.BAD_REQUEST),
    SAMPLE_NOT_FOUND("001", "올바르지 않은 예외코드 입력입니다.",  HttpStatus.NOT_FOUND),
    SAMPLE_SERVER_ERROR("002", "샘플 서버 예외입니다.",  HttpStatus.INTERNAL_SERVER_ERROR),
    SAMPLE_FORBIDDEN("003", "접근 권한이 없습니다.",  HttpStatus.FORBIDDEN),
    SAMPLE_UNAUTHORIZED("004", "인증이 실패했습니다.",  HttpStatus.UNAUTHORIZED),
    ;

    override val code = "SAMPLE$sequence"
}
