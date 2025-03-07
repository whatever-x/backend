package com.whatever.config

import com.whatever.domain.user.model.UserStatus
import com.whatever.global.security.filter.JwtAuthenticationFilter
import com.whatever.global.security.filter.JwtExceptionFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtExceptionFilter: JwtExceptionFilter,
    private val caramelAuthenticationEntryPoint: AuthenticationEntryPoint,
    private val caramelAccessDeniedHandler: AccessDeniedHandler
) {

    companion object {
        private val swaggerUrlPatterns = arrayOf(
            "/swagger",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**"
        )
    }

    @Value("\${swagger.user}")
    lateinit var swaggerUser: String
    @Value("\${swagger.password}")
    lateinit var swaggerPassword: String

    @Profile("production", "dev")
    @Bean
    @Order(1)
    fun swaggerFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher(*swaggerUrlPatterns)
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }
            httpBasic {  }
        }
        return http.build()
    }

    @Bean
    fun defaultFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            httpBasic { disable() }
            formLogin { disable() }
            logout { disable() }
            csrf { disable() }
            cors { disable() }

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
        }

        http {
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)  // TODO(준용) API에 따른 Role 추가 필요, 현재 임시로 모두 허용
            }
        }

        http {
            addFilterAfter<LogoutFilter>(jwtExceptionFilter)
            addFilterAfter<JwtExceptionFilter>(jwtAuthenticationFilter)
        }

        http {
            exceptionHandling {
                authenticationEntryPoint = caramelAuthenticationEntryPoint
                accessDeniedHandler = caramelAccessDeniedHandler
            }
        }

        return http.build()
    }

    @Bean
    fun swaggerPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(10)
    }

    /** InMemory User for Swagger */
    @Bean
    fun swaggerUserDetailsService(): UserDetailsService {
        val users = User.builder()
            .passwordEncoder { pw -> swaggerPasswordEncoder().encode(pw) }
        val manager = InMemoryUserDetailsManager()
        manager.createUser(
            users.username(swaggerUser).password(swaggerPassword)
                .roles("SWAGGER").build()
        )
        return manager
    }

    @Profile("local-mem")
    @Bean
    fun swaggerSecurityBypassCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer {
            it.ignoring().requestMatchers(*swaggerUrlPatterns)
        }
    }

    @Bean
    fun roleHierarchy(): RoleHierarchy {
        return RoleHierarchyImpl.withDefaultRolePrefix()
            .role(UserStatus.COUPLED.name).implies(UserStatus.SINGLE.name)
            .role(UserStatus.SINGLE.name).implies(UserStatus.NEW.name)
            .build()
    }

    @Bean
    fun methodSecurityExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setRoleHierarchy(roleHierarchy())
        return expressionHandler
    }

    @Bean
    fun jwtAuthenticationFilterRegistration(filter: JwtAuthenticationFilter): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registration: FilterRegistrationBean<JwtAuthenticationFilter> = FilterRegistrationBean<JwtAuthenticationFilter>(filter)
        registration.isEnabled = false
        return registration
    }

    @Bean
    fun jwtExceptionFilterRegistration(filter: JwtExceptionFilter): FilterRegistrationBean<JwtExceptionFilter> {
        val registration: FilterRegistrationBean<JwtExceptionFilter> = FilterRegistrationBean<JwtExceptionFilter>(filter)
        registration.isEnabled = false
        return registration
    }

}