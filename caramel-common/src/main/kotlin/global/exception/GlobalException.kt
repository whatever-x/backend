package global.exception

import global.exception.common.CaramelException

class GlobalException(
    errorCode: GlobalExceptionCode,
) : CaramelException(errorCode)
