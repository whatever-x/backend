package com.whatever.caramel.security.principal

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CaramelUserDetails(
    val userId: Long,
    val status: String,
    val coupleId: Long = 0L,
) : UserDetails {

    private val userIdStr = userId.toString()

    override fun getAuthorities(): Set<SimpleGrantedAuthority> {
        return setOf(
            SimpleGrantedAuthority("ROLE_${status}"),
        )
    }

    override fun getPassword(): String? {
        return null
    }

    override fun getUsername(): String {
        return userIdStr
    }
}
