package com.whatever.caramel.domain.content.tag.service

import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.tag.vo.TagDetailListVo
import com.whatever.caramel.domain.content.tag.vo.TagDetailVo
import org.springframework.stereotype.Service

@Service
class TagService(
    private val tagRepository: TagRepository,
) {
    fun getTags(): TagDetailListVo {
        val tags = tagRepository.findAllByIsDeleted()
        return TagDetailListVo(tags.map { TagDetailVo.from(it) })
    }
}
