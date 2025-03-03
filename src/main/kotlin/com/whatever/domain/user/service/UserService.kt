package com.whatever.domain.user.service

import com.whatever.domain.user.dto.PostUserProfileRequest
import com.whatever.domain.user.dto.PostUserProfileResponse
import com.whatever.domain.user.dto.PutUserProfileRequest
import com.whatever.domain.user.dto.PutUserProfileResponse
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.getCurrentUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun createProfile(postUserProfileRequest: PostUserProfileRequest): PostUserProfileResponse {
        val userId = getCurrentUserId()
        val user = userRepository.getReferenceById(userId).apply {
            nickname = postUserProfileRequest.nickname
            birthDate = postUserProfileRequest.birthday
        }

        return PostUserProfileResponse(
            id = userId,
            nickname = user.nickname!!,
            userStatus = user.userStatus,
        )
    }

    @Transactional
    fun updateProfile(putUserProfileRequest: PutUserProfileRequest): PutUserProfileResponse {
        val userId = getCurrentUserId()
        val user = userRepository.getReferenceById(userId).apply {
            nickname = putUserProfileRequest.nickname
            birthDate = putUserProfileRequest.birthday
        }

        return PutUserProfileResponse(
            id = userId,
            nickname = user.nickname!!,
            birthday = user.birthDate!!,
        )
    }
}