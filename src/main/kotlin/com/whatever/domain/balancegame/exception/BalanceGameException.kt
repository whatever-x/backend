package com.whatever.domain.balancegame.exception

import com.whatever.global.exception.common.CaramelException

open class BalanceGameException(
    errorCode: BalanceGameExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)

class BalanceGameIllegalStateException(
    errorCode: BalanceGameExceptionCode,
    detailMessage: String? = null
) : BalanceGameException(errorCode, detailMessage)

class BalanceGameIllegalArgumentException(
    errorCode: BalanceGameExceptionCode,
    detailMessage: String? = null
) : BalanceGameException(errorCode, detailMessage)

class BalanceGameNotFoundException(
    errorCode: BalanceGameExceptionCode,
    detailMessage: String? = null
) : BalanceGameException(errorCode, detailMessage)

class BalanceGameOptionNotFoundException(
    errorCode: BalanceGameExceptionCode,
    detailMessage: String? = null
) : BalanceGameException(errorCode, detailMessage)