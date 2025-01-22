package com.whatever.domain.content.tag.repository

import com.whatever.domain.content.tag.model.TagContentMapping
import org.springframework.data.jpa.repository.JpaRepository

interface TagContentMappingRepository : JpaRepository<TagContentMapping, Long> {
}