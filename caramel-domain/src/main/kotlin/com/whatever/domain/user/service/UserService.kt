package com.whatever.domain.user.service

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.domain.findByIdAndNotDeleted
import com.whatever.domain.user.exception.UserExceptionCode.NOT_FOUND
import com.whatever.domain.user.exception.UserExceptionCode.SETTING_DATA_NOT_FOUND
import com.whatever.domain.user.exception.UserIllegalStateException
import com.whatever.domain.user.exception.UserNotFoundException
import com.whatever.domain.user.model.UserSetting
import com.whatever.domain.user.repository.UserRepository
import com.whatever.domain.user.repository.UserSettingRepository
import com.whatever.domain.user.vo.CreateUserProfileVo
import com.whatever.domain.user.vo.CreatedUserProfileVo
import com.whatever.domain.user.vo.UpdateUserProfileVo
import com.whatever.domain.user.vo.UpdateUserSettingVo
import com.whatever.domain.user.vo.UpdatedUserProfileVo
import com.whatever.domain.user.vo.UserInfoVo
import com.whatever.domain.user.vo.UserSettingVo
import com.whatever.domain.user.vo.UserVo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userSettingRepository: UserSettingRepository,
) {
    @Transactional
    fun createProfile(
        createUserProfileVo: CreateUserProfileVo,
        userTimeZone: ZoneId,
        userId: Long,
    ): CreatedUserProfileVo {
        val user = userRepository.findByIdAndNotDeleted(userId)
            ?: throw UserNotFoundException(NOT_FOUND)

        with(createUserProfileVo) {
            user.register(nickname, birthday, gender, userTimeZone)
        }

        if (!userSettingRepository.existsByUserAndIsDeleted(user)) {
            userSettingRepository.save(UserSetting(user))
        }

        return CreatedUserProfileVo.from(user, userId)
    }

    @Transactional
    fun updateProfile(
        updateUserProfileVo: UpdateUserProfileVo,
        userTimeZone: ZoneId,
        userId: Long,
    ): UpdatedUserProfileVo {
        val user = userRepository.findByIdAndNotDeleted(userId)?.apply {
            if (updateUserProfileVo.nickname.isNullOrBlank().not()) {
                nickname = updateUserProfileVo.nickname
            }

            if (updateUserProfileVo.birthday != null) {
                updateBirthDate(updateUserProfileVo.birthday, userTimeZone)
            }
        } ?: throw UserNotFoundException(errorCode = NOT_FOUND)

        return UpdatedUserProfileVo.from(user, userId)
    }

    fun getUserInfo(
        userId: Long,
    ): UserInfoVo {
        val user = userRepository.findByIdAndNotDeleted(userId) ?: throw UserNotFoundException(errorCode = NOT_FOUND)
        return UserInfoVo.from(user)
    }

    fun getUserWithCouple(
        userId: Long,
    ): UserVo? {
        return userRepository.findByIdWithCouple(userId)?.let { UserVo.from(it) }
    }

    @Transactional
    fun updateUserSetting(
        updateUserSettingVo: UpdateUserSettingVo,
        userId: Long,
    ): UserSettingVo {
        val userRef = userRepository.getReferenceById(userId)
        val userSetting = userSettingRepository.findByUserAndIsDeleted(userRef)
            ?: throw UserIllegalStateException(
                errorCode = SETTING_DATA_NOT_FOUND,
                errorUi = ErrorUi.Toast("유저 설정 정보를 찾을 수 없어요."),
            )

        with(updateUserSettingVo) {
            notificationEnabled?.let { userSetting.notificationEnabled = it }
        }

        return UserSettingVo.from(userSetting)
    }

    fun getUserSetting(
        userId: Long,
    ): UserSettingVo {
        val userRef = userRepository.getReferenceById(userId)
        val userSetting = userSettingRepository.findByUserAndIsDeleted(userRef)
            ?: throw UserIllegalStateException(
                errorCode = SETTING_DATA_NOT_FOUND,
                errorUi = ErrorUi.Toast("유저 설정 정보를 찾을 수 없어요."),
            )
        return UserSettingVo.from(userSetting)
    }
}
