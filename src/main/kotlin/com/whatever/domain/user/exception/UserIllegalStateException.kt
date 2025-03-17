package com.whatever.domain.user.exception

open class UserIllegalStateException(
    errorCode: UserExceptionCode,
    detailMessage: String? = null
) : UserException(errorCode, detailMessage)
