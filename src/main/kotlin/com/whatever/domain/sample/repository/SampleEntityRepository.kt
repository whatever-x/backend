package com.whatever.domain.sample.repository

import com.whatever.domain.sample.model.SampleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SampleEntityRepository : JpaRepository<SampleEntity, Long> {
}