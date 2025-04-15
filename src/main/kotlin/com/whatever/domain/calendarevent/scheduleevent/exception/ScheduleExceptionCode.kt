package com.whatever.domain.calendarevent.scheduleevent.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class ScheduleExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    UNKNOWN("000", "예상치 못한 오류가 발생했습니다."),
    ILLEGAL_CONTENT_DETAIL("001", "입력할 수 없는 제목, 혹은 본문 내용입니다."),
    ILLEGAL_DURATION("002", "잘못된 기간 설정입니다."),
    SCHEDULE_NOT_FOUND("003", "스케줄을 찾을 수 없습니다."),
    ILLEGAL_PARTNER_STATUS("004", "파트너의 커플 정보를 확인할 수 없습니다."),
    COUPLE_NOT_MATCHED("005", "수정할 수 없는 일정입니다."),
    UPDATE_CONFLICT("006", "상대방이 수정 중인 일정입니다. 잠시 후 재시도 해주세요.", HttpStatus.CONFLICT),
    ;

    override val code = "SCHEDULE$sequence"
}
