package com.whatever.domain.couple.service

import com.whatever.domain.couple.controller.dto.response.CoupleInvitationCodeResponse
import com.whatever.domain.couple.exception.CoupleException
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVALID_USER_STATUS
import com.whatever.domain.couple.exception.CoupleExceptionCode.INVITATION_CODE_GENERATION_FAIL
import com.whatever.domain.user.model.UserStatus
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.RedisUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.viascom.nanoid.NanoId
import org.springframework.stereotype.Service
import java.time.Duration

private val logger = KotlinLogging.logger { }

@Service
class CoupleService(
    private val redisUtil: RedisUtil,
) {

    companion object {
        const val INVITATION_CODE_LENGTH = 10
    }

    fun createInvitationCode(): CoupleInvitationCodeResponse {
        val currentUserStatus = SecurityUtil.getCurrentUserStatus()
        if (currentUserStatus != UserStatus.SINGLE) {
            throw CoupleException(
                errorCode = INVALID_USER_STATUS,
                detailMessage = "current user status: $currentUserStatus"
            )
        }

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

    private fun generateInvitationCode(maxRegeneration: Int = 3): String {
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