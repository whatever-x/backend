package com.whatever.domain.balancegame.exception

import com.whatever.global.exception.ErrorUiType
import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class BalanceGameExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
    override val errorUiType: ErrorUiType = ErrorUiType.TOAST,
) : CaramelExceptionCode {

    UNKNOWN( "000", "알 수 없는 에러입니다. 담당자에게 문의해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    GAME_OPTION_NOT_ENOUGH( "001", "게임 선택지가 부족해 진행할 수 없는 게임입니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    GAME_CHANGED( "002", "오늘의 게임이 아닙니다. 다시 요청해주세요."),
    ILLEGAL_OPTION( "003", "오늘의 게임에 맞지 않는 선택지입니다."),
    GAME_NOT_EXISTS( "004", "등록된 게임이 없습니다.", HttpStatus.NOT_FOUND),
    ;

    override val code = "BGAME$sequence"
}
