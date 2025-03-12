package com.whatever.domain.couple.exception

open class CoupleIllegalStateException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null
) : CoupleException(errorCode, detailMessage)
