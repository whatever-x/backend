package com.whatever.caramel.domain.sample.repository

import com.whatever.caramel.domain.sample.model.SampleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SampleEntityRepository : JpaRepository<SampleEntity, Long>
