package com.whatever.security.exception

import global.exception.ErrorUi
import global.exception.common.CaramelException

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
