package com.whatever.caramel.domain.content.tag.vo

data class TagDetailListVo(
    val tagList: List<TagDetailVo>,
) {
    companion object {
        fun from(tagList: List<TagDetailVo>): TagDetailListVo {
            return TagDetailListVo(tagList)
        }
    }
} 
