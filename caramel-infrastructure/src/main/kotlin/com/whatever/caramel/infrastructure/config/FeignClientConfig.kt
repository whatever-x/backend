package com.whatever.caramel.infrastructure.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(basePackages = ["com.whatever.caramel.infrastructure"])
class FeignClientConfig