package global.jwt.exception

import global.exception.ErrorUi
import global.exception.common.CaramelException

open class CaramelJwtException(
    errorCode: JwtExceptionCode,
    val detailMessage: String? = null,
    errorUi: ErrorUi = ErrorUi.Toast("로그인 정보에 문제가 발생했어요.\n다시 로그인이 필요해요."),
) : CaramelException(errorCode, errorUi)

class JwtMalformedException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null,
) : CaramelJwtException(errorCode, detailMessage)

class JwtSignatureException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null,
) : CaramelJwtException(errorCode, detailMessage)

class JwtSecurityException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null,
) : CaramelJwtException(errorCode, detailMessage)

class JwtExpiredException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null,
) : CaramelJwtException(errorCode, detailMessage)

class JwtUnsupportedException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null,
) : CaramelJwtException(errorCode, detailMessage)

class JwtMissingClaimException(
    errorCode: JwtExceptionCode,
    detailMessage: String? = null,
) : CaramelJwtException(errorCode, detailMessage)
