package com.whatever.domain.content.exception

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelException

open class ContentException(
    errorCode: ContentExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("알 수 없는 에러입니다."),
) : CaramelException(errorCode, errorUi)

class ContentIllegalArgumentException(
    errorCode: ContentExceptionCode,
    errorUi: ErrorUi,
) : ContentException(errorCode, errorUi)

class ContentNotFoundException(
    errorCode: ContentExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("메모 정보를 찾을 수 없어요."),
) : ContentException(errorCode, errorUi)

class ContentIllegalStateException(
    errorCode: ContentExceptionCode,
    errorUi: ErrorUi,
) : ContentException(errorCode, errorUi)

class ContentAccessDeniedException(
    errorCode: ContentExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("커플 멤버가 작성한 콘텐츠만 접근할 수 있어요."),
) : ContentException(errorCode, errorUi)
