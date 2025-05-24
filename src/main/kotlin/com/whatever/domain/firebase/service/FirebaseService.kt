package com.whatever.domain.firebase.service

import com.whatever.domain.firebase.model.FcmToken
import com.whatever.domain.firebase.repository.FcmTokenRepository
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class FirebaseService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val userRepository: UserRepository,
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
}