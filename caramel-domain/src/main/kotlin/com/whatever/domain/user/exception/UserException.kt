package com.whatever.domain.user.exception

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException

open class UserException(
    errorCode: UserExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("알 수 없는 에러입니다."),
) : CaramelException(errorCode, errorUi)

class UserNotFoundException(
    errorCode: UserExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("대상 유저를 찾을 수 없습니다."),
) : UserException(errorCode, errorUi)

class UserIllegalStateException(
    errorCode: UserExceptionCode,
    errorUi: ErrorUi,
) : UserException(errorCode, errorUi)

class UserIllegalArgumentException(
    errorCode: UserExceptionCode,
    errorUi: ErrorUi,
) : UserException(errorCode, errorUi)
