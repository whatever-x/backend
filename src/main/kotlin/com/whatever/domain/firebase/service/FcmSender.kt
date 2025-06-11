package com.whatever.domain.firebase.service

import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode.INTERNAL
import com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT
import com.google.firebase.messaging.MessagingErrorCode.QUOTA_EXCEEDED
import com.google.firebase.messaging.MessagingErrorCode.SENDER_ID_MISMATCH
import com.google.firebase.messaging.MessagingErrorCode.UNAVAILABLE
import com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import com.whatever.domain.firebase.exception.FcmIllegalArgumentException
import com.whatever.domain.firebase.exception.FcmSendException
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.FCM_EMPTY_TOKEN
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.FCM_INTERNAL_SERVER_ERROR
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.FCM_INVALID_ARGUMENT
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.FCM_QUOTA_EXCEEDED
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.FCM_SENDER_ID_MISMATCH
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.FCM_SERVER_UNAVAILABLE
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.FCM_UNREGISTERED_TOKEN
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.UNKNOWN
import com.whatever.global.exception.ErrorUi
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@DependsOn("firebaseInitializer")
class FcmSender {

    fun sendNotification(
        token: String,
        notification: Notification,
    ): String {
        val message = Message.builder()
            .setNotification(notification)
            .setToken(token)
            .build()

        return executeFcmCall("sendNotification to token: $token") {
            FirebaseMessaging.getInstance().send(message)
        }
    }

    fun sendNotificationAll(
        tokens: List<String>,
        notification: Notification,
    ): BatchResponse {
        if (tokens.isEmpty()) {
            logger.info { "No tokens provided for sendNotificationAll. Skipping." }
            throw FcmIllegalArgumentException(
                errorCode = FCM_EMPTY_TOKEN,
                errorUi = ErrorUi.Toast("알림을 전송할 대상이 없어요.")
            )
        }
        val message = MulticastMessage.builder()
            .setNotification(notification)
            .addAllTokens(tokens)
            .build()

        return executeFcmCall("sendNotificationAll to ${tokens.size} tokens") {
            FirebaseMessaging.getInstance().sendEachForMulticast(message)
        }
    }

    fun sendData(
        token: String,
        data: Map<String, String>,
    ): String {
        val message = Message.builder()
            .putAllData(data)
            .setToken(token)
            .build()

        return executeFcmCall("sendData to token: $token") {
            FirebaseMessaging.getInstance().send(message)
        }
    }

    fun sendDataAll(
        tokens: List<String>,
        data: Map<String, String>,
    ): BatchResponse {
        if (tokens.isEmpty()) {
            logger.info { "No tokens provided for sendDataAll. Skipping." }
            throw FcmIllegalArgumentException(
                errorCode = FCM_EMPTY_TOKEN,
                errorUi = ErrorUi.Toast("알림을 전송할 대상이 없어요.")
            )
        }

        val message = MulticastMessage.builder()
            .putAllData(data)
            .addAllTokens(tokens)
            .build()

        return executeFcmCall("sendDataAll to ${tokens.size} tokens") {
            FirebaseMessaging.getInstance().sendEachForMulticast(message)
        }
    }

    private fun <T> executeFcmCall(
        callDescription: String,
        fcmCall: () -> T
    ): T {
        try {
            return fcmCall()
        } catch (e: FirebaseMessagingException) {
            val mappedCode =
                when (e.messagingErrorCode) {
                    INVALID_ARGUMENT -> FCM_INVALID_ARGUMENT
                    UNREGISTERED -> FCM_UNREGISTERED_TOKEN
                    SENDER_ID_MISMATCH -> FCM_SENDER_ID_MISMATCH
                    QUOTA_EXCEEDED -> FCM_QUOTA_EXCEEDED
                    UNAVAILABLE -> FCM_SERVER_UNAVAILABLE
                    INTERNAL -> FCM_INTERNAL_SERVER_ERROR
                    else -> UNKNOWN
                }
            logger.error(e) {
                "FCM call failed: $callDescription. FCM ErrorCode: ${e.messagingErrorCode}, MappedCode: $mappedCode"
            }

            throw FcmSendException(
                errorCode = mappedCode,
                errorUi = ErrorUi.Toast("알림을 전송에 실패했어요.")
            )
        } catch (e: Exception) {
            logger.error(e) { "Generic error during FCM call: $callDescription" }
            throw FcmSendException(
                errorCode = UNKNOWN,
                errorUi = ErrorUi.Toast("알림을 전송에 실패했어요.")
            )
        }
    }
}
