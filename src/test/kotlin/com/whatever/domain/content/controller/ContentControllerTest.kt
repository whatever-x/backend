package com.whatever.domain.content.controller

import com.whatever.domain.ControllerTestSupport
import com.whatever.domain.content.controller.dto.request.CreateContentRequest
import com.whatever.domain.content.controller.dto.request.TagIdDto
import com.whatever.domain.content.controller.dto.request.UpdateContentRequest
import com.whatever.domain.content.exception.ContentExceptionCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

class ContentControllerTest : ControllerTestSupport() {

    @DisplayName("콘텐츠를 생성한다. (Memo 타입)")
    @Test
    fun createContent_Memo() {
        // given
        val request = CreateContentRequest(
            title = "메모 제목",
            description = "메모 설명",
            tags = listOf(TagIdDto(tagId = 1L)),
//            dateTimeInfo = null
        )

        // when // then
        mockMvc.post("/v1/content/memo") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

//    @DisplayName("콘텐츠를 생성한다. (Schedule 타입)")
//    @Test
//    fun createContent_Schedule() {
//        // given
//        val request = CreateContentRequest(
//            title = "일정 제목",
//            description = "일정 설명",
//            tags = listOf(TagIdDto(tagId = 1L)),
//            dateTimeInfo = DateTimeInfoDto(
//                startDateTime = DateTimeUtil.localNow(),
//                startTimezone = "Asia/Seoul",
//                endDateTime = DateTimeUtil.localNow().plusDays(1),
//                endTimezone = "Asia/Seoul"
//            )
//        )
//
//        // when // then
//        mockMvc.post("/v1/content") {
//            content = objectMapper.writeValueAsString(request)
//            contentType = MediaType.APPLICATION_JSON
//        }
//            .andDo { print() }
//            .andExpect {
//                status {
//                    isOk()
//                }
//            }
//
//    }

    @DisplayName("콘텐츠 생성 시 제목과 설명이 모두 비어있으면 실패한다.")
    @Test
    fun createContent_WithBlankTitleAndDescription() {
        // given
        val request = CreateContentRequest(
            title = "",
            description = "",
            isCompleted = false,
            tags = emptyList(),
//            dateTimeInfo = null
        )

        // when // then
        mockMvc.post("/v1/content/memo") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(ContentExceptionCode.TITLE_OR_DESCRIPTION_REQUIRED.code) }
            }
    }


    @DisplayName("콘텐츠를 업데이트한다.")
    @Test
    fun updateContent() {
        // given
        val request = UpdateContentRequest(
            title = "수정된 제목",
            description = "수정된 설명",
            isCompleted = true,
            tagList = listOf(TagIdDto(tagId = 1L))
        )

        // when // then
        mockMvc.put("/v1/content/memo/{memoId}", 1L) {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

    @DisplayName("콘텐츠를 삭제한다.")
    @Test
    fun deleteContent() {
        // when // then
        mockMvc.delete("/v1/content/memo/{memoId}", 1L) {
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

}
