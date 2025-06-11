package com.whatever.domain.sample.exception

import com.whatever.global.exception.ErrorUiType
import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class SampleExceptionCode(
    sequence: String,
    override val message: String,
    override val description: String? = null,
    override val status: HttpStatus,
    override val errorUiType: ErrorUiType = ErrorUiType.TOAST,
) : CaramelExceptionCode {

    SAMPLE_CODE(
        sequence = "000",
        message = "샘플 예외코드입니다.",
        status = HttpStatus.NOT_FOUND
    ),
    ;

    override val code = "SAMPLE$sequence"
}
