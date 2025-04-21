package com.whatever.domain.couple.exception

import com.whatever.global.exception.common.CaramelException

open class CoupleException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)

class CoupleAccessDeniedException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null
) : CoupleException(errorCode, detailMessage)

class CoupleIllegalStateException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null
) : CoupleException(errorCode, detailMessage)

class CoupleIllegalArgumentException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null
) : CoupleException(errorCode, detailMessage)
