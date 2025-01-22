package com.whatever.domain.content.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.user.model.User
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Content(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    val user: User,

    var title: String? = null,

    var body: String,

    var wishDate: LocalDate? = null,

//    @OneToMany(mappedBy = "content", fetch = FetchType.LAZY)
//    val dayEvents: MutableList<DayEvent> = mutableListOf(),
//
//    @OneToMany(mappedBy = "content", fetch = FetchType.LAZY)
//    val timeEvents: MutableList<TimeEvent> = mutableListOf(),
) : BaseEntity() {
}