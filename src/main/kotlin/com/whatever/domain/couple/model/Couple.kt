package com.whatever.domain.couple.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.couple.exception.CoupleExceptionCode
import com.whatever.domain.couple.exception.CoupleIllegalStateException
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
    protected val mutableUsers: MutableSet<User> = mutableSetOf()
    val users: Set<User> get() = mutableUsers.toSet()

    fun addUsers(user: User) {
        mutableUsers.add(user)
    }

    @PrePersist
    @PreUpdate
    fun validateUsers() {
        if (mutableUsers.size != 2) {
            throw CoupleIllegalStateException(
                errorCode = CoupleExceptionCode.ILLEGAL_USER_SIZE,
                detailMessage = "current user size: ${mutableUsers.size}"
            )
        }
    }
}