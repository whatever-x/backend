package com.whatever.domain.auth.controller

import com.whatever.domain.ControllerTestSupport
import com.whatever.domain.auth.dto.ServiceTokenResponse
import com.whatever.domain.auth.dto.SignInRequest
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.global.constants.CaramelHttpHeaders.AUTH_JWT_HEADER
import com.whatever.global.constants.CaramelHttpHeaders.DEVICE_ID
import com.whatever.global.security.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

class AuthControllerTest : ControllerTestSupport() {

    @DisplayName("로그인 또는 회원가입을 수행한다")
    @Test
    fun signIn_Success() {
        // given
        val deviceId = "test-device"
        val request = SignInRequest(
            loginPlatform = LoginPlatform.TEST,
            idToken = "token123"
        )

        // when // then
        mockMvc.post("/v1/auth/sign-in") {
            header(DEVICE_ID, deviceId)
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

    @DisplayName("로그아웃을 수행한다")
    @Test
    fun signOut_Success() {
        // given
        val accessHeader = "Bearer access-xxx"
        val deviceId = "test-device"

        mockStatic(SecurityUtil::class.java).use {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(0L)

            // when // then
            mockMvc.post("/v1/auth/sign-out") {
                header(AUTH_JWT_HEADER, accessHeader)
                header(DEVICE_ID, deviceId)
            }
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                }
        }
    }

    @DisplayName("토큰을 재발급한다")
    @Test
    fun refresh_Success() {
        // given
        val deviceId = "test-device"
        val requestDto = ServiceTokenResponse(
            accessToken = "old-access",
            refreshToken = "old-refresh"
        )

        // when // then
        mockMvc.post("/v1/auth/refresh") {
            header(DEVICE_ID, deviceId)
            content = objectMapper.writeValueAsString(requestDto)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }
}
