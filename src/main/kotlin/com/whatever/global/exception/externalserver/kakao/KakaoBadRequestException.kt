package com.whatever.global.exception.externalserver.kakao

class KakaoBadRequestException(
    errorCode: KakaoServerExceptionCode,
    detailMessage: String? = null
) : KakaoServerException(errorCode, detailMessage)
