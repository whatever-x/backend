package com.whatever.caramel.domain.notification.model

import com.whatever.caramel.domain.base.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "notification_history",
    uniqueConstraints = [UniqueConstraint(name = "uk_notification_history_source_notification_id", columnNames = ["sourceNotificationId"])],
)
class NotificationHistory(
    @Column(nullable = false)
    val sourceNotificationId: Long,
    val targetUserId: Long,

    @Enumerated(value = EnumType.STRING)
    @Column(length = 50, nullable = false)
    val notificationType: NotificationType,

    val notifyAt: LocalDateTime,

    val title: String,

    val body: String,

    val image: String? = null,

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20, nullable = false)
    val sendStatus: SendStatus,

    val errorMessage: String? = null,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    companion object {
        fun failed(
            source: ScheduledNotification,
            errorMessage: String,
        ): NotificationHistory = NotificationHistory(
            sourceNotificationId = source.id,
            targetUserId = source.targetUserId,
            notificationType = source.notificationType,
            notifyAt = source.notifyAt,
            title = source.title,
            body = source.body,
            image = source.image,
            sendStatus = SendStatus.FAILED,
            errorMessage = errorMessage,
        )
    }
}
