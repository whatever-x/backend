package com.whatever.domain.calendarevent.scheduleevent.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class ScheduleExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    UNKNOWN(
        sequence = "000",
        message = "예상치 못한 오류가 발생했습니다.",
        status = HttpStatus.INTERNAL_SERVER_ERROR
    ),
    ILLEGAL_CONTENT_DETAIL(
        sequence = "001",
        message = "입력할 수 없는 제목, 혹은 본문 내용입니다."
    ),
    ILLEGAL_DURATION(
        sequence = "002",
        message = "잘못된 기간 설정입니다."
    ),
    SCHEDULE_NOT_FOUND(
        sequence = "003",
        message = "스케줄을 찾을 수 없습니다.",
        status = HttpStatus.NOT_FOUND
    ),
    ILLEGAL_PARTNER_STATUS(
        sequence = "004",
        message = "파트너의 커플 정보를 확인할 수 없습니다.",
        status = HttpStatus.NOT_FOUND
    ),
    COUPLE_NOT_MATCHED(
        sequence = "005",
        message = "내 커플의 일정이 아닙니다.",
        status = HttpStatus.FORBIDDEN
    ),
    UPDATE_CONFLICT(
        sequence = "006",
        message = "상대방이 수정 중인 일정입니다. 잠시 후 재시도 해주세요.",
        status = HttpStatus.CONFLICT
    ),
    ILLEGAL_CONTENT_ID(
        sequence = "007",
        message = "사용할 수 없는 콘텐츠입니다."
    ),
    ILLEGAL_CONTENT_TYPE(
        sequence = "008",
        message = "일정으로 변경할 수 없는 콘텐츠입니다."
    ),
    ;

    override val code = "SCHEDULE$sequence"
}
