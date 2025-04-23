package com.whatever.domain.content.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class ContentDetail(
    var title: String?,

    var description: String?,

    @Column(nullable = false)
    var isCompleted: Boolean = false,
){
    companion object {
        const val MAX_TITLE_LENGTH = 30
        const val MAX_DESCRIPTION_LENGTH = 5000
    }
}