package com.whatever.config

import com.whatever.domain.user.dto.UserStatus
import com.whatever.global.security.filter.JwtAuthenticationFilter
import com.whatever.global.security.filter.JwtExceptionFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtExceptionFilter: JwtExceptionFilter,
    private val caramelAuthenticationEntryPoint: AuthenticationEntryPoint,
    private val caramelAccessDeniedHandler: AccessDeniedHandler
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.invoke {
            httpBasic { disable() }
            formLogin { disable() }
            logout { disable() }
            csrf { disable() }
            cors { disable() }
        }

        http.sessionManagement{ it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        http.invoke {
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)  // TODO(준용) API에 따른 Role 추가 필요, 현재 임시로 모두 허용
            }
        }

        http.invoke {
            addFilterBefore<BasicAuthenticationFilter>(jwtAuthenticationFilter)
            addFilterBefore<JwtAuthenticationFilter>(jwtExceptionFilter)
        }

        http.invoke {
            exceptionHandling {
                authenticationEntryPoint = caramelAuthenticationEntryPoint
                accessDeniedHandler = caramelAccessDeniedHandler
            }
        }

        return http.build()
    }

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer {
            it.ignoring().requestMatchers(
                "/swagger",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/api-docs/**"
            )
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

}