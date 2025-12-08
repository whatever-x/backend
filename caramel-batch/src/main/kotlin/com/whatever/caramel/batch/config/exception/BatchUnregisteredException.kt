package com.whatever.caramel.batch.config.exception

import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode
import com.whatever.caramel.infrastructure.firebase.exception.FcmSendFailedReason

class BatchUnregisteredException(
    val tokens: List<FcmSendFailedReason>,
    val errorCode: CaramelExceptionCode,
) : CaramelBatchException()

open class CaramelBatchException : Exception()
