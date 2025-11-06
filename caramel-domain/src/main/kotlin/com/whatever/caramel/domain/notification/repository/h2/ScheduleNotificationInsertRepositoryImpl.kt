package com.whatever.caramel.domain.notification.repository.h2

import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.repository.ScheduleNotificationInsertRepository
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
@Profile("test")
class ScheduleNotificationInsertRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : ScheduleNotificationInsertRepository {

    @Transactional
    override fun insertAllWithoutConflict(notifications: List<ScheduledNotification>) {
        if (notifications.isEmpty()) return

        val sql = """
            MERGE INTO scheduled_notification
                (target_user_id, notification_type, notify_at, title, body, image, created_at, updated_at)
            KEY(target_user_id, notification_type)
            VALUES (?, ?, ?, ?, ?, ?, now(), now())
        """.trimIndent()

        val batchArgs = notifications.map { notification ->
            arrayOf<Any?>(
                notification.targetUserId,
                notification.notificationType.name,
                notification.notifyAt.let { Timestamp.valueOf(it) },
                notification.title,
                notification.body,
                notification.image // nullable
            )
        }

        jdbcTemplate.batchUpdate(sql, batchArgs)
    }
}
