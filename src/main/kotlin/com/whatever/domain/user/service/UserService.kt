package com.whatever.domain.user.service

import com.whatever.domain.user.dto.PostUserProfileRequest
import com.whatever.domain.user.dto.PostUserProfileResponse
import com.whatever.domain.user.dto.PutUserProfileRequest
import com.whatever.domain.user.dto.PutUserProfileResponse
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.getCurrentUserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun createProfile(postUserProfileRequest: PostUserProfileRequest): PostUserProfileResponse {
        val userId = getCurrentUserId()
        val user = userRepository.findByIdOrNull(userId)?.apply {
            nickname = postUserProfileRequest.nickname
            birthDate = postUserProfileRequest.birthday
        }

        return PostUserProfileResponse(
            id = userId,
            nickname = user?.nickname!!,
            userStatus = user.userStatus,
        )
    }

    @Transactional
    fun updateProfile(putUserProfileRequest: PutUserProfileRequest): PutUserProfileResponse {
        val userId = getCurrentUserId()
        val user = userRepository.findByIdOrNull(userId)?.apply {
            if (putUserProfileRequest.nickname != null) {
                nickname = putUserProfileRequest.nickname
            }

            if (putUserProfileRequest.birthday != null) {
                birthDate = putUserProfileRequest.birthday
            }
        }

        return PutUserProfileResponse(
            id = userId,
            nickname = user?.nickname!!,
            birthday = user.birthDate!!,
        )
    }
}