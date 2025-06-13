package com.whatever.domain.couple.repository

import com.whatever.domain.couple.model.Couple
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CoupleRepository : JpaRepository<Couple, Long> {
    @Query("""
        select c from Couple c
            join fetch c.mutableMembers
        where c.id = :coupleId
            and c.isDeleted = false 
    """)
    fun findByIdWithMembers(coupleId: Long): Couple?
}