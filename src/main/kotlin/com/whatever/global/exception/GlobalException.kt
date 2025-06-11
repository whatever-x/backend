package com.whatever.global.exception

import com.whatever.global.exception.common.CaramelException
import com.whatever.global.exception.common.CaramelExceptionCode

class GlobalException(
    errorCode: GlobalExceptionCode,
) : CaramelException(errorCode)