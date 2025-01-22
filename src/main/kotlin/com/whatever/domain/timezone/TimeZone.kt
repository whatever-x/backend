package com.whatever.domain.timezone

import jakarta.persistence.*

@Entity
class TimeZone (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val label: String,
) {
}