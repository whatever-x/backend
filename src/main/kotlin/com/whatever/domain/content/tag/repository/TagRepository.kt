package com.whatever.domain.content.tag.repository

import com.whatever.domain.content.tag.model.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Long> {
}