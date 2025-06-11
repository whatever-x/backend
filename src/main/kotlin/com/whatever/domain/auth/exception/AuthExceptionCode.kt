package com.whatever.domain.auth.exception

import com.whatever.global.exception.ErrorUiType
import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class AuthExceptionCode(
    sequence: String,
    override val message: String,
    override val description: String? = null,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
    override val errorUiType: ErrorUiType = ErrorUiType.TOAST,
) : CaramelExceptionCode {

    UNKNOWN(
        sequence = "000",
        message = "로그인을 하지 못 했어요.\n다시 한 번 시도해주세요.",
        status = HttpStatus.INTERNAL_SERVER_ERROR
    ),
    USER_NOT_FOUND(
        sequence = "001",
        message = "해당 소셜 계정으로 가입된 유저가 없습니다.",
        status = HttpStatus.NOT_FOUND
    ),
    UNAUTHORIZED(
        sequence = "002",
        message = "발급받은 인증 정보가 유효하지 않습니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    ILLEGAL_KID(
        sequence = "003",
        message = "사용자 정보 확인에 실패했어요.\n다시 한 번 시도해주세요.",
        errorUiType = ErrorUiType.DIALOG
    ),
    USER_PROVIDER_NOT_FOUND(
        sequence = "004",
        message = "지원되지 않는 로그인 플랫폼이에요.\n다른 플랫폼으로 다시 한 번 시도해주세요.",
        errorUiType = ErrorUiType.DIALOG,
    ),
    ;

    override val code = "AUTH$sequence"
}
