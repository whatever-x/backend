package com.whatever.domain.content.tag.repository

import com.whatever.domain.content.tag.model.TagContentMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TagContentMappingRepository : JpaRepository<TagContentMapping, Long> {

    @Query("""
        select tcm from TagContentMapping tcm
            join fetch tcm.tag t
        where tcm.content.id = :contentId
            and tcm.isDeleted = false
    """)
    fun findAllByContentIdWithTag(contentId: Long): List<TagContentMapping>

    fun findAllByContent_IdAndIsDeleted(contentId: Long, isDeleted: Boolean = false): List<TagContentMapping>
}