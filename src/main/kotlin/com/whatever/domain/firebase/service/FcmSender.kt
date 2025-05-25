package com.whatever.domain.firebase.service

import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import com.whatever.domain.firebase.exception.FcmIllegalArgumentException
import com.whatever.domain.firebase.exception.FcmSendException
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.FCM_EMPTY_TOKEN
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.UNKNOWN
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
            throw FcmIllegalArgumentException(FCM_EMPTY_TOKEN)
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
            throw FcmIllegalArgumentException(FCM_EMPTY_TOKEN)
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
        return runCatching {
            fcmCall()
        }.onFailure { e ->
            logger.debug(e) { "FCM call failed: $callDescription" }
        }.getOrElse { throw FcmSendException(UNKNOWN) }
    }
}
