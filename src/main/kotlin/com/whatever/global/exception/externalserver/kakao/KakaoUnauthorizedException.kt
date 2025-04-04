package com.whatever.global.exception.externalserver.kakao

class KakaoUnauthorizedException(
    errorCode: KakaoServerExceptionCode,
    detailMessage: String? = null
) : KakaoServerException(errorCode, detailMessage)
