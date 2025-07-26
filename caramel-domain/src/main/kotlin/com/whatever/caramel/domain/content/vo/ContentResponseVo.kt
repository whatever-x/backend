package com.whatever.caramel.domain.content.vo

import com.whatever.caramel.common.util.DateTimeUtil.KST_ZONE_ID
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.tag.vo.TagVo
import java.time.LocalDate

data class ContentResponseVo(
    val id: Long,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val tagList: List<TagVo>,
    val createdAt: LocalDate,
    val contentAssignee: ContentAssignee,
) {
    companion object {
        fun from(
            content: Content,
            tagList: List<TagVo>,
        ): ContentResponseVo {
            return ContentResponseVo(
                id = content.id,
                title = content.contentDetail.title ?: "",
                description = content.contentDetail.description ?: "",
                isCompleted = content.contentDetail.isCompleted,
                tagList = tagList,
                createdAt = content.getCreatedAtInZone(KST_ZONE_ID).toLocalDate(),
                contentAssignee = content.contentAssignee
            )
        }
    }
} 
