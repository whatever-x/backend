package com.whatever.domain.content.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.content.exception.ContentExceptionCode.ILLEGAL_CONTENT_DETAIL
import com.whatever.domain.content.exception.ContentIllegalArgumentException
import com.whatever.domain.user.model.User
import com.whatever.global.exception.ErrorUi
import jakarta.persistence.*

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
) : BaseEntity() {

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
}