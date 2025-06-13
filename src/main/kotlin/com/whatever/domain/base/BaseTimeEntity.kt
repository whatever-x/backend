package com.whatever.domain.base

import com.whatever.util.DateTimeUtil
import com.whatever.util.DateTimeUtil.SYS_ZONE_ID
import com.whatever.util.DateTimeUtil.changeTimeZone
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.ZoneId

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: LocalDateTime
        protected set

    fun getCreatedAtInZone(zoneId: ZoneId): LocalDateTime {
        return changeTimeZone(
            sourceZonedDateTime = createdAt.atZone(SYS_ZONE_ID),
            targetZone = zoneId
        ).toLocalDateTime()
    }
}
