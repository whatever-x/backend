package com.whatever.domain.couple.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class CoupleExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    INVALID_USER_STATUS("001", "기능을 이용할 수 없는 유저 상태입니다."),
    INVITATION_CODE_GENERATION_FAIL("002","초대 코드 생성에 실패했습니다."),
    INVITATION_CODE_EXPIRED("003", "존재하지 않는 코드이거나, 만료되었습니다."),
    INVITATION_CODE_SELF_GENERATED("004", "스스로 생성한 코드는 사용할 수 없습니다."),
    MEMBER_NOT_FOUND("005", "존재하지 않는 유저입니다."),
    COUPLE_NOT_FOUND("006", "존재하지 않는 커플입니다."),
    NOT_A_MEMBER("007", "커플에 속한 유저가 아닙니다."),
    ;

    override val code = "COUPLE$sequence"
}