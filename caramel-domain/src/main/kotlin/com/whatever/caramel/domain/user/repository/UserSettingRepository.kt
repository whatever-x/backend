package com.whatever.caramel.domain.user.repository

import com.whatever.caramel.domain.user.model.UserSetting
import org.springframework.data.jpa.repository.JpaRepository

interface UserSettingRepository : JpaRepository<UserSetting, Long> {
    fun findByUserAndIsDeleted(
        user: com.whatever.caramel.domain.user.model.User,
        isDeleted: Boolean = false,
    ): UserSetting?

    fun existsByUserAndIsDeleted(user: com.whatever.caramel.domain.user.model.User, isDeleted: Boolean = false): Boolean
}
