package com.whatever.domain.sample.repository

import com.whatever.domain.user.model.User
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.CrudRepository

@Profile("dev", "local-mem")
interface SampleUserRepository : CrudRepository<User, Long> {
    fun findByEmailAndIsDeleted(email: String, isDeleted: Boolean = false): User?
    fun findByPlatformUserId(platformUserId: String): User?
}
