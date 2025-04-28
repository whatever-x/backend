package com.whatever.domain.content.repository

import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ContentRepository : JpaRepository<Content, Long> {

    @Query("SELECT c FROM Content c WHERE c.id = :id AND c.type = :type AND c.isDeleted = false")
    fun findContentByIdAndType(id: Long, type: ContentType): Content?
}