package com.whatever.domain.content.tag.model

import com.whatever.domain.base.BaseEntity
import jakarta.persistence.*

@Entity
class Tag (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, unique = true)
    val label: String
) : BaseEntity() {
}