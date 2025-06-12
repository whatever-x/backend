package com.whatever.domain.couple.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class CoupleExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    INVALID_USER_STATUS(
        sequence = "001",
        message = "기능을 이용할 수 없는 유저 상태입니다.",
        status = HttpStatus.FORBIDDEN
    ),
    INVITATION_CODE_GENERATION_FAIL(
        sequence = "002",
        message = "초대 코드 생성에 실패했습니다.",
        status = HttpStatus.INTERNAL_SERVER_ERROR
    ),
    INVITATION_CODE_EXPIRED(
        sequence = "003",
        message = "사용할 수 없는 초대코드에요",
        status = HttpStatus.NOT_FOUND,
    ),
    INVITATION_CODE_SELF_GENERATED(
        sequence = "004",
        message = "내가 만든 초대코드는 등록할 수 없어요",
    ),
    MEMBER_NOT_FOUND(
        sequence = "005",
        message = "상대 유저를 찾을 수 없어요\n가입상태를 다시 확인해주세요",
        status = HttpStatus.NOT_FOUND,
    ),
    COUPLE_NOT_FOUND(
        sequence = "006",
        message = "존재하지 않는 커플입니다.",
        status = HttpStatus.NOT_FOUND
    ),
    NOT_A_MEMBER(
        sequence = "007",
        message = "커플에 속한 유저가 아닙니다.",
        status = HttpStatus.FORBIDDEN
    ),
    ILLEGAL_MEMBER_SIZE(
        sequence = "008",
        message = "커플에는 반드시 두 명의 유저가 있어야 합니다."
    ),
    UPDATE_FAIL(
        sequence = "009",
        message = "상대방이 수정 중입니다. 잠시 후 재시도 해주세요.",
        status = HttpStatus.CONFLICT
    ),
    ILLEGAL_START_DATE(
        sequence = "010",
        message = "커플 시작일은 오늘 이전이어야 합니다."
    ),
    INACTIVE_COUPLE_STATUS(
        sequence = "011",
        message = "커플의 연결이 끊어져 더 이상 이 공간의 데이터를 수정할 수 없어요.",
    ),
    SHARED_MESSAGE_OUT_OF_LENGTH(
        sequence = "012",
        message = "기억할 말의 길이가 초과되었습니다."
    ),
    ;

    override val code = "COUPLE$sequence"
}