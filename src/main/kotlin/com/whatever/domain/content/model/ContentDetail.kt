package com.whatever.domain.content.model

import com.whatever.domain.content.exception.ContentExceptionCode
import com.whatever.domain.content.exception.ContentIllegalArgumentException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class ContentDetail(
    @Column(length = MAX_TITLE_LENGTH)
    var title: String?,

    @Column(length = MAX_DESCRIPTION_LENGTH)
    var description: String?,

    @Column(nullable = false)
    var isCompleted: Boolean = false,
) {

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

        title?.let {
            if (it.codePointCount(0, it.length) > MAX_TITLE_LENGTH) {
                throw ContentIllegalArgumentException(
                    errorCode = ContentExceptionCode.TITLE_OUT_OF_LENGTH,
                    detailMessage = "Maximum title length is ${MAX_TITLE_LENGTH}. Current:${it.length}"
                )
            }
        }
        description?.let {
            if (it.codePointCount(0, it.length) > MAX_DESCRIPTION_LENGTH) {
                throw ContentIllegalArgumentException(
                    errorCode = ContentExceptionCode.DESCRIPTION_OUT_OF_LENGTH,
                    detailMessage = "Maximum description length is ${MAX_DESCRIPTION_LENGTH}. Current:${it.length}"
                )
            }
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 30
        const val MAX_DESCRIPTION_LENGTH = 5000
    }
}