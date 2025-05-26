package com.whatever.domain.firebase.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS

enum class FirebaseExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = BAD_REQUEST,
) : CaramelExceptionCode {

    UNKNOWN("001", "FCM 전송에 실패했습니다.", INTERNAL_SERVER_ERROR),
    FCM_EMPTY_TOKEN("002", "등록된 FCM 토큰이 없습니다."),

    FCM_INVALID_ARGUMENT(
        "003",
        "FCM 요청에 잘못된 인자가 포함되었습니다. (예: 토큰 형식 오류, 페이로드 크기 초과)",
        BAD_REQUEST,
    ),
    FCM_UNREGISTERED_TOKEN(
        "004",
        "FCM 토큰이 더 이상 유효하지 않습니다. (예: 앱 삭제, 등록 해제)",
        BAD_REQUEST,
    ),
    FCM_SERVER_UNAVAILABLE(
        "005",
        "FCM 서버가 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.",
        SERVICE_UNAVAILABLE,
    ),
    FCM_INTERNAL_SERVER_ERROR(
        "006",
        "FCM 서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
        INTERNAL_SERVER_ERROR,
    ),
    FCM_QUOTA_EXCEEDED(
        "007",
        "FCM 전송 할당량을 초과했습니다.",
        TOO_MANY_REQUESTS,
    ),
    FCM_SENDER_ID_MISMATCH(
        "008",
        "FCM 토큰이 현재 서버의 발신자 ID와 일치하지 않습니다.",
        BAD_REQUEST,
    ),
    FCM_THIRD_PARTY_AUTH_ERROR( // 추가된 에러 코드 (APNs 등 연동 오류)
        "009",
        "FCM 외부 인증 서비스(예: APNs)에서 오류가 발생했습니다.",
        INTERNAL_SERVER_ERROR,
    )
    ;

    override val code = "FIREBASE$sequence"
}