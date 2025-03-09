package com.whatever.domain.content.service

import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import org.springframework.data.repository.findByIdOrNull
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
        tagIds: List<Long>,
    ): Content {
        val replacedTitle = when {
            title.isNullOrBlank() && !description.isNullOrBlank() -> description.take(30)
            else -> title!!
        }
        val contentDetail = ContentDetail(
            title = replacedTitle,
            description = description,
            isCompleted = isCompleted
        )

        val userId = getCurrentUserId()
        val user = userRepository.findByIdOrNull(userId)
        val content = Content(
            user = user!!,
            contentDetail = contentDetail,
            wishDate = null,
            type = ContentType.MEMO
        )

        val savedContent = contentRepository.save(content)

        if (tagIds.isNotEmpty()) {
            val tags = tagRepository.findByIdIn(tagIds)
            val mappings = tags.map { tag ->
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
