package com.whatever.domain.firebase.service

import com.whatever.domain.firebase.model.FcmToken
import com.whatever.domain.firebase.repository.FcmTokenRepository
import com.whatever.domain.firebase.service.event.FcmNotification
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {  }

@Service
class FirebaseService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val userRepository: UserRepository,
    private val fcmSender: FcmSender,
) {

    @Transactional
    fun setFcmToken(
        deviceId: String,
        token: String,
        userId: Long = getCurrentUserId(),
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

    fun getActiveFcmTokens(userIds: Set<Long>): List<FcmToken> {
        return fcmTokenRepository.findAllActiveTokensByUserIds(userIds)
    }

    fun sendNotification(
        targetUserIds: Set<Long>,
        fcmNotification: FcmNotification,
    ) {
        val tokens = getActiveFcmTokens(targetUserIds)
            .map { it.token }

        if (tokens.isEmpty()) {
            logger.debug { "No FCM tokens to send notification. User IDs: ${targetUserIds}" }
            return
        }

        if (tokens.size == 1){
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
        val tokens = getActiveFcmTokens(targetUserIds)
            .map { it.token }

        if (tokens.isEmpty()) {
            logger.debug { "No FCM tokens to send data. User IDs: ${targetUserIds}" }
            return
        }

        if (tokens.size == 1){
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