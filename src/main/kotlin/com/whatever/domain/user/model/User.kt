package com.whatever.domain.user.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.couple.model.Couple
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

    var gender: String? = null,

    @Enumerated(EnumType.STRING)
    var userStatus: UserStatus = UserStatus.NEW,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", referencedColumnName = "id")
    val couple: Couple? = null,

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    val contents: MutableList<Content> = mutableListOf(),
) : BaseEntity() {
}