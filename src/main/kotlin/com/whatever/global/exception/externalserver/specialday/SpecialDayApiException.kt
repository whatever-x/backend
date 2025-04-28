package com.whatever.global.exception.externalserver.specialday

import com.whatever.global.exception.externalserver.ExternalServerException

open class SpecialDayApiException(
    errorCode: SpecialDayApiExceptionCode,
    detailMessage: String? = null,
) : ExternalServerException(errorCode, detailMessage)

class SpecialDayDecodeException(
    errorCode: SpecialDayApiExceptionCode,
    detailMessage: String? = null,
) : SpecialDayApiException(errorCode, detailMessage)

class SpecialDayFailedOperationException(
    errorCode: SpecialDayApiExceptionCode,
    detailMessage: String? = null,
    val resultCode: String? = null,
    val resultMsg: String? = null,
) : SpecialDayApiException(errorCode, detailMessage)
