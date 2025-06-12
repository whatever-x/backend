package com.whatever.domain.couple.exception

import com.whatever.global.exception.ErrorUi
import com.whatever.global.exception.common.CaramelException

open class CoupleException(
    errorCode: CoupleExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("알 수 없는 에러입니다."),
) : CaramelException(errorCode, errorUi)

class CoupleAccessDeniedException(
    errorCode: CoupleExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("내 커플의 정보만 접근할 수 있어요."),
) : CoupleException(errorCode, errorUi)

class CoupleIllegalStateException(
    errorCode: CoupleExceptionCode,
    errorUi: ErrorUi,
) : CoupleException(errorCode, errorUi)

class CoupleIllegalArgumentException(
    errorCode: CoupleExceptionCode,
    errorUi: ErrorUi,
) : CoupleException(errorCode, errorUi)

class CoupleNotFoundException(
    errorCode: CoupleExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("커플 정보를 찾을 수 없어요."),
) : CoupleException(errorCode, errorUi)
