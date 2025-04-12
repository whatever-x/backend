package com.whatever.domain.content.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.user.model.User
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class Content(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    val user: User,

    @Embedded
    val contentDetail: ContentDetail,

    @Enumerated(EnumType.STRING)
    var type: ContentType = ContentType.MEMO,
) : BaseEntity() {

    fun updateContentDetail(newContentDetail: ContentDetail) {
        with(contentDetail) {
            title = newContentDetail.title
            description = newContentDetail.description
            isCompleted = newContentDetail.isCompleted
        }
    }

    fun updateType(type: ContentType) {
        this.type = type
    }

}