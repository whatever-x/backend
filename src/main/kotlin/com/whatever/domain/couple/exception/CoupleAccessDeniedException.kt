package com.whatever.domain.couple.exception

class CoupleAccessDeniedException(
    errorCode: CoupleExceptionCode,
    detailMessage: String? = null
) : CoupleException(errorCode, detailMessage)
