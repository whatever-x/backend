package com.whatever.domain.content.exception

import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class ContentExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    TITLE_OR_DESCRIPTION_REQUIRED(
        sequence = "001",
        message = "title 또는 description 중 하나는 필수입니다."
    ),
    ILLEGAL_CONTENT_DETAIL(
        sequence = "002",
        message = "입력할 수 없는 제목, 혹은 본문 내용입니다."
    ),
    CONTENT_NOT_FOUND(
        sequence = "003",
        message = "존재하지 않는 컨텐츠입니다."
    ),
    UPDATE_CONFLICT(
        sequence = "004",
        message = "상대방이 수정 중인 메모입니다. 잠시 후 재시도 해주세요.",
        status = HttpStatus.CONFLICT
    ),
    MEMO_NOT_FOUND(
        sequence = "005",
        message = "존재하지 않는 메모입니다.",
        status = HttpStatus.NOT_FOUND
    ),
    COUPLE_NOT_MATCHED(
        sequence = "006",
        message = "내 커플의 메모가 아닙니다.",
        status = HttpStatus.FORBIDDEN
    ),
    TITLE_OUT_OF_LENGTH(
        sequence = "007",
        message = "제목 길이가 초과되었습니다."
    ),
    DESCRIPTION_OUT_OF_LENGTH(
        sequence = "008",
        message = "본문 길이가 초과되었습니다."
    ),
    ;

    override val code = "Content$sequence"
}
