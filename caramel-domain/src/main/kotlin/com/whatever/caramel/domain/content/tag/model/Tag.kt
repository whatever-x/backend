package com.whatever.caramel.domain.content.tag.model

import com.whatever.caramel.domain.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.validator.constraints.CodePointLength

@Entity
class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, unique = true, length = MAX_LABEL_LENGTH)
    @field:CodePointLength(max = MAX_LABEL_LENGTH)
    val label: String,
) : BaseEntity() {
    companion object {
        const val MAX_LABEL_LENGTH = 50
    }
}
