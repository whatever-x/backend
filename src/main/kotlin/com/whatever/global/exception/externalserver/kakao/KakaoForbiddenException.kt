package com.whatever.global.exception.externalserver.kakao

class KakaoForbiddenException(
    errorCode: KakaoServerExceptionCode,
    detailMessage: String? = null
) : KakaoServerException(errorCode, detailMessage)
