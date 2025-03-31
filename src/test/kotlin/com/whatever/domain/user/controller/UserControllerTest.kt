package com.whatever.domain.user.controller

import com.whatever.domain.ControllerTestSupport
import com.whatever.domain.user.dto.PostUserProfileRequest
import com.whatever.domain.user.dto.PutUserProfileRequest
import com.whatever.domain.user.model.UserGender
import com.whatever.global.exception.GlobalExceptionCode
import com.whatever.util.DateTimeUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put


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
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(GlobalExceptionCode.ARGS_VALIDATION_FAILED.code) }
            }
    }

    @DisplayName("프로필을 등록할 때 닉네임은 3~10자여야 한다.")
    @Test
    fun createProfile_WithBelowMinLengthNickname() {
        // given
        val request = PostUserProfileRequest(
            nickname = "22",
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

    @DisplayName("프로필을 등록할 때 닉네임은 11자 미만이어야 한다.")
    @Test
    fun createProfile_WithExceedingMaxLengthNickname() {
        // given
        val request = PostUserProfileRequest(
            nickname = "12345678901",
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

    @DisplayName("프로필을 수정할 때 닉네임은 3~10자여야 한다. (최소 길이 미만)")
    @Test
    fun updateProfile_WithBelowMinLengthNickname() {
        // given
        val request = PutUserProfileRequest(
            nickname = "ab",  // 2자
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

    @DisplayName("프로필을 수정할 때 닉네임은 11자 미만이어야 한다. (최대 길이 초과)")
    @Test
    fun updateProfile_WithExceedingMaxLengthNickname() {
        // given
        val request = PutUserProfileRequest(
            nickname = "12345678901",  // 11자
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
}