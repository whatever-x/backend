package com.whatever.domain.user.repository

import com.whatever.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByPlatformUserId(platformUserId: String): User?
}
