package com.whatever.domain.balancegame.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class BalanceGameExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    UNKNOWN(
        sequence = "000",
        message = "알 수 없는 에러입니다. 담당자에게 문의해주세요.",
        status = HttpStatus.INTERNAL_SERVER_ERROR
    ),
    GAME_OPTION_NOT_ENOUGH(
        sequence = "001",
        message = "게임 선택지가 부족해 진행할 수 없는 게임입니다.",
        status = HttpStatus.UNPROCESSABLE_ENTITY
    ),
    GAME_CHANGED(
        sequence = "002",
        message = "이 전 날짜의 밸런스 게임입니다. 새로운 게임으로 갱신이 필요합니다.",
    ),
    ILLEGAL_OPTION(
        sequence = "003",
        message = "오늘의 게임에 맞지 않는 선택지입니다."
    ),
    GAME_NOT_EXISTS(
        sequence = "004",
        message = "등록된 게임이 없습니다.",
        status = HttpStatus.NOT_FOUND
    ),
    ;

    override val code = "BGAME$sequence"
}
