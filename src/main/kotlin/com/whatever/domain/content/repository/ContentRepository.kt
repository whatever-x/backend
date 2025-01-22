package com.whatever.domain.content.repository

import com.whatever.domain.content.model.Content
import org.springframework.data.jpa.repository.JpaRepository

interface ContentRepository : JpaRepository<Content, Long> {
}