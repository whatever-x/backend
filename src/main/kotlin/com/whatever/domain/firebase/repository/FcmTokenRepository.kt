package com.whatever.domain.firebase.repository

import com.whatever.domain.firebase.model.FcmToken
import com.whatever.util.DateTimeUtil
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface FcmTokenRepository : JpaRepository<FcmToken, Long> {
    fun findAllByUser_IdAndIsDeleted(userId: Long, isDeleted: Boolean = false): List<FcmToken>

    @Query(
        """
        select tk from FcmToken tk
        join UserSetting us on us.user = tk.user
        where tk.user.id in :userIds
            and tk.isDeleted = false
            and tk.updatedAt >= :expireDateTime
            and us.notificationEnabled = true
    """
    )
    fun findAllSendableTokensByUserIds(
        userIds: Set<Long>,
        expireDateTime: LocalDateTime = DateTimeUtil.localNow().minusDays(270),
    ): List<FcmToken>
}
