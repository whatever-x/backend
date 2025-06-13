package com.whatever.global.exception.externalserver.kakao

import com.whatever.global.exception.ErrorUi
import com.whatever.global.exception.externalserver.ExternalServerException

open class KakaoServerException(
    errorCode: KakaoServerExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("카카오 서버에서 에러가 발생했어요. 다시 시도해주세요."),
) : ExternalServerException(errorCode, errorUi)

class KakaoServiceUnavailableException(
    errorCode: KakaoServerExceptionCode,
) : KakaoServerException(errorCode)

class KakaoUnauthorizedException(
    errorCode: KakaoServerExceptionCode,
) : KakaoServerException(errorCode)

class KakaoForbiddenException(
    errorCode: KakaoServerExceptionCode,
) : KakaoServerException(errorCode)

class KakaoBadRequestException(
    errorCode: KakaoServerExceptionCode,
) : KakaoServerException(errorCode)
