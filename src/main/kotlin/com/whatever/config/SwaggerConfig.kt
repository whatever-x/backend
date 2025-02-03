package com.whatever.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info = Info(
        title = "Caramel API Doc",
        description = "Caramel API 문서입니다.",
        version = "v1"
    )
)
@Configuration
class SwaggerConfig {

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
    }

}