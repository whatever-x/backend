package com.whatever.config

import com.whatever.WhateverBackendApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@EnableFeignClients(basePackageClasses = [WhateverBackendApplication::class])
@Configuration
class ApplicationConfig