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

    var startDate: LocalDate?,

    @OneToMany(mappedBy = "couple", fetch = FetchType.LAZY)
    val users: MutableList<User> = mutableListOf(),
) : BaseEntity() {
}