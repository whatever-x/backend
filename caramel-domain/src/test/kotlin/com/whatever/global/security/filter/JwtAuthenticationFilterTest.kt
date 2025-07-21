package com.whatever.global.security.filter

import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.AUTH_JWT_HEADER
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.DEVICE_ID
import com.whatever.domain.auth.repository.AuthRedisRepository
import com.whatever.domain.auth.service.AuthService
import com.whatever.domain.auth.service.JwtHelper
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.domain.user.service.createUser
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Duration
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest @Autowired constructor(
    private val jwtHelper: JwtHelper,
    private val mockMvc: MockMvc,
    private val authRedisRepository: AuthRedisRepository,
    private val userRepository: UserRepository,
) {

    @MockitoBean
    private lateinit var authService: AuthService

    @DisplayName("로그아웃한 access token은 인증 필터를 통과하지 못한다.")
    @Test
    fun doFilterInternal_WithBlacklistedToken() {
        // given
        val user = createUser(userRepository = userRepository, userStatus = UserStatus.SINGLE)
        val accessToken = jwtHelper.createAccessToken(user.id)
        val deviceId = "test-device"

        val jti = jwtHelper.extractJti(accessToken)
        authRedisRepository.saveJtiToBlacklist(jti, Duration.ofSeconds(100L))

        // when, then
        mockMvc.post("/v1/auth/sign-out") {  // access token이 필요한 endpoint
            header(AUTH_JWT_HEADER, "Bearer $accessToken")
            header(DEVICE_ID, deviceId)
        }
            .andDo { print() }
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
