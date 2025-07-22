package com.whatever.caramel.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<com.whatever.caramel.domain.user.model.User, Long> {
    @Query(
        """
        select u from User u
        where u.platformUserId = :platformUserId
            and u.isDeleted = false
    """
    )
    fun findByPlatformUserId(platformUserId: String): com.whatever.caramel.domain.user.model.User?

    @Query(
        """
        select u from User u
        where u.id in :ids
            and u.isDeleted = false
    """
    )
    fun findUserByIdIn(ids: Set<Long>): List<com.whatever.caramel.domain.user.model.User>

    @Query(
        """
        select u from User u
            left join fetch u._couple c 
        where u.id = :userId 
            and u.isDeleted = false 
    """
    )
    fun findByIdWithCouple(userId: Long): com.whatever.caramel.domain.user.model.User?
}
