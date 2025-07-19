package firebase.service.event.dto

import content.model.ContentDetail

data class ScheduleCreateEvent(
    val userId: Long,
    val coupleId: Long,
    val memberIds: Set<Long>,
    val contentDetail: ContentDetail,
)
