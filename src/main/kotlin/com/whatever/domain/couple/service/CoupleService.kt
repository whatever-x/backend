package com.whatever.domain.couple.service

import com.whatever.domain.couple.controller.dto.request.CreateCoupleRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleSharedMessageRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleStartDateRequest
import com.whatever.domain.couple.controller.dto.response.CoupleBasicResponse
import com.whatever.domain.couple.controller.dto.response.CoupleDetailResponse
import com.whatever.domain.couple.controller.dto.response.CoupleInvitationCodeResponse
import com.whatever.domain.couple.exception.CoupleAccessDeniedException
import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.couple.exception.CoupleExceptionCode.COUPLE_NOT_FOUND
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVALID_USER_STATUS
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVITATION_CODE_EXPIRED
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVITATION_CODE_GENERATION_FAIL
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVITATION_CODE_SELF_GENERATED
import com.whatever.domain.couple.exception.CoupleExceptionCode.MEMBER_NOT_FOUND
import com.whatever.domain.couple.exception.CoupleExceptionCode.NOT_A_MEMBER
import com.whatever.domain.couple.exception.CoupleExceptionCode.UPDATE_FAIL
import com.whatever.domain.couple.exception.CoupleIllegalArgumentException
import com.whatever.domain.couple.exception.CoupleIllegalStateException
import com.whatever.domain.couple.exception.CoupleNotFoundException
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.couple.service.event.dto.CoupleMemberLeaveEvent
import com.whatever.domain.couple.repository.InvitationCodeRedisRepository
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.exception.common.CaramelException
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.findByIdAndNotDeleted
import com.whatever.util.toZonId
import io.github.oshai.kotlinlogging.KotlinLogging
import io.viascom.nanoid.NanoId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


private val logger = KotlinLogging.logger { }

