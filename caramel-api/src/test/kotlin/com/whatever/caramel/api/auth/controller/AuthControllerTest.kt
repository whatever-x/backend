package com.whatever.caramel.api.auth.controller

import com.whatever.SecurityUtil
import com.whatever.caramel.api.ControllerTestSupport
import com.whatever.caramel.api.auth.dto.ServiceTokenResponse
import com.whatever.caramel.api.auth.dto.SignInRequest
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.AUTH_JWT_HEADER
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.DEVICE_ID
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.domain.auth.service.AuthService
import com.whatever.domain.auth.vo.ServiceTokenVo
import com.whatever.domain.auth.vo.SignInVo
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.UserStatus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
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

        whenever(authService.signUpOrSignIn(
            loginPlatform = any(),
            idToken = any(),
            deviceId = any(),
        )).thenReturn(
            SignInVo(
                accessToken = "",
                refreshToken = "",
                userStatus = UserStatus.SINGLE.name,
                nickname = "",
                birthDay = DateTimeUtil.localNow().toLocalDate(),
                coupleId = 0L,
            )
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

        val newAccessToken = "new-fresh-access-token"
        val newRefreshToken = "new-fresh-refresh-token"
        val newServiceTokenVo = ServiceTokenVo(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )

        whenever(authService.refresh(
            accessToken = requestDto.accessToken,
            refreshToken = requestDto.refreshToken,
            deviceId = deviceId
        )).thenReturn(newServiceTokenVo)

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
