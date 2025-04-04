package com.whatever.domain.auth.exception

import com.whatever.global.exception.common.CaramelException

open class AuthException(
    errorCode: AuthExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)

class AuthFailedException(
    errorCode: AuthExceptionCode,
    detailMessage: String? = null
) : AuthException(errorCode, detailMessage)

class IllegalOidcTokenException(
    errorCode: AuthExceptionCode,
    detailMessage: String? = null,
) : AuthException(errorCode, detailMessage)

class OidcPublicKeyMismatchException(
    errorCode: AuthExceptionCode,
    detailMessage: String? = null,
) : AuthException(errorCode, detailMessage)