@Service
class CoupleService(
    private val userRepository: UserRepository,
    private val coupleRepository: CoupleRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val inviCodeRedisRepository: InvitationCodeRedisRepository,
) {

    companion object {
        const val INVITATION_CODE_LENGTH = 10
        const val INVITATION_CODE_REGENERATION_DEFAULT = 3
        const val INVITATION_CODE_EXPIRATION_DAY = 1L
    }

    @Retryable(
        retryFor = [OptimisticLockingFailureException::class],
        notRecoverable = [CaramelException::class],
        backoff = Backoff(delay = 100, maxDelay = 300),
        maxAttempts = 3,
        recover = "updateSharedMessageRecover",
    )
    @Transactional
    fun updateSharedMessage(coupleId: Long, request: UpdateCoupleSharedMessageRequest): CoupleBasicResponse {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val couple = coupleRepository.findCoupleById(coupleId)
        couple.members.find { it.id == currentUserId }
            ?: throw CoupleAccessDeniedException(errorCode = NOT_A_MEMBER)

        val updatedCouple =  couple.apply {
            updateSharedMessage(request.sharedMessage)
        }

        return CoupleBasicResponse.from(updatedCouple)
    }
    @Recover
    fun updateSharedMessageRecover(e: OptimisticLockingFailureException, coupleId: Long): CoupleBasicResponse {
        logger.error { "couple shared message update fail. couple id: ${coupleId}" }
        throw CoupleIllegalStateException(errorCode = UPDATE_FAIL)
    }

    @Retryable(
        retryFor = [OptimisticLockingFailureException::class],
        notRecoverable = [CaramelException::class],
        backoff = Backoff(delay = 100, maxDelay = 300),
        maxAttempts = 3,
        recover = "updateStartDateRecover",
    )
    @Transactional
    fun updateStartDate(
        coupleId: Long,
        request: UpdateCoupleStartDateRequest,
        timeZone: String,
    ): CoupleBasicResponse {
        val currentUserId = SecurityUtil.getCurrentUserId()
        val couple = coupleRepository.findCoupleById(coupleId)
        couple.members.find { it.id == currentUserId }
            ?: throw CoupleAccessDeniedException(errorCode = NOT_A_MEMBER)

        val updatedCouple = couple.apply {
            updateStartDate(
                newDate = request.startDate,
                userZoneId = timeZone.toZonId()
            )
        }

        return CoupleBasicResponse.from(updatedCouple)
    }
    @Recover
    fun updateStartDateRecover(
        e: OptimisticLockingFailureException,
        coupleId: Long,
        request: UpdateCoupleStartDateRequest,
    ): CoupleBasicResponse {
        logger.error { "couple start date update fail. couple id: ${coupleId}" }
        throw CoupleIllegalStateException(errorCode = UPDATE_FAIL)
    }

    fun getCoupleInfo(
        coupleId: Long = SecurityUtil.getCurrentUserCoupleId(),
    ): CoupleBasicResponse {
        val couple = coupleRepository.findByIdAndNotDeleted(coupleId)
            ?: throw CoupleNotFoundException(COUPLE_NOT_FOUND)
        return CoupleBasicResponse.from(couple)
    }

    @Transactional(readOnly = true)
    fun getCoupleAndMemberInfo(coupleId: Long): CoupleDetailResponse {
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
    fun leaveCouple(
        coupleId: Long,
        userId: Long = SecurityUtil.getCurrentUserId(),
    ) {
        val couple = coupleRepository.findByIdWithMembers(coupleId)
            ?: throw CoupleNotFoundException(errorCode = COUPLE_NOT_FOUND)
        val user = couple.members.find { it.id == userId }
            ?: throw CoupleIllegalArgumentException(errorCode = NOT_A_MEMBER)

        couple.removeMember(user)
        applicationEventPublisher.publishEvent(CoupleMemberLeaveEvent(coupleId, userId))
    }

    @Transactional
    fun createCouple(request: CreateCoupleRequest): CoupleDetailResponse {
        val invitationCode = request.invitationCode
        val creatorUserId = inviCodeRedisRepository.getInvitationUser(invitationCode)
            ?: throw CoupleException(errorCode = INVITATION_CODE_EXPIRED)
        val joinerUserId = SecurityUtil.getCurrentUserId()

        if (creatorUserId == joinerUserId) {
            throw CoupleException(errorCode = INVITATION_CODE_SELF_GENERATED)
        }

        val users = userRepository.findUserByIdIn(setOf(creatorUserId, joinerUserId))
        val creatorUser = users.find { it.id == creatorUserId }
            ?: throw CoupleException(errorCode = MEMBER_NOT_FOUND, detailMessage = "host user not found")
        validateSingleUser(creatorUser)
        val joinerUser = users.find { it.id == joinerUserId }
            ?: throw CoupleException(errorCode = MEMBER_NOT_FOUND, detailMessage = "partner user not found")
        validateSingleUser(joinerUser)

        val savedCouple = coupleRepository.save(Couple())
        savedCouple.addMembers(creatorUser, joinerUser)

        inviCodeRedisRepository.deleteInvitationCode(invitationCode, creatorUserId)

        return CoupleDetailResponse.from(
            couple = savedCouple,
            myUser = joinerUser,
            partnerUser = creatorUser
        )
    }

    fun createInvitationCode(): CoupleInvitationCodeResponse {
        val userId = SecurityUtil.getCurrentUserId()
        val user = userRepository.findUserById(userId)
        validateSingleUser(user)

        inviCodeRedisRepository.getInvitationCode(userId)?.let {
            val expirationTime = inviCodeRedisRepository.getInvitationExpirationTime(it)
            return CoupleInvitationCodeResponse(
                invitationCode = it,
                expirationDateTime = expirationTime,
            )
        }

        val newInvitationCode = generateInvitationCode()
        val expirationDateTime = DateTimeUtil.zonedNow().plusDays(INVITATION_CODE_EXPIRATION_DAY)

        val expirationTime = DateTimeUtil.getDuration(expirationDateTime)
        val result = inviCodeRedisRepository.saveInvitationCode(
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
            expirationDateTime = expirationDateTime.toLocalDateTime()
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
            if (inviCodeRedisRepository.getInvitationUser(newInvitationCode) == null) {
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
    return findByIdAndNotDeleted(id)
        ?: throw CoupleIllegalArgumentException(errorCode = MEMBER_NOT_FOUND, detailMessage = exceptionMessage)
}

private fun CoupleRepository.findCoupleById(id: Long, exceptionMessage: String? = null): Couple {
    return findByIdAndNotDeleted(id)
        ?: throw CoupleNotFoundException(errorCode = COUPLE_NOT_FOUND, detailMessage = exceptionMessage)
}