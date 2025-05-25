package com.whatever.domain.firebase.service

import com.whatever.domain.firebase.model.FcmToken
import com.whatever.domain.firebase.repository.FcmTokenRepository
import com.whatever.domain.firebase.service.event.FcmNotification
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
            // 전송 대상 token이 없습니다.
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
            // 전송 대상 token이 없습니다.
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