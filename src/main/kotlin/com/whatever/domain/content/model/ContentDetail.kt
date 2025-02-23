package com.whatever.domain.content.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class ContentDetail(
    @Column(nullable = false)
    var title: String,

    var description: String?,

    @Column(nullable = false)
    var isCompleted: Boolean = false,
)