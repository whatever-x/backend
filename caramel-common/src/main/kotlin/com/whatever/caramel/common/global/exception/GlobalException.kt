package com.whatever.caramel.common.global.exception

import com.whatever.caramel.common.global.exception.common.CaramelException

class GlobalException(
    errorCode: GlobalExceptionCode,
) : CaramelException(errorCode)
