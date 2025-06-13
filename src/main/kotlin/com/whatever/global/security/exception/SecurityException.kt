package com.whatever.global.security.exception

import com.whatever.global.exception.ErrorUi
import com.whatever.global.exception.common.CaramelException

open class CaramelSecurityException(
    errorCode: SecurityExceptionCode,
    errorUi: ErrorUi,
) : CaramelException(errorCode, errorUi)

class AccessDeniedException(
    errorCode: SecurityExceptionCode,
    errorUi: ErrorUi,
) : CaramelSecurityException(errorCode, errorUi)

class AuthenticationException(
    errorCode: SecurityExceptionCode,
    errorUi: ErrorUi,
) : CaramelSecurityException(errorCode, errorUi)
