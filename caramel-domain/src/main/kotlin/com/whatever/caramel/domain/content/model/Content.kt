package com.whatever.caramel.domain.content.model

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.domain.base.BaseEntity
import com.whatever.caramel.domain.content.exception.ContentExceptionCode.ILLEGAL_CONTENT_DETAIL
import com.whatever.caramel.domain.content.exception.ContentIllegalArgumentException
import com.whatever.caramel.domain.content.vo.ContentOwnerType
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.user.model.User
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(
    indexes = [
        Index(
            name = "content_idx_user_id",
            columnList = "user_id",
        )
    ]
)
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
    @Column(length = 50, nullable = false)
    var type: ContentType = ContentType.MEMO,

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    var ownerType: ContentOwnerType = ContentOwnerType.ME,
) : BaseEntity() {
    @Version
    private var version: Long = 0L

    fun updateContentDetail(newContentDetail: ContentDetail) {
        if (newContentDetail.title == null && newContentDetail.description == null) {
            throw ContentIllegalArgumentException(
                errorCode = ILLEGAL_CONTENT_DETAIL,
                errorUi = ErrorUi.Toast("제목이나 본문 중 하나는 입력해야 해요."),
            )
        }
        with(contentDetail) {
            title = newContentDetail.title
            description = newContentDetail.description
            isCompleted = newContentDetail.isCompleted
        }
    }

    fun updateType(type: ContentType) {
        this.type = type
    }

    fun updateOwnerType(ownerType: ContentOwnerType) {
        this.ownerType = ownerType
    }
}
