package com.whatever.caramel.domain.notification.repository

import com.whatever.caramel.domain.notification.model.ScheduledNotification
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.core.env.Environment
import org.springframework.stereotype.Repository

@Repository
class ScheduleNotificationInsertRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager,
    private val env: Environment,
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

        // H2 에서는 ON CONFLICT가 없어서 아래처럼 분기, 더좋은 방법이 있다면 알려주세용
        val sql = if (env.activeProfiles.contains("batch")) {
            """
            INSERT INTO scheduled_notification 
                (target_user_id, notification_type, notify_at, title, body, image, created_at, updated_at)
            VALUES $values
            ON CONFLICT (target_user_id, notification_type) DO NOTHING
        """
        } else {
            """
            INSERT INTO scheduled_notification 
                (target_user_id, notification_type, notify_at, title, body, image, created_at, updated_at)
            VALUES $values
        """
        }

        entityManager.createNativeQuery(sql).executeUpdate()
    }
}
