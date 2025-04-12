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
    ;
    override val code = "Content$sequence"
}