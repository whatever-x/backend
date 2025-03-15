package com.whatever.domain.couple.service

import com.whatever.domain.couple.controller.dto.request.CreateCoupleRequest
import com.whatever.domain.couple.controller.dto.response.CoupleDetailResponse
import com.whatever.domain.couple.controller.dto.response.CoupleInvitationCodeResponse
import com.whatever.domain.couple.controller.dto.response.CoupleUserInfoDto
import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.couple.exception.CoupleExceptionCode.*
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.RedisUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.viascom.nanoid.NanoId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger { }

@Service
class CoupleService(
    private val redisUtil: RedisUtil,
    private val userRepository: UserRepository,
    private val coupleRepository: CoupleRepository,
) {

    companion object {
        const val INVITATION_CODE_LENGTH = 10
        const val INVITATION_CODE_REGENERATION_DEFAULT = 3
    }

    @Transactional
    fun createCouple(request: CreateCoupleRequest): CoupleDetailResponse {
        val invitationCode = request.invitationCode
        val hostUserId = redisUtil.getCoupleInvitationUser(invitationCode)
            ?: throw CoupleException(errorCode = INVITATION_CODE_EXPIRED)
        val partnerUserId = SecurityUtil.getCurrentUserId()

        if (hostUserId == partnerUserId) {
            throw CoupleException(errorCode = INVITATION_CODE_SELF_GENERATED)
        }

        val hostUser = userRepository.findUserById(hostUserId, "host user not found")
        validateSingleUser(hostUser.userStatus)
        val partnerUser = userRepository.findUserById(hostUserId, "partner user not found")
        validateSingleUser(partnerUser.userStatus)

        val newCouple = Couple()
        hostUser.setCouple(newCouple)
        partnerUser.setCouple(newCouple)

        val savedCouple = coupleRepository.save(newCouple)
        redisUtil.deleteCoupleInvitationCode(invitationCode, hostUserId)

        return CoupleDetailResponse(
            coupleId = savedCouple.id,
            startDate = savedCouple.startDate,
            sharedMessage = savedCouple.sharedMessage,
            hostInfo = CoupleUserInfoDto(
                id = hostUser.id,
                nickname = hostUser.nickname!!,
                birthDate = hostUser.birthDate!!
            ),
            partnerInfo = CoupleUserInfoDto(
                id = partnerUser.id,
                nickname = partnerUser.nickname!!,
                birthDate = partnerUser.birthDate!!
            ),
        )
    }

    fun createInvitationCode(): CoupleInvitationCodeResponse {
        validateSingleUser(SecurityUtil.getCurrentUserStatus())

        val userId = SecurityUtil.getCurrentUserId()

        redisUtil.getCoupleInvitationCode(userId)?.let {
            val expirationTime = redisUtil.getCoupleInvitationExpirationTime(it)
            return CoupleInvitationCodeResponse(
                invitationCode = it,
                expirationDateTime = expirationTime,
            )
        }

        val newInvitationCode = generateInvitationCode()
        val result = redisUtil.saveCoupleInvitationCode(
            userId = userId,
            invitationCode = newInvitationCode,
        )
        if (!result) {
            throw CoupleException(
                errorCode = INVITATION_CODE_GENERATION_FAIL,
                detailMessage = "invitation code conflict. try again"
            )
        }

        return CoupleInvitationCodeResponse(
            invitationCode = newInvitationCode,
            expirationDateTime = DateTimeUtil.localNow().plusDays(1)
        )
    }

    private fun validateSingleUser(userStatus: UserStatus) {
        if (userStatus != UserStatus.SINGLE) {
            throw CoupleException(
                errorCode = INVALID_USER_STATUS,
                detailMessage = "user status: $userStatus"
            )
        }
    }

    private fun generateInvitationCode(maxRegeneration: Int = INVITATION_CODE_REGENERATION_DEFAULT): String {
        require(maxRegeneration in 0..10) {
            "생성 시도 횟수는 최대 10까지 세팅할 수 있습니다."
        }

        var attempts = maxRegeneration
        var newInvitationCode: String
        do {
            newInvitationCode = NanoId.generate(INVITATION_CODE_LENGTH)
            if (redisUtil.getCoupleInvitationUser(newInvitationCode) == null) {
                return newInvitationCode
            }
            logger.info { "already exists code: $newInvitationCode. retry(${attempts-1} attempts left)." }
        } while (--attempts > 0)

        throw CoupleException(
            errorCode = INVITATION_CODE_GENERATION_FAIL,
            detailMessage = "생성 시도 횟수: $maxRegeneration"
        )
    }

}

private fun UserRepository.findUserById(userId: Long, exceptionMessage: String): User {
    return findByIdOrNull(userId)
        ?: throw CoupleException(errorCode = USER_NOT_FOUND, detailMessage = exceptionMessage)
}