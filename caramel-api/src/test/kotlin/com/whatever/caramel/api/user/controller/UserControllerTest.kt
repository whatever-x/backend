package com.whatever.caramel.api.user.controller

import com.whatever.SecurityUtil
import com.whatever.caramel.api.ControllerTestSupport
import com.whatever.caramel.api.user.dto.PostUserProfileRequest
import com.whatever.caramel.api.user.dto.PutUserProfileRequest
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders
import com.whatever.caramel.common.global.exception.GlobalExceptionCode
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.vo.CreatedUserProfileVo
import com.whatever.domain.user.vo.UpdatedUserProfileVo
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate

class UserControllerTest : ControllerTestSupport() {

    @BeforeEach
    fun setUp() {
        mockkStatic(SecurityUtil::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(SecurityUtil::class)
    }

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
        whenever(userService.createProfile(any(), any(), any()))
            .thenReturn(CreatedUserProfileVo(0L, "test-nick", UserStatus.SINGLE))
        every { SecurityUtil.getCurrentUserId() } returns 0L

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
        whenever(userService.updateProfile(any(), any(), any()))
            .thenReturn(UpdatedUserProfileVo(
                id = 0L,
                nickname = "test-nick",
                birthday = DateTimeUtil.localNow().toLocalDate()
            ))
        every { SecurityUtil.getCurrentUserId() } returns 0L

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
        whenever(userService.updateProfile(any(), any(), any()))
            .thenReturn(UpdatedUserProfileVo(
                id = 0L,
                nickname = "test-nick",
                birthday = request.birthday!!
            ))
        every { SecurityUtil.getCurrentUserId() } returns 0L

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
        whenever(userService.updateProfile(any(), any(), any()))
            .thenReturn(UpdatedUserProfileVo(
                id = 0L,
                nickname = request.nickname!!,
                birthday = DateTimeUtil.localNow().toLocalDate()
            ))
        every { SecurityUtil.getCurrentUserId() } returns 0L

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

}
