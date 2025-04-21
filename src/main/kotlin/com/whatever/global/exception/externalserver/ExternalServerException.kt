package com.whatever.global.exception.externalserver

import com.whatever.global.exception.common.CaramelException

abstract class ExternalServerException(
    errorCode: ExternalServerExceptionCode,
    detailMessage: String? = null,
) : CaramelException(errorCode, detailMessage)
