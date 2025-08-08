package com.whatever.caramel.domain.notification.exception

import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class NotificationExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
) : CaramelExceptionCode {

    INVALID_SCHEDULING_PARAMETER(
        sequence = "001",
        message = "알림 예약에 필요한 정보가 잘못되었습니다.",
    ),
    INVALID_MESSAGE_PARAMETER(
        sequence = "002",
        message = "알림 메시지 생성에 필요한 정보가 잘못되었습니다.",
    ),
    UNSUPPORTED_NOTIFICATION_TYPE(
        sequence = "003",
        message = "지원하지 않는 알림 타입입니다.",
    ),
    UNSUPPORTED_COUPLE_ANNIV_TYPE(
        sequence = "004",
        message = "지원하지 않는 커플 기념일 타입입니다.",
    ),
    ;

    override val code = "NOTIFICATION$sequence"
}
