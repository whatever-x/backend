package com.whatever.domain.auth.exception

import com.whatever.global.exception.ErrorUiType
import com.whatever.global.exception.common.CaramelException

open class AuthException(
    errorCode: AuthExceptionCode,
    detailMessage: String? = null,
    overrideErrorUiType: ErrorUiType? = null,
) : CaramelException(errorCode, detailMessage, overrideErrorUiType)

class AuthFailedException(
    errorCode: AuthExceptionCode,
    detailMessage: String? = null,
    overrideErrorUiType: ErrorUiType? = null,
) : AuthException(errorCode, detailMessage, overrideErrorUiType)

class IllegalOidcTokenException(
    errorCode: AuthExceptionCode,
    detailMessage: String? = null,
) : AuthException(errorCode, detailMessage)

class OidcPublicKeyMismatchException(
    errorCode: AuthExceptionCode,
    detailMessage: String? = null,
) : AuthException(errorCode, detailMessage)