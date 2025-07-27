package com.whatever.caramel.domain.content.vo

import com.whatever.caramel.domain.content.model.Content

data class ContentVo(
    val id: Long,
    val contentDetail: ContentDetailVo,
    val type: ContentType,
    val contentAssignee: ContentAssignee,
) {
    companion object {
        fun from(content: Content): ContentVo {
            return ContentVo(
                id = content.id,
                contentDetail = ContentDetailVo.from(content.contentDetail),
                type = content.type,
                contentAssignee = content.contentAssignee
            )
        }

        fun from(content: Content, requestUserId: Long): ContentVo {
            val isContentOwnerSameAsRequester = content.user.id == requestUserId
            return ContentVo(
                id = content.id,
                contentDetail = ContentDetailVo.from(content.contentDetail),
                type = content.type,
                contentAssignee = content.contentAssignee.fromRequestorPerspective(isContentOwnerSameAsRequester)
            )
        }
    }
} 
