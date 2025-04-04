package com.whatever.global.exception.externalserver.kakao

import com.whatever.global.exception.common.CaramelException

open class KakaoServerException(
    errorCode: KakaoServerExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)
