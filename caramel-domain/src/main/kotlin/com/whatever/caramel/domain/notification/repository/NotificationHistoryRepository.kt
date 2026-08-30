package com.whatever.caramel.domain.notification.repository

import com.whatever.caramel.domain.notification.model.NotificationHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationHistoryRepository : JpaRepository<NotificationHistory, Long>
