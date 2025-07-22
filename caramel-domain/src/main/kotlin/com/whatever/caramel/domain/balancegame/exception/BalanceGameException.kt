package com.whatever.caramel.domain.balancegame.exception

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException

open class BalanceGameException(
    errorCode: BalanceGameExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("알 수 없는 에러입니다."),
) : CaramelException(errorCode, errorUi)

class BalanceGameIllegalStateException(
    errorCode: BalanceGameExceptionCode,
    errorUi: ErrorUi,
) : BalanceGameException(errorCode, errorUi)

class BalanceGameIllegalArgumentException(
    errorCode: BalanceGameExceptionCode,
    errorUi: ErrorUi,
) : BalanceGameException(errorCode, errorUi)

class BalanceGameNotFoundException(
    errorCode: BalanceGameExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("밸런스 게임을 찾을 수 없어요."),
) : BalanceGameException(errorCode, errorUi)

class BalanceGameOptionNotFoundException(
    errorCode: BalanceGameExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("밸런스 선택지를 찾을 수 없어요."),
) : BalanceGameException(errorCode, errorUi)
