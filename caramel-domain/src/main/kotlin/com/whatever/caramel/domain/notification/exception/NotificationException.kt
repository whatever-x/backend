package com.whatever.caramel.domain.notification.exception

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException
import com.whatever.caramel.domain.notification.exception.NotificationExceptionCode.INVALID_MESSAGE_PARAMETER
import com.whatever.caramel.domain.notification.exception.NotificationExceptionCode.INVALID_SCHEDULING_PARAMETER
import com.whatever.caramel.domain.notification.exception.NotificationExceptionCode.UNSUPPORTED_NOTIFICATION_TYPE

open class NotificationException(
    errorCode: NotificationExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("알림에 문제가 발생했어요."),
    val detailMessage: String?,
) : CaramelException(errorCode, errorUi)

class InvalidSchedulingParameterException(
    errorCode: NotificationExceptionCode = INVALID_SCHEDULING_PARAMETER,
) : NotificationException(errorCode = errorCode, detailMessage = null)

class InvalidMessageParameterException(
    errorCode: NotificationExceptionCode = INVALID_MESSAGE_PARAMETER,
) : NotificationException(errorCode = errorCode, detailMessage = null)

class UnsupportedNotificationTypeException(
    errorCode: NotificationExceptionCode = UNSUPPORTED_NOTIFICATION_TYPE,
    detailMessage: String,
) : NotificationException(errorCode = errorCode, detailMessage = detailMessage)