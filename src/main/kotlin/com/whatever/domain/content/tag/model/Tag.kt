package com.whatever.domain.content.tag.model

import com.whatever.domain.base.BaseEntity
import jakarta.persistence.*
import org.hibernate.validator.constraints.CodePointLength

@Entity
class Tag (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, unique = true, length = MAX_LABEL_LENGTH)
    @field:CodePointLength(max = MAX_LABEL_LENGTH)
    val label: String
) : BaseEntity() {
    companion object {
        const val MAX_LABEL_LENGTH = 50
    }
}