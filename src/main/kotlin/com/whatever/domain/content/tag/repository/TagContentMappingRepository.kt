package com.whatever.domain.content.tag.repository

import com.whatever.domain.content.tag.model.TagContentMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface TagContentMappingRepository : JpaRepository<TagContentMapping, Long> {

    @Query("""
        select tcm from TagContentMapping tcm
            join fetch tcm.tag t
        where tcm.content.id = :contentId
            and tcm.isDeleted = false
    """)
    fun findAllByContentIdWithTag(contentId: Long): List<TagContentMapping>

    fun findAllByContent_IdAndIsDeleted(contentId: Long, isDeleted: Boolean = false): List<TagContentMapping>

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update TagContentMapping tcm
        set tcm.isDeleted = true
        where tcm.id in (
            select ttcm.id from TagContentMapping ttcm
                join ttcm.content c
            where c.user.id = :userId
                and ttcm.isDeleted = false
        )
    """)
    fun softDeleteAllByUserIdInBulk(userId: Long): Int
}