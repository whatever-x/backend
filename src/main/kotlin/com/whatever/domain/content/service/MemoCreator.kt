package com.whatever.domain.content.service

import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentDetail.Companion.MAX_TITLE_LENGTH
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MemoCreator(
    private val contentRepository: ContentRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository
) {
    @Transactional
    fun createMemo(
        title: String?,
        description: String?,
        isCompleted: Boolean,
        tagIds: Set<Long>,
    ): Content {
        val contentDetail = ContentDetail(
            title = title,
            description = description,
            isCompleted = isCompleted
        )

        val userId = getCurrentUserId()
        val user = userRepository.getReferenceById(userId)
        val content = Content(
            user = user,
            contentDetail = contentDetail,
            type = ContentType.MEMO
        )

        val savedContent = contentRepository.save(content)

        if (tagIds.isNotEmpty()) {
            val mappings = tagIds.map { tagId ->
                val tag = tagRepository.getReferenceById(tagId)
                TagContentMapping(
                    tag = tag,
                    content = savedContent
                )
            }

            tagContentMappingRepository.saveAll(mappings)
        }

        return content
    }
}
