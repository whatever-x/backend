package com.whatever.domain.user.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.user.exception.UserExceptionCode
import com.whatever.domain.user.exception.UserExceptionCode.INVALID_USER_STATUS_FOR_COUPLING
import com.whatever.domain.user.exception.UserIllegalStateException
import com.whatever.domain.user.model.UserStatus.COUPLED
import com.whatever.domain.user.model.UserStatus.SINGLE
import jakarta.persistence.*
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(unique = true)
    var email: String? = null,

    var birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    val platform: LoginPlatform,

    @Column(unique = true)
    val platformUserId: String,

    @Size(min = 2, max = 8)
    @Column(length = 8)
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
        if (userStatus != SINGLE) {
            throw UserIllegalStateException(
                errorCode = INVALID_USER_STATUS_FOR_COUPLING,
                detailMessage = "Current user status is '${userStatus}'. To be coupled, the user status must be '${SINGLE}'."
            )
        }
        _couple = couple
        updateUserStatus(COUPLED)
    }

    fun leaveFromCouple() {
        this._couple = null
        updateUserStatus(SINGLE)
    }

    fun updateUserStatus(newStatus: UserStatus) {
        userStatus = newStatus
    }

    fun register(
        nickname: String,
        birthday: LocalDate,
        gender: UserGender,
    ) {
        this.nickname = nickname
        this.birthDate = birthday
        this.gender = gender
        this.userStatus = SINGLE
    }
}