package com.whatever.domain.couple.repository

import com.whatever.domain.couple.model.Couple
import org.springframework.data.jpa.repository.JpaRepository

interface CoupleRepository : JpaRepository<Couple, Long> {
}