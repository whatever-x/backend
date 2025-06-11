package com.whatever.domain.sample.exception

import com.whatever.global.exception.ErrorUi
import com.whatever.global.exception.common.CaramelException
import com.whatever.global.exception.common.CaramelExceptionCode

class SampleNotFoundException(
    errorCode: CaramelExceptionCode,
    errorUi: ErrorUi,
) : CaramelException(errorCode, errorUi)
