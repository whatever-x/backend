package com.whatever.domain.user.controller

import com.whatever.domain.ControllerTestSupport
import com.whatever.domain.user.dto.PostUserProfileRequest
import com.whatever.domain.user.dto.PutUserProfileRequest
import com.whatever.domain.user.dto.PutUserProfileResponse
import com.whatever.domain.user.model.UserGender
import com.whatever.global.constants.CaramelHttpHeaders
import com.whatever.global.exception.GlobalExceptionCode
import com.whatever.caramel.common.util.DateTimeUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate

class UserControllerTest : ControllerTestSupport() {

    @DisplayName("유저의 프로필을 등록한다.")
    @Test
    fun createProfile() {
        // given
        val request = PostUserProfileRequest(
            nickname = "333",
            birthday = DateTimeUtil.localNow().toLocalDate(),
            agreementServiceTerms = true,
            agreementPrivatePolicy = true,
            gender = UserGender.MALE
        )

        // when // then
        mockMvc.post("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
            header(CaramelHttpHeaders.TIME_ZONE, DateTimeUtil.KST_ZONE_ID.toString())
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

    @DisplayName("프로필을 등록할 때 닉네임은 공백을 제외한 문자가 있어야 한다.")
    @Test
    fun createProfile_WithBlankNickname() {
        // given
        val request = PostUserProfileRequest(
            nickname = "      ",
            birthday = DateTimeUtil.localNow().toLocalDate(),
            agreementServiceTerms = true,
            agreementPrivatePolicy = true,
            gender = UserGender.MALE
        )

        // when // then
        mockMvc.post("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
            header(CaramelHttpHeaders.TIME_ZONE, DateTimeUtil.KST_ZONE_ID.toString())
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(GlobalExceptionCode.ARGS_VALIDATION_FAILED.code) }
            }
    }

    @DisplayName("프로필을 등록할 때 닉네임은 0자 초과여야 한다.")
    @Test
    fun createProfile_WithBelowMinLengthNickname() {
        // given
        val request = PostUserProfileRequest(
            nickname = "",
            birthday = DateTimeUtil.localNow().toLocalDate(),
            agreementServiceTerms = true,
            agreementPrivatePolicy = true,
            gender = UserGender.MALE
        )

        // when // then
        mockMvc.post("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(GlobalExceptionCode.ARGS_VALIDATION_FAILED.code) }
            }
    }

    @DisplayName("프로필을 등록할 때 닉네임은 9자 미만이어야 한다.")
    @Test
    fun createProfile_WithExceedingMaxLengthNickname() {
        // given
        val request = PostUserProfileRequest(
            nickname = "123456789",
            birthday = DateTimeUtil.localNow().toLocalDate(),
            agreementServiceTerms = true,
            agreementPrivatePolicy = true,
            gender = UserGender.MALE
        )

        // when // then
        mockMvc.post("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(GlobalExceptionCode.ARGS_VALIDATION_FAILED.code) }
            }
    }

    @DisplayName("유저의 프로필을 수정한다.")
    @Test
    fun updateProfile() {
        // given
        val request = PutUserProfileRequest(
            nickname = "테스트닉네임",
            birthday = DateTimeUtil.localNow().toLocalDate(),
        )

        // when // then
        mockMvc.put("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
            header(CaramelHttpHeaders.TIME_ZONE, DateTimeUtil.KST_ZONE_ID.toString())
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

    @DisplayName("프로필을 수정할 때 닉네임은 공백을 제외한 문자가 있어야 한다.")
    @Test
    fun updateProfile_WithBlankNickname() {
        // given
        val request = PutUserProfileRequest(
            nickname = "     ",
            birthday = DateTimeUtil.localNow().toLocalDate(),
        )

        // when // then
        mockMvc.put("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(GlobalExceptionCode.ARGS_VALIDATION_FAILED.code) }
            }
    }

    @DisplayName("프로필을 수정할 때 닉네임은 1~8자여야 한다. (최소 길이 미만)")
    @Test
    fun updateProfile_WithBelowMinLengthNickname() {
        // given
        val request = PutUserProfileRequest(
            nickname = "",  // 0자
            birthday = DateTimeUtil.localNow().toLocalDate(),
        )

        // when // then
        mockMvc.put("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(GlobalExceptionCode.ARGS_VALIDATION_FAILED.code) }
            }
    }

    @DisplayName("프로필을 수정할 때 닉네임은 9자 미만이어야 한다. (최대 길이 초과)")
    @Test
    fun updateProfile_WithExceedingMaxLengthNickname() {
        // given
        val request = PutUserProfileRequest(
            nickname = "123456789",  // 9자
            birthday = DateTimeUtil.localNow().toLocalDate(),
        )

        // when // then
        mockMvc.put("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
            header(CaramelHttpHeaders.TIME_ZONE, DateTimeUtil.KST_ZONE_ID.toString())
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(GlobalExceptionCode.ARGS_VALIDATION_FAILED.code) }
            }
    }

    @DisplayName("프로필 수정 시 nickname이 null이고 birthday만 있을 경우 생일만 업데이트된다.")
    @Test
    fun updateProfile_OnlyBirthday() {
        // given
        val request = PutUserProfileRequest(
            nickname = null,
            birthday = DateTimeUtil.localNow().toLocalDate().plusDays(2),
        )

        // when // then
        mockMvc.put("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
            header(CaramelHttpHeaders.TIME_ZONE, DateTimeUtil.KST_ZONE_ID.toString())
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

    @DisplayName("프로필 수정 시 birthday가 null이고 nickname만 있을 경우 닉네임만 업데이트된다.")
    @Test
    fun updateProfile_OnlyNickname() {
        val request = PutUserProfileRequest(
            nickname = "새닉네임",
            birthday = null,
        )

        // when // then
        mockMvc.put("/v1/user/profile") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
            header(CaramelHttpHeaders.TIME_ZONE, DateTimeUtil.KST_ZONE_ID.toString())
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

    @DisplayName("프로필 수정 시 nickname이 비어있을 경우 변경되지 않는다.")
    @Test
    fun updateProfile_WithBlankNickname_NoChange() {
        @Test
        fun updateProfile_WithBlankNickname_NoChange() {
            val originalNickname = "변경전닉네임"
            val originalBirthday = LocalDate.of(2025, 4, 20)
            given(userService.updateProfile(any(), any()))
                .willReturn(
                    PutUserProfileResponse(
                        id = 1,
                        nickname = originalNickname,
                        birthday = originalBirthday
                    )
                )

            val updateRequest = PutUserProfileRequest(
                nickname = "",
                birthday = null
            )

            // when // then
            mockMvc.put("/v1/user/profile") {
                content = objectMapper.writeValueAsString(updateRequest)
                contentType = MediaType.APPLICATION_JSON
                header(CaramelHttpHeaders.TIME_ZONE, DateTimeUtil.KST_ZONE_ID.toString())
            }
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.data.nickname") { value(originalNickname) }
                }
        }
    }
}
