package com.whatever.caramel.domain.notification.repository

import com.whatever.caramel.domain.notification.model.ScheduledNotification

interface ScheduleNotificationInsertRepository {
    fun insertAllWithoutConflict(notifications: List<ScheduledNotification>)
}
