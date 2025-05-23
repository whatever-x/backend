package com.whatever.domain.content.tag.service

import com.whatever.domain.content.tag.controller.dto.response.TagDetailDto
import com.whatever.domain.content.tag.controller.dto.response.TagDetailListResponse
import com.whatever.domain.content.tag.repository.TagRepository
import org.springframework.stereotype.Service

@Service
class TagService(
    private val tagRepository: TagRepository,
) {
    fun getTags(): TagDetailListResponse {
        val tags = tagRepository.findAllByIsDeleted()
        return TagDetailListResponse(tags.map { TagDetailDto.from(it) })
    }
}