package com.whatever.global.exception.externalserver.kakao

class KakaoServiceUnavailableException(
    errorCode: KakaoServerExceptionCode,
    detailMessage: String? = null
) : KakaoServerException(errorCode, detailMessage)
