package com.whatever.domain.user.service

import com.whatever.domain.user.dto.GetUserInfoResponse
import com.whatever.domain.user.dto.PatchUserSettingRequest
import com.whatever.domain.user.dto.UserSettingResponse
import com.whatever.domain.user.dto.PostUserProfileRequest
import com.whatever.domain.user.dto.PostUserProfileResponse
import com.whatever.domain.user.dto.PutUserProfileRequest
import com.whatever.domain.user.dto.PutUserProfileResponse
import com.whatever.domain.user.exception.UserExceptionCode.NOT_FOUND
import com.whatever.domain.user.exception.UserExceptionCode.SETTING_DATA_NOT_FOUND
import com.whatever.domain.user.exception.UserIllegalStateException
import com.whatever.domain.user.exception.UserNotFoundException
import com.whatever.domain.user.model.UserSetting
import com.whatever.domain.user.repository.UserRepository
import com.whatever.domain.user.repository.UserSettingRepository
import com.whatever.global.exception.ErrorUi
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import com.whatever.util.findByIdAndNotDeleted
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class UserService(
    private val userRepository: UserRepository,
    private val userSettingRepository: UserSettingRepository,
) {
    @Transactional
    fun createProfile(postUserProfileRequest: PostUserProfileRequest): PostUserProfileResponse {
        val userId = getCurrentUserId()
        val user = userRepository.findByIdAndNotDeleted(userId)
            ?: throw UserNotFoundException(NOT_FOUND)

        with(postUserProfileRequest) {
            user.register(nickname, birthday, gender)
        }

        if (!userSettingRepository.existsByUserAndIsDeleted(user)) {
            userSettingRepository.save(UserSetting(user))
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
        val user = userRepository.findByIdAndNotDeleted(userId)?.apply {
            if (putUserProfileRequest.nickname.isNullOrBlank().not()) {
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

    fun getUserInfo(userId: Long = getCurrentUserId()): GetUserInfoResponse {
        val user = userRepository.findByIdAndNotDeleted(userId) ?: throw UserNotFoundException(errorCode = NOT_FOUND)
        return GetUserInfoResponse.from(user)
    }

    @Transactional
    fun updateUserSetting(
        request: PatchUserSettingRequest,
        userId: Long = getCurrentUserId(),
    ): UserSettingResponse {
        val userRef = userRepository.getReferenceById(userId)
        val userSetting = userSettingRepository.findByUserAndIsDeleted(userRef)
            ?: throw UserIllegalStateException(
                errorCode = SETTING_DATA_NOT_FOUND,
                errorUi = ErrorUi.Toast("유저 설정 정보를 찾을 수 없어요."),
            )

        with(request) {
            request.notificationEnabled?.let { userSetting.notificationEnabled = it }
        }

        return UserSettingResponse.from(userSetting)
    }

    fun getUserSetting(
        userId: Long = getCurrentUserId(),
    ): UserSettingResponse {
        val userRef = userRepository.getReferenceById(userId)
        val userSetting = userSettingRepository.findByUserAndIsDeleted(userRef)
            ?: throw UserIllegalStateException(
                SETTING_DATA_NOT_FOUND,
                errorUi = ErrorUi.Toast("유저 설정 정보를 찾을 수 없어요."),
            )
        return UserSettingResponse.from(userSetting)
    }
}