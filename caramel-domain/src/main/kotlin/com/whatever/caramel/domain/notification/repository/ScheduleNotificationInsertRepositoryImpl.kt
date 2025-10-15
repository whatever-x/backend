package com.whatever.caramel.domain.notification.repository

import com.whatever.caramel.domain.notification.model.ScheduledNotification
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.stereotype.Repository

@Repository
class ScheduleNotificationInsertRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager,
) : ScheduleNotificationInsertRepository {

    @Transactional
    override fun insertAllWithoutConflict(notifications: List<ScheduledNotification>) {
        if (notifications.isEmpty()) return

        val values = notifications.joinToString(",") { notification ->
            val imageValue = notification.image?.let { "'${it.replace("'", "''")}'" } ?: "NULL"
            """
            (${notification.targetUserId}, '${notification.notificationType}', '${notification.notifyAt}', 
             '${notification.title.replace("'", "''")}', '${notification.body.replace("'", "''")}', 
             $imageValue, NOW(), NOW())
            """.trimIndent()
        }

        val sql = """
            INSERT INTO scheduled_notification 
                (target_user_id, notification_type, notify_at, title, body, image, created_at, updated_at)
            VALUES $values
            ON CONFLICT (target_user_id, notification_type) DO NOTHING
        """

        entityManager.createNativeQuery(sql).executeUpdate()
    }
}
