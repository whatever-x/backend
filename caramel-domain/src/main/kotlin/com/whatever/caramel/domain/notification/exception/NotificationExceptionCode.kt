package com.whatever.caramel.domain.notification.exception

import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class NotificationExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    INVALID_SCHEDULING_PARAMETER(
        sequence = "001",
        message = "알림 예약에 필요한 정보가 잘못되었습니다.",
        status = HttpStatus.INTERNAL_SERVER_ERROR,
    ),
    INVALID_MESSAGE_PARAMETER(
        sequence = "002",
        message = "알림 메시지 생성에 필요한 정보가 잘못되었습니다.",
        status = HttpStatus.INTERNAL_SERVER_ERROR,
    ),
    ;

    override val code = "NOTIFICATION$sequence"
}
