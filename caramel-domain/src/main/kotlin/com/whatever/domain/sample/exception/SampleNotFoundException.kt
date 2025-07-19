package com.whatever.domain.sample.exception

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException
import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode

class SampleNotFoundException(
    errorCode: SampleExceptionCode,
    errorUi: ErrorUi,
) : CaramelException(errorCode, errorUi)
