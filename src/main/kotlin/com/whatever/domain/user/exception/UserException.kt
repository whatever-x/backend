package com.whatever.domain.user.exception

import com.whatever.global.exception.common.CaramelException

open class UserException(
    errorCode: UserExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)

class UserNotFoundException(
    errorCode: UserExceptionCode,
    detailMessage: String? = null
) : UserException(errorCode, detailMessage)

class UserIllegalStateException(
    errorCode: UserExceptionCode,
    detailMessage: String? = null
) : UserException(errorCode, detailMessage)
