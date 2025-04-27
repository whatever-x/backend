package com.whatever.domain.content.repository

import com.whatever.domain.content.model.Content
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ContentRepository : JpaRepository<Content, Long> {

    @Query("SELECT c FROM Content c WHERE c.id = :id AND c.type = 'MEMO' AND c.isDeleted = false")
    fun findMemoContentById(id: Long): Content?
}