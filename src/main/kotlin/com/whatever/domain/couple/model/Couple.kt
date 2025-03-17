package com.whatever.domain.couple.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.user.model.User
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Couple (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    var startDate: LocalDate? = null,

    var sharedMessage: String? = null,
) : BaseEntity() {

    @OneToMany(mappedBy = "_couple", fetch = FetchType.LAZY)
    protected val mutableMembers: MutableSet<User> = mutableSetOf()
    val members: Set<User> get() = mutableMembers.toSet()

    fun addUsers(user: User) {
        mutableMembers.add(user)
    }

    fun updateStartDate(newDate: LocalDate) {
        startDate = newDate
    }

    @PreUpdate
    protected fun validateMemberSize() {
        if (mutableMembers.size != 2) {
            throw IllegalStateException("커플에는 반드시 두 명의 유저가 있어야 합니다. 현재 등록된 유저 수: ${mutableMembers.size}")
        }
    }
}
