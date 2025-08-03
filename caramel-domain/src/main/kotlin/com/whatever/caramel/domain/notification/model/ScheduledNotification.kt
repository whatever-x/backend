package com.whatever.caramel.domain.notification.model

import com.whatever.caramel.domain.base.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class ScheduledNotification(
    val targetUserId: Long,

    @Enumerated(value = EnumType.STRING)
    @Column(length = 50, nullable = false)
    val notificationType: NotificationType,

    val notifyAt: LocalDateTime,

    val title: String,

    val body: String,

    val image: String? = null,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
}