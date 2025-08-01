package com.whatever.caramel.config

import com.whatever.caramel.common.global.annotation.DisableSwaggerAuthButton
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.method.HandlerMethod

@OpenAPIDefinition(
    info = Info(
        title = "Caramel API Doc",
        description = "Caramel API 문서입니다.",
        version = "v1"
    )
)
@Configuration
@Profile("!production")
class SwaggerConfig {

    @Value("\${swagger.local-server-url}")
    lateinit var localServerUrl: String

    @Value("\${swagger.dev-server-url}")
    lateinit var devServerUrl: String

    @Bean
    fun openApi(): OpenAPI {
        val jwtScheme = "Caramel Access Token"
        val securityRequirement = SecurityRequirement().addList(jwtScheme)

        val securityScheme = SecurityScheme()
            .name(jwtScheme)
            .type(SecurityScheme.Type.HTTP)
            .`in`(SecurityScheme.In.HEADER)
            .scheme("Bearer")
            .bearerFormat("JWT")

        val components = Components().addSecuritySchemes(jwtScheme, securityScheme)

        return OpenAPI()
            .addSecurityItem(securityRequirement)
            .components(components)
            .servers(
                listOf(
                    Server(devServerUrl, "for dev"),
                    Server(localServerUrl, "for local"),
                )
            )
    }

    @Bean
    fun operationCustomizer(): OperationCustomizer {
        return OperationCustomizer { operation: Operation, handlerMethod: HandlerMethod ->
            if (handlerMethod.hasMethodAnnotation(DisableSwaggerAuthButton::class.java)) {
                operation.security = emptyList()
            }

            operation.apply {
                description = description?.trimIndent()
            }
        }
    }

    private fun Server(url: String, description: String): Server {
        return Server().apply {
            this.url = url
            this.description = description
        }
    }
}
