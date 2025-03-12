package com.whatever.domain.couple.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.user.model.User
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Couple (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

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
            throw IllegalStateException("커플에는 반드시 두 명의 유저가 있어야 합니다. 현재 등록된 유저 수: ${mutableUsers.size}")
        }
    }
}