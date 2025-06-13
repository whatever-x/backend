package com.whatever.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.domain.auth.controller.AuthController
import com.whatever.domain.auth.service.AuthService
import com.whatever.domain.balancegame.controller.BalanceGameController
import com.whatever.domain.balancegame.service.BalanceGameService
import com.whatever.domain.calendarevent.scheduleevent.controller.ScheduleController
import com.whatever.domain.calendarevent.scheduleevent.service.ScheduleEventService
import com.whatever.domain.content.controller.ContentController
import com.whatever.domain.content.service.ContentService
import com.whatever.domain.user.controller.UserController
import com.whatever.domain.user.service.UserService
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
    private lateinit var authService: AuthService
}