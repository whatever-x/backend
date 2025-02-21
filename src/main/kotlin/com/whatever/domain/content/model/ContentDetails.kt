package com.whatever.domain.content.model

import jakarta.persistence.Embeddable

@Embeddable
class ContentDetails(
    var title: String? = null,
    var description: String,
)