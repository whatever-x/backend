package com.whatever.caramel.api.auth.filter

import com.whatever.caramel.common.global.constants.CaramelHttpHeaders
import com.whatever.caramel.common.global.jwt.JwtHelper
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.auth.repository.AuthRedisRepository
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserGender
import com.whatever.caramel.domain.user.model.UserStatus
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
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
) {

    @DisplayName("로그아웃한 access token은 인증 필터를 통과하지 못한다.")
    @Test
    fun doFilterInternal_WithBlacklistedToken() {
        // given
        val user = User(
            id = 0L,
            email = "test-email",
            birthDate = DateTimeUtil.localNow().toLocalDate(),
            platform = LoginPlatform.TEST,
            platformUserId = "test-platform-id",
            nickname = "testnick",
            gender = UserGender.FEMALE,
            userStatus = UserStatus.SINGLE,
        )
        val accessToken = jwtHelper.createAccessToken(user.id)
        val deviceId = "test-device"

        val jti = jwtHelper.extractJti(accessToken)
        authRedisRepository.saveJtiToBlacklist(jti, Duration.ofSeconds(100L))

        // when, then
        mockMvc.post("/v1/auth/sign-out") {  // access token이 필요한 endpoint
            header(CaramelHttpHeaders.AUTH_JWT_HEADER, "Bearer $accessToken")
            header(CaramelHttpHeaders.DEVICE_ID, deviceId)
        }
            .andDo { print() }
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
