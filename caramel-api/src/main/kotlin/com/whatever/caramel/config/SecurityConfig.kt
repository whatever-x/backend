package com.whatever.caramel.config

import com.whatever.caramel.domain.user.model.UserStatus.COUPLED
import com.whatever.caramel.domain.user.model.UserStatus.NEW
import com.whatever.caramel.domain.user.model.UserStatus.SINGLE
import com.whatever.caramel.security.filter.JwtAuthenticationFilter
import com.whatever.caramel.security.filter.JwtExceptionFilter
import com.whatever.caramel.security.filter.RequestResponseLoggingFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
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
    private val requestResponseLoggingFilter: RequestResponseLoggingFilter,
    private val caramelAuthenticationEntryPoint: AuthenticationEntryPoint,
    private val caramelAccessDeniedHandler: AccessDeniedHandler,
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

    @Value("\${management.endpoints.web.base-path}")
    lateinit var actuatorPath: String

    @Profile("production", "dev")
    @Bean
    @Order(1)
    fun swaggerFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher(*swaggerUrlPatterns)
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }
            httpBasic { }
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
                // 1. Public Access
                authorize(HttpMethod.POST, "/v1/auth/sign-in", permitAll)
                authorize(HttpMethod.POST, "/v1/auth/refresh", permitAll)
                authorize("/sample/**", permitAll)
                authorize("${actuatorPath}/**", permitAll)

                // 2. Role: NEW
                authorize(HttpMethod.POST, "/v1/user/profile", hasAnyRole(NEW.name))

                // 3. Role: SINGLE
                authorize("/v1/couples/invitation-code", hasAnyRole(SINGLE.name))
                authorize("/v1/couples/connect", hasAnyRole(SINGLE.name))

                // 4. Role: COUPLED
                authorize("/v1/calendar/**", hasAnyRole(COUPLED.name))
                authorize("/v1/content/**", hasAnyRole(COUPLED.name))
                authorize("/v1/couples/{couple-id}/**", hasAnyRole(COUPLED.name))
                authorize("/v1/couples/me", hasAnyRole(COUPLED.name))
                authorize("/v1/balance-game/**", hasAnyRole(COUPLED.name))
                authorize(HttpMethod.GET, "/v1/tags", hasAnyRole(COUPLED.name))

                // 5. Authenticated Users
                authorize("/v1/auth/sign-out", authenticated)
                authorize(HttpMethod.DELETE, "/v1/auth/account", authenticated)
                authorize("/v1/user/me", authenticated) // 모든 인증된 사용자는 자신의 정보를 볼 수 있음

                // 6. Service Members
                authorize("/v1/user/**", hasAnyRole(SINGLE.name, COUPLED.name))

                // 7. Default Rule
                authorize(anyRequest, authenticated)
            }
        }

        http {
            addFilterAfter<LogoutFilter>(jwtExceptionFilter)
            addFilterAfter<JwtExceptionFilter>(jwtAuthenticationFilter)
            addFilterAfter<JwtAuthenticationFilter>(requestResponseLoggingFilter)
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
    fun jwtAuthenticationFilterRegistration(filter: JwtAuthenticationFilter): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registration: FilterRegistrationBean<JwtAuthenticationFilter> =
            FilterRegistrationBean<JwtAuthenticationFilter>(filter)
        registration.isEnabled = false
        return registration
    }

    @Bean
    fun jwtExceptionFilterRegistration(filter: JwtExceptionFilter): FilterRegistrationBean<JwtExceptionFilter> {
        val registration: FilterRegistrationBean<JwtExceptionFilter> =
            FilterRegistrationBean<JwtExceptionFilter>(filter)
        registration.isEnabled = false
        return registration
    }

    @Bean
    fun requestLoggingFilterRegistration(filter: RequestResponseLoggingFilter): FilterRegistrationBean<RequestResponseLoggingFilter> {
        val registration: FilterRegistrationBean<RequestResponseLoggingFilter> =
            FilterRegistrationBean<RequestResponseLoggingFilter>(filter)
        registration.isEnabled = false
        return registration
    }
}

@Configuration
class CryptoConfig {
    @Value("\${crypto.password}")
    lateinit var textEncryptorPassword: String

    @Value("\${crypto.salt}")
    lateinit var textEncryptorSalt: String

    @Profile("production")
    @Bean(name = ["textEncryptor"])
    fun productionTextEncryptor(): TextEncryptor {
        return Encryptors.delux(textEncryptorPassword, textEncryptorSalt)
    }

    @Profile("!production")
    @Bean(name = ["textEncryptor"])
    fun testTextEncryptor(): TextEncryptor {
        return Encryptors.noOpText()
    }
}
