package com.whatever.domain.couple.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.couple.exception.CoupleExceptionCode
import com.whatever.domain.couple.exception.CoupleExceptionCode.ILLEGAL_MEMBER_SIZE
import com.whatever.domain.couple.exception.CoupleExceptionCode.ILLEGAL_START_DATE
import com.whatever.domain.couple.exception.CoupleExceptionCode.SHARED_MESSAGE_OUT_OF_LENGTH
import com.whatever.domain.couple.exception.CoupleIllegalArgumentException
import com.whatever.domain.couple.exception.CoupleIllegalStateException
import com.whatever.domain.couple.model.CoupleStatus.INACTIVE
import com.whatever.domain.user.model.User
import com.whatever.util.DateTimeUtil
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Version
import org.hibernate.validator.constraints.CodePointLength
import java.time.LocalDate
import java.time.ZoneId

@Entity
class Couple(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    var startDate: LocalDate? = null,

    @Column(length = MAX_SHARED_MESSAGE_LENGTH_WITH_BUFFER)
    @field:CodePointLength(max = MAX_SHARED_MESSAGE_LENGTH)
    var sharedMessage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = MAX_STATUS_LENGTH, nullable = false)
    var status: CoupleStatus = CoupleStatus.ACTIVE,
) : BaseEntity() {

    @OneToMany(mappedBy = "_couple", fetch = FetchType.LAZY)
    protected val mutableMembers: MutableSet<User> = mutableSetOf()
    val members: Set<User> get() = mutableMembers.toSet()

    @Version
    private var version: Long = 0L

    fun addMembers(user1: User, user2: User) {
        if (user1.id == user2.id) {
            throw CoupleIllegalArgumentException(
                errorCode = ILLEGAL_MEMBER_SIZE,

                )
        }
        if (mutableMembers.isNotEmpty()) {
            throw CoupleIllegalStateException(
                errorCode = ILLEGAL_MEMBER_SIZE,
                detailMessage = "커플에는 반드시 두 명의 유저가 있어야 합니다. 현재 등록된 유저 수: ${mutableMembers.size}"
            )
        }
        user1.setCouple(this)
        user2.setCouple(this)
        mutableMembers.add(user1)
        mutableMembers.add(user2)
    }

    fun removeMember(user: User) {
        if (!mutableMembers.removeIf { it.id == user.id }) {
            throw CoupleIllegalArgumentException(errorCode = CoupleExceptionCode.NOT_A_MEMBER)
        }

        user.leaveFromCouple()
        status = INACTIVE
        if (mutableMembers.isEmpty()) {
            this.deleteEntity()
        }
    }

    fun updateStartDate(newDate: LocalDate, userZoneId: ZoneId) {
        if (status == INACTIVE) {
            throw CoupleIllegalStateException(errorCode = CoupleExceptionCode.INACTIVE_COUPLE_STATUS)
        }
        val todayInUserZone = DateTimeUtil.zonedNow(userZoneId).toLocalDate()
        if (newDate.isAfter(todayInUserZone)) {
            throw CoupleIllegalArgumentException(errorCode = ILLEGAL_START_DATE)
        }

        startDate = newDate
    }

    fun updateSharedMessage(newMessage: String?) {
        if (status == INACTIVE) {
            throw CoupleIllegalStateException(errorCode = CoupleExceptionCode.INACTIVE_COUPLE_STATUS)
        }
        newMessage?.let {
            if (newMessage.codePointCount(0, newMessage.length) > MAX_SHARED_MESSAGE_LENGTH) {
                throw CoupleIllegalArgumentException(errorCode = SHARED_MESSAGE_OUT_OF_LENGTH)
            }
        }
        sharedMessage = newMessage.takeUnless { it.isNullOrBlank() }
    }

    companion object {
        const val MAX_SHARED_MESSAGE_LENGTH_WITH_BUFFER = 50
        const val MAX_SHARED_MESSAGE_LENGTH = 24
        const val MAX_STATUS_LENGTH = 50
    }
}
