package com.whatever.domain.couple.exception

class CoupleIllegalStateException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null
) : CoupleException(errorCode, detailMessage)
