package com.whatever.caramel.api.calendarevent.scheduleevent.controller.dto

import com.whatever.caramel.api.calendarevent.controller.dto.response.ScheduleDetailDto
import com.whatever.caramel.api.content.controller.dto.response.TagDto
import com.whatever.domain.calendarevent.vo.GetScheduleVo
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "일정 조회 응답 DTO")
data class GetScheduleResponse(
    @Schema(description = "일정 상세 정보")
    val scheduleDetail: ScheduleDetailDto,
    @Schema(description = "연관 태그 리스트")
    val tags: List<TagDto> = emptyList(),
) {
    companion object {
        fun from(getScheduleVo: GetScheduleVo): GetScheduleResponse {
            return GetScheduleResponse(
                scheduleDetail = ScheduleDetailDto.from(getScheduleVo.scheduleDetail),
                tags = getScheduleVo.tags.map { TagDto.from(it) }
            )
        }
    }
}
