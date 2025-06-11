package com.whatever.domain.couple.exception

import com.whatever.global.exception.ErrorUiType
import com.whatever.global.exception.common.CaramelException

open class CoupleException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null,
    overrideErrorUiType: ErrorUiType? = null
) : CaramelException(errorCode, detailMessage, overrideErrorUiType)

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

class CoupleNotFoundException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null
) : CoupleException(errorCode, detailMessage)
