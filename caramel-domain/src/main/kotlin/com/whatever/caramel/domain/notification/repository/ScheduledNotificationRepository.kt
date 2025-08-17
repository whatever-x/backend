package com.whatever.caramel.domain.notification.repository

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface ScheduledNotificationRepository : JpaRepository<ScheduledNotification, Long> {
    @Modifying
    @Query("""
        delete from ScheduledNotification sn
        where sn.notificationType in :notificationTypes
            and sn.targetUserId in :targetUserIds
    """)
    fun deleteAllByNotificationTypeInAndTargetUserIdIn(
        notificationTypes: Set<NotificationType>,
        targetUserIds: Set<Long>
    ): Int

    @Query("""
        SELECT s FROM ScheduledNotification s 
        WHERE s.notifyAt BETWEEN :startOfDay AND :endOfDay
    """)
    fun findByNotifyAt(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<ScheduledNotification>
}
