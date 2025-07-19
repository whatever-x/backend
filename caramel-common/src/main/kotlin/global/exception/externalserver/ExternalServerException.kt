package global.exception.externalserver

import global.exception.ErrorUi
import global.exception.common.CaramelException

abstract class ExternalServerException(
    errorCode: ExternalServerExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("외부 서버에서 에러가 발생했어요. 다시 시도해주세요."),
) : CaramelException(errorCode, errorUi)
