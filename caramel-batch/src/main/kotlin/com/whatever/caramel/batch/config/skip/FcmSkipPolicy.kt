package com.whatever.caramel.batch.config.skip

import com.whatever.caramel.batch.config.exception.BatchUnregisteredException
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.infrastructure.firebase.exception.FcmSendFailedReason
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_BLANK_TOKEN
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_EMPTY_TOKEN
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_INTERNAL_SERVER_ERROR
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_INVALID_ARGUMENT
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_MULTIPLE_TOKEN_ERROR
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_QUOTA_EXCEEDED
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_SENDER_ID_MISMATCH
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_SERVER_UNAVAILABLE
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_THIRD_PARTY_AUTH_ERROR
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_UNREGISTERED_TOKEN
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.UNKNOWN
import org.springframework.batch.core.step.skip.SkipPolicy

class FcmSkipPolicy(
    private val fcmTokenService: FirebaseService,
) : SkipPolicy {

    override fun shouldSkip(throwable: Throwable, skipCount: Long): Boolean {
        if (throwable !is BatchUnregisteredException) return true

        if (throwable.errorCode == FCM_UNREGISTERED_TOKEN || throwable.errorCode == FCM_MULTIPLE_TOKEN_ERROR) {
            throwable.tokens.forEach { fcmToken ->
                checkUnregisterToken(fcmToken)
            }
        }
        return true
    }

    private fun checkUnregisterToken(fcmToken: FcmSendFailedReason) {
        when (fcmToken.errorMessageCode) {
            FCM_UNREGISTERED_TOKEN -> {
                fcmTokenService.removeToken(fcmToken.errorToken)
            }

            UNKNOWN,
            FCM_EMPTY_TOKEN,
            FCM_INVALID_ARGUMENT,
            FCM_SERVER_UNAVAILABLE,
            FCM_INTERNAL_SERVER_ERROR,
            FCM_QUOTA_EXCEEDED,
            FCM_SENDER_ID_MISMATCH,
            FCM_THIRD_PARTY_AUTH_ERROR,
            FCM_BLANK_TOKEN,
            FCM_MULTIPLE_TOKEN_ERROR -> {
                /** no-op **/
            }
        }
    }
}
