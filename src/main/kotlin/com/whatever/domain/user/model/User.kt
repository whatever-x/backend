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

    @Column(nullable = false)
    var name: String,

    @Column(unique = true, nullable = false)
    var email: String,

    var birthDate: LocalDate,

    @Enumerated(EnumType.STRING)
    val platform: LoginPlatform,

    var nickname: String? = null,

    var gender: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", referencedColumnName = "id")
    val couple: Couple? = null,

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    val contents: MutableList<Content> = mutableListOf(),
) : BaseEntity() {
}