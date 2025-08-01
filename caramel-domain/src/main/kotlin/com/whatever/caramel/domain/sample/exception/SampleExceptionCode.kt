package com.whatever.caramel.domain.sample.exception

import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class SampleExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus,
) : CaramelExceptionCode {

    SAMPLE_CODE(
        sequence = "000",
        message = "샘플 예외코드입니다.",
        status = HttpStatus.NOT_FOUND
    ),
    ;

    override val code = "SAMPLE$sequence"
}
