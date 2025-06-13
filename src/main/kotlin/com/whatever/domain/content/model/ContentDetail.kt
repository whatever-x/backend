package com.whatever.domain.content.model

import com.whatever.domain.content.exception.ContentExceptionCode.ILLEGAL_CONTENT_DETAIL
import com.whatever.domain.content.exception.ContentIllegalArgumentException
import com.whatever.global.exception.ErrorUi
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.validator.constraints.CodePointLength

@Embeddable
class ContentDetail(
    @Column(length = MAX_TITLE_LENGTH)
    @field:CodePointLength(max = MAX_TITLE_LENGTH)
    var title: String?,

    @Column(length = MAX_DESCRIPTION_LENGTH)
    @field:CodePointLength(max = MAX_DESCRIPTION_LENGTH)
    var description: String?,

    @Column(nullable = false)
    var isCompleted: Boolean = false,
) {

    init {
        if (title == null && description == null) {
            throw ContentIllegalArgumentException(
                errorCode = ILLEGAL_CONTENT_DETAIL,
                errorUi = ErrorUi.Toast("제목이나 본문 중 하나는 입력해야 해요."),
            )
        }
        if ((title?.isBlank() == true) || (description?.isBlank() == true)) {
            throw ContentIllegalArgumentException(
                errorCode = ILLEGAL_CONTENT_DETAIL,
                errorUi = ErrorUi.Toast("공백은 입력할 수 없어요."),
            )
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 30
        const val MAX_DESCRIPTION_LENGTH = 5000
    }
}