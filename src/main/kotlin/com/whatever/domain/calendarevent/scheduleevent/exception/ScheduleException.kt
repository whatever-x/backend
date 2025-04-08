package com.whatever.domain.calendarevent.scheduleevent.exception

import com.whatever.global.exception.common.CaramelException

open class CaramelScheduleException(
    errorCode: ScheduleExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)

class ScheduleNotFoundException(
    errorCode: ScheduleExceptionCode,
    detailMessage: String? = null
) : CaramelScheduleException(errorCode)

class ScheduleIllegalArgumentException(
    errorCode: ScheduleExceptionCode,
    detailMessage: String? = null
) : CaramelScheduleException(errorCode)

class ScheduleIllegalStateException(
    errorCode: ScheduleExceptionCode,
    detailMessage: String? = null
) : CaramelScheduleException(errorCode)

class ScheduleAccessDeniedException(
    errorCode: ScheduleExceptionCode,
    detailMessage: String? = null
) : CaramelScheduleException(errorCode)
