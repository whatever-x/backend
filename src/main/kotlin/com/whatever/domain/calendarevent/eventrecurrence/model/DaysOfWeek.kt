package com.whatever.domain.calendarevent.eventrecurrence.model

import jakarta.persistence.*
import java.time.DayOfWeek

@Entity
class DaysOfWeek(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    val label: DayOfWeek,
)