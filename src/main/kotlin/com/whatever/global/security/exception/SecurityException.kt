package com.whatever.global.security.exception

import com.whatever.global.exception.common.CaramelException

open class CaramelSecurityException(
    errorCode: SecurityExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)

class AccessDeniedException(
    errorCode: SecurityExceptionCode,
    detailMessage: String? = null
) : CaramelSecurityException(errorCode)

class AuthenticationException(
    errorCode: SecurityExceptionCode,
    detailMessage: String? = null
) : CaramelSecurityException(errorCode)
