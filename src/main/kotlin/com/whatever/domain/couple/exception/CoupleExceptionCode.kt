package com.whatever.domain.couple.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class CoupleExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    INVALID_USER_STATUS("001", "기능을 이용할 수 없는 유저 상태입니다."),
    INVITATION_CODE_GENERATION_FAIL("002", "초대 코드 생성에 실패했습니다.")
    ;

    override val code = "USER$sequence"
}