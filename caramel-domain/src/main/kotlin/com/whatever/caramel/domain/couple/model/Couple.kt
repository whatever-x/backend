package com.whatever.caramel.domain.couple.model

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.base.BaseEntity
import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode
import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode.ILLEGAL_MEMBER_SIZE
import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode.ILLEGAL_START_DATE
import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode.SHARED_MESSAGE_OUT_OF_LENGTH
import com.whatever.caramel.domain.couple.exception.CoupleIllegalArgumentException
import com.whatever.caramel.domain.couple.exception.CoupleIllegalStateException
import com.whatever.caramel.domain.couple.model.CoupleStatus.INACTIVE
import com.whatever.caramel.domain.user.model.User
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
    @field:CodePointLength(
        max = MAX_SHARED_MESSAGE_LENGTH,
        message = "Maximum description length is $MAX_SHARED_MESSAGE_LENGTH"
    )
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

    fun addMembers(
        user1: User,
        user2: User,
    ) {
        if (user1.id == user2.id) {
            throw CoupleIllegalArgumentException(
                errorCode = ILLEGAL_MEMBER_SIZE,
                errorUi = ErrorUi.Toast("커플 멤버 구성에 문제가 생겼어요. 다시 시도해주세요."),
            )
        }
        if (mutableMembers.isNotEmpty()) {
            throw CoupleIllegalStateException(
                errorCode = ILLEGAL_MEMBER_SIZE,
                errorUi = ErrorUi.Toast("이미 구성된 커플에는 멤버를 추가할 수 없어요."),
            )
        }
        user1.setCouple(this)
        user2.setCouple(this)
        mutableMembers.add(user1)
        mutableMembers.add(user2)
    }

    fun removeMember(user: User) {
        if (!mutableMembers.removeIf { it.id == user.id }) {
            throw CoupleIllegalArgumentException(
                errorCode = CoupleExceptionCode.NOT_A_MEMBER,
                errorUi = ErrorUi.Toast("커플 멤버가 아닌 유저는 제거할 수 없어요."),
            )
        }

        user.leaveFromCouple()
        status = INACTIVE
        if (mutableMembers.isEmpty()) {
            this.deleteEntity()
        }
    }

    fun updateStartDate(newDate: LocalDate, userZoneId: ZoneId) {
        if (status == INACTIVE) {
            throw CoupleIllegalStateException(
                errorCode = CoupleExceptionCode.INACTIVE_COUPLE_STATUS,
                errorUi = ErrorUi.Dialog("커플 연결이 끊어져 더 이상 이 공간의 데이터를 더 이상 수정할 수 없어요."),
            )
        }
        val todayInUserZone = DateTimeUtil.zonedNow(userZoneId).toLocalDate()
        if (newDate.isAfter(todayInUserZone)) {
            throw CoupleIllegalArgumentException(
                errorCode = ILLEGAL_START_DATE,
                errorUi = ErrorUi.Toast("오늘보다 미래의 날짜는 설정할 수 없어요.")
            )
        }

        startDate = newDate
    }

    fun updateSharedMessage(newMessage: String?) {
        if (status == INACTIVE) {
            throw CoupleIllegalStateException(
                errorCode = CoupleExceptionCode.INACTIVE_COUPLE_STATUS,
                errorUi = ErrorUi.Dialog("커플 연결이 끊어져 더 이상 이 공간의 데이터를 더 이상 수정할 수 없어요."),
            )
        }
        newMessage?.let {
            if (newMessage.codePointCount(0, newMessage.length) > MAX_SHARED_MESSAGE_LENGTH) {
                throw CoupleIllegalArgumentException(
                    errorCode = SHARED_MESSAGE_OUT_OF_LENGTH,
                    errorUi = ErrorUi.Toast("최대 ${MAX_SHARED_MESSAGE_LENGTH}자 까지 입력할 수 있어요.")
                )
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
