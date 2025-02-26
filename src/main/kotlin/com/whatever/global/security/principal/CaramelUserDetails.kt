package com.whatever.global.security.principal

import com.whatever.domain.user.model.UserStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CaramelUserDetails(
    private val userId: Long,
    private val status: UserStatus,
) : UserDetails {

    private val userIdStr = userId.toString()

    override fun getAuthorities(): Set<SimpleGrantedAuthority> {
        return setOf(
            SimpleGrantedAuthority("ROLE_${status.name}"),
        )
    }

    override fun getPassword(): String? {
        return null
    }

    override fun getUsername(): String {
        return userIdStr
    }

    fun getUserId(): Long {
        return userId
    }

}