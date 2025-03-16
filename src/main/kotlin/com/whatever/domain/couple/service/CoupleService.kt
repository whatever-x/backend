package com.whatever.domain.couple.service

import com.whatever.domain.couple.controller.dto.request.CreateCoupleRequest
import com.whatever.domain.couple.controller.dto.response.CoupleDetailResponse
import com.whatever.domain.couple.controller.dto.response.CoupleInvitationCodeResponse
import com.whatever.domain.couple.controller.dto.response.CoupleUserInfoDto
import com.whatever.domain.couple.exception.CoupleAccessDeniedException
import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.couple.exception.CoupleExceptionCode.COUPLE_NOT_FOUND
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVALID_USER_STATUS
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVITATION_CODE_EXPIRED
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVITATION_CODE_GENERATION_FAIL
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVITATION_CODE_SELF_GENERATED
import com.whatever.domain.couple.exception.CoupleExceptionCode.MEMBER_NOT_FOUND
import com.whatever.domain.couple.exception.CoupleExceptionCode.NOT_A_MEMBER
import com.whatever.domain.couple.exception.CoupleIllegalStateException
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
import java.time.Duration

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
        const val INVITATION_CODE_EXPIRATION_DAY = 1L
    }

    fun getCoupleInfo(coupleId: Long): CoupleDetailResponse {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val couple = coupleRepository.findCoupleById(coupleId)

        val myUser = couple.members.firstOrNull { it.id == currentUserId }
            ?: throw CoupleAccessDeniedException(errorCode = NOT_A_MEMBER)
        val partnerUser = couple.members.firstOrNull { it.id != currentUserId }
            ?: throw CoupleIllegalStateException(
                errorCode = MEMBER_NOT_FOUND,
                detailMessage = "상대방 유저에 대한 정보가 존재하지 않습니다."
            )

        return CoupleDetailResponse.from(
            couple = couple,
            myUser = myUser,
            partnerUser = partnerUser
        )
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
        validateSingleUser(hostUser)
        val partnerUser = userRepository.findUserById(hostUserId, "partner user not found")
        validateSingleUser(partnerUser)

        val newCouple = Couple()
        hostUser.setCouple(newCouple)
        partnerUser.setCouple(newCouple)

        val savedCouple = coupleRepository.save(newCouple)
        redisUtil.deleteCoupleInvitationCode(invitationCode, hostUserId)

        return CoupleDetailResponse(
            coupleId = savedCouple.id,
            startDate = savedCouple.startDate,
            sharedMessage = savedCouple.sharedMessage,
            myInfo = CoupleUserInfoDto(
                id = partnerUser.id,
                nickname = partnerUser.nickname!!,
                birthDate = partnerUser.birthDate!!
            ),
            partnerInfo = CoupleUserInfoDto(
                id = hostUser.id,
                nickname = hostUser.nickname!!,
                birthDate = hostUser.birthDate!!
            ),
        )
    }

    fun createInvitationCode(): CoupleInvitationCodeResponse {
        val userId = SecurityUtil.getCurrentUserId()
        val user = userRepository.findUserById(userId)
        validateSingleUser(user)

        redisUtil.getCoupleInvitationCode(userId)?.let {
            val expirationTime = redisUtil.getCoupleInvitationExpirationTime(it)
            return CoupleInvitationCodeResponse(
                invitationCode = it,
                expirationDateTime = expirationTime,
            )
        }

        val newInvitationCode = generateInvitationCode()
        val expirationDateTime = DateTimeUtil.localNow().plusDays(INVITATION_CODE_EXPIRATION_DAY)

        val expirationTime = Duration.between(DateTimeUtil.localNow(), expirationDateTime)
        val result = redisUtil.saveCoupleInvitationCode(
            userId = userId,
            invitationCode = newInvitationCode,
            expirationTime = expirationTime
        )
        if (!result) {
            throw CoupleException(
                errorCode = INVITATION_CODE_GENERATION_FAIL,
                detailMessage = "invitation code conflict. try again"
            )
        }

        return CoupleInvitationCodeResponse(
            invitationCode = newInvitationCode,
            expirationDateTime = expirationDateTime
        )
    }

    private fun validateSingleUser(user: User) {
        if (user.userStatus != UserStatus.SINGLE) {
            throw CoupleException(
                errorCode = INVALID_USER_STATUS,
                detailMessage = "user status: ${user.userStatus}"
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

private fun UserRepository.findUserById(id: Long, exceptionMessage: String? = null): User {
    return findByIdOrNull(id)
        ?: throw CoupleException(errorCode = MEMBER_NOT_FOUND, detailMessage = exceptionMessage)
}

private fun CoupleRepository.findCoupleById(id: Long, exceptionMessage: String? = null): Couple {
    return findByIdOrNull(id)
        ?: throw CoupleException(errorCode = COUPLE_NOT_FOUND, detailMessage = exceptionMessage)
}