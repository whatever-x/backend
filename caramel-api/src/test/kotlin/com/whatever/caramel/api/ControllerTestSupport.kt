package com.whatever.caramel.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.caramel.api.auth.controller.AuthController
import com.whatever.caramel.api.balancegame.controller.BalanceGameController
import com.whatever.caramel.api.calendarevent.scheduleevent.controller.ScheduleController
import com.whatever.caramel.api.content.controller.ContentController
import com.whatever.caramel.api.user.controller.UserController
import com.whatever.caramel.domain.auth.service.AuthService
import com.whatever.caramel.domain.balancegame.service.BalanceGameService
import com.whatever.caramel.domain.calendarevent.service.ScheduleEventService
import com.whatever.caramel.domain.content.service.ContentService
import com.whatever.caramel.domain.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Controller 테스트 시 Spring Security 의존성을 제거하고 싶을 경우 사용
 */
@WebMvcTest(
    controllers = [
        UserController::class,
        ContentController::class,
        ScheduleController::class,
        BalanceGameController::class,
        AuthController::class,
    ],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class
    ],
    excludeFilters = [
        ComponentScan.Filter(classes = [OncePerRequestFilter::class], type = FilterType.ASSIGNABLE_TYPE),
    ]
)
abstract class ControllerTestSupport {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockitoBean
    protected lateinit var userService: UserService

    @MockitoBean
    protected lateinit var contentService: ContentService

    @MockitoBean
    protected lateinit var scheduleEventService: ScheduleEventService

    @MockitoBean
    protected lateinit var balanceGameService: BalanceGameService

    @MockitoBean
    protected lateinit var authService: AuthService
}
