package com.whatever.firebase.service

import com.whatever.caramel.infrastructure.firebase.FcmSender
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import com.whatever.caramel.infrastructure.properties.FirebaseProperties
import com.whatever.domain.firebase.model.FcmToken
import com.whatever.domain.firebase.repository.FcmTokenRepository
import com.whatever.domain.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger { }

@Service
class FirebaseService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val userRepository: UserRepository,
    private val fcmSender: FcmSender,
    private val firebaseProperties: FirebaseProperties,
) {

    @Transactional
    fun setFcmToken(
        deviceId: String,
        token: String,
        userId: Long,
    ) {
        val tokens = fcmTokenRepository.findAllByUser_IdAndIsDeleted(userId)
        tokens.firstOrNull { it.deviceId == deviceId }
            ?.updateToken(token)
            ?: fcmTokenRepository.save(
                FcmToken(
                    initialToken = token,
                    deviceId = deviceId,
                    user = userRepository.getReferenceById(userId),
                )
            )
    }

    fun getSendableFcmTokens(userIds: Set<Long>): List<FcmToken> {
        return fcmTokenRepository.findAllSendableTokensByUserIds(userIds)
    }

    fun sendNotification(
        targetUserIds: Set<Long>,
        fcmNotification: FcmNotification,
    ) {
        if (!firebaseProperties.fcmEnabled) {
            return
        }

        val tokens = getSendableFcmTokens(targetUserIds).map { it.token }

        if (tokens.isEmpty()) {
            logger.debug { "No FCM tokens to send notification. User IDs: ${targetUserIds}" }
            return
        }
        logger.info { "Sending FCM notification to ${tokens.size} tokens." }

        if (tokens.size == 1) {
            fcmSender.sendNotification(
                token = tokens.single(),
                notification = fcmNotification.toNotification(),
            )
        } else {
            fcmSender.sendNotificationAll(
                tokens = tokens,
                notification = fcmNotification.toNotification(),
            )
        }
    }

    fun sendData(
        targetUserIds: Set<Long>,
        data: Map<String, String>,
    ) {
        if (!firebaseProperties.fcmEnabled) {
            return
        }

        val tokens = getSendableFcmTokens(targetUserIds)
            .map { it.token }

        if (tokens.isEmpty()) {
            logger.debug { "No FCM tokens to send data. User IDs: ${targetUserIds}" }
            return
        }
        logger.info { "Sending FCM notification to ${tokens.size} tokens." }

        if (tokens.size == 1) {
            fcmSender.sendData(
                token = tokens.single(),
                data = data,
            )
        } else {
            fcmSender.sendDataAll(
                tokens = tokens,
                data = data,
            )
        }
    }
}
