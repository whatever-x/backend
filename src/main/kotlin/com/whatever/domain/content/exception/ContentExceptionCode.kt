package com.whatever.domain.content.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class ContentExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    TITLE_OR_DESCRIPTION_REQUIRED("001", "title 또는 description 중 하나는 필수입니다."),
    ILLEGAL_CONTENT_DETAIL("002", "입력할 수 없는 제목, 혹은 본문 내용입니다."),
    CONTENT_NOT_FOUND("003", "존재하지 않는 컨텐츠입니다."),
    UPDATE_CONFLICT("004", "상대방이 수정 중인 메모입니다. 잠시 후 재시도 해주세요.", HttpStatus.CONFLICT),
    MEMO_NOT_FOUND("005", "존재하지 않는 메모입니다.", HttpStatus.NOT_FOUND),
    COUPLE_NOT_MATCHED("006", "내 커플의 메모가 아닙니다.", HttpStatus.FORBIDDEN),
    TITLE_OUT_OF_LENGTH("007", "제목 길이가 초과되었습니다."),
    DESCRIPTION_OUT_OF_LENGTH("008", "본문 길이가 초과되었습니다."),
    ;
    override val code = "Content$sequence"
}