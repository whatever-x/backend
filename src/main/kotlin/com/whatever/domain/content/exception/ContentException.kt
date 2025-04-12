package com.whatever.domain.content.exception

import com.whatever.global.exception.common.CaramelException

open class ContentException(
    errorCode: ContentExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)

class ContentIllegalArgumentException(
    errorCode: ContentExceptionCode,
    detailMessage: String? = null
) : ContentException(errorCode, detailMessage)
