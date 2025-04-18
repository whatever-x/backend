package com.whatever.global.exception.externalserver.kakao

import com.whatever.global.exception.externalserver.ExternalServerException

open class KakaoServerException(
    errorCode: KakaoServerExceptionCode,
    detailMessage: String? = null
) : ExternalServerException(errorCode, detailMessage)
