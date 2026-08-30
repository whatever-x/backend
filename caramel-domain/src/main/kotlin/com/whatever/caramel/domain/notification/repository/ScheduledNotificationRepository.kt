package com.whatever.caramel.domain.notification.repository

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ScheduledNotificationRepository :
    JpaRepository<ScheduledNotification, Long>,
    ScheduleNotificationInsertRepository {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        delete from ScheduledNotification sn
        where sn.notificationType in :notificationTypes
            and sn.targetUserId in :targetUserIds
    """
    )
    fun deleteAllByNotificationTypeInAndTargetUserIdIn(
        notificationTypes: Set<NotificationType>,
        targetUserIds: Set<Long>
    ): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        delete from ScheduledNotification sn
        where sn.sentAt is not null
            or exists (
            select 1 from NotificationHistory nh
            where nh.sourceNotificationId = sn.id
        )
        """
    )
    fun deleteAllProcessed(): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update ScheduledNotification sn
        set sn.sentAt = :sentAt
        where sn.id = :id
            and sn.sentAt is null
        """
    )
    fun markAsSent(
        id: Long,
        sentAt: LocalDateTime,
    ): Int
}
