package com.whatever.global.jwt.exception

import com.whatever.global.exception.common.CaramelException

open class CaramelJwtException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)

class JwtMalformedException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null
) : CaramelJwtException(errorCode)

class JwtSignatureException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null
) : CaramelJwtException(errorCode)

class JwtSecurityException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null
) : CaramelJwtException(errorCode)

class JwtExpiredException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null
) : CaramelJwtException(errorCode)

class JwtUnsupportedException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null
) : CaramelJwtException(errorCode)

class JwtMissingClaimException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null
) : CaramelJwtException(errorCode)