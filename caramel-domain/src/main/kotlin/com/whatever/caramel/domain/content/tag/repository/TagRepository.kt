package com.whatever.caramel.domain.content.tag.repository

import com.whatever.caramel.domain.content.tag.model.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Long> {
    fun findByIdIn(ids: List<Long>): List<Tag>
    fun findAllByIdInAndIsDeleted(tagIds: Set<Long>, isDeleted: Boolean = false): Set<Tag>
    fun findAllByIsDeleted(isDeleted: Boolean = false): Set<Tag>
}
