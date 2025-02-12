package com.whatever.domain.user.service

import com.whatever.domain.user.dto.UserSignUpResponse
import com.whatever.domain.user.dto.UserStatus
import org.springframework.stereotype.Service

interface UserService {
    fun createProfile(): UserSignUpResponse
}

@Service
class DefaultUserService : UserService {

    override fun createProfile(): UserSignUpResponse {
        return UserSignUpResponse(1, UserStatus.NEW)
    }

}