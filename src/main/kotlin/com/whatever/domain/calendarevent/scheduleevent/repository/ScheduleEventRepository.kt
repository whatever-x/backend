package com.whatever.domain.calendarevent.scheduleevent.repository

import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface ScheduleEventRepository : JpaRepository<ScheduleEvent, Long> {
    @Query("""
        select se from ScheduleEvent se
            join fetch se.content c
            join c.user u
        where se.isDeleted = false
            and (se.startDateTime <= :endDateTime and se.endDateTime >= :startDateTime)
            and u.id in :memberIds
        order by se.startDateTime
    """)
    fun findAllByDurationAndUsers(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        memberIds: Set<Long>,
    ): List<ScheduleEvent>

    @Query("""
        select se from ScheduleEvent se
            join fetch se.content c
            join fetch c.user u
        where se.id = :scheduleId
            and se.isDeleted = false 
    """)
    fun findByIdWithContentAndUser(scheduleId: Long): ScheduleEvent?

    @Query("""
        select se from ScheduleEvent se
            join fetch se.content c
        where se.id = :scheduleId
            and se.isDeleted = false 
    """)
    fun findByIdWithContent(scheduleId: Long): ScheduleEvent?

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update ScheduleEvent se
        set se.isDeleted = true
        where se.id in (
            select tse.id from ScheduleEvent tse
                join tse.content c
            where c.user.id = :userId
                and tse.isDeleted = false
        )
    """
    )
    fun softDeleteAllByUserIdInBulk(userId: Long): Int

}