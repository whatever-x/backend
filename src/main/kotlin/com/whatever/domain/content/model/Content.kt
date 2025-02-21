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

    @Embedded
    val contentDetails: ContentDetails,

    var wishDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    var status: ContentStatus = ContentStatus.ACTIVE,

) : BaseEntity() {
}