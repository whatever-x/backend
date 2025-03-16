package com.whatever.domain.couple.exception

import com.whatever.global.exception.common.CaramelException

open class CoupleAccessDeniedException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)
