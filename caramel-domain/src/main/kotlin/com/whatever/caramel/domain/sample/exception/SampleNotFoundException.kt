package com.whatever.caramel.domain.sample.exception

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException

class SampleNotFoundException(
    errorCode: com.whatever.caramel.domain.sample.exception.SampleExceptionCode,
    errorUi: ErrorUi,
) : CaramelException(errorCode, errorUi)
