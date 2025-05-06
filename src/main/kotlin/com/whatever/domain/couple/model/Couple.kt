package com.whatever.domain.couple.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.couple.exception.CoupleExceptionCode.ILLEGAL_MEMBER_SIZE
import com.whatever.domain.couple.exception.CoupleExceptionCode.ILLEGAL_START_DATE
import com.whatever.domain.couple.exception.CoupleIllegalArgumentException
import com.whatever.domain.couple.exception.CoupleIllegalStateException
import com.whatever.domain.couple.model.CoupleStatus.INACTIVE
import com.whatever.domain.user.model.User
import com.whatever.util.DateTimeUtil
import jakarta.persistence.*
import java.time.LocalDate
import java.time.ZoneId

@Entity
class Couple (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    var startDate: LocalDate? = null,

    var sharedMessage: String? = null,

    @Enumerated(EnumType.STRING)
    var status: CoupleStatus = CoupleStatus.ACTIVE,
) : BaseEntity() {

    @OneToMany(mappedBy = "_couple", fetch = FetchType.LAZY)
    protected val mutableMembers: MutableSet<User> = mutableSetOf()
    val members: Set<User> get() = mutableMembers.toSet()

    @Version
    private var version: Long = 0L

    fun addMembers(user1: User, user2: User) {
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
            throw RuntimeException("커플 멤버가 아님")  // TODO(준용) IAE
        }

        user.leaveFromCouple()
        status = INACTIVE
        if (mutableMembers.isEmpty()) {
            this.deleteEntity()
        }
    }

    fun updateStartDate(newDate: LocalDate, userZoneId: ZoneId) {
        if (status == INACTIVE) {
            throw RuntimeException()  // TODO ISE
        }
        val todayInUserZone = DateTimeUtil.zonedNow(userZoneId).toLocalDate()
        if (newDate.isAfter(todayInUserZone)) {
            throw CoupleIllegalArgumentException(errorCode = ILLEGAL_START_DATE)
        }

        startDate = newDate
    }

    fun updateSharedMessage(newMessage: String?) {
        if (status == INACTIVE) {
            throw RuntimeException()  // TODO ISE
        }
        sharedMessage = newMessage.takeUnless { it.isNullOrBlank() }
    }
}
