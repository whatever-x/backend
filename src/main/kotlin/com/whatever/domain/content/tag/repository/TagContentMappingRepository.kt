package com.whatever.domain.content.tag.repository

import com.whatever.domain.content.tag.model.TagContentMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TagContentMappingRepository : JpaRepository<TagContentMapping, Long> {

    @Query("""
        select tcm from TagContentMapping tcm
            join fetch tcm.tag t
        where tcm.content.id = :contentId
    """)
    fun findAllByContentId(contentId: Long): MutableList<TagContentMapping>
}