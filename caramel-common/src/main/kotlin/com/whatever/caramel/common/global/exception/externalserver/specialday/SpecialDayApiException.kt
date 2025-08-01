package com.whatever.caramel.common.global.exception.externalserver.specialday

import com.whatever.caramel.common.global.exception.externalserver.ExternalServerException

open class SpecialDayApiException(
    errorCode: SpecialDayApiExceptionCode,
) : ExternalServerException(errorCode)

class SpecialDayDecodeException(
    errorCode: SpecialDayApiExceptionCode,
) : SpecialDayApiException(errorCode)

class SpecialDayFailedOperationException(
    errorCode: SpecialDayApiExceptionCode,
    val resultCode: String? = null,
    val resultMsg: String? = null,
) : SpecialDayApiException(errorCode)
