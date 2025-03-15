package com.whatever.domain.sample.model

import com.whatever.domain.base.BaseEntity
import jakarta.persistence.*

@Entity
class SampleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, unique = true)
    val sampleAttribute: String,
) : BaseEntity() {
}