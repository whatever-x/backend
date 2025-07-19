package com.whatever.domain.auth.exception

import com.whatever.global.exception.ErrorUi
import com.whatever.global.exception.common.CaramelException

open class AuthException(
    errorCode: AuthExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("알 수 없는 에러입니다."),
) : CaramelException(errorCode, errorUi)

class AuthFailedException(
    errorCode: AuthExceptionCode,
    errorUi: ErrorUi,
) : AuthException(errorCode, errorUi)

class IllegalOidcTokenException(
    errorCode: AuthExceptionCode,
    errorUi: ErrorUi,
) : AuthException(errorCode, errorUi)

class OidcPublicKeyMismatchException(
    errorCode: AuthExceptionCode,
) : AuthException(errorCode)
