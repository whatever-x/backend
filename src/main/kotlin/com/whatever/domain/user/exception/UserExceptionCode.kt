package com.whatever.domain.user.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class UserExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    INVALID_NICKNAME_CHARACTER(
        sequence = "002",
        message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다."
    ),
    INVALID_USER_STATUS_FOR_COUPLING(
        sequence = "003",
        message = "커플이 될 수 없는 유저 상태입니다."
    ),
    ALREADY_EXIST_COUPLE(
        sequence = "007",
        message = "이미 커플에 소속된 유저 입니다."
    ),
    NOT_FOUND(
        sequence = "008",
        message = "존재하지 않는 유저입니다.",
        status = HttpStatus.NOT_FOUND
    ),
    SETTING_DATA_NOT_FOUND(
        sequence = "009",
        message = "설정 정보가 존재하지 않습니다.",
        status = HttpStatus.CONFLICT
    ),
    ;

    override val code = "USER$sequence"
}