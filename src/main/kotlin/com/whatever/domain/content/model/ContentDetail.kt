package com.whatever.domain.content.model

import com.whatever.domain.content.exception.ContentExceptionCode
import com.whatever.domain.content.exception.ContentIllegalArgumentException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class ContentDetail(
    var title: String?,

    var description: String?,

    @Column(nullable = false)
    var isCompleted: Boolean = false,
){

    init {
        if (title == null && description == null) {
            throw ContentIllegalArgumentException(
                errorCode = ContentExceptionCode.ILLEGAL_CONTENT_DETAIL,
                detailMessage = "Both title and description cannot be Null."
            )
        }
        if ((title?.isBlank() == true) || (description?.isBlank() == true)) {
            throw ContentIllegalArgumentException(
                errorCode = ContentExceptionCode.ILLEGAL_CONTENT_DETAIL,
                detailMessage = "Title and description must not be blank."
            )
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 30
        const val MAX_DESCRIPTION_LENGTH = 5000
    }
}