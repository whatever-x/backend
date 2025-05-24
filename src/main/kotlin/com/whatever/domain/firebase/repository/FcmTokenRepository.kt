package com.whatever.domain.firebase.repository

import com.whatever.domain.firebase.model.FcmToken
import org.springframework.data.jpa.repository.JpaRepository

interface FcmTokenRepository : JpaRepository<FcmToken, Long> {
    fun findAllByUser_IdAndIsDeleted(userId: Long, isDeleted: Boolean = false): List<FcmToken>
}