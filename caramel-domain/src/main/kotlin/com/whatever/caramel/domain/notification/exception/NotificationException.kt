package com.whatever.caramel.domain.notification.exception

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException
import com.whatever.caramel.domain.notification.exception.NotificationExceptionCode.INVALID_SCHEDULING_PARAMETER

open class NotificationException(
    errorCode: NotificationExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("알 수 없는 에러입니다."),
) : CaramelException(errorCode, errorUi)

class InvalidSchedulingParameterException(
    errorCode: NotificationExceptionCode = INVALID_SCHEDULING_PARAMETER,
    errorUi: ErrorUi = ErrorUi.Toast("알림 예약에 필요한 정보를 처리하지 못했어요."),
) : NotificationException(errorCode, errorUi)
