package com.whatever.caramel.api.content.controller.dto.request

import com.whatever.caramel.domain.content.model.ContentDetail.Companion.MAX_DESCRIPTION_LENGTH
import com.whatever.caramel.domain.content.model.ContentDetail.Companion.MAX_TITLE_LENGTH
import com.whatever.caramel.domain.content.vo.ContentAssignee
import com.whatever.caramel.domain.content.vo.UpdateContentRequestVo
import com.whatever.com.whatever.caramel.api.content.tag.controller.dto.request.TagIdDto
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import org.hibernate.validator.constraints.CodePointLength

@Schema(description = "콘텐츠 수정 요청 DTO")
data class UpdateContentRequest(
    @Schema(description = "콘텐츠 제목", example = "맛집 리스트")
    @field:CodePointLength(max = MAX_TITLE_LENGTH, message = "제목은 최대 ${MAX_TITLE_LENGTH}자까지 가능합니다.")
    val title: String?,

    @Schema(description = "콘텐츠 설명", example = "어제 함께 갔던 맛집들")
    @field:CodePointLength(max = MAX_DESCRIPTION_LENGTH, message = "설명은 최대 ${MAX_DESCRIPTION_LENGTH}자까지 가능합니다.")
    val description: String?,

    @Schema(description = "완료 여부", requiredMode = NOT_REQUIRED)
    val isCompleted: Boolean = false,

    @Schema(description = "태그 번호 리스트", requiredMode = NOT_REQUIRED)
    val tagList: List<TagIdDto> = emptyList(),

    @Schema(description = "일정 정보 (캘린더 추가시에만 사용)", requiredMode = NOT_REQUIRED)
    val dateTimeInfo: DateTimeInfoDto? = null,

    @Schema(description = "소유자 타입 (ME: 나, PARTNER: 상대방, US: 우리)")
    val contentAsignee: ContentAssignee,
) {

    fun toVo(): UpdateContentRequestVo {
        return UpdateContentRequestVo(
            title = this.title,
            description = this.description,
            isCompleted = this.isCompleted,
            tagList = this.tagList.map { it.tagId },
            dateTimeInfo = this.dateTimeInfo?.toVo(),
            contentAsignee = this.contentAsignee
        )
    }
}
