package com.whatever.domain.firebase.exception

import com.whatever.global.exception.common.CaramelException

open class FirebaseException(
    errorCode: FirebaseExceptionCode,
    detailMessage: String? = null
) : CaramelException(errorCode, detailMessage)

open class FcmException(
    errorCode: FirebaseExceptionCode,
    detailMessage: String? = null
) : FirebaseException(errorCode, detailMessage)

class FcmIllegalArgumentException(
    errorCode: FirebaseExceptionCode,
    detailMessage: String? = null
) : FcmException(errorCode, detailMessage)

class FcmSendException(
    errorCode: FirebaseExceptionCode,
    detailMessage: String? = null
) : FcmException(errorCode, detailMessage)