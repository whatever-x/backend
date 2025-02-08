package com.whatever.domain.sample.exception

import com.whatever.global.exception.common.CaramelException
import com.whatever.global.exception.common.CaramelExceptionCode

class SampleForbiddenException(
    errorCode: CaramelExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)
