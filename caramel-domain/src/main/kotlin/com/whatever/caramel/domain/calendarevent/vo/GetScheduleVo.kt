package com.whatever.caramel.domain.calendarevent.vo

import com.whatever.caramel.domain.calendarevent.model.ScheduleEvent
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.tag.model.Tag
import com.whatever.caramel.domain.content.tag.vo.TagVo

data class GetScheduleVo(
    val scheduleDetail: ScheduleDetailVo,
    val tags: List<TagVo> = emptyList(),
) {
    companion object {
        fun from(schedule: ScheduleEvent, content: Content, tags: List<Tag>): GetScheduleVo {
            return GetScheduleVo(
                scheduleDetail = ScheduleDetailVo.from(schedule, content),
                tags = tags.map { TagVo.from(it) }
            )
        }
    }
}
