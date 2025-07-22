package com.whatever.caramel.domain.sample.repository

import com.whatever.caramel.domain.user.model.User
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.CrudRepository

@Profile("dev", "local-mem")
interface SampleUserRepository : CrudRepository<com.whatever.caramel.domain.user.model.User, Long> {
    fun findByEmailAndIsDeleted(email: String, isDeleted: Boolean = false): com.whatever.caramel.domain.user.model.User?
    fun findByPlatformUserId(platformUserId: String): com.whatever.caramel.domain.user.model.User?
}
