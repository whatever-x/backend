package firebase.exception

import global.exception.ErrorUi
import global.exception.common.CaramelException

open class FirebaseException(
    errorCode: FirebaseExceptionCode,
    errorUi: ErrorUi = ErrorUi.Toast("알 수 없는 에러입니다."),
) : CaramelException(errorCode, errorUi)

open class FcmException(
    errorCode: FirebaseExceptionCode,
    errorUi: ErrorUi,
) : FirebaseException(errorCode, errorUi)

class FcmIllegalArgumentException(
    errorCode: FirebaseExceptionCode,
    errorUi: ErrorUi,
) : FcmException(errorCode, errorUi)

class FcmSendException(
    errorCode: FirebaseExceptionCode,
    errorUi: ErrorUi,
) : FcmException(errorCode, errorUi)
