package com.whatever.domain.firebase.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class FirebaseExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    UNKNOWN("001", "FCM 전송에 실패했습니다."),
    FCM_EMPTY_TOKEN("002", "등록된 FCM 토큰이 없습니다."),
    ;

    override val code = "FIREBASE$sequence"
}