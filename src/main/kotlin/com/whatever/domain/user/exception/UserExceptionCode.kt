package com.whatever.domain.user.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class UserExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    INVALID_NICKNAME_CHARACTER("002", "닉네임은 한글, 영문, 숫자만 사용 가능합니다."),
    ALREADY_EXIST_COUPLE("007", "이미 커플에 소속된 유저 입니다."),
    ;

    override val code = "USER$sequence"
}