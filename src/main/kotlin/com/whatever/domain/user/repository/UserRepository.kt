package com.whatever.domain.user.repository

import com.whatever.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    fun findByPlatformUserId(platformUserId: String): User?
    fun findUserByIdIn(ids: Set<Long>): List<User>

    @Query("""
        select u from User u
            left join fetch u._couple c 
        where u.id = :userId 
            and u.isDeleted = false 
    """)
    fun findByIdWithCouple(userId: Long): User?

}
