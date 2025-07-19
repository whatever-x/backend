package com.whatever.global.exception

import com.whatever.global.exception.common.CaramelException

class GlobalException(
    errorCode: GlobalExceptionCode,
) : CaramelException(errorCode)
