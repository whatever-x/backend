package com.whatever.domain.user.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.user.exception.UserExceptionCode
import com.whatever.domain.user.exception.UserIllegalStateException
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    var email: String? = null,

    var birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    val platform: LoginPlatform,

    @Column(unique = true)
    val platformUserId: String,

    var nickname: String? = null,

    @Enumerated(EnumType.STRING)
    var gender: UserGender? = null,

    @Enumerated(EnumType.STRING)
    var userStatus: UserStatus = UserStatus.NEW,

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    val contents: MutableList<Content> = mutableListOf(),
) : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", referencedColumnName = "id")
    protected var _couple: Couple? = null
    val couple: Couple? get() = _couple

    fun setCouple(couple: Couple) {
        if (_couple != null) {
            throw UserIllegalStateException(UserExceptionCode.ALREADY_EXIST_COUPLE)
        }
        _couple = couple
        couple.addUsers(this)
        updateUserStatus(UserStatus.COUPLED)
    }

    fun updateUserStatus(newStatus: UserStatus) {
        userStatus = newStatus
    }
}