package com.whatever.domain.sample.exception

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException

class SampleNotFoundException(
    errorCode: SampleExceptionCode,
    errorUi: ErrorUi,
) : CaramelException(errorCode, errorUi)
