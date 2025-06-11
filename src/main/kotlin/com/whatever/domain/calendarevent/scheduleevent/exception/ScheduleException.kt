package com.whatever.domain.calendarevent.scheduleevent.exception

import com.whatever.global.exception.ErrorUi
import com.whatever.global.exception.common.CaramelException

open class CaramelScheduleException(
    errorCode: ScheduleExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("알 수 없는 에러입니다."),
) : CaramelException(errorCode, errorUi)

class ScheduleNotFoundException(
    errorCode: ScheduleExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("일정 정보를 찾을 수 없어요."),
) : CaramelScheduleException(errorCode)

class ScheduleIllegalArgumentException(
    errorCode: ScheduleExceptionCode,
    errorUi: ErrorUi,
) : CaramelScheduleException(errorCode)

class ScheduleIllegalStateException(
    errorCode: ScheduleExceptionCode,
    errorUi: ErrorUi,
) : CaramelScheduleException(errorCode)

class ScheduleAccessDeniedException(
    errorCode: ScheduleExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("커플 멤버가 작성한 일정만 접근할 수 있어요.")
) : CaramelScheduleException(errorCode)